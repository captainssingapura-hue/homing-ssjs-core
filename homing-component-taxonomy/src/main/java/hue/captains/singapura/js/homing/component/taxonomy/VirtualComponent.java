package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A leaf with no look of its own: an interface, which a local style's concrete leaves implement, each its
 * local style's drawing of it (RFC 0067, appendix Virtual Leaves and Designs, and Local Styles). It means
 * something and shows states, as any leaf does; the targets its concrete leaves answer are design language,
 * declared beside it. Locked to its branch by its type: a virtual leaf sits under a {@link LocalStyleBranch}
 * and nowhere else, and {@link #parent()} is that branch. Its concrete leaves and the local styles they
 * belong to are the design side's ({@code homing-design-local-style}); the tree knows neither.
 *
 * <p>It is never cast and plays no part: what a page wears is a concrete leaf of it, never it. Each
 * carries its own interface for extension, binding itself and its branch once, so that a local style
 * writes only its own name:</p>
 *
 * <pre>{@code
 * public record CursorBorder() implements VirtualComponent<GridFrame>, Owns {
 *     public static final CursorBorder INSTANCE = new CursorBorder();
 *     @Override public GridFrame parent() { return GridFrame.INSTANCE; }
 *     …
 *     public interface Concrete<D extends LocalStyle<?, GridFrame>> extends ConcreteComponent<CursorBorder, GridFrame, D> {}
 * }
 * }</pre>
 *
 * @param <N> the local-style branch it sits under
 */
public interface VirtualComponent<N extends ComponentBranch & LocalStyleBranch> extends Component<N> {}
