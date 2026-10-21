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
        /** A node of any tree - components, roles, states, events, words - with no meaning: no section of its own, or one with no words in it. */
        NO_MEANING,
        /** A meanings file with a section that names no node its class declares, a section twice, or words outside every section. */
        MEANING_ASTRAY,
        /** Two nodes that mean the same: one node, or two meanings to write. */
        MEANING_TWICE,
        /** Two nodes of the tree of states, or two of the tree of events, answer to one name: one word, one meaning, within each tree. */
        TREE_NAME_TWICE,
        /** A type of state with fewer than two values, or one value twice: its values are what a component is in, one at a time. */
        BAD_STATE_VALUES,
        /** Two values of the tree of states answered by one design word: one word, one meaning. */
        DESIGN_WORD_TWICE,
        /** A type of state with no level of importance: every state implements one, intrinsic, on the tree of states. */
        STATE_UNRANKED,
        /** A pseudo state that is no flag, or whose pseudo-class is none. */
        PSEUDO_BADLY,
        /** A component or a part that lists a state twice or lists nothing, or overrides the level of a state it does not list, or of one twice. */
        STATES_LISTED_BADLY,
        /** A component under a local-style branch that is not virtual: a local style could never be complete over it. */
        NOT_VIRTUAL,
        /** A virtual leaf that declares parts: it has none, and its concrete leaves add none. */
        VIRTUAL_PARTS,
        /** A branch under a local-style branch, which holds virtual leaves only. */
        BRANCH_IN_LOCAL_STYLE_BRANCH,
        /** A part played by a virtual leaf: a part is cast, and a virtual leaf never is. */
        VIRTUAL_PLAYS_A_PART,
        /** A part played by the layouts' styling: it owns parts for their design, and is no part of anything. */
        LAYOUT_STYLING_PLAYS_A_PART,
        /** A family head with another family head between it and the root: families are headed at one level. */
        FAMILY_HEAD_NESTED,
        /** A component that says it heads a family: only a branch does. */
        FAMILY_HEAD_NOT_A_BRANCH,
        /** A leaf that declares parts, a composite that declares none, or a component that says it is both (D53). */
        KIND_BADLY,
        /** A component cast in a role with a look of its own that its role set does not allow - or that has no role set (D55). */
        ROLE_NOT_ALLOWED,
        /** A role set with no roles, a role twice, or one that only places a part (D55). */
        ROLE_SET_BADLY,
        /** A component that names a room twice. */
        ROOMS_BADLY
    }

    @Override public String toString() { return rule + ": " + says; }
}
