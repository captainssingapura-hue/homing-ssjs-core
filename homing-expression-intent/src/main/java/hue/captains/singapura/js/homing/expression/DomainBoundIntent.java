package hue.captains.singapura.js.homing.expression;

import com.hominglabs.rebar.core.desc.WithDesc;

/**
 * An Expression Intent that <strong>expresses a problem concept</strong> — it
 * references the domain dimension it conveys (Expression-Intent ontology, A3).
 * The intent is the {@code (domain × concern)} cell; {@link #domain()} is the
 * problem-side of that cell, {@link #key()} carries the concern and the value.
 *
 * <p>Example: every {@code glance.phase-status.*} intent binds to the same domain
 * — {@code realm.plan.Status} — and differs only by the {@code PhaseState} value
 * it references. The value (blocked / done / …) is the intent's, not a sub-domain
 * of the dimension.</p>
 *
 * <p>The Realizer resolves a missing realisation by walking the domain axis to
 * the nearest realised ancestor (Realizer A5); {@code domain()} is the entry
 * point for that walk. The bound {@code D extends WithDesc} keeps the referenced
 * node self-describing.</p>
 *
 * @param <D> the domain dimension this intent binds to
 */
public interface DomainBoundIntent<D extends WithDesc> extends ExpressionIntent {

    /** The problem dimension this intent expresses a value of. */
    D domain();
}
