package hue.captains.singapura.js.homing.studio.base.tracker;

/**
 * One high-level objective for a {@link Plan} — what the plan is <i>trying to
 * achieve</i> at a glance, distinct from per-phase outcomes (operational steps)
 * and from {@link Acceptance} criteria (pass/fail ship-gates).
 *
 * <p>Objectives describe goals — the "why" of the plan — and are deliberately
 * un-checkboxed: there is no {@code met} flag. A reader skimming the tracker's
 * top-of-page Objectives list should understand what the plan exists to do
 * before scrolling into questions, phases, or acceptance.</p>
 *
 * <p>The shape is intentionally minimal: a short {@code label} (5–10 words)
 * and a longer {@code description} (one sentence). Trackers with no objectives
 * yet return {@code List.of()} — the renderer hides the section.</p>
 *
 * @param label       short title (5–10 words, e.g. {@code "Ship a downstream-ready studio base"})
 * @param description one-sentence elaboration
 *
 * @since RFC 0005-ext1 (4th pillar — added in v1 release tracker for tracker self-orientation)
 */
public record Objective(String label, String description) implements hue.captains.singapura.tao.ontology.ValueObject {}
