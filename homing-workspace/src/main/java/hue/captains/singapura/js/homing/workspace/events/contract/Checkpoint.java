package hue.captains.singapura.js.homing.workspace.events.contract;

import java.time.Instant;
import java.util.Objects;

/**
 * One stored snapshot envelope. {@code S} is the workspace-specific state
 * type — typically {@link
 * hue.captains.singapura.js.homing.workspace.state.WorkspaceState} for
 * RFC 0029-shaped chromes.
 *
 * <p>The {@code lastEventSeq} field is the watermark: every event with
 * {@code seq <= lastEventSeq} is folded into {@code state}; boot's tail
 * replay starts from {@code seq > lastEventSeq}.</p>
 *
 * <p>The {@code schemaVersion} field gates compatibility. Boot readers
 * compare against {@link CheckpointStore#schemaVersion()} and treat
 * mismatches as "no checkpoint" — falls back to full event-log replay.</p>
 *
 * @param state         the aggregated workspace state at capture time
 * @param lastEventSeq  events with seq <= this are folded into state
 * @param capturedAt    wall-clock when the snapshot was taken
 * @param schemaVersion envelope schema; bumped when the on-disk shape changes
 *
 * @since RFC 0035 P1
 */
public record Checkpoint<S>(
        S        state,
        EventSeq lastEventSeq,
        Instant  capturedAt,
        int      schemaVersion
) {

    public Checkpoint {
        Objects.requireNonNull(state,        "Checkpoint.state");
        Objects.requireNonNull(lastEventSeq, "Checkpoint.lastEventSeq");
        Objects.requireNonNull(capturedAt,   "Checkpoint.capturedAt");
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("Checkpoint.schemaVersion: must be positive, got " + schemaVersion);
        }
    }
}
