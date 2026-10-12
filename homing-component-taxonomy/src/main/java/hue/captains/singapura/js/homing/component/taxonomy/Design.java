package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A design of one {@link DesignBranch design branch} (RFC 0067, appendix Virtual Leaves and Designs): a
 * typed identity - a stateless record with an {@code INSTANCE}, its type its identity - gathering the
 * {@link ConcreteComponent concrete leaves} that name it. It is complete when every virtual leaf of its
 * branch has exactly one concrete leaf in it. A flavour of a design is a design of its own.
 *
 * <pre>{@code
 * public record MinesweeperClassic() implements Design<GridFrame> {
 *     public static final MinesweeperClassic INSTANCE = new MinesweeperClassic();
 * }
 * }</pre>
 *
 * @param <N> the design branch it designs
 */
public interface Design<N extends ComponentBranch & DesignBranch> extends StatelessFunctionalObject {

    /** Its name, from its type: {@code MinesweeperClassic} gives {@code minesweeper-classic}. */
    default String token() { return NodeName.ofType(getClass(), "").value(); }
}
