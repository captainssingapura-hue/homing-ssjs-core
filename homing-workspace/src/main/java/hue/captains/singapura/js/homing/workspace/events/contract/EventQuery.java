package hue.captains.singapura.js.homing.workspace.events.contract;

import java.util.Optional;

/**
 * Range / limit options for {@link EventLog#query(EventQuery)}.
 *
 * <p>{@code fromSeqExclusive} returns events with {@code seq >
 * fromSeqExclusive.value}. Empty means "from the beginning."</p>
 *
 * <p>{@code limit} caps the result count. Empty means "no limit."</p>
 *
 * <p>The "ALL" sentinel — {@code new EventQuery(Optional.empty(),
 * Optional.empty())} — reads every event for the workspace. Useful for
 * full-history queries (e.g. boot-time replay before checkpoint pruning
 * lands).</p>
 *
 * @since RFC 0035 P1
 */
public record EventQuery(
        Optional<EventSeq> fromSeqExclusive,
        Optional<Integer>  limit
) {

    public EventQuery {
        if (fromSeqExclusive == null) fromSeqExclusive = Optional.empty();
        if (limit            == null) limit            = Optional.empty();
        limit.ifPresent(n -> {
            if (n < 0) throw new IllegalArgumentException("EventQuery.limit: must be non-negative, got " + n);
        });
    }

    public static final EventQuery ALL = new EventQuery(Optional.empty(), Optional.empty());

    public static EventQuery from(EventSeq fromSeqExclusive) {
        return new EventQuery(Optional.of(fromSeqExclusive), Optional.empty());
    }

    public static EventQuery fromWithLimit(EventSeq fromSeqExclusive, int limit) {
        return new EventQuery(Optional.of(fromSeqExclusive), Optional.of(limit));
    }
}
