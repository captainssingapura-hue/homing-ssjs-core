package hue.captains.singapura.js.homing.workspace.party;

import java.util.Objects;

/**
 * A message in flight. The envelope carries the {@link #message()} payload
 * (of the Party's scoped ADT type {@code M}) plus the {@link #from()}
 * identity — the sending Actor's or Secretary's path within the Party's
 * tree.
 *
 * <p>The {@code from} field replaces an explicit direction tag. A
 * Secretary's behaviour can derive the message's direction by comparing
 * {@code from} to its own {@link AgentId} — see
 * {@link AgentId#isStrictAncestorOf(AgentId)} for the FromMember vs
 * FromParent decision.</p>
 *
 * @param <M> the Party's scoped message ADT type
 * @param from sender's identity, must be non-null
 * @param message the payload, must be non-null
 * @since RFC 0028 cycle 1
 */
public record Envelope<M>(AgentId from, M message) {

    public Envelope {
        Objects.requireNonNull(from,    "Envelope.from");
        Objects.requireNonNull(message, "Envelope.message");
    }
}
