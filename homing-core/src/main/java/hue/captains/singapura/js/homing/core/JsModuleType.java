package hue.captains.singapura.js.homing.core;

/**
 * The <b>domain</b> classification of a served JS module — the axis the RFC 0044
 * rule policy is polymorphic over. A closed, enumerable set (mirrors the
 * {@code PhaseStatus} / {@code DecisionStatus} shape), so the studio can iterate
 * every type and show the rule set it maps to.
 *
 * <p>A module's type selects which rules apply: a bundle isn't asked to use the
 * {@code css} manager; a {@link #PURE_LOGIC} module must touch no DOM at all; a
 * {@link #PRIMITIVE} must publish its mutations. Some types are <b>mechanical</b>
 * (inferred from a module's {@link ModuleForm}: a CSS group is {@link
 * #GENERATED_CSS}, an external bundle is {@link #BUNDLED_EXTERNAL}); the
 * <b>domain</b> types ({@link #CONSUMER}, {@link #PRIMITIVE}, {@link #SECRETARY},
 * {@link #PURE_LOGIC}) carry no mechanical marker and are <b>declared</b> on the
 * owning {@link CrateEntry} — an undeclared module defaults to {@link #CONSUMER},
 * the full-discipline baseline.</p>
 *
 * <p>This lives in homing-core (not the conformance-rules module) precisely so a
 * {@link CrateEntry} can declare it: crates are authored in each Maven module
 * against homing-core alone, upstream of the rule engine that consumes them.</p>
 */
public enum JsModuleType {

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

    JsModuleType(String label, String slug) {
        this.label = label;
        this.slug  = slug;
    }

    /** Human-readable label, for the studio. */
    public String label() { return label; }

    /** URL-safe slug. */
    public String slug() { return slug; }
}
