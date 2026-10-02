package hue.captains.singapura.js.homing.studio.base.tracker;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * An open or resolved design decision attached to a {@link Plan}. The renderer
 * shows decisions in their own section above the phase list, with status
 * badges and (when resolved) the chosen value.
 */
public record Decision(
        String id,
        String question,
        String recommendation,
        String chosenValue,        // null when OPEN; chosen option when RESOLVED
        DecisionStatus status,
        String rationale,
        String notes
) implements ValueObject {}
