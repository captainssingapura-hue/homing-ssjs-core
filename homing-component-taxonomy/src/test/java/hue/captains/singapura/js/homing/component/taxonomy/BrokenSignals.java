package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;
import java.util.Map;

/** Types of state that break the tree: one value only, a value twice, and a name the {@link Signals} already use. */
final class BrokenSignals {

    private BrokenSignals() {}

    record Single() implements StateType<StateRoot>, Importance.Senary {
        static final Single INSTANCE = new Single();
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
        @Override public List<StateValue> values() { return List.of(StateValue.TRUE); }
    }

    record Twice() implements StateType<StateRoot>, Importance.Senary {
        static final Twice INSTANCE = new Twice();
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
        @Override public List<StateValue> values() { return List.of(StateValue.TRUE, StateValue.FALSE, StateValue.TRUE); }
    }

    /** Its off answered by the word the signals' Enabled already has. */
    record Off() implements Flag<StateRoot>, Importance.Senary {
        static final Off INSTANCE = new Off();
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
        @Override public boolean rest() { return true; }
        @Override public Map<StateValue, String> words() { return Map.of(StateValue.FALSE, "disabled"); }
    }

    /** A design word for its rest, which a design answers as the look. */
    record Quiet() implements Flag<StateRoot>, Importance.Senary {
        static final Quiet INSTANCE = new Quiet();
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
        @Override public Map<StateValue, String> words() { return Map.of(StateValue.FALSE, "quiet"); }
    }

    /** Named as the signals' glow is. */
    record Glow() implements Flag<StateRoot>, Importance.Senary {
        static final Glow INSTANCE = new Glow();
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
    }
}
