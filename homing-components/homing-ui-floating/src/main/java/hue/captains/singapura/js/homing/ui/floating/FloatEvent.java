package hue.captains.singapura.js.homing.ui.floating;

import java.util.Objects;

/**
 * The closed vocabulary of a desk's mutations — the sealed sum the JS
 * {@code FloatEvents} factories mirror, record for record, component for
 * component. The desk reports each mutation as one frozen object tagged by
 * {@code kind}, the record's simple name, with the record's components as
 * its fields; {@code FloatEventsTest} holds the two together, so a kind or
 * a field cannot be added, dropped or reordered on one side alone.
 *
 * <p>Five kinds: a pane opened, moved, resized, raised and closed. Positions
 * and measures are whole pixels within the desk; a move or a resize is
 * reported once, when the hand lets go or the method returns, never per
 * pixel of a drag.</p>
 */
public sealed interface FloatEvent {

    /** The kind: the record's simple name, the JS object's {@code kind}. */
    default String kind() { return getClass().getSimpleName(); }

    private static String requireId(String id, String what) {
        Objects.requireNonNull(id, what);
        if (id.isEmpty()) throw new IllegalArgumentException(what + ": must not be empty");
        return id;
    }
    private static int positive(int v, String what) {
        if (v <= 0) throw new IllegalArgumentException(what + ": must be positive");
        return v;
    }

    /** A pane was opened on the desk, at this place and measure, and is the active one. */
    record Opened(String id, String title, int x, int y, int w, int h) implements FloatEvent {
        public Opened {
            requireId(id, "Opened.id");
            Objects.requireNonNull(title, "Opened.title");
            positive(w, "Opened.w");
            positive(h, "Opened.h");
        }
    }

    /** A pane came to rest somewhere else — the hand let go, or {@code moveTo} returned. */
    record Moved(String id, int x, int y) implements FloatEvent {
        public Moved { requireId(id, "Moved.id"); }
    }

    /** A pane has another measure — the grip let go, or {@code resizeTo} returned. */
    record Resized(String id, int w, int h) implements FloatEvent {
        public Resized {
            requireId(id, "Resized.id");
            positive(w, "Resized.w");
            positive(h, "Resized.h");
        }
    }

    /** A pane became the active one, on top of the stack — a press on it, focus into it, or {@code raise}. */
    record Raised(String id) implements FloatEvent {
        public Raised { requireId(id, "Raised.id"); }
    }

    /** A pane was closed — the cross, Escape, or {@code close}; its widget is already disposed. */
    record Closed(String id) implements FloatEvent {
        public Closed { requireId(id, "Closed.id"); }
    }
}
