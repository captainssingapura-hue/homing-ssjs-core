package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A leaf with no look of its own: an interface, which a design's {@link ConcreteComponent concrete
 * leaves} implement, each its design's drawing of it (RFC 0067, appendix Virtual Leaves and Designs).
 * It means something and shows states, as any leaf does; the targets its concrete leaves answer are
 * design language, declared beside it on the design's side. Locked to its branch by its type: a
 * virtual leaf sits under a {@link DesignBranch} and nowhere else, and {@link #parent()} is that
 * branch.
 *
 * <p>It is never cast and plays no part: what a page wears is a concrete leaf of it, never it. Each
 * carries its own interface for extension, binding itself and its branch once, so that a design writes
 * only its own name:</p>
 *
 * <pre>{@code
 * public record CursorBorder() implements VirtualComponent<GridFrame>, Owns {
 *     public static final CursorBorder INSTANCE = new CursorBorder();
 *     @Override public GridFrame parent() { return GridFrame.INSTANCE; }
 *     …
 *     public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<CursorBorder, GridFrame, D> {}
 * }
 * }</pre>
 *
 * @param <N> the design branch it sits under
 */
public interface VirtualComponent<N extends ComponentBranch & DesignBranch> extends Component<N> {}
