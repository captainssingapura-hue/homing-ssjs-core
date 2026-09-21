package hue.captains.singapura.js.homing.ui.menu;

import java.util.Objects;
import java.util.Set;

/**
 * The closed vocabulary of context menus — the sealed sum the JS
 * {@code MenuEvents} factories mirror, record for record, component for
 * component. A menu of a kind opened at a point, an item of it picked, the
 * menu closed for a reason; the object the menu was bound to is not here —
 * it is the holder's, told through the kind's handler. {@code MenuEventsTest}
 * holds the two sides together.
 */
public sealed interface MenuEvent {

    /** Why a menu closed: a pick, the Escape key, a press outside, a scroll, the window losing focus, another menu replacing it, or its owner. */
    Set<String> REASONS = Set.of("pick", "escape", "outside", "scroll", "blur", "taken", "replaced", "owner");

    /** The kind: the record's simple name, the JS object's {@code kind}. */
    default String kind() { return getClass().getSimpleName(); }

    private static String requireId(String id, String what) {
        Objects.requireNonNull(id, what);
        if (id.isEmpty()) throw new IllegalArgumentException(what + ": must not be empty");
        return id;
    }

    /** A menu of this kind opened, its top-left at this point of the viewport. */
    record Opened(String menuKind, double x, double y) implements MenuEvent {
        public Opened {
            requireId(menuKind, "Opened.menuKind");
            if (!Double.isFinite(x) || !Double.isFinite(y)) throw new IllegalArgumentException("Opened.x/y: must be finite");
        }
    }

    /** An item of the open menu was picked. */
    record Picked(String menuKind, String itemId) implements MenuEvent {
        public Picked {
            requireId(menuKind, "Picked.menuKind");
            requireId(itemId, "Picked.itemId");
        }
    }

    /** The open menu closed, for one of the {@link #REASONS}. */
    record Closed(String menuKind, String reason) implements MenuEvent {
        public Closed {
            requireId(menuKind, "Closed.menuKind");
            if (!REASONS.contains(reason)) throw new IllegalArgumentException("Closed.reason: not one of " + REASONS + ": " + reason);
        }
    }
}
