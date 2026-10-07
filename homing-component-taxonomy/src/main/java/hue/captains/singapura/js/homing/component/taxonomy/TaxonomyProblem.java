package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * Why a taxonomy was refused, one problem each.
 *
 * @param rule what was broken
 * @param says the problem, naming what broke it
 */
public record TaxonomyProblem(Rule rule, String says) implements ValueObject {

    /** The rules a taxonomy is read under: what the compiler cannot refuse. */
    public enum Rule {
        /** A kind, a component or a family names no parent; a role names no family. */
        NO_PARENT,
        /** A chain of parents comes back on itself, in the taxonomy or the role catalogue. */
        PARENT_CYCLE,
        /** A slot whose role is played by nothing. */
        NO_BASE,
        /** A slot that plays no role. */
        NO_ROLE,
        /** A component names one role twice. */
        ROLE_TWICE,
        /** A component is, through the parts of its parts, a part of itself - whatever the cardinalities on the way. */
        COMPOSITION_CYCLE,
        /** Two nodes derive one token. */
        TOKEN_TWICE,
        /** Two roles answer to one name: one word, one role. */
        ROLE_NAME_TWICE,
        /** A role is named as a kind or a component: it says what plays a part, not what the part does. */
        ROLE_NAMES_A_NODE,
        /** A count that is no cardinality: none, a most below the least, a range of one. */
        BAD_CARDINALITY
    }

    @Override public String toString() { return rule + ": " + says; }
}
