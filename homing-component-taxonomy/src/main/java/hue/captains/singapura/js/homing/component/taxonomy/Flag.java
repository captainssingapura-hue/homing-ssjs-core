package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A type of state that is yes or no, its look a class ({@link Classed}): its values {@link StateValue#FALSE false} and {@link
 * StateValue#TRUE true}, the one at rest first - false, unless the type says otherwise: a component
 * is enabled at rest, and pressed only when pressed.
 *
 * <pre>{@code
 * public record Enabled() implements Flag<Availability> {
 *     public static final Enabled INSTANCE = new Enabled();
 *     @Override public Availability parent() { return Availability.INSTANCE; }
 *     @Override public boolean rest() { return true; }
 * }
 * }</pre>
 *
 * @param <P> the branch it is filed under
 */
public interface Flag<P extends StateBranch> extends Classed<P> {

    /** Its value at rest: false, unless it says otherwise. */
    default boolean rest() { return false; }

    @Override default List<StateValue> values() {
        return rest() ? List.of(StateValue.TRUE, StateValue.FALSE) : List.of(StateValue.FALSE, StateValue.TRUE);
    }
}
