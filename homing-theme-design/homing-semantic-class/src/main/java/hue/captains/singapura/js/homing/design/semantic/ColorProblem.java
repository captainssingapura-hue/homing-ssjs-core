package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * Why a colour tree was refused, one problem each.
 *
 * @param rule what was broken
 * @param says the problem, naming what broke it
 */
public record ColorProblem(Rule rule, String says) implements ValueObject {

    /** The rules a colour tree is read under: what the compiler cannot refuse. */
    public enum Rule {
        /** A node whose parent or kind cannot be read off its declaration: one written raw. */
        UNREADABLE,
        /** A node that is not a record with no components: a node is a stateless record, its type its meaning. */
        NOT_STATELESS,
        /** A node in no scheme - the marker itself, or a variable - or under a parent its scheme cannot see. */
        SCHEME_ASTRAY,
        /** A node whose parent the tree has not: a branch neither its owner nor an extension lists. */
        PARENT_MISSING,
        /** Two nodes with one name, or one node listed twice. */
        NAME_TWICE,
        /** A scale with fewer than three knots, knots not strictly rising from -1 to 1, or a knot of a colour the tree has not. */
        SCALE_BADLY,
        /** An interpolated colour at a degree outside -1 to 1, or of a scale the tree has not. */
        INTERPOLATED_BADLY,
        /** A fixed colour that gives no colour. */
        FIXED_BADLY,
        /** An ink of no share, or of more than the ink whole. */
        INK_BADLY,
        /** An extension grafting palettable colours whose base sub-palette, its last word, is missing or leaves one unanswered. */
        LAST_WORD_MISSING
    }

    @Override public String toString() { return rule + ": " + says; }
}
