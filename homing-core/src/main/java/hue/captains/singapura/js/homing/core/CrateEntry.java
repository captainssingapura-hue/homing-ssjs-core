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
 * PURE_LOGIC} is a promise it touches no DOM, which the engine then enforces.</p>
 *
 * @param module       the packed module (a singleton {@code INSTANCE})
 * @param form         its mechanical form, inferred from its type
 * @param declaredType its declared domain role, or {@code null} if undeclared
 */
public record CrateEntry(EsModule<?> module, ModuleForm form, JsModuleType declaredType) {

    public CrateEntry {
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(form, "form");
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
