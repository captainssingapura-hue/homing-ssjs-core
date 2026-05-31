package hue.captains.singapura.js.homing.codec.ecma;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.lang.reflect.RecordComponent;

/**
 * Concrete {@link FunctionsCodeGen} that emits an ECMAScript codec class
 * implementing {@code TransformationFunctions<T, plain-object>} for a Java
 * record's shape.
 *
 * <p>For a Java record {@code R(c1: T1, c2: T2, …)}, emits JS of the form:</p>
 *
 * <pre>{@code
 * class RCodec {
 *     static KIND = 'R';
 *     static transformTo(typed) {
 *         return { c1: typed.c1, c2: typed.c2, ... };
 *     }
 *     static transformFrom(wire) {
 *         return new R(wire.c1, wire.c2, ...);
 *     }
 * }
 * }</pre>
 *
 * <p>The generated codec name is the Java record's simple name + {@code
 * "Codec"}. The {@code KIND} static carries the Java simple name as the
 * wire-side discriminator — used by sealed-sum codecs in a later cycle.</p>
 *
 * <p>The generated JS depends on the matching JS class declaration
 * (from {@link EcmaDefinitionCodeGen}) being present in the same module
 * scope, so {@code new R(...)} can construct typed instances.</p>
 *
 * <p>Stateless Functional Object. {@link #INSTANCE} is the canonical
 * handle.</p>
 *
 * <p><b>Scope of V0 (foundation slice).</b> Identity wire shape for
 * record components that are JS-native scalars (strings, numbers,
 * booleans). Subsequent cycles will introduce specialised codegens for:
 * nested records (delegate to inner codec via name lookup), sealed-of-
 * records sums (kind-discriminated dispatch), {@code Optional} (null in
 * wire), collections (array / object in wire), enums (string-name in
 * wire), {@code Instant} (ISO-8601 string in wire).</p>
 *
 * @since homing-codec — ECMAScript target
 */
public record EcmaFunctionsCodeGen() implements FunctionsCodeGen, StatelessFunctionalObject {

    public static final EcmaFunctionsCodeGen INSTANCE = new EcmaFunctionsCodeGen();

    @Override
    public String generate(ObjectDefinition<?> definition) {
        Class<?> type = definition.type();
        if (type.isEnum())   return generateEnum(type);
        if (type.isRecord()) return generateRecord(type);
        throw new IllegalArgumentException(
                "EcmaFunctionsCodeGen V0 supports record or enum types only — got " + type);
    }

    private String generateRecord(Class<?> type) {
        RecordComponent[] components = type.getRecordComponents();
        String className   = type.getSimpleName();
        String codecName   = className + "Codec";

        var sb = new StringBuilder();
        sb.append("/** Generated — do not edit. Codec for Java type: ")
          .append(type.getName()).append(" */\n");
        sb.append("class ").append(codecName).append(" {\n");
        sb.append("    static KIND = ").append(jsString(className)).append(";\n");
        sb.append("\n");
        sb.append("    static transformTo(typed) {\n");
        sb.append("        return {\n");
        for (int i = 0; i < components.length; i++) {
            String n = components[i].getName();
            sb.append("            ").append(n).append(": typed.").append(n);
            if (i < components.length - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("        };\n");
        sb.append("    }\n");
        sb.append("\n");
        sb.append("    static transformFrom(wire) {\n");
        sb.append("        return new ").append(className).append("(");
        for (int i = 0; i < components.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("wire.").append(components[i].getName());
        }
        sb.append(");\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Emit an enum codec. The wire form is the enum constant's name as a
     * string. {@code transformFrom} validates against the known-constants
     * set so a malformed wire value fails loud instead of becoming an
     * undefined-shaped silent corruption.
     */
    private String generateEnum(Class<?> type) {
        String className = type.getSimpleName();
        String codecName = className + "Codec";
        Object[] constants = type.getEnumConstants();

        var sb = new StringBuilder();
        sb.append("/** Generated — do not edit. Codec for Java enum: ")
          .append(type.getName()).append(" */\n");
        sb.append("class ").append(codecName).append(" {\n");
        sb.append("    static KIND = ").append(jsString(className)).append(";\n");
        sb.append("    static VALUES = Object.freeze([");
        for (int i = 0; i < constants.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(jsString(((Enum<?>) constants[i]).name()));
        }
        sb.append("]);\n");
        sb.append("\n");
        sb.append("    static transformTo(typed) {\n");
        sb.append("        // Typed value is already the enum's string name.\n");
        sb.append("        return typed;\n");
        sb.append("    }\n");
        sb.append("\n");
        sb.append("    static transformFrom(wire) {\n");
        sb.append("        if (!").append(codecName).append(".VALUES.includes(wire)) {\n");
        sb.append("            throw new TypeError(").append(jsString(codecName + ": unknown wire value '"))
          .append(" + wire + ").append(jsString("'"))
          .append(");\n");
        sb.append("        }\n");
        sb.append("        return wire;\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    /** Emit a JS single-quoted string literal, escaping backslashes and quotes. */
    private static String jsString(String s) {
        return "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }
}
