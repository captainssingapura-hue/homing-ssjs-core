package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * One answer a theme gives: a node, a state, a target. At rest it answers a casting's look; in any
 * other state it answers a treatment, which is common to every role - so a state is answered on a
 * component on its own, never on a role (RFC 0067, appendix Castings: a state answered per role is
 * refused).
 *
 * @param node   the casting the theme answers; on its own unless the state is {@link State#REST}
 * @param state  the state: {@link State#REST} for the look, any other for a treatment
 * @param target the target leaf
 */
public record Answer(Casting node, State state, Target target) implements ValueObject {

    public Answer {
        Objects.requireNonNull(node, "Answer.node");
        Objects.requireNonNull(state, "Answer.state");
        Objects.requireNonNull(target, "Answer.target");
        if (state != State.REST && !node.standalone())
            throw new IllegalArgumentException(node + " is answered " + SemanticTokens.INSTANCE.state(state)
                    + ": a state is a treatment, common to every role, and is answered on the component on its own");
    }
}
