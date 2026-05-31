package hue.captains.singapura.js.homing.workspace.events.contract;

/**
 * Typed sequence number for one event in the log — strictly monotonic
 * within a single {@code (kind, workspaceId)} pair, assigned by the store
 * at append time.
 *
 * <p>Per the Names Are Types doctrine, no raw {@code long} crosses a
 * contract boundary as an event sequence. The wrapper carries the
 * non-negativity invariant; downstream code reads it with no further
 * validation needed.</p>
 *
 * @param value the underlying non-negative sequence number
 * @since RFC 0035 P1
 */
public record EventSeq(long value) {

    public EventSeq {
        if (value < 0) {
            throw new IllegalArgumentException("EventSeq.value: must be non-negative, got " + value);
        }
    }

    /** Seed sentinel — {@code lastEventSeq} of a freshly-cleared workspace. */
    public static final EventSeq ZERO = new EventSeq(0);

    public static EventSeq of(long value) { return new EventSeq(value); }

    public boolean greaterThan(EventSeq other) { return this.value > other.value; }

    @Override public String toString() { return "EventSeq(" + value + ")"; }
}
