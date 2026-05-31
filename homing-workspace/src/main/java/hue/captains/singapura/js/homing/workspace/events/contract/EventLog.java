package hue.captains.singapura.js.homing.workspace.events.contract;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Append-only typed event log, bound to one {@code (kind, workspaceId)}
 * pair. The payload type {@code P} is the sealed sum for the domain.
 *
 * <p>Substrate-free contract — implementations exist per substrate:</p>
 * <ul>
 *   <li>JS: IndexedDB-backed (today's {@code EventLogStore.js}, refactored
 *       under RFC 0035 P2).</li>
 *   <li>Java: JDBC / file / in-memory (test fixtures).</li>
 *   <li>C: flat-file with append fsync (hypothetical performance kernel).</li>
 * </ul>
 *
 * <p>Per the Diligent Primitives doctrine, the implementation publishes
 * its mutations and never lets consumers reach past the public method
 * set. The three methods below ARE the public method set.</p>
 *
 * @param <P> sealed payload type for this domain (e.g. {@link WorkspaceEventPayload})
 *
 * @since RFC 0035 P1
 */
public interface EventLog<P> {

    /**
     * Append one event to the log. Returns the assigned sequence number.
     *
     * <p>Fire-and-forget is the expected call style on the recording path;
     * the future is there for tests + diagnostics that need the seq back.
     * Implementations must not throw out of {@code append} synchronously
     * for storage-layer reasons — failures land in the returned future.</p>
     */
    CompletableFuture<EventSeq> append(EventName name, P payload);

    /** Range-query events for this workspace, sorted by seq ascending. */
    CompletableFuture<List<Event<P>>> query(EventQuery query);

    /**
     * Drop every event for this workspace. Used by Reset State and
     * pruning (post-checkpoint). Resolves to the count deleted.
     */
    CompletableFuture<Long> clear();
}
