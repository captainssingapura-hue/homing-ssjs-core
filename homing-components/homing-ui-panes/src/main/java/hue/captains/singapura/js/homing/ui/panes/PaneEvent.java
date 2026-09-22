package hue.captains.singapura.js.homing.ui.panes;

import java.util.Objects;

/**
 * The closed vocabulary of a pane's mutations — the sealed sum the JS
 * {@code PaneEvents} factories mirror, record for record, component for
 * component. A pane reports each mutation as one frozen object tagged by
 * {@code kind}, the record's simple name, with the record's components as
 * its fields; {@code PaneEventsTest} holds the two together, so a kind or
 * a field cannot be added, dropped or reordered on one side alone.
 *
 * <p>Six kinds, from the studio pane's vocabulary: a tab added, removed,
 * moved, activated, attached, and an add requested. A move carries both
 * ends; within one pane they are the same slot. {@link Tab} is the
 * descriptor as the holder gave it — id, title, pinned — without the
 * widget, which is the JS side's and does not travel.</p>
 */
public sealed interface PaneEvent {

    /** The kind: the record's simple name, the JS object's {@code kind}. */
    default String kind() { return getClass().getSimpleName(); }

    /** The tab as reported: what the holder gave the pane, less the widget. */
    record Tab(String id, String title, boolean pinned) {
        public Tab {
            Objects.requireNonNull(id, "Tab.id");
            if (id.isEmpty()) throw new IllegalArgumentException("Tab.id: must not be empty");
        }
    }

    /** A tab was added at the end of its block via {@code addTab}. */
    record TabAdded(String slotId, Tab tab, int index) implements PaneEvent {
        public TabAdded {
            Objects.requireNonNull(slotId, "TabAdded.slotId");
            Objects.requireNonNull(tab, "TabAdded.tab");
            if (index < 0) throw new IllegalArgumentException("TabAdded.index: must be non-negative");
        }
    }

    /** A tab was closed via {@code removeTab}; its widget is already disposed. */
    record TabRemoved(String slotId, Tab tab, int fromIndex) implements PaneEvent {
        public TabRemoved {
            Objects.requireNonNull(slotId, "TabRemoved.slotId");
            Objects.requireNonNull(tab, "TabRemoved.tab");
            if (fromIndex < 0) throw new IllegalArgumentException("TabRemoved.fromIndex: must be non-negative");
        }
    }

    /** A tab moved, within a pane or between two; {@code destIndex} is where it ended up. */
    record TabMoved(String srcSlotId, Tab tab, int srcIndex, String destSlotId, int destIndex) implements PaneEvent {
        public TabMoved {
            Objects.requireNonNull(srcSlotId, "TabMoved.srcSlotId");
            Objects.requireNonNull(tab, "TabMoved.tab");
            Objects.requireNonNull(destSlotId, "TabMoved.destSlotId");
            if (srcIndex < 0) throw new IllegalArgumentException("TabMoved.srcIndex: must be non-negative");
            if (destIndex < 0) throw new IllegalArgumentException("TabMoved.destIndex: must be non-negative");
        }
    }

    /** The active tab changed — a chip, a key, or {@code switchTab}. */
    record TabActivated(String slotId, String tabId) implements PaneEvent {
        public TabActivated {
            Objects.requireNonNull(slotId, "TabActivated.slotId");
            Objects.requireNonNull(tabId, "TabActivated.tabId");
        }
    }

    /** A tab was attached from outside via {@code attachTab} — a re-dock, a programmatic re-parent. */
    record TabAttached(String slotId, Tab tab, int atIndex) implements PaneEvent {
        public TabAttached {
            Objects.requireNonNull(slotId, "TabAttached.slotId");
            Objects.requireNonNull(tab, "TabAttached.tab");
            if (atIndex < 0) throw new IllegalArgumentException("TabAttached.atIndex: must be non-negative");
        }
    }

    /** The add button was pressed while a tab could be added; the holder decides what that means. */
    record AddRequested(String slotId) implements PaneEvent {
        public AddRequested {
            Objects.requireNonNull(slotId, "AddRequested.slotId");
        }
    }

    /** Shift+Down on the pane while it holds the keys: the active tab asked to detach and float; the holder that has a desk does it. */
    record DetachRequested(String slotId, String tabId) implements PaneEvent {
        public DetachRequested {
            Objects.requireNonNull(slotId, "DetachRequested.slotId");
            Objects.requireNonNull(tabId, "DetachRequested.tabId");
        }
    }
}
