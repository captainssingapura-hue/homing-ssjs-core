package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A component with no parts (RFC 0067, D53): nothing nests in it, so where it plays a part its look is an implementation
 * alone. A leaf that declares parts is refused when the taxonomy is read ({@code KIND_BADLY}).
 *
 * <pre>{@code
 * public record PlainButton() implements Leaf<Button> { ... }
 * }</pre>
 *
 * @param <P> the branch this component sits under
 */
public interface Leaf<P extends ComponentBranch> extends Component<P> {}
