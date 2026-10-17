package hue.captains.singapura.js.homing.design.local;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.LocalStyleBranch;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * A local style (RFC 0067, D29, appendix Local Styles; was a {@code Design}): the look of one {@link LocalStyleBranch
 * local-style branch}, chosen where the thing is made - a grid made with the house's grid, a game's board with the
 * game's own - and drawn inside whatever style the reader wears. A style ({@code DesignStyle}) is something else: the
 * house's look of every component, chosen by the reader. A typed identity - a stateless record with an {@code INSTANCE},
 * its type its identity - listing its {@link ConcreteComponent concrete leaves}, one for each virtual leaf of its
 * branch, each naming it back. A flavour of a local style is a local style of its own.
 *
 * <p>It is in one colour scheme, its first type argument, as a style is (RFC 0067 D33): the colours its leaves are drawn
 * in are ones that scheme sees, and it is drawn only in a style whose scheme sees its own.</p>
 *
 * <pre>{@code
 * public record MinesweeperVista() implements LocalStyle<BaseScheme, GridFrame> {
 *     public static final MinesweeperVista INSTANCE = new MinesweeperVista();
 *     @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
 *     @Override public List<ConcreteComponent<?, GridFrame, ?>> concretes() { return List.of(Edge.INSTANCE, Lattice.INSTANCE, …); }
 * }
 * }</pre>
 *
 * @param <S> the colour scheme it is in
 * @param <N> the local-style branch it styles
 */
public interface LocalStyle<S extends ColorScheme, N extends ComponentBranch & LocalStyleBranch> extends StatelessFunctionalObject {

    /** The scheme it is in: the scheme's one object, said by the local style itself (D49) - nothing reads it off its type. */
    S scheme();

    /** How it draws its branch: a concrete leaf of each virtual leaf, each naming this local style. */
    List<? extends ConcreteComponent<?, N, ?>> concretes();

    /** Its name, from its type: {@code MinesweeperVista} gives {@code minesweeper-vista}. */
    default String token() { return NodeName.ofType(getClass(), "").value(); }
}
