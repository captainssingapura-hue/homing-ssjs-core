package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * Why a local style was refused, one problem each.
 *
 * @param rule what was broken
 * @param says the problem, naming what broke it
 */
public record LocalStyleProblem(Rule rule, String says) implements ValueObject {

    /** The rules local styles are read under: what the compiler cannot refuse. */
    public enum Rule {
        /** A concrete leaf whose leaf or local style cannot be read off its declaration. */
        UNREADABLE,
        /** A concrete leaf a local style lists that names another local style: each lists its own. */
        NAMES_ANOTHER,
        /** A concrete leaf that says nothing of how it is drawn. */
        NOT_DRAWN,
        /** Two local styles with one name, or one given twice. */
        NAME_TWICE,
        /** A local style in no scheme: its first type argument the marker, a variable, or no abstract scheme (D33). */
        NO_SCHEME,
        /** A local style of a branch, or a concrete leaf of a leaf, the taxonomy has not: its branch comes with it, in the module that registers it. */
        NOT_IN_TAXONOMY,
        /** A local style given a style whose scheme does not see its own: drawn only where its colours are (D33). */
        SCHEME_UNSEEN,
        /** A local style with two concrete leaves of one leaf. */
        LEAF_TWICE,
        /** A local style with no concrete leaf of a leaf of its branch: it is complete over its branch, or refused. */
        LEAF_MISSING,
        /** A virtual leaf that owns no target, owns one twice, or owns one no sheet carries. */
        OWNS_BADLY,
        /** A concrete leaf whose drawings cannot be made in a style. */
        DRAWING_REFUSED,
        /** A drawing of a target its leaf does not own. */
        NOT_OWNED,
        /** A target drawn twice by one concrete leaf. */
        DRAWN_TWICE,
        /** A target its leaf owns that a concrete leaf leaves undrawn: nothing falls through to the style. */
        UNDRAWN,
        /** A drawing taken from a concrete leaf of another leaf, from one that draws nothing of it, or round in a circle. */
        AS_ASTRAY,
        /** An animation effect that takes a property another target its leaf owns writes: what an effect takes, nothing else there may write. */
        EFFECT_TAKES_OWNED
    }

    @Override public String toString() { return rule + ": " + says; }
}
