package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.base.lang.StatelessSingleton;
import hue.captains.singapura.js.homing.component.EventFields;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;


/**
 * What the keyboard party says on its sink: one event per change of holder,
 * one per move of the candidate — the cursor the keyboard walk keeps
 * beside the holder — and one per move of the marker, where the focus is. The keys themselves are the browser's, passed through
 * to the holder, and are not events of ours. A sealed sum; the JS
 * {@code KeyboardEvents} class mirrors it, and a structural test holds the
 * two to one shape.
 */
public sealed interface KeyboardEvent {

    /** The kind: the record's simple name, the JS object's {@code kind}. */
    default String kind() { return getClass().getSimpleName(); }

    /** A member holds the keyboard now: by a claim, by a yield that reached it, by a member that left, or by the browser's focus arriving in it ({@code claim}, {@code yield}, {@code left}, {@code native}). */
    record Granted(String id, String by) implements KeyboardEvent {
        public Granted { EventFields.INSTANCE.id(id, "Granted.id"); EventFields.INSTANCE.id(by, "Granted.by"); }
        public Granted(String id) { this(id, "claim"); }
    }

    /** A member lost the keyboard: evicted by {@code by}, or, when {@code by} is null, released or gone. */
    record Taken(String id, String by) implements KeyboardEvent {
        public Taken { EventFields.INSTANCE.id(id, "Taken.id"); if (by != null) EventFields.INSTANCE.id(by, "Taken.by"); }
    }

    /** A member gave the keyboard up, or left the party while holding it. */
    record Released(String id) implements KeyboardEvent {
        public Released { EventFields.INSTANCE.id(id, "Released.id"); }
    }

    /** The keys are offered to a member: the walk's cursor rests on it, and a confirming key would give it the keys. Never the holder. */
    record Offered(String id) implements KeyboardEvent {
        public Offered { EventFields.INSTANCE.id(id, "Offered.id"); }
    }

    /** The offer is withdrawn: the walk moved on, was confirmed, was called off, or something else took the keys. */
    record Withdrawn(String id) implements KeyboardEvent {
        public Withdrawn { EventFields.INSTANCE.id(id, "Withdrawn.id"); }
    }

    /**
     * The marker moved (RFC 0066 E3, keyboard §17.5): the holder is {@code id}, and it is
     * {@code held} — nothing natively focused — {@code lent} — a native control of its own
     * has the browser's focus — or {@code away} — the browser's focus is outside every
     * member, and nothing is routed.
     */
    record Marked(String id, String state) implements KeyboardEvent {
        /** The states a mark says. */
        public record States() implements StatelessFunctionalObject, StatelessSingleton {
            public static final States INSTANCE = new States();

            /** Every state, as the JS side says it. */
            public java.util.List<String> all() { return java.util.List.of("held", "lent", "away"); }
        }

        public Marked {
            EventFields.INSTANCE.id(id, "Marked.id");
            var states = States.INSTANCE.all();
            if (!states.contains(state)) throw new IllegalArgumentException("Marked.state must be one of " + states + ": " + state);
        }
    }
}
