package hue.captains.singapura.js.homing.ui.menu.tree;

import java.util.List;

/**
 * A second-level row: listed under an {@link M1_Node}; its children, if
 * any, are {@link M3_Node}s.
 *
 * @param <P>    the concrete first-level row
 * @param <Self> the concrete row's own type
 */
public non-sealed interface M2_Node<P extends M1_Node<?, P>, Self extends M2_Node<P, Self>> extends MenuRow<P, Self> {
    @Override default List<? extends M3_Node<Self, ?>> children() { return List.of(); }
}
