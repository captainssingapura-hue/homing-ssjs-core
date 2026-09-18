package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A product's fulfilment of the design classes it added — its own semantic
 * leaves, projected — registered beside a design in a deployment. The same
 * shape as a {@link Design}: a function from a pair to an impl, or
 * {@code null}. Two honest forms: <i>bound</i>, values for one specific
 * design; <i>derived</i>, an answer computed from the base design's own —
 * {@code base.impl(DesignClass.of(Success.class, target))} for an {@code Up}
 * — which fulfils the new class for every design at once.
 *
 * <p>An extension may answer only what the design does not: the same pair
 * from a design and an extension, or from two extensions, is a refusal —
 * order must not matter.</p>
 */
public interface DesignExtension extends StatelessFunctionalObject {

    String slug();

    /** The fulfilment of a design class this extension owns; {@code null} for every other. */
    Impl impl(DesignClass<?> pair);
}
