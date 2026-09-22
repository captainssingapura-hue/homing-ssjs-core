package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.util.ResourceReader;
import hue.captains.singapura.js.homing.design.Icon;
import hue.captains.singapura.js.homing.ui.menu.ContextMenuRegistry;
import hue.captains.singapura.js.homing.ui.menu.tree.MenuTrees;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The tab menu: a kind this crate owns, named by the pane's declaration as
 * its need, and the one word the JS opens it by agreeing with the Java.
 */
class TabMenuTest {

    @Test
    void theKindIsTab_detachThenClose_withTheirIcons() {
        assertEquals("tab", TabMenu.INSTANCE.kind());
        assertEquals(List.of(), MenuTrees.validate(TabMenu.INSTANCE));
        assertEquals(List.of("detach", "close"), MenuTrees.rows(TabMenu.INSTANCE).stream().map(r -> r.id()).toList());
        assertEquals(Icon.Detach.class, TabMenu.Detach.INSTANCE.icon());
        assertEquals(Icon.Close.class, TabMenu.Close.INSTANCE.icon());
        assertEquals(1, TabMenu.Close.INSTANCE.section(), "a divider before Close");
    }

    @Test
    void thePaneDeclaresTheNeed_andItsJsOpensTheSameKind() {
        assertEquals(java.util.Set.of(TabMenu.INSTANCE), new MultiTabPaneModule.MultiTabPane().required());
        String js = String.join("\n", ResourceReader.INSTANCE.getStringsFromResource("homing/js/hue/captains/singapura/js/homing/ui/panes/MultiTabPaneModule.js"));
        assertTrue(js.contains("static MENU = \"" + TabMenu.INSTANCE.kind() + "\";"), "the JS constant is the Java kind");
        assertTrue(js.contains("PaneMenus.forChip(this, tab.id, MultiTabPane.MENU)"), "the pane names its kind by its constant, never a loose string");
        String menus = String.join("\n", ResourceReader.INSTANCE.getStringsFromResource("homing/js/hue/captains/singapura/js/homing/ui/panes/PaneMenusModule.js"));
        assertTrue(menus.contains("pane._menus.open(kind,"), "and PaneMenus opens the kind it was handed, never one of its own");
        // a site that serves the pane's crate holds the kind, listing nothing
        assertEquals(List.of("tab"), ContextMenuRegistry.requiredBy(List.of(UiPanesCrate.INSTANCE)).kinds().stream().map(k -> k.kind()).toList());
        assertEquals(List.of(), ContextMenuRegistry.validate(List.of(UiPanesCrate.INSTANCE)));
    }
}
