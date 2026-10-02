// =============================================================================
// KeyboardSecretaryModule — the Secretary of the keyboard party: one field.
//
// Exactly one member holds the keyboard, or none. The behaviour is pure,
// (state, envelope) -> { newState, actions }, and blind: it is told that a
// member claims or releases, never why or how; a claim is a fact, and it
// evicts whoever held — who is told. Keys go to the holder and nowhere else.
//
//   state              { holder: id | null }
//   Claim   { id?, by? } holder := id (the sender, unless said); Taken to the old
//                      holder, Granted to the new, carrying `by` — claim unless
//                      said: yield, left — for the new holder's ear; the secretary
//                      passes it through and reads nothing into it. A claim by the
//                      holder is nothing
//   Release { id? }    holder := null, if it was id; Taken to it
//   Left    { id }     the same as Release: a member that left cannot hold
//   KeyDown { ev }     to the holder, or nowhere
//   KeyUp   { ev }     to the holder, or nowhere
//
// Anything else is dropped. No reasons, no priorities, no refusals, no
// holds, no chain: whoever claims last holds. The steward is the party's
// face to the DOM; the convention by which components claim is theirs.
// =============================================================================

var KeyboardSecretary = {

    initial: { holder: null },

    /** Pure. Same inputs, same step; no DOM, no clock, no console. */
    behavior: function (state, envelope) {
        var msg = envelope.message || {};
        var who = msg.id != null ? String(msg.id) : envelope.from;
        switch (msg.kind) {

            case "Claim": {
                if (!who || who === state.holder) return { newState: state, actions: [] };
                var actions = [];
                if (state.holder != null) actions.push({ kind: "SendToMember", to: state.holder, message: { kind: "Taken", by: who } });
                actions.push({ kind: "SendToMember", to: who, message: { kind: "Granted", by: msg.by == null ? "claim" : String(msg.by) } });
                return { newState: { holder: who }, actions: actions };
            }

            case "Release":
            case "Left": {
                if (!who || who !== state.holder) return { newState: state, actions: [] };
                return { newState: { holder: null }, actions: [{ kind: "SendToMember", to: who, message: { kind: "Taken", by: null } }] };
            }

            case "KeyDown":
            case "KeyUp": {
                if (state.holder == null) return { newState: state, actions: [] };
                return { newState: state, actions: [{ kind: "SendToMember", to: state.holder, message: { kind: msg.kind, ev: msg.ev } }] };
            }

            default:
                return { newState: state, actions: [] };
        }
    }
};
