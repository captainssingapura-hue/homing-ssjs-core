package hue.captains.singapura.js.homing.workspace.events.contract;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed sum of reasons a {@link Checkpointer} would write. Each variant
 * carries the diagnostic context that justifies the write, so logs and
 * conformance tests can distinguish them without overloading a string
 * tag.
 *
 * <p>Replaces a degenerate enum. The same "every closed vocabulary is a
 * sealed sum" rule applied to {@link WorkspaceEventPayload} applies here:
 * variants can carry their own data, exhaustive {@code switch} is
 * compiler-enforced, and adding a new trigger reason later is a structural
 * change consumers can't quietly drop.</p>
 *
 * @since RFC 0035 P1
 */
public sealed interface CheckpointTrigger {

    /** Cadence timer fired. Carries the tick time for diagnostic ordering. */
    record Timer(Instant scheduledAt) implements CheckpointTrigger {
        public Timer { Objects.requireNonNull(scheduledAt, "Timer.scheduledAt"); }
    }

    /** Event-count threshold crossed. Carries observed + threshold for cadence audits. */
    record EventCount(int observed, int threshold) implements CheckpointTrigger {
        public EventCount {
            if (observed < 0)  throw new IllegalArgumentException("EventCount.observed: must be non-negative, got " + observed);
            if (threshold < 1) throw new IllegalArgumentException("EventCount.threshold: must be positive, got " + threshold);
        }
    }

    /** {@code pagehide}/{@code beforeunload} best-effort flush. */
    record Unload() implements CheckpointTrigger {}

    /** DevTools / programmatic force-write. {@code note} is an optional human-readable reason. */
    record Manual(Optional<String> note) implements CheckpointTrigger {
        public Manual { if (note == null) note = Optional.empty(); }
        public static Manual withoutNote()        { return new Manual(Optional.empty()); }
        public static Manual withNote(String n)   { return new Manual(Optional.ofNullable(n)); }
    }
}
