package hue.captains.singapura.js.homing.workspace.party;

import java.util.List;
import java.util.Objects;

/**
 * The return value of a {@link Secretary}'s behaviour — a (new state,
 * actions to execute) pair. The runtime commits {@link #newState()} to
 * the Secretary, then executes {@link #actions()} in declared order
 * depth-first.
 *
 * <p>Pure: {@code Step} construction does not perform side effects;
 * actions are descriptions, not invocations. The runtime owns
 * execution.</p>
 *
 * @param <S> the Secretary's state type
 * @param <M> the Party's scoped message ADT type
 * @param newState the post-handler state; must be non-null
 * @param actions the actions to execute, in order; immutable copy
 * @since RFC 0028 cycle 1
 */
public record Step<S, M>(S newState, List<Action<M>> actions) {

    public Step {
        Objects.requireNonNull(newState, "Step.newState");
        actions = actions == null ? List.of() : List.copyOf(actions);
    }

    /** No actions; state advances (or stays) silently. */
    public static <S, M> Step<S, M> stay(S state) {
        return new Step<>(state, List.of());
    }

    /** Convenience: one action plus the new state. */
    public static <S, M> Step<S, M> of(S state, Action<M> action) {
        return new Step<>(state, List.of(action));
    }

    /** Convenience: many actions plus the new state. */
    @SafeVarargs
    public static <S, M> Step<S, M> of(S state, Action<M>... actions) {
        return new Step<>(state, List.of(actions));
    }
}
