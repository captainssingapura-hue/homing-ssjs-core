package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;
import java.util.Map;

/**
 * A type of state whose look is a class (RFC 0069, amended 21 Oct): named values only, which exclude
 * one another, the one at rest first; each but its rest worn as a class while it holds. A type writes a
 * class only where it is declared so: every change of state can be seen, and a position, a size or a
 * count is drawn by what it moves or shows. A type that is yes or no is a {@link Flag}.
 *
 * <p>A design answers a value by its word. Every value but the one at rest has one: its established
 * design word where there is one ({@link #words()} - Enabled's false is {@code disabled}, Hovered's
 * true {@code hover}), and otherwise the type's name and the value's, joined: {@code busy-true}.</p>
 *
 * <pre>{@code
 * public record Elevation() implements Classed<Physical> {
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
public interface Classed<P extends StateBranch> extends StateType<P> {

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

    /** Its domain: its named values, each a point, its rest the first. */
    @Override default StateDomain domain() {
        return new StateDomain.Ranges(values().stream().<StateDomain.Range>map(StateDomain.Point::new).toList(), new StateDomain.Named(values().get(0)));
    }
}
