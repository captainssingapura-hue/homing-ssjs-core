package hue.captains.singapura.js.homing.component.keyboard;

import java.util.Objects;

/**
 * What the keyboard party says on its sink: one event per change of holder.
 * The keys themselves are the browser's, passed through to the holder, and
 * are not events of ours. A sealed sum; the JS {@code KeyboardEvents} class
 * mirrors it, and a structural test holds the two to one shape.
 */
public sealed interface KeyboardEvent {

    /** The kind: the record's simple name, the JS object's {@code kind}. */
    default String kind() { return getClass().getSimpleName(); }

    private static String requireId(String id, String what) {
        Objects.requireNonNull(id, what);
        if (id.isEmpty()) throw new IllegalArgumentException(what + ": must not be empty");
        return id;
    }

    /** A member holds the keyboard now. */
    record Granted(String id) implements KeyboardEvent {
        public Granted { requireId(id, "Granted.id"); }
    }

    /** A member lost the keyboard: evicted by {@code by}, or, when {@code by} is null, released or gone. */
    record Taken(String id, String by) implements KeyboardEvent {
        public Taken { requireId(id, "Taken.id"); if (by != null) requireId(by, "Taken.by"); }
    }

    /** A member gave the keyboard up, or left the party while holding it. */
    record Released(String id) implements KeyboardEvent {
        public Released { requireId(id, "Released.id"); }
    }
}
