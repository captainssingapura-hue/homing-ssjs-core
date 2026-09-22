package hue.captains.singapura.js.homing.component.keyboard;

import java.util.Objects;

/**
 * What the keyboard party says on its sink: one event per change of holder,
 * and one per move of the candidate — the cursor the keyboard walk keeps
 * beside the holder. The keys themselves are the browser's, passed through
 * to the holder, and are not events of ours. A sealed sum; the JS
 * {@code KeyboardEvents} class mirrors it, and a structural test holds the
 * two to one shape.
 */
public sealed interface KeyboardEvent {

    /** The kind: the record's simple name, the JS object's {@code kind}. */
    default String kind() { return getClass().getSimpleName(); }

    private static String requireId(String id, String what) {
        Objects.requireNonNull(id, what);
        if (id.isEmpty()) throw new IllegalArgumentException(what + ": must not be empty");
        return id;
    }

    /** A member holds the keyboard now: by a claim, by a yield that reached it, by a member that left, or by the steward's Tab ({@code claim}, {@code yield}, {@code left}, {@code tab}). */
    record Granted(String id, String by) implements KeyboardEvent {
        public Granted { requireId(id, "Granted.id"); requireId(by, "Granted.by"); }
        public Granted(String id) { this(id, "claim"); }
    }

    /** A member lost the keyboard: evicted by {@code by}, or, when {@code by} is null, released or gone. */
    record Taken(String id, String by) implements KeyboardEvent {
        public Taken { requireId(id, "Taken.id"); if (by != null) requireId(by, "Taken.by"); }
    }

    /** A member gave the keyboard up, or left the party while holding it. */
    record Released(String id) implements KeyboardEvent {
        public Released { requireId(id, "Released.id"); }
    }

    /** The keys are offered to a member: the walk's cursor rests on it, and a confirming key would give it the keys. Never the holder. */
    record Offered(String id) implements KeyboardEvent {
        public Offered { requireId(id, "Offered.id"); }
    }

    /** The offer is withdrawn: the walk moved on, was confirmed, was called off, or something else took the keys. */
    record Withdrawn(String id) implements KeyboardEvent {
        public Withdrawn { requireId(id, "Withdrawn.id"); }
    }
}
