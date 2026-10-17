package hue.captains.singapura.js.homing.design.local;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.LocalStyleBranch;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * One {@link VirtualComponent virtual leaf} as one {@link LocalStyle} draws it (RFC 0067, appendix Virtual Leaves and
 * Designs, and Local Styles). Never a node of the tree: it adds no part, role, state or event, is never cast but on its
 * own, and its states are its virtual leaf's - so a local style arrives without altering the taxonomy.
 *
 * <p>The types hold the pairing. The leaf and the local style share their branch {@code N}, so a concrete leaf of one
 * branch's leaf with another branch's local style does not compile; and a class cannot implement this interface twice
 * with different arguments, so a concrete leaf is of one leaf and one local style. A concrete leaf implements its leaf's
 * own interface, which binds the leaf and the branch, and names only its local style:</p>
 *
 * <pre>{@code
 * public record VistaCursorBorder() implements CursorBorder.Concrete<MinesweeperVista> { … }
 * }</pre>
 *
 * <p>Both accessors are read off the declaration ({@link ReadConcrete}), as {@code ImplFor} reads its leaf: the first
 * and the third type arguments' {@code INSTANCE}.</p>
 *
 * @param <VC> the virtual leaf it draws
 * @param <N>  the local-style branch the leaf and the local style share
 * @param <D>  the local style it belongs to
 */
public interface ConcreteComponent<VC extends VirtualComponent<N>, N extends ComponentBranch & LocalStyleBranch, D extends LocalStyle<?, N>>
        extends StatelessFunctionalObject {

    /** The virtual leaf it draws: the first type argument's {@code INSTANCE}. */
    @SuppressWarnings("unchecked")
    default VC virtual() { return (VC) ReadConcrete.INSTANCE.virtualOf(getClass()); }

    /** The local style it belongs to: the third type argument's {@code INSTANCE}. */
    @SuppressWarnings("unchecked")
    default D localStyle() { return (D) ReadConcrete.INSTANCE.localStyleOf(getClass()); }
}
