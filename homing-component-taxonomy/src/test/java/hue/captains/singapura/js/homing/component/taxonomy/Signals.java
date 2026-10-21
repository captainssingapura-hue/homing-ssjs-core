package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;
import java.util.Map;

/**
 * A hypothetical tree of states and of events for the {@link Sketch} house: a branch of states with a
 * type that is yes or no and one with three values, a type filed under the root, a branch of events,
 * and a lamp that has the states and offers the event.
 */
final class Signals {

    private Signals() {}

    record Availability() implements L1_StateBranch<StateRoot> {
        static final Availability INSTANCE = new Availability();
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
    }

    /** True at rest; off, it has an established design word. */
    record Enabled() implements Flag<Availability>, Importance.Quaternary {
        static final Enabled INSTANCE = new Enabled();
        @Override public Availability parent() { return Availability.INSTANCE; }
        @Override public boolean rest() { return true; }
        @Override public Map<StateValue, String> words() { return Map.of(StateValue.FALSE, "disabled"); }
    }

    /** Three values, off at rest; no design word established, so its values' are its name and theirs. */
    record Glow() implements Classed<Availability>, Importance.Tertiary {
        static final Glow INSTANCE = new Glow();
        @Override public Availability parent() { return Availability.INSTANCE; }
        @Override public List<StateValue> values() { return List.of(new StateValue("off"), new StateValue("dim"), new StateValue("bright")); }
    }

    /** Filed under the root; no component has it. */
    record Lit() implements Flag<StateRoot>, Importance.Senary {
        static final Lit INSTANCE = new Lit();
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
    }

    record Pointing() implements L1_EventBranch<EventRoot> {
        static final Pointing INSTANCE = new Pointing();
        @Override public EventRoot parent() { return EventRoot.INSTANCE; }
    }

    record Tap() implements EventType<Pointing> {
        static final Tap INSTANCE = new Tap();
        @Override public Pointing parent() { return Pointing.INSTANCE; }
    }

    /** A control that has the states and offers the event. */
    record Lamp() implements Component<Sketch.Control> {
        static final Lamp INSTANCE = new Lamp();
        @Override public Sketch.Control parent() { return Sketch.Control.INSTANCE; }
        @Override public List<StateType<?>> states() { return List.of(Enabled.INSTANCE, Glow.INSTANCE); }
        @Override public List<EventType<?>> events() { return List.of(Tap.INSTANCE); }
    }
}
