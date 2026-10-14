package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * One {@link VirtualComponent virtual leaf} as one {@link Design} draws it (RFC 0067, appendix Virtual
 * Leaves and Designs). Never a node of the tree: it adds no part, role, state or event, is never cast but
 * on its own, and its states are its virtual leaf's - so a design arrives without altering the taxonomy.
 *
 * <p>The types hold the pairing. The leaf and the design share their branch {@code N}, so a concrete leaf
 * of one branch's leaf with another branch's design does not compile; and a class cannot implement this
 * interface twice with different arguments, so a concrete leaf is of one leaf and one design. A concrete
 * leaf implements its leaf's own interface, which binds the leaf and the branch, and names only its
 * design:</p>
 *
 * <pre>{@code
 * public record ClassicCursorBorder() implements CursorBorder.Concrete<MinesweeperClassic> { … }
 * }</pre>
 *
 * <p>Both accessors are read off the declaration ({@link ReadConcrete}), as {@code ImplFor} reads its leaf:
 * the first and the third type arguments' {@code INSTANCE}.</p>
 *
 * @param <VC> the virtual leaf it draws
 * @param <N>  the design branch the leaf and the design share
 * @param <D>  the design it belongs to
 */
public interface ConcreteComponent<VC extends VirtualComponent<N>, N extends ComponentBranch & DesignBranch, D extends Design<?, N>>
        extends StatelessFunctionalObject {

    /** The virtual leaf it draws: the first type argument's {@code INSTANCE}. */
    @SuppressWarnings("unchecked")
    default VC virtual() { return (VC) ReadConcrete.INSTANCE.virtualOf(getClass()); }

    /** The design it belongs to: the third type argument's {@code INSTANCE}. */
    @SuppressWarnings("unchecked")
    default D design() { return (D) ReadConcrete.INSTANCE.designOf(getClass()); }
}
