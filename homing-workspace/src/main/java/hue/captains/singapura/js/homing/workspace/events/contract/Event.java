package hue.captains.singapura.js.homing.workspace.events.contract;

import hue.captains.singapura.js.homing.workspace.state.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceKind;

import java.time.Instant;
import java.util.Objects;

/**
 * One row in the event log. The payload type {@code P} is the sealed sum
 * for the domain — e.g. {@link WorkspaceEventPayload} for workspace events,
 * other domains can define their own.
 *
 * <p>Per-workspace scoping via the {@code (kind, workspaceId)} pair —
 * matches the storage shape from RFC 0031 multi-workspace at the contract
 * layer (pre-multi-instance code passes {@link
 * WorkspaceInstanceId#placeholderFor(WorkspaceKind)} so single-workspace
 * use today still types correctly).</p>
 *
 * @param seq         assigned by the store at append time; monotonic per (kind, workspaceId)
 * @param kind        which kind of workspace this event belongs to
 * @param workspaceId which instance of that kind
 * @param name        the typed event name (matches a variant of P)
 * @param payload     the typed payload data (one variant of the sealed sum)
 * @param timestamp   wall-clock at append time
 *
 * @since RFC 0035 P1
 */
public record Event<P>(
        EventSeq            seq,
        WorkspaceKind       kind,
        WorkspaceInstanceId workspaceId,
        EventName           name,
        P                   payload,
        Instant             timestamp
) {

    public Event {
        Objects.requireNonNull(seq,         "Event.seq");
        Objects.requireNonNull(kind,        "Event.kind");
        Objects.requireNonNull(workspaceId, "Event.workspaceId");
        Objects.requireNonNull(name,        "Event.name");
        Objects.requireNonNull(payload,     "Event.payload");
        Objects.requireNonNull(timestamp,   "Event.timestamp");
    }
}
