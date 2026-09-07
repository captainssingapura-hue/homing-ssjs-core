// =============================================================================
// TocSyncSecretaryModule — the two-way TOC↔body sync law, as a Secretary.
//
// Lifted out of DocTreeRenderer (RFC 0043) unchanged in behaviour, so a second
// reader can OBEY the law rather than reinvent it. The two doc readers draw
// different tables of contents — a TreeRenderer over structure nodes, and a flat
// list of heading anchors — but the sync between a TOC and the body it points
// into is the same problem in both, and it has exactly one bug risk:
//
//     a TOC row is chosen → the body scrolls → the scroll fires → a scroll-spy
//     re-selects → the body scrolls again.
//
// Dissolved structurally rather than by timing: ONE authority (`currentKey`),
// TWO writers (a TOC Actor via NavRequested, a scroll-spy Actor via ScrolledTo),
// and an ASYMMETRY — navigation SCROLLS, the spy only HIGHLIGHTS, never scrolls.
// The one residual, flicker through intermediate sections DURING a programmatic
// scroll, is absorbed by the `programmaticScroll` field, which ScrollSettled
// lifts when the host's scrollend (or its fallback timer) says the ride is over.
//
//   TocSyncSecretary.initial
//   TocSyncSecretary.behavior(state, msg) -> { newState, actions }
//
// The Diligent Secretaries shape, with a plain message rather than an envelope:
// this Secretary is hosted locally by one reader and joins no Party, so there is
// no `from` to be diligent about.
//
//   Messages   NavRequested{key, path}   a TOC row was chosen — click or keyboard
//              ScrolledTo{key, path}     the spy reports what the reader is on
//              ScrollSettled             the guard may lift
//
//   Action     SyncTo{key, path, scroll} — the only one, and the HOST applies it:
//              highlight `key`; move the TOC selection SILENTLY (a selection that
//              re-enters as NavRequested is the loop again); and scroll ONLY when
//              `scroll` is true, which happens for NavRequested and never for the
//              spy.
//
// `key` is opaque on purpose: a structure node's index-key in one reader, a
// heading slug in the other. `path` is carried through untouched for a host
// whose TOC addresses rows by path; a host without one passes null and the field
// rides along unread.
//
// Pure — no DOM, no timers, no captures. The scroll, the guard timer and the spy
// are the host's Actors; this decides only what should happen.
// =============================================================================

var TocSyncSecretary = {

    initial: { currentKey: null, programmaticScroll: false },

    /**
     * Pure coordinator. (state, msg) -> { newState, actions }.
     * Same inputs always produce the same outputs; no side effects.
     */
    behavior: function (state, msg) {
        switch (msg.kind) {

            case 'NavRequested': {
                // Navigation is the one writer allowed to scroll, and it arms the
                // guard in the SAME step — so the scroll it is about to cause
                // cannot return as a ScrolledTo that moves the selection again.
                return {
                    newState: { currentKey: msg.key, programmaticScroll: true },
                    actions: [{ kind: 'SyncTo', key: msg.key, path: msg.path, scroll: true }]
                };
            }

            case 'ScrolledTo': {
                if (state.programmaticScroll) {
                    return { newState: state, actions: [] };   // our own scroll, ignored
                }
                if (msg.key === state.currentKey) {
                    return { newState: state, actions: [] };   // already there, no churn
                }
                // scroll:false is the asymmetry. It is what makes the loop
                // unrepresentable rather than merely unlikely.
                return {
                    newState: { currentKey: msg.key, programmaticScroll: false },
                    actions: [{ kind: 'SyncTo', key: msg.key, path: msg.path, scroll: false }]
                };
            }

            case 'ScrollSettled': {
                return {
                    newState: { currentKey: state.currentKey, programmaticScroll: false },
                    actions: []
                };
            }

            default: {
                return { newState: state, actions: [] };
            }
        }
    }
};
