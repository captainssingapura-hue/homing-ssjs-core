package hue.captains.singapura.js.homing.core;

import java.util.Objects;

/**
 * One JS module packed into a {@link Crate}, with its classification (RFC 0044,
 * Crate model). Two axes:
 * <ul>
 *   <li><b>Mechanical</b> — the inferred {@link ModuleForm}, decidable from the
 *       module's type with zero author ceremony ({@link #of(EsModule)}).</li>
 *   <li><b>Domain</b> — an optional <em>declared</em> {@link JsModuleType}: the
 *       author's assertion of a module's role (a primitive, a secretary, a
 *       headless {@link JsModuleType#PURE_LOGIC} module …). Unlike the form it
 *       carries no mechanical marker, so the crate declares it explicitly via
 *       {@link #of(EsModule, JsModuleType)}. A {@code null} here means
 *       undeclared — the conformance engine treats that as the full-discipline
 *       {@link JsModuleType#CONSUMER} baseline.</li>
 * </ul>
 * <p>The declaration is intent, not observation: marking a module {@code
 * PURE_LOGIC} is a promise it touches no DOM, which the engine then enforces.
 * Only a <em>domain</em> role can be declared: the mechanical types
 * ({@code MANAGER_INJECTOR}, {@code GENERATED_CSS}, {@code BUNDLED_EXTERNAL}) are
 * the classifier's to infer, and declaring one is refused — it would be a
 * first-party module granting itself the bundled external's empty rule set.</p>
 *
 * @param module       the packed module (a singleton {@code INSTANCE})
 * @param form         its mechanical form, inferred from its type
 * @param declaredType its declared domain role, or {@code null} if undeclared
 */
public record CrateEntry(EsModule<?> module, ModuleForm form, JsModuleType declaredType) {

    public CrateEntry {
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(form, "form");
        if (declaredType instanceof StandardJsModuleType std && isMechanical(std)) {
            throw new IllegalArgumentException(
                    "'" + module.getClass().getName() + "' declares the mechanical type " + std
                            + ". A mechanical type is inferred from the module's form, never declared:"
                            + " declaring one would exempt a first-party module from its discipline."
                            + " Declare a domain role (PRIMITIVE, SECRETARY, PURE_LOGIC) or leave it undeclared.");
        }
    }

    /** The types the classifier infers from a module's form — not an author's to assert. */
    private static boolean isMechanical(StandardJsModuleType t) {
        return switch (t) {
            case MANAGER_INJECTOR, GENERATED_CSS, BUNDLED_EXTERNAL -> true;
            case CONSUMER, PRIMITIVE, SECRETARY, PURE_LOGIC -> false;
        };
    }

    /** Pack a module, inferring its {@link ModuleForm}, domain role undeclared — the normal call. */
    public static CrateEntry of(EsModule<?> module) {
        return new CrateEntry(module, ModuleForm.of(module), null);
    }

    /** Pack a module with its declared domain {@link JsModuleType} — for the non-{@code CONSUMER} roles. */
    public static CrateEntry of(EsModule<?> module, JsModuleType declaredType) {
        return new CrateEntry(module, ModuleForm.of(module),
                Objects.requireNonNull(declaredType, "declaredType"));
    }

    /** The module's fully-qualified class name — its stable served identity. */
    public String moduleClass() {
        return module.getClass().getName();
    }
}
