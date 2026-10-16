package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A design of one {@link DesignBranch design branch} (RFC 0067, appendix Virtual Leaves and Designs): a
 * typed identity - a stateless record with an {@code INSTANCE}, its type its identity - gathering the
 * {@link ConcreteComponent concrete leaves} that name it. It is complete when every virtual leaf of its
 * branch has exactly one concrete leaf in it. A flavour of a design is a design of its own.
 *
 * <p>It is in one colour scheme, its first type argument, as a style is (RFC 0067 D33): the colours its leaves are
 * drawn in are ones that scheme sees, and it is drawn only in a theme whose scheme sees its own. The house's designs
 * are the base's, and so is a domain's drawn in the house's palettes - Minesweeper's Vista.</p>
 *
 * <pre>{@code
 * public record MinesweeperClassic() implements Design<BaseScheme, GridFrame> {
 *     public static final MinesweeperClassic INSTANCE = new MinesweeperClassic();
 * }
 * }</pre>
 *
 * @param <S> the colour scheme it is in
 * @param <N> the design branch it designs
 */
public interface Design<S extends ColorScheme, N extends ComponentBranch & DesignBranch> extends StatelessFunctionalObject {

    /** Its name, from its type: {@code MinesweeperClassic} gives {@code minesweeper-classic}. */
    default String token() { return NodeName.ofType(getClass(), "").value(); }
}
