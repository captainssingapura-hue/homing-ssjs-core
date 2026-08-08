package hue.captains.singapura.js.homing.core;

import java.util.Objects;

/**
 * One JS module packed into a {@link Crate}, with its mechanical metadata
 * (RFC 0044, Crate model). Today that is the module itself plus its inferred
 * {@link ModuleForm}; <b>domain</b> classification (e.g. a primitive/secretary/
 * consumer role, or downstream-defined taxonomies) is deliberately NOT here —
 * per Decision 1 it will ride on a separate typed, extensible metadata map
 * added when the first rule actually needs it. Keeping this record to the
 * mechanical, compiler-decidable facts is what lets {@link #of(EsModule)}
 * populate it with zero author ceremony.
 *
 * @param module the packed module (a singleton {@code INSTANCE})
 * @param form   its mechanical form, inferred from its type
 */
public record CrateEntry(EsModule<?> module, ModuleForm form) {

    public CrateEntry {
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(form, "form");
    }

    /** Pack a module, inferring its {@link ModuleForm} — the normal call. */
    public static CrateEntry of(EsModule<?> module) {
        return new CrateEntry(module, ModuleForm.of(module));
    }

    /** The module's fully-qualified class name — its stable served identity. */
    public String moduleClass() {
        return module.getClass().getName();
    }
}
