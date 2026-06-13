// =============================================================================
// NavigatorSecretaryModule — the Secretary for the Studio Workspace's
// NavigatorParty (the navigation message bus).
//
// Per the Diligent Secretaries doctrine: a pure (state, envelope) -> Step
// behavior plus an initial state, exposed as members of one top-level object
// for clean framework import + isolated test-harness load. Side-effect-free;
// no DOM, no console, no captures.
//
// "Just redirects": an incoming NodeSelected (from the tree) is rebroadcast
// to every Party member as NavigateTo. The Secretary names no consumer — any
// widget that joins receives the redirect. Diligence: it remembers the last
// selection and bounds a recentUnknown ring for observability via inspect().
// =============================================================================

var NavigatorSecretary = {

    initial: {
        lastSelected:  null,
        recentUnknown: []
    },

    /**
     * Pure routing function. (state, envelope) -> { newState, actions }.
     * Same inputs always produce same outputs; no side effects.
     */
    behavior: function (state, envelope) {
        var msg = envelope.message;

        switch (msg.kind) {

            case "NodeSelected": {
                // Redirect: relay the tree's selection to all members.
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
                // Diligent defensive observability: track unknowns (bounded at
                // 10 via slice(-10)) so they surface via inspect() rather than
                // vanishing silently.
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
