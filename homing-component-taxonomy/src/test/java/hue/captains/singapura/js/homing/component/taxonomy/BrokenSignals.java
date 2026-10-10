package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/** Types of state that break the tree: one value only, a value twice, and a name the {@link Signals} already use. */
final class BrokenSignals {

    private BrokenSignals() {}

    record Single() implements StateType<StateRoot> {
        static final Single INSTANCE = new Single();
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
        @Override public List<StateValue> values() { return List.of(StateValue.TRUE); }
    }

    record Twice() implements StateType<StateRoot> {
        static final Twice INSTANCE = new Twice();
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
        @Override public List<StateValue> values() { return List.of(StateValue.TRUE, StateValue.FALSE, StateValue.TRUE); }
    }

    /** Named as the signals' glow is. */
    record Glow() implements Flag<StateRoot> {
        static final Glow INSTANCE = new Glow();
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
    }
}
