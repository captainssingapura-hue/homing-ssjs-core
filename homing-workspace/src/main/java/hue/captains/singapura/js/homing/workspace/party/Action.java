package hue.captains.singapura.js.homing.workspace.party;

import java.util.Objects;

/**
 * What a {@link Secretary}'s behaviour returns as the "what should happen
 * next" intent. The runtime executes Actions; Secretaries never perform
 * side effects directly. Sealed for exhaustive pattern-matching at the
 * dispatch site.
 *
 * <p><b>Three variants — three routing primitives:</b></p>
 *
 * <ul>
 *   <li>{@link SendToParent} — bubble up to this Secretary's parent
 *       Secretary. Used when the message is for a wider audience than
 *       this Secretary's own members.</li>
 *   <li>{@link BroadcastToMembers} — fan out to every direct member
 *       (Actors and sub-Secretaries) of this Secretary. The norm:
 *       most facts propagate via broadcast.</li>
 *   <li>{@link SendToMember} — targeted send, by identity. The
 *       exception case: response to a recorded request, addressed to
 *       the original Actor.</li>
 * </ul>
 *
 * <p><b>What is deliberately absent:</b> there is no {@code LocalEffect}
 * variant. Secretaries cannot perform UI side effects — if a state
 * change implies a side effect (e.g. fullscreen toggle), the Secretary
 * emits a {@code BroadcastToMembers} carrying a fact message, and an
 * Actor reacts to the fact and performs the DOM mutation. This keeps
 * the Secretary/Actor split (pure router / concrete UI doer) airtight.</p>
 *
 * @param <M> the Party's scoped message ADT type
 * @since RFC 0028 cycle 1
 */
public sealed interface Action<M>
        permits Action.SendToParent, Action.BroadcastToMembers, Action.SendToMember {

    /** Bubble {@link #message()} up to the parent Secretary. */
    record SendToParent<M>(M message) implements Action<M> {
        public SendToParent {
            Objects.requireNonNull(message, "Action.SendToParent.message");
        }
    }

    /** Fan {@link #message()} out to every direct member of this Secretary. */
    record BroadcastToMembers<M>(M message) implements Action<M> {
        public BroadcastToMembers {
            Objects.requireNonNull(message, "Action.BroadcastToMembers.message");
        }
    }

    /**
     * Send {@link #message()} to one specific member by {@link #to()} identity.
     * Used for request-response: the Secretary records the requester in its
     * state when the request arrives, then addresses the response back to
     * the recorded {@code AgentId}.
     */
    record SendToMember<M>(AgentId to, M message) implements Action<M> {
        public SendToMember {
            Objects.requireNonNull(to,      "Action.SendToMember.to");
            Objects.requireNonNull(message, "Action.SendToMember.message");
        }
    }
}
