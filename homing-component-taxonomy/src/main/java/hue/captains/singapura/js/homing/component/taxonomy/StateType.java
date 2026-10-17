package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;
import java.util.Map;

/**
 * A type of state, a leaf of the tree of states: one respect in which a component may be one way or
 * another - Enabled (true or false), Elevation (flat, elevated or sunken). The tree files the types,
 * never their values: a component that has a type is in exactly one of its values at a time, so it
 * has one value of each type it names ({@link Component#states()}). A shared, stateless singleton
 * filed under a branch, the root's included; each has its change, an event no tree needs to hold.
 * A type that is yes or no is a {@link Flag}.
 *
 * <p>A design answers a value by its word. Every value but the one at rest has one: its established
 * design word where there is one ({@link #words()} - Enabled's false is {@code disabled}, Hovered's
 * true {@code hover}), and otherwise the type's name and the value's, joined: {@code busy-true}.</p>
 *
 * <pre>{@code
 * public record Elevation() implements StateType<Physical> {
 *     public static final Elevation INSTANCE = new Elevation();
 *     @Override public Physical parent() { return Physical.INSTANCE; }
 *     @Override public List<StateValue> values() {
 *         return List.of(new StateValue("flat"), new StateValue("elevated"), new StateValue("sunken"));
 *     }
 * }
 * }</pre>
 *
 * @param <P> the branch it is filed under
 */
public non-sealed interface StateType<P extends StateBranch> extends StateNode {

    /** The branch it is filed under. */
    P parent();

    /**
     * Its values, which exclude one another, the one at rest first: the value a look is drawn for
     * when it says nothing of the state. Two at least, each once - held when the tree is read.
     */
    List<StateValue> values();

    /**
     * The established design words of those of its values that have one - Enabled's false is
     * {@code disabled}; none by default. Never its rest's, which a design answers as the look itself.
     */
    default Map<StateValue, String> words() { return Map.of(); }

    /** The word a design answers a value by: its established one, or the type's name and the value's joined - {@code busy-true}. */
    default String word(StateValue value) {
        if (!values().contains(value)) throw new IllegalArgumentException(name().value() + " has no value " + value);
        if (value.equals(values().get(0))) throw new IllegalArgumentException(name().value() + "'s " + value + " is its rest: a look, not a word");
        String word = words().get(value);
        return word != null ? word : name().value() + "-" + value.token();
    }
}
