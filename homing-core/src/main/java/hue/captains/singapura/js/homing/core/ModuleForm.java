package hue.captains.singapura.js.homing.core;

/**
 * The <b>mechanical</b>, language-level form of a served JS module — how its
 * JavaScript is produced. Decidable purely by the interface a module
 * implements, so it is inferred, never declared (RFC 0044, Crate model,
 * Decision 1: mechanical facts are fixed + inferred; domain classification is
 * carried separately as extensible metadata).
 *
 * <p>The ladder in {@link #of(EsModule)} mirrors the {@code instanceof} order
 * the serving path ({@code EsModuleGetAction}) already uses to pick a content
 * provider — the single source of truth for "what kind of module is this,
 * mechanically." {@code ManagerInjector} is deliberately absent: it is a
 * cross-cutting capability a module mixes in, not a mutually-exclusive form.</p>
 */
public enum ModuleForm {

    /** Ships verbatim bytes from a bundled third-party artifact. */
    BUNDLED_EXTERNAL,
    /** Java-emitted SVG group (no {@code .js} resource). */
    SVG_GROUP,
    /** Java-emitted CSS group (no {@code .js} resource). */
    CSS_GROUP,
    /** Java-emitted module content via {@link SelfContent} (no {@code .js} resource). */
    SELF_CONTENT,
    /** Content read from a {@code homing/js/<fqcn>.js} classpath resource. */
    RESOURCE_BACKED;

    /**
     * Infer the form from the module's type, in the same priority order the
     * serving path resolves its content provider.
     */
    public static ModuleForm of(EsModule<?> module) {
        if (module instanceof BundledExternalModule<?>) return BUNDLED_EXTERNAL;
        if (module instanceof SvgGroup<?>)              return SVG_GROUP;
        if (module instanceof CssGroup<?>)              return CSS_GROUP;
        if (module instanceof SelfContent)              return SELF_CONTENT;
        return RESOURCE_BACKED;
    }
}
