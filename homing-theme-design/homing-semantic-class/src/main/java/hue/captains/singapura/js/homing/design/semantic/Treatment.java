package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * A treatment a sheet has a rule for: a component, in a state, on a target - worn as
 * {@code .component__target--state}. Common to every role, so a component on its own.
 *
 * @param component the component treated
 * @param state     the state, never rest
 * @param target    the target
 */
public record Treatment(Component<?> component, State state, Target target) implements ValueObject {

    public Treatment {
        Objects.requireNonNull(component, "Treatment.component");
        Objects.requireNonNull(state, "Treatment.state");
        Objects.requireNonNull(target, "Treatment.target");
        if (state == State.REST) throw new IllegalArgumentException("rest is a look, not a treatment");
    }
}
