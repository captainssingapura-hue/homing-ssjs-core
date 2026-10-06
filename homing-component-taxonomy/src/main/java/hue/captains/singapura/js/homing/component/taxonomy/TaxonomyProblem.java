package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * Why a taxonomy was refused, one problem each.
 *
 * @param rule what was broken
 * @param says the problem, naming what broke it
 */
public record TaxonomyProblem(Rule rule, String says) implements ValueObject {

    /** The rules a taxonomy is read under. */
    public enum Rule {
        /** A kind or a component names no parent. */
        NO_PARENT,
        /** A chain of parents comes back on itself. */
        PARENT_CYCLE,
        /** A component lists a role that is not nested in it: only a component declares its roles. */
        ROLE_NOT_ITS_OWN,
        /** A component lists one role twice. */
        ROLE_TWICE,
        /** A role names no component to play it. */
        NO_BASE,
        /** A component is, through the roles of its roles, a part of itself. */
        COMPOSITION_CYCLE,
        /** Two nodes derive one token. */
        TOKEN_TWICE
    }

    @Override public String toString() { return rule + ": " + says; }
}
