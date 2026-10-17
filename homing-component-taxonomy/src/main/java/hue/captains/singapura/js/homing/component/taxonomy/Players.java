package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Who may play a slot's role (RFC 0067, D52): one component; one of several, the owner mounting one at a time; or any
 * component under a branch - those the instance registers there, read with the taxonomy. One slot for a role still, and
 * its cardinality the role's: how many of them it holds at once, whichever plays.
 */
public sealed interface Players extends ValueObject permits Players.One, Players.AnyOf, Players.Under {

    /** How a reader says it: {@code button}, {@code size | aspect}, {@code any icon}. */
    String say();

    /**
     * One component plays the role; none is refused when the taxonomy is read.
     *
     * @param component the component
     */
    record One(Component<?> component) implements Players {
        @Override public String say() { return component == null ? "nothing" : component.name().value(); }
    }

    /**
     * One of several components plays it, the owner choosing which: each different, two at the least.
     *
     * @param components the alternatives, in the order the owner lists them
     */
    record AnyOf(List<Component<?>> components) implements Players {
        public AnyOf {
            Objects.requireNonNull(components, "AnyOf.components");
            components = List.copyOf(components);
            if (components.size() < 2) throw new IllegalArgumentException("one of " + components.size() + " is no choice: name the one, or two at the least");
            if (new HashSet<>(components).size() != components.size()) throw new IllegalArgumentException("a component is named twice among the alternatives");
        }

        @Override public String say() { return String.join(" | ", components.stream().map(c -> c.name().value()).toList()); }
    }

    /**
     * Any component under a branch plays it - those the instance registers there, a downstream's with the house's; none
     * is refused when the taxonomy is read.
     *
     * @param branch the branch
     */
    record Under(ComponentBranch branch) implements Players {
        @Override public String say() { return branch == null ? "nothing" : "any " + branch.name().value(); }
    }
}
