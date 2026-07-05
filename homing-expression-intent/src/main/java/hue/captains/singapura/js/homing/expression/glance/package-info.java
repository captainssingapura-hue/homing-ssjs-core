/**
 * The <strong>Glance</strong> usage — a top-level, purpose-named usage that shows
 * information <em>at a glance</em> (a compact overview). It involves plan phases
 * but typically alongside other information (title, progress, dates), so it is
 * <em>not</em> owned by {@code Plan Phases Management}; it lives here as a usage
 * and <em>references</em> the conceptual domain.
 *
 * <p>Built family:
 * {@link hue.captains.singapura.js.homing.expression.glance.GlancePhaseStatusIntent}
 * — one intent per {@code PhaseState}, each binding to {@code realm.plan.Status}
 * and referencing the canonical value. Because status is expressed <em>as a
 * glance chip here</em>, its realization is this usage's own; a progress-bar or
 * overview-row usage would carry its own phase-status family with a different
 * realization but the same referenced {@code PhaseState}. That is the
 * usage-recurs-per-concern pattern: the concept lives once, each usage references
 * it, realization varies.</p>
 *
 * <p>Deferred: other glance families (progress magnitude, title, …), and the
 * Realizer that projects any of these to form (journey S2).</p>
 */
package hue.captains.singapura.js.homing.expression.glance;
