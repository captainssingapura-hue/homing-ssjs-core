package hue.captains.singapura.js.homing.workspace.shell;

import java.util.Objects;

/**
 * Typed dispatch handler for a ribbon/footer {@code actionId}.
 * Workspaces declare a {@code Map<String, ActionDispatch>} on their
 * spec; the substrate's {@code onAction(actionId, value)} callback in
 * the workspace layout looks up the entry and interprets the typed
 * value without per-workspace JS.
 *
 * <p>Sealed for exhaustive interpretation in the substrate JS.</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition
 */
public sealed interface ActionDispatch
        permits ActionDispatch.TellParty, ActionDispatch.LogOnly {

    /**
     * Dispatch the action by telling a Party. The substrate calls
     * {@code party[partyName].tellFrom(actorId, {kind: messageKind,
     * [valueKey]: value})}; {@code value} is the action's payload as
     * supplied by the layout's {@code onAction}.
     *
     * @param partyName    the {@link PartyDecl#name() PartyDecl.name}
     *                     of a constructed Party
     * @param actorId      the sender Actor's id (typically a ribbon
     *                     control's path)
     * @param messageKind  the Party message kind to send
     * @param valueKey     key on the message payload object that
     *                     receives the action's {@code value} (e.g.
     *                     {@code "animal"} → {@code {animal: value}})
     */
    record TellParty(String partyName, String actorId,
                     String messageKind, String valueKey) implements ActionDispatch {
        public TellParty {
            Objects.requireNonNull(partyName,   "TellParty.partyName");
            Objects.requireNonNull(actorId,     "TellParty.actorId");
            Objects.requireNonNull(messageKind, "TellParty.messageKind");
            Objects.requireNonNull(valueKey,    "TellParty.valueKey");
        }
    }

    /**
     * No-op dispatch — the substrate logs the action and value.
     * Useful for actions a workspace knows about but hasn't wired up
     * yet, or for footer-only actions whose handling lives elsewhere.
     */
    record LogOnly() implements ActionDispatch {
        public static final LogOnly INSTANCE = new LogOnly();
    }

    /** Convenience factory for the common {@link TellParty} shape. */
    static TellParty tellParty(String partyName, String actorId,
                               String messageKind, String valueKey) {
        return new TellParty(partyName, actorId, messageKind, valueKey);
    }
}
