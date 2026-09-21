package hue.captains.singapura.js.homing.ui.menu.tree;

import hue.captains.singapura.js.homing.design.Icon;

/**
 * A row of a context menu: what is constant about it — its label, the
 * icon word it shows, a hint, and the section it sits in. Rows are listed
 * in order, and a divider is drawn where the section changes from one row
 * to the next. What varies with the object the menu is opened for —
 * disabled, checked, hidden — is the handler's, asked at bind, never the
 * row's. A row with children opens them beside it; a row without is picked.
 *
 * @param <P>    the parent node's type: the kind for a first-level row, the row above for a deeper one
 * @param <Self> the concrete row's own type
 */
public sealed interface MenuRow<P extends MenuNode<P>, Self extends MenuRow<P, Self>> extends MenuNode<Self> permits M1_Node, M2_Node, M3_Node {

    /** The node this row is listed under. */
    P parent();

    /** The text of the row. */
    String label();

    /** The icon word shown before the label, or null for none: the design draws it. */
    default Class<? extends Icon> icon() { return null; }

    /** A hint after the label — a shortcut, a note — or null. */
    default String hint() { return null; }

    /** The section the row sits in: a divider is drawn between rows of different sections. */
    default int section() { return 0; }
}
