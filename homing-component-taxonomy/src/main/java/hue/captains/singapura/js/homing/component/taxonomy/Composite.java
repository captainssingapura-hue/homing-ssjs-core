package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A component with parts (RFC 0067, D53): components nest in it, so where it plays a part its look is a declaration - its
 * own implementation, and a look for each of its slots - never an implementation alone. It says its parts; a composite
 * that declares none is refused when the taxonomy is read ({@code KIND_BADLY}).
 *
 * <pre>{@code
 * public record Dialog() implements Composite<Pane> {
 *     @Override public List<Slot<?>> parts() { return List.of(DSL.part(ActionBar.INSTANCE).as(Actions.INSTANCE).one()); }
 * }
 * }</pre>
 *
 * @param <P> the branch this component sits under
 */
public interface Composite<P extends ComponentBranch> extends Component<P> {

    /** Its parts: at least one. */
    @Override List<Slot<?>> parts();
}
