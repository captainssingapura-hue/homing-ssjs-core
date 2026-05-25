// =============================================================================
// LayoutSecretaryModule — the Secretary for the framework's LayoutParty.
//
// Per the Diligent Secretaries doctrine: pure (state, envelope) -> Step
// behavior plus initial state, both exposed as members of a single
// top-level object for clean framework import + isolated test harness load.
//
// Diligence enrichment in state:
//   - fullscreen     : current value
//   - lastChangedBy  : provenance of the most recent state change (envelope.from)
//   - recentUnknown  : last 10 unrecognised messages (kind + origin), bounded
//
// Side-effect-free; no DOM, no console, no captures. Per the
// Lambdas-Are-Functional-Object-Shortcuts doctrine, this entire module
// is a Functional Object simplification — pure behavior + immutable state shape.
// =============================================================================

var LayoutSecretary = {

    initial: {
        fullscreen   : false,
        lastChangedBy: null,
        recentUnknown: []
    },

    /**
     * Pure routing function. (state, envelope) -> { newState, actions }.
     * Same inputs always produce same outputs; no side effects.
     */
    behavior: function (state, envelope) {
        var msg = envelope.message;

        switch (msg.kind) {

            case "FullscreenToggleRequested": {
                var next = !state.fullscreen;
                return {
                    newState: {
                        fullscreen   : next,
                        lastChangedBy: envelope.from,
                        recentUnknown: state.recentUnknown
                    },
                    actions: [{
                        kind   : "BroadcastToMembers",
                        message: { kind: "FullscreenChanged", on: next }
                    }]
                };
            }

            case "FullscreenSetRequested": {
                // No-op when already at the requested value; preserves state identity
                // so dispatch is exactly as expensive as the no-op branch.
                if (!!msg.on === state.fullscreen) {
                    return { newState: state, actions: [] };
                }
                return {
                    newState: {
                        fullscreen   : !!msg.on,
                        lastChangedBy: envelope.from,
                        recentUnknown: state.recentUnknown
                    },
                    actions: [{
                        kind   : "BroadcastToMembers",
                        message: { kind: "FullscreenChanged", on: !!msg.on }
                    }]
                };
            }

            default: {
                // Diligent defensive observability: track unknowns in state so
                // they surface via inspect() rather than silently disappearing.
                // Bounded at 10 entries via .slice(-10) so state cannot grow.
                var recent = state.recentUnknown.concat([{
                    kind: msg.kind,
                    from: envelope.from
                }]).slice(-10);
                return {
                    newState: {
                        fullscreen   : state.fullscreen,
                        lastChangedBy: state.lastChangedBy,
                        recentUnknown: recent
                    },
                    actions: []
                };
            }
        }
    }
};
