package hue.captains.singapura.js.homing.design.color;

/**
 * What kind a leaf of the colour tree is, as a type. A leaf names its kind once, through the interface it implements,
 * and a class cannot implement {@link ColorNode} twice with different arguments - so a leaf with two kinds does not
 * compile, nor one with none, ColorLeaf being sealed.
 */
public sealed interface LeafKind extends ColorNode.Kind permits LeafKind.ColorKind, LeafKind.ScaleKind {

    /** A colour: what a target may be painted in. */
    sealed interface ColorKind extends LeafKind permits ConcreteKind, InterpolatedKind {}

    /** A colour derived from no other: what a scale's knots may be. */
    sealed interface ConcreteKind extends ColorKind permits PalettableKind, FixedKind {}

    /** The worn palette gives it. */
    record PalettableKind() implements ConcreteKind {}

    /** Its declaration gives it. */
    record FixedKind() implements ConcreteKind {}

    /** A colour scale's, at a degree. */
    record InterpolatedKind() implements ColorKind {}

    /** A colour scale: no colour a target is painted in, the range interpolated colours come from. */
    record ScaleKind() implements LeafKind {}
}
