package hue.captains.singapura.js.homing.ui.menu.tree;

import java.util.List;

/**
 * A first-level row: listed under a {@link ContextMenuKind}; its children,
 * if any, are {@link M2_Node}s.
 *
 * @param <P>    the concrete kind
 * @param <Self> the concrete row's own type
 */
public non-sealed interface M1_Node<P extends ContextMenuKind<P>, Self extends M1_Node<P, Self>> extends MenuRow<P, Self> {
    @Override default List<? extends M2_Node<Self, ?>> children() { return List.of(); }
}
