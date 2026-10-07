package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * A sign, not a fault: what reading a taxonomy notices and refuses nothing for.
 *
 * @param sign what was noticed
 * @param says the finding, naming what showed it
 */
public record TaxonomyFinding(Sign sign, String says) implements ValueObject {

    /** What reading notices. */
    public enum Sign {
        /** A component whose every part is optional: probably a branch missing its plain leaf. */
        ALL_OPTIONAL,
        /** A role given to the reader that no component names. */
        ROLE_UNNAMED,
        /** A role played by different components in different places: the material for deciding what may play it. */
        ROLE_PLAYED_VARIOUSLY
    }

    @Override public String toString() { return sign + ": " + says; }
}
