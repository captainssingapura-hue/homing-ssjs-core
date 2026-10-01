package hue.captains.singapura.js.homing.studio.base.tracker;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * Before/after measurement captured at phase completion. Surfaced as one row
 * of a metrics table on the step-detail page — the quantitative proof that a
 * phase moved the needle on what it claimed.
 *
 * <p>Examples (from the Rfc0002Ext1 tracker that motivated this primitive):</p>
 * <ul>
 *   <li>{@code Metric("File line count", "938 lines", "939 lines", "+1 (one new semantic token)")}</li>
 *   <li>{@code Metric("Primitive var(--st-*) refs in component bodies", "52", "0", "−52 (100% theme-portable)")}</li>
 *   <li>{@code Metric("Tests passing", "1284", "1287", "+3 new")}</li>
 * </ul>
 *
 * <p>Designed for cleanup / refactor / migration / hardening plans where the
 * payoff is hard to argue without numbers.</p>
 */
public record Metric(String label, String before, String after, String delta) implements ValueObject {}
