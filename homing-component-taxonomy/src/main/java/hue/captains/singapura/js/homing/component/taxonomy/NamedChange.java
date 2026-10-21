package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * A change of a type of state, named on it (RFC 0069, amended 21 Oct): {@code click}, a count going up; {@code press},
 * a held state going down; {@code dismiss}, shown to dismissed. A listener may be set for one named change alone. That
 * it fits its type's domain - its values named there, a number's rise only where there is a span - is held when the
 * tree is read.
 *
 * @param name       its name: lower case, its words joined by hyphens
 * @param transition which change it is
 */
public record NamedChange(String name, Transition transition) implements ValueObject {

    public NamedChange {
        Objects.requireNonNull(name, "NamedChange.name");
        Objects.requireNonNull(transition, "NamedChange.transition");
        if (!name.matches("[a-z][a-z0-9]*(-[a-z0-9]+)*"))
            throw new IllegalArgumentException("a change is named in lower case, its words joined by hyphens: '" + name + "'");
    }

    /** Which change a named change is. */
    public sealed interface Transition extends ValueObject permits Between, Rises, Falls {}

    /** From one named value to another. */
    public record Between(StateValue from, StateValue to) implements Transition {
        public Between {
            Objects.requireNonNull(from, "Between.from");
            Objects.requireNonNull(to, "Between.to");
            if (from.equals(to)) throw new IllegalArgumentException("a change is between two different values, not " + from + " and " + to);
        }
    }

    /** A number going up. */
    public record Rises() implements Transition {}

    /** A number going down. */
    public record Falls() implements Transition {}
}
