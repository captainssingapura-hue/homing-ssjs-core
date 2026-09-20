package hue.captains.singapura.js.homing.ui.docking;

import java.util.Objects;

/**
 * The closed vocabulary of docking — the sealed sum the JS {@code DockEvents}
 * factories mirror, record for record, component for component. A tab
 * docked into a pane, or undocked from one to float; the desk's and the
 * pane's own events say the rest (Opened, Released, TabAttached) on the same
 * sink, and {@code DockEventsTest} holds the two sides together.
 */
public sealed interface DockEvent {

    /** The kind: the record's simple name, the JS object's {@code kind}. */
    default String kind() { return getClass().getSimpleName(); }

    private static String requireId(String id, String what) {
        Objects.requireNonNull(id, what);
        if (id.isEmpty()) throw new IllegalArgumentException(what + ": must not be empty");
        return id;
    }

    /** A floating tab was dropped on a dock and is its tab now, at this index. */
    record Docked(String tabId, String slotId, int index) implements DockEvent {
        public Docked {
            requireId(tabId, "Docked.tabId");
            requireId(slotId, "Docked.slotId");
            if (index < 0) throw new IllegalArgumentException("Docked.index: must be non-negative");
        }
    }

    /** A tab was pulled off a dock's strip and floats now, under the same hand. */
    record Undocked(String tabId, String slotId) implements DockEvent {
        public Undocked {
            requireId(tabId, "Undocked.tabId");
            requireId(slotId, "Undocked.slotId");
        }
    }
}
