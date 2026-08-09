package hue.captains.singapura.js.homing.core;

/**
 * The framework's fixed set of {@link JsModuleType}s — the <b>sealed branch</b>.
 * Because it is an enum (a closed set), framework code (the default policy, the
 * studio's policy view) can iterate {@link #values()} and exhaustive-{@code
 * switch} it with compiler-checked completeness. Downstream types live outside
 * this enum (they implement {@link JsModuleType} directly) and are dispatched by
 * dictionary lookup instead.
 *
 * <p>Some values are <b>mechanical</b> (inferred from a module's {@link
 * ModuleForm}: a CSS group is {@link #GENERATED_CSS}, an external bundle is
 * {@link #BUNDLED_EXTERNAL}); the <b>domain</b> values ({@link #CONSUMER},
 * {@link #PRIMITIVE}, {@link #SECRETARY}, {@link #PURE_LOGIC}) carry no mechanical
 * marker and are <b>declared</b> on the owning {@link CrateEntry} — an undeclared
 * module defaults to {@link #CONSUMER}, the full-discipline baseline.</p>
 */
public enum StandardJsModuleType implements JsModuleType {

    /** A consumer DomModule — the full discipline (no HTML, owned refs, typed css/href, no DOM destruction). */
    CONSUMER("Consumer", "consumer"),
    /** A structural primitive (SplitPane, MultiTabPane, …) — owns branch DOM; must publish its mutations, never wholesale-wipe. */
    PRIMITIVE("Primitive", "primitive"),
    /** A Secretary reducer body — pure (state, envelope) → Step; no DOM, no console, no captures. */
    SECRETARY("Secretary", "secretary"),
    /** A headless logic/data module (codec, store, worker, registry) — pure computation or data; must touch no DOM at all. */
    PURE_LOGIC("Pure logic", "pure-logic"),
    /** A ManagerInjector / manager module — header shape; exactly one manager import; no authored body. */
    MANAGER_INJECTOR("ManagerInjector", "manager-injector"),
    /** Generated CSS-group content — no hand-authored JS to police. */
    GENERATED_CSS("Generated CSS", "generated-css"),
    /** A bundled third-party module (tone / three) — exempt from consumer rules; pinned + integrity only. */
    BUNDLED_EXTERNAL("Bundled external", "bundled-external");

    private final String label;
    private final String slug;

    StandardJsModuleType(String label, String slug) {
        this.label = label;
        this.slug  = slug;
    }

    @Override public String label() { return label; }
    @Override public String slug()  { return slug; }
}
