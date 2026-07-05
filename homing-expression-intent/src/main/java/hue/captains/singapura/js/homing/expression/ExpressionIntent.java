package hue.captains.singapura.js.homing.expression;

/**
 * A <strong>UI Expression Intent</strong> — something intended to be expressed
 * in the UI, decoupled from any <em>physical</em> realization (colour, shape,
 * shadow, sound). It is the portable core of the {@code ui.expression} Facet:
 * application code names an intent; a <em>Realizer</em> (a later module) projects
 * that intent to concrete per-substrate form.
 *
 * <p>Identity is a stable, dotted {@link #key()} — the name application code
 * expresses and the Realizer resolves. The key encodes the intent's place in the
 * vocabulary ({@code <usage>.<concept>.<value>}, e.g.
 * {@code glance.phase-status.blocked}), so it is stable across substrates and
 * doubles as the semantic-class name (RFC 0036).</p>
 *
 * <p>Two kinds (Expression-Intent ontology, axiom A3):
 * <ul>
 *   <li><strong>domain-bound</strong> — expresses a problem concept; references a
 *       domain node (see {@link DomainBoundIntent}). A glance's phase-status
 *       intent binds to {@code realm.plan.Status} and carries a {@code PhaseState}
 *       value.</li>
 *   <li><strong>Facet-native</strong> — a pure presentation role scoped to a
 *       surface ({@code emphasis}, {@code surface}). Deferred until its first
 *       family is built.</li>
 * </ul>
 */
public interface ExpressionIntent {

    /**
     * Stable dotted identity — {@code <concern>.<domain>.<value>}. Application
     * code expresses it; the Realizer resolves it to form. Never a presentation
     * value itself (no colour / size here).
     */
    String key();
}
