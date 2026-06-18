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
// to every Party member as NavigateTo; an incoming NodeOpened (Enter /
// double-click — the intentional open) is rebroadcast as OpenDoc. Two tiers,
// same redirect shape: select is cheap + frequent (panes that re-label), open
// is expensive + deliberate (the content pane that fetches + renders). The
// Secretary names no consumer — any widget that joins receives the redirect.
// Diligence: it remembers the last selection AND the last open, and bounds a
// recentUnknown ring for observability via inspect().
// =============================================================================

var NavigatorSecretary = {

    initial: {
        lastSelected:  null,
        lastOpened:    null,
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
                // Redirect: relay the tree's selection to all members. Cheap,
                // high-frequency — fires on every arrow-move / single click.
                return {
                    newState: {
                        lastSelected:  msg.node,
                        lastOpened:    state.lastOpened,
                        recentUnknown: state.recentUnknown
                    },
                    actions: [{
                        kind:    "BroadcastToMembers",
                        message: { kind: "NavigateTo", node: msg.node }
                    }]
                };
            }

            case "NodeOpened": {
                // Redirect: relay an INTENTIONAL open (Enter / double-click) to
                // all members as OpenDoc. The content pane reacts to this — and
                // only this — so the expensive doc render is paid on intent.
                return {
                    newState: {
                        lastSelected:  state.lastSelected,
                        lastOpened:    msg.node,
                        recentUnknown: state.recentUnknown
                    },
                    actions: [{
                        kind:    "BroadcastToMembers",
                        message: { kind: "OpenDoc", node: msg.node }
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
                        lastOpened:    state.lastOpened,
                        recentUnknown: recent
                    },
                    actions: []
                };
            }
        }
    }
};
