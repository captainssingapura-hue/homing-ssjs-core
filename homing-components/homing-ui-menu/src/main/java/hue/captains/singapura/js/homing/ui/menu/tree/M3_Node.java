package hue.captains.singapura.js.homing.ui.menu.tree;

import java.util.List;

/**
 * A third-level row: listed under an {@link M2_Node}, and the last level —
 * there is no fourth, so it has no children by type: nothing can name an
 * M3 as its parent. A context menu three levels deep is as deep as one
 * should be.
 *
 * @param <P>    the concrete second-level row
 * @param <Self> the concrete row's own type
 */
public non-sealed interface M3_Node<P extends M2_Node<?, P>, Self extends M3_Node<P, Self>> extends MenuRow<P, Self> {
    @Override default List<? extends MenuRow<?, ?>> children() { return List.of(); }
}
