package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * A product's fulfilment of the design classes it added — its own semantic
 * leaves' projections — registered beside a design in a deployment. Two
 * honest forms: <i>bound</i>, values for one specific design; <i>derived</i>,
 * expressed over the base design's own classes through a lazy provider
 * ({@code Up} as {@code Success}), which fulfils the new class for every
 * design at once while any one design may still specialise it.
 *
 * <p>An extension may fulfil only classes the design does not: the same
 * class from a design and an extension, or from two extensions, is a refusal
 * — order must not matter.</p>
 */
public interface DesignExtension extends StatelessFunctionalObject {

    String slug();

    List<ImplProvider<?>> providers();
}
