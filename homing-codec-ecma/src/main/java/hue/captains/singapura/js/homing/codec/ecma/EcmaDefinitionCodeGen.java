package hue.captains.singapura.js.homing.codec.ecma;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.lang.reflect.RecordComponent;

/**
 * Concrete {@link DefinitionCodeGen} that emits an ECMAScript class
 * declaration mirroring a Java record's shape.
 *
 * <p>For a Java record {@code R(c1, c2, …)}, emits JS of the form:</p>
 *
 * <pre>{@code
 * class R {
 *     constructor(c1, c2, ...) {
 *         this.c1 = c1;
 *         this.c2 = c2;
 *         ...
 *         Object.freeze(this);
 *     }
 * }
 * }</pre>
 *
 * <p>The generated class is intentionally minimal — no validation, no
 * static factories. Its only role is to give the matching
 * {@link EcmaFunctionsCodeGen} output something to {@code transformTo} /
 * {@code transformFrom} against, and to let consumer JS construct
 * instances via {@code new R(...)} when it builds typed values from
 * scratch.</p>
 *
 * <p>{@code Object.freeze(this)} mirrors Java records' structural
 * immutability — instances cannot be mutated after construction.</p>
 *
 * <p>Stateless. The singleton {@link #INSTANCE} is the canonical handle;
 * callers may also construct fresh instances ({@code new
 * EcmaDefinitionCodeGen()}) — both behave identically per the Functional
 * Objects doctrine.</p>
 *
 * <p><b>Scope of V0 (foundation slice).</b> Handles record types whose
 * components are JS-native scalars (strings, numbers, booleans), nested
 * records (treated structurally — the nested record's name appears in
 * the constructor's parameter list), and {@code Class}-typed enum
 * components. Sealed-of-records sums, collections, and {@code Optional}
 * are deferred to subsequent cycles via specialised codegens that
 * dispatch off the same {@code ObjectDefinition.type()}.</p>
 *
 * @since homing-codec — ECMAScript target
 */
public record EcmaDefinitionCodeGen() implements DefinitionCodeGen, StatelessFunctionalObject {

    public static final EcmaDefinitionCodeGen INSTANCE = new EcmaDefinitionCodeGen();

    @Override
    public String generate(ObjectDefinition<?> definition) {
        Class<?> type = definition.type();
        if (type.isEnum())   return generateEnum(type);
        if (type.isRecord()) return generateRecord(type);
        throw new IllegalArgumentException(
                "EcmaDefinitionCodeGen V0 supports record or enum types only — got " + type);
    }

    private String generateRecord(Class<?> type) {
        RecordComponent[] components = type.getRecordComponents();
        String className = type.getSimpleName();

        var sb = new StringBuilder();
        sb.append("/** Generated — do not edit. Java type: ").append(type.getName()).append(" */\n");
        sb.append("class ").append(className).append(" {\n");
        sb.append("    constructor(");
        for (int i = 0; i < components.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(components[i].getName());
        }
        sb.append(") {\n");
        for (RecordComponent c : components) {
            sb.append("        this.").append(c.getName())
              .append(" = ").append(c.getName()).append(";\n");
        }
        sb.append("        Object.freeze(this);\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Emit a frozen object mapping each enum constant's name to its own
     * string name. JS has no enums; the convention here is identity-shaped
     * string discriminators, which round-trip naturally through JSON.
     */
    private String generateEnum(Class<?> type) {
        String name = type.getSimpleName();
        var sb = new StringBuilder();
        sb.append("/** Generated — do not edit. Java enum: ").append(type.getName()).append(" */\n");
        sb.append("const ").append(name).append(" = Object.freeze({\n");
        Object[] constants = type.getEnumConstants();
        for (int i = 0; i < constants.length; i++) {
            String constantName = ((Enum<?>) constants[i]).name();
            sb.append("    ").append(constantName).append(": ").append(jsString(constantName));
            if (i < constants.length - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("});\n");
        return sb.toString();
    }

    private static String jsString(String s) {
        return "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }
}
