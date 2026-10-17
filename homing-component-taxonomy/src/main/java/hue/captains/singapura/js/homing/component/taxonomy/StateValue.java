package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * One value of a type of state: Enabled's true, Elevation's sunken. A type's values exclude one
 * another - a component that has the type is in exactly one of them at a time - and the first is its
 * rest. A value is named in lower case, its words joined by hyphens; it is no node of the tree, which
 * files the types, not their values.
 *
 * @param token the value's name: {@code true}, {@code sunken}
 */
public record StateValue(String token) implements ValueObject {

    /** Yes, for a type that is yes or no. */
    public static final StateValue TRUE = new StateValue("true");

    /** No, for a type that is yes or no. */
    public static final StateValue FALSE = new StateValue("false");

    public StateValue {
        Objects.requireNonNull(token, "StateValue.token");
        if (!token.matches("[a-z][a-z0-9]*(-[a-z0-9]+)*"))
            throw new IllegalArgumentException("a state's value is named in lower case, its words joined by hyphens: '" + token + "'");
    }

    @Override public String toString() { return token; }
}
