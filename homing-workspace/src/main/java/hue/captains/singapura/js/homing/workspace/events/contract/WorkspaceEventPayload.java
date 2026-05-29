package hue.captains.singapura.js.homing.workspace.events.contract;

import hue.captains.singapura.js.homing.workspace.state.Orientation;
import hue.captains.singapura.js.homing.workspace.state.PaneId;
import hue.captains.singapura.js.homing.workspace.state.WidgetInstanceId;
import hue.captains.singapura.js.homing.workspace.state.WidgetKind;
import hue.captains.singapura.js.homing.workspace.state.WidgetTitle;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed sum of the recordable workspace events. Every event a chrome
 * emits is a typed instance of one of these variants; the sealed shape
 * gives consumers exhaustive {@code switch} and the compiler catches a
 * missed variant the moment it's added.
 *
 * <p>Each variant declares a {@link EventName} constant naming itself.
 * The {@link EventLog#append(EventName, Object) EventLog.append} call
 * passes both the name and the payload — and the typed binding is what
 * forbids the {@code _emit('WidgetSpawnedFormPicker', ...)} category of
 * typo at the contract boundary.</p>
 *
 * <p>Variants per the RFC 0035 spec table:</p>
 * <ul>
 *   <li>{@link SessionStarted} — boot bookmark</li>
 *   <li>{@link WidgetSpawnedFromPicker} — user spawned a widget via picker</li>
 *   <li>{@link WidgetSpawnedPinned} — PINNED auto-spawn</li>
 *   <li>{@link TabClosed} — widget tab closed</li>
 *   <li>{@link TabMoved} — tab relocated (strip-drag or modal redock)</li>
 *   <li>{@link SplitCreated} — a pane split</li>
 *   <li>{@link SplitMerged} — two panes merged back</li>
 *   <li>{@link WorkspaceActiveChanged} — workspace-active tab transition</li>
 * </ul>
 *
 * @since RFC 0035 P1
 */
public sealed interface WorkspaceEventPayload {

    /** The typed name of this payload — matches {@code event.name()}. */
    EventName name();

    // ─── Variants ────────────────────────────────────────────────────────

    /** Boot bookmark — first event of every session. */
    record SessionStarted(URI href, Instant startedAt) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("SessionStarted");
        public SessionStarted {
            Objects.requireNonNull(href,      "SessionStarted.href");
            Objects.requireNonNull(startedAt, "SessionStarted.startedAt");
        }
        @Override public EventName name() { return NAME; }
    }

    /** User picked a widget from the picker and it spawned. */
    record WidgetSpawnedFromPicker(
            WidgetInstanceId widgetInstanceId,
            WidgetKind       widgetKind,
            WidgetTitle      title,
            Location         to
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("WidgetSpawnedFromPicker");
        public WidgetSpawnedFromPicker {
            Objects.requireNonNull(widgetInstanceId, "WidgetSpawnedFromPicker.widgetInstanceId");
            Objects.requireNonNull(widgetKind,       "WidgetSpawnedFromPicker.widgetKind");
            Objects.requireNonNull(title,            "WidgetSpawnedFromPicker.title");
            Objects.requireNonNull(to,               "WidgetSpawnedFromPicker.to");
        }
        @Override public EventName name() { return NAME; }
    }

    /** Auto-spawn of a PINNED widget at chrome boot. */
    record WidgetSpawnedPinned(
            WidgetInstanceId widgetInstanceId,
            WidgetKind       widgetKind,
            Location         to
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("WidgetSpawnedPinned");
        public WidgetSpawnedPinned {
            Objects.requireNonNull(widgetInstanceId, "WidgetSpawnedPinned.widgetInstanceId");
            Objects.requireNonNull(widgetKind,       "WidgetSpawnedPinned.widgetKind");
            Objects.requireNonNull(to,               "WidgetSpawnedPinned.to");
        }
        @Override public EventName name() { return NAME; }
    }

    /** Widget tab closed by the user (× click). */
    record TabClosed(
            WidgetInstanceId widgetInstanceId,
            WidgetKind       widgetKind,
            Location         from
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("TabClosed");
        public TabClosed {
            Objects.requireNonNull(widgetInstanceId, "TabClosed.widgetInstanceId");
            Objects.requireNonNull(widgetKind,       "TabClosed.widgetKind");
            Objects.requireNonNull(from,             "TabClosed.from");
        }
        @Override public EventName name() { return NAME; }
    }

    /**
     * Tab relocated between (or within) panes. {@code from} is empty when
     * the tab re-docked from "outside" a pane (modal redock, picker-modal
     * dock); the replay handler resolves the live source by widgetInstanceId.
     */
    record TabMoved(
            WidgetInstanceId   widgetInstanceId,
            Optional<Location> from,
            Location           to
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("TabMoved");
        public TabMoved {
            Objects.requireNonNull(widgetInstanceId, "TabMoved.widgetInstanceId");
            if (from == null) from = Optional.empty();
            Objects.requireNonNull(to,               "TabMoved.to");
        }
        @Override public EventName name() { return NAME; }
    }

    /** A pane split into two. {@code paneId} is the parent's path post-split. */
    record SplitCreated(
            PaneId      paneId,
            Orientation orientation,
            double      newRatio
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("SplitCreated");
        public SplitCreated {
            Objects.requireNonNull(paneId,      "SplitCreated.paneId");
            Objects.requireNonNull(orientation, "SplitCreated.orientation");
            if (newRatio <= 0.0 || newRatio >= 1.0) {
                throw new IllegalArgumentException("SplitCreated.newRatio: must be in (0, 1), got " + newRatio);
            }
        }
        @Override public EventName name() { return NAME; }
    }

    /** Two child panes merged back into one. {@code paneId} is the surviving leaf. */
    record SplitMerged(PaneId paneId) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("SplitMerged");
        public SplitMerged {
            Objects.requireNonNull(paneId, "SplitMerged.paneId");
        }
        @Override public EventName name() { return NAME; }
    }

    /** Workspace-active tab transition. Either side may be absent (no active tab). */
    record WorkspaceActiveChanged(
            Optional<TabRef> from,
            Optional<TabRef> to
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("WorkspaceActiveChanged");
        public WorkspaceActiveChanged {
            if (from == null) from = Optional.empty();
            if (to   == null) to   = Optional.empty();
        }
        @Override public EventName name() { return NAME; }
    }
}
