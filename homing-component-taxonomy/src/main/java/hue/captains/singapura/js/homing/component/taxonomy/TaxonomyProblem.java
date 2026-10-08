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
        /** A branch, a component or a role names no parent. */
        NO_PARENT,
        /** A node with state: jOntology's contract of a stateless functional object, broken. */
        NOT_STATELESS,
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
        /** Two nodes of the role catalogue - branches or roles, at any level - answer to one name: one word, one meaning. */
        CATALOGUE_NAME_TWICE,
        /** A role is named as a branch or a component: it says what plays a part, not what the part does. */
        ROLE_NAMES_A_NODE,
        /** A count that is no cardinality: none, a most below the least, a range of one. */
        BAD_CARDINALITY,
        /** An axis a node declares twice, or one a branch above it already declares: each axis is declared once along a lineage. */
        EXTENT_TWICE,
        /** A node of either tree with no meaning: no section of its own, or one with no words in it. */
        NO_MEANING,
        /** A meanings file with a section that names no node its class declares, a section twice, or words outside every section. */
        MEANING_ASTRAY,
        /** Two nodes that mean the same: one node, or two meanings to write. */
        MEANING_TWICE
    }

    @Override public String toString() { return rule + ": " + says; }
}
