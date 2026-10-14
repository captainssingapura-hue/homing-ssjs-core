package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * Why a design was refused, one problem each.
 *
 * @param rule what was broken
 * @param says the problem, naming what broke it
 */
public record DesignProblem(Rule rule, String says) implements ValueObject {

    /** The rules designs are read under: what the compiler cannot refuse. */
    public enum Rule {
        /** A concrete leaf whose leaf or design cannot be read off its declaration. */
        UNREADABLE,
        /** A concrete leaf that says nothing of how it is drawn. */
        NOT_DRAWN,
        /** Two designs with one name. */
        NAME_TWICE,
        /** A design in no scheme: its first type argument the marker, a variable, or no abstract scheme (D33). */
        NO_SCHEME,
        /** A design given a theme whose scheme does not see the design's: drawn only where its colours are (D33). */
        SCHEME_UNSEEN,
        /** A design with two concrete leaves of one leaf. */
        LEAF_TWICE,
        /** A design with no concrete leaf of a leaf of its branch: it is complete over its branch, or refused. */
        LEAF_MISSING,
        /** A virtual leaf that owns no target, owns one twice, or owns one no sheet carries. */
        OWNS_BADLY,
        /** A concrete leaf whose drawings cannot be made in a theme. */
        DRAWING_REFUSED,
        /** A drawing of a target its leaf does not own. */
        NOT_OWNED,
        /** A target drawn twice by one concrete leaf. */
        DRAWN_TWICE,
        /** A target its leaf owns that a concrete leaf leaves undrawn: nothing falls through to the theme. */
        UNDRAWN,
        /** A drawing taken from a concrete leaf of another leaf, from one that draws nothing of it, or round in a circle. */
        AS_ASTRAY,
        /** An animation effect that takes a property another target its leaf owns writes: what an effect takes, nothing else there may write. */
        EFFECT_TAKES_OWNED
    }

    @Override public String toString() { return rule + ": " + says; }
}
