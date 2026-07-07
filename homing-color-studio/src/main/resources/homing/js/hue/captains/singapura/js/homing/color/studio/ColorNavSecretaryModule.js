// =============================================================================
// ColorNavSecretaryModule — the Secretary for the Color Studio's colornav Party.
//
// A pure (state, envelope) -> Step redirect (Diligent Secretaries doctrine).
// "Just redirects": an incoming NodeSelected (from the ColorTreeWidget) is
// rebroadcast to every member as NavigateTo; the ColorListWidget reacts by
// listing the selected group's colours. No DOM, no console, no captures.
// =============================================================================

var ColorNavSecretary = {

    initial: {
        lastSelected:  null,
        recentUnknown: []
    },

    behavior: function (state, envelope) {
        var msg = envelope.message;

        switch (msg.kind) {

            case "NodeSelected": {
                return {
                    newState: {
                        lastSelected:  msg.node,
                        recentUnknown: state.recentUnknown
                    },
                    actions: [{
                        kind:    "BroadcastToMembers",
                        message: { kind: "NavigateTo", node: msg.node }
                    }]
                };
            }

            default: {
                var recent = state.recentUnknown.concat([{
                    kind: msg.kind,
                    from: envelope.from
                }]).slice(-10);
                return {
                    newState: {
                        lastSelected:  state.lastSelected,
                        recentUnknown: recent
                    },
                    actions: []
                };
            }
        }
    }
};
