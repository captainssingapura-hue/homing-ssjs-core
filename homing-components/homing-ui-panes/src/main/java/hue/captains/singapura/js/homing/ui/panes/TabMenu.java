package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.design.Icon;
import hue.captains.singapura.js.homing.ui.menu.tree.ContextMenuKind;
import hue.captains.singapura.js.homing.ui.menu.tree.M1_Node;

import java.util.List;

/**
 * The menu of a tab: what a right-click or Shift+F10 on a chip offers —
 * detach it to float, or close it. Owned here, by the crate whose component
 * opens it ({@link MultiTabPaneModule.MultiTabPane} names it as a need), and
 * derived into any site's registry that serves the pane. The object bound
 * while it is open is {@code { pane, tab, anchor }}: the dock, the tab
 * record, the chip; what a pick does — the undock, the removal — is the
 * page's handler, since the pane knows no desk.
 */
public record TabMenu() implements ContextMenuKind<TabMenu> {

    public static final TabMenu INSTANCE = new TabMenu();

    @Override public List<? extends M1_Node<TabMenu, ?>> children() { return List.of(Detach.INSTANCE, Close.INSTANCE); }

    /** Float this tab in a pane of its own. */
    public record Detach() implements M1_Node<TabMenu, Detach> {
        public static final Detach INSTANCE = new Detach();
        @Override public TabMenu parent() { return TabMenu.INSTANCE; }
        @Override public String label() { return "Detach"; }
        @Override public Class<? extends Icon> icon() { return Icon.Detach.class; }
        @Override public String hint() { return "float it in a pane of its own"; }
    }

    /** Close this tab; its widget is disposed. */
    public record Close() implements M1_Node<TabMenu, Close> {
        public static final Close INSTANCE = new Close();
        @Override public TabMenu parent() { return TabMenu.INSTANCE; }
        @Override public String label() { return "Close"; }
        @Override public Class<? extends Icon> icon() { return Icon.Close.class; }
        @Override public int section() { return 1; }
    }
}
