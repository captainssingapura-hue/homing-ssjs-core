package hue.captains.singapura.js.homing.conformance.rules;

/**
 * The classification of a served JS module — the axis the rule policy is
 * polymorphic over. A closed, enumerable set (mirrors the {@code PhaseStatus} /
 * {@code DecisionStatus} shape), so the studio can iterate every type and show
 * the rule set it maps to. RFC 0044 sketched a sealed interface; an enum is the
 * idiomatic refinement — the variants carry no data today, only identity, and
 * enumeration ({@link #values()}) is exactly what the studio's policy view needs.
 *
 * <p>A module's type selects which rules apply: a bundle isn't asked to use the
 * {@code css} manager; a Secretary must touch no DOM; a primitive must publish
 * its mutations. Classification (Phase 3) prefers an explicit marker, falling
 * back to structural inference.</p>
 */
public enum JsModuleType {

    /** A consumer DomModule — the full discipline (no HTML, owned refs, typed css/href, no DOM destruction). */
    CONSUMER("Consumer", "consumer"),
    /** A structural primitive (SplitPane, MultiTabPane, …) — must publish its mutations; typed contract. */
    PRIMITIVE("Primitive", "primitive"),
    /** A Secretary reducer body — pure (state, envelope) → Step; no DOM, no console, no captures. */
    SECRETARY("Secretary", "secretary"),
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
