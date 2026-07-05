/**
 * UI Expression Intents — the {@code ui.expression} Facet's vocabulary.
 *
 * <p>An {@link hue.captains.singapura.js.homing.expression.ExpressionIntent} is
 * something intended to be expressed in the UI, <em>decoupled from any physical
 * realization</em>. Application code names an intent; a Realizer (a later module)
 * projects it to per-substrate form. This is the portable core of the whole
 * journey — the vocabulary survives a substrate change; the Realizers are the
 * replaceable edge.</p>
 *
 * <h2>An intent is a cell</h2>
 * <pre>
 *   ExpressionIntent  =  (usage)  ×  (problem concept)  ×  (value)
 * </pre>
 * A <strong>domain-bound</strong> intent
 * ({@link hue.captains.singapura.js.homing.expression.DomainBoundIntent})
 * references its problem dimension via {@code domain()}; the usage and value live
 * in the {@code key()}. The problem concepts come from {@code homing-problem-realm}
 * — this module never redefines them, only references them.
 *
 * <h2>Usages, not generic buckets</h2>
 * Intents live under <em>usages</em> (purpose-named: {@code Glance}, …), not under
 * a flat, borrowed presentation palette. The same concept realises differently per
 * usage because those are different usages, so each usage carries its own family —
 * all referencing one canonical value, so meaning is shared and only realization
 * varies. (An earlier {@code feedback.status.*} set was exactly the borrowed
 * generic vocabulary this avoids; it was retired.)
 *
 * <h2>Built</h2>
 * <ul>
 *   <li>{@code glance.phase-status.*}
 *       ({@link hue.captains.singapura.js.homing.expression.glance.GlancePhaseStatusIntent})
 *       — the Glance usage's phase-status family, one intent per
 *       {@code realm.plan.PhaseState}, bound to {@code realm.plan.Status}.</li>
 * </ul>
 *
 * <h2>Deferred</h2>
 * Other glance families (progress magnitude, …) and other usages
 * (progress-bar, overview-row). The <strong>Facet-native</strong> branch —
 * {@code emphasis}, {@code surface}, and the presentation-<em>role</em> layer that
 * domain outcomes map onto — lands with its first family. The Realizer that
 * projects any intent to form is a separate module (journey S2).
 */
package hue.captains.singapura.js.homing.expression;
