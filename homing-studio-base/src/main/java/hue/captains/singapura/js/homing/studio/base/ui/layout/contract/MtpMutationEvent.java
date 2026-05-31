package hue.captains.singapura.js.homing.studio.base.ui.layout.contract;

import java.util.Objects;
import java.util.Optional;

/**
 * Sealed sum of MultiTabPane's typed mutation events — the structured
 * counterpart to its eight typed callback options ({@code onTabAdded},
 * {@code onTabRemoved}, {@code onTabMoved}, {@code onTabActivated},
 * {@code onTabAttached}, {@code onWorkspaceActiveChanged}, {@code
 * onSplit}, {@code onMerge}).
 *
 * <p>Each variant carries the typed arguments of its corresponding
 * callback. Consumers that record MTP's mutations into an event log
 * (e.g. AnimalsPlaygroundChrome under RFC 0030) construct the variant
 * inside the typed callback, then forward to the event log.</p>
 *
 * <p>Per the Make It Impossible Not Forbidden doctrine, every closed
 * MTP-mutation vocabulary is a sealed sum: variants can carry their
 * own argument shapes, exhaustive {@code switch} is compiler-enforced,
 * adding a new mutation kind later is a structural change consumers
 * can't quietly drop.</p>
 *
 * @since RFC 0035 — MTP contract package
 */
public sealed interface MtpMutationEvent {

    /** Strip-chip click or programmatic {@code switchTab} — focused tab changed within a pane. */
    record TabActivated(SlotId slotId, TabId tabId) implements MtpMutationEvent {
        public TabActivated {
            Objects.requireNonNull(slotId, "TabActivated.slotId");
            Objects.requireNonNull(tabId,  "TabActivated.tabId");
        }
    }

    /** A new tab was added to a slot via {@code addTab(slotId, tab)}. */
    record TabAdded(SlotId slotId, TabDescriptor tab, int tabIndex) implements MtpMutationEvent {
        public TabAdded {
            Objects.requireNonNull(slotId, "TabAdded.slotId");
            Objects.requireNonNull(tab,    "TabAdded.tab");
            if (tabIndex < 0) throw new IllegalArgumentException("TabAdded.tabIndex: must be non-negative");
        }
    }

    /** A tab was removed via {@code removeTab(slotId, tabId)} (× click or programmatic close). */
    record TabRemoved(SlotId slotId, TabDescriptor tab, int fromIndex) implements MtpMutationEvent {
        public TabRemoved {
            Objects.requireNonNull(slotId, "TabRemoved.slotId");
            Objects.requireNonNull(tab,    "TabRemoved.tab");
            if (fromIndex < 0) throw new IllegalArgumentException("TabRemoved.fromIndex: must be non-negative");
        }
    }

    /** A tab was moved between (or within) panes via strip drag → {@code moveTab(...)}. */
    record TabMoved(
            SlotId        srcSlotId,
            TabDescriptor tab,
            int           srcIndex,
            SlotId        destSlotId,
            int           destIndex
    ) implements MtpMutationEvent {
        public TabMoved {
            Objects.requireNonNull(srcSlotId,  "TabMoved.srcSlotId");
            Objects.requireNonNull(tab,        "TabMoved.tab");
            Objects.requireNonNull(destSlotId, "TabMoved.destSlotId");
            if (srcIndex  < 0) throw new IllegalArgumentException("TabMoved.srcIndex: must be non-negative");
            if (destIndex < 0) throw new IllegalArgumentException("TabMoved.destIndex: must be non-negative");
        }
    }

    /**
     * A tab was attached to a pane from "outside" — modal redock, picker-modal
     * dock, programmatic re-parent. Source is not a pane (per RFC 0032 P4).
     */
    record TabAttached(SlotId slotId, TabDescriptor tab, int atIndex) implements MtpMutationEvent {
        public TabAttached {
            Objects.requireNonNull(slotId, "TabAttached.slotId");
            Objects.requireNonNull(tab,    "TabAttached.tab");
            if (atIndex < 0) throw new IllegalArgumentException("TabAttached.atIndex: must be non-negative");
        }
    }

    /** Workspace-active tab transitioned. Either side may be absent (no tab active). */
    record WorkspaceActiveChanged(Optional<TabId> fromTabId, Optional<TabId> toTabId)
            implements MtpMutationEvent {
        public WorkspaceActiveChanged {
            if (fromTabId == null) fromTabId = Optional.empty();
            if (toTabId   == null) toTabId   = Optional.empty();
        }
    }

    /**
     * A pane was split via {@code split(slotId, orientation)}.
     * V1 compromise: {@code orientation} is a {@code String}
     * ({@code "horizontal"} | {@code "vertical"}) rather than the typed
     * {@code Orientation} from {@code homing-workspace}; dependency
     * direction prevents the import. Follow-up to tighten when the
     * Orientation type moves into a shared module.
     */
    record Split(SlotId sourceSlotId, String orientation, SlotId newSlotId)
            implements MtpMutationEvent {
        public Split {
            Objects.requireNonNull(sourceSlotId, "Split.sourceSlotId");
            Objects.requireNonNull(orientation,  "Split.orientation");
            Objects.requireNonNull(newSlotId,    "Split.newSlotId");
            if (!orientation.equals("horizontal") && !orientation.equals("vertical")) {
                throw new IllegalArgumentException(
                        "Split.orientation: must be 'horizontal' or 'vertical', got '" + orientation + "'");
            }
        }
    }

    /** Two child panes merged back into one via {@code merge(slotId)}. */
    record Merge(SlotId survivingSlotId, SlotId removedSlotId) implements MtpMutationEvent {
        public Merge {
            Objects.requireNonNull(survivingSlotId, "Merge.survivingSlotId");
            Objects.requireNonNull(removedSlotId,   "Merge.removedSlotId");
        }
    }
}
