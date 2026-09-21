package hue.captains.singapura.js.homing.ui.menu;

import org.junit.jupiter.api.Test;

import static hue.captains.singapura.js.homing.ui.menu.ContextMenuRegistry.ContextMenuType;
import static hue.captains.singapura.js.homing.ui.menu.ContextMenuRegistry.MenuItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The registry: kinds declared once, item ids unique within a kind, a submenu one level deep, and the data it stamps. */
class ContextMenuRegistryTest {

    private static final ContextMenuRegistry R = ContextMenuRegistry.of(
            ContextMenuType.of("animal",
                    MenuItem.of("rotate", "Rotate", "a quarter turn"),
                    MenuItem.divider(),
                    MenuItem.submenu("animal", "Animal", MenuItem.of("cat", "Cat"), MenuItem.of("dog", "Dog"))),
            ContextMenuType.of("counter", MenuItem.of("reset", "Reset")));

    @Test
    void itStampsTheKindsAndTheirItemsAsData() {
        assertEquals("{\"animal\":{\"kind\":\"animal\",\"items\":[{\"id\":\"rotate\",\"label\":\"Rotate\",\"hint\":\"a quarter turn\"},{\"separator\":true},"
                   + "{\"id\":\"animal\",\"label\":\"Animal\",\"items\":[{\"id\":\"cat\",\"label\":\"Cat\"},{\"id\":\"dog\",\"label\":\"Dog\"}]}]},"
                   + "\"counter\":{\"kind\":\"counter\",\"items\":[{\"id\":\"reset\",\"label\":\"Reset\"}]}}", R.json());
        assertTrue(R.js().get(2).startsWith("const MENUS = Object.freeze({"), R.js().toString());
    }

    @Test
    void aKindDeclaredTwiceIsRefused() {
        var ex = assertThrows(IllegalArgumentException.class, () -> ContextMenuRegistry.of(ContextMenuType.of("a", MenuItem.of("x", "X")), ContextMenuType.of("a", MenuItem.of("y", "Y"))));
        assertTrue(ex.getMessage().contains("kind declared twice: a"), ex.getMessage());
    }

    @Test
    void anItemIdRepeatedWithinAKindIsRefused_acrossLevelsToo() {
        var ex = assertThrows(IllegalArgumentException.class, () -> ContextMenuType.of("a", MenuItem.of("x", "X"), MenuItem.submenu("s", "S", MenuItem.of("x", "again"))));
        assertTrue(ex.getMessage().contains("item id repeated: x"), ex.getMessage());
        assertThrows(IllegalArgumentException.class, () -> ContextMenuType.of("a"), "no items");
        assertThrows(IllegalArgumentException.class, () -> MenuItem.submenu("s", "S"), "a submenu with no items");
        assertThrows(IllegalArgumentException.class, () -> MenuItem.submenu("s", "S", MenuItem.submenu("t", "T", MenuItem.of("u", "U"))), "one level deep");
        assertThrows(IllegalArgumentException.class, () -> MenuItem.of("", "X"));
    }
}
