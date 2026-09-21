// =============================================================================
// Keys — the claiming convention, as one line for a component: a press in
// its root, or the focus arriving in it, claims the keyboard for it; the
// focus leaving it for somewhere outside releases. The party is told the
// result and never the cause; what makes the convention resolve nesting is
// the CAPTURE phase — capture runs outermost first, so when a press lands in
// a widget inside a pane inside a dialog, the dialog claims, then the pane,
// then the widget, and the innermost holds because it claimed last. No one
// needs to know who contains whom.
//
//   var off = Keys.claimOn(root, steward, id, { release?: true });
//     root      the component's own element
//     steward   the page's KeyboardSteward, which id has joined
//     release   whether the focus leaving the root releases (the default), or
//               the component keeps the keys until the next claim
//   off()       the listeners removed; a component calls it on dispose
//
// A component that claims by call — a shortcut summoning it — calls
// steward.claim(id) itself; whether it also moves physical focus is its own.
// =============================================================================

var Keys = Object.freeze({

    claimOn: function (root, steward, id, opts) {
        if (!root || typeof root.addEventListener !== "function") throw new Error("[Keys] claimOn wants the component's root element");
        if (!steward || typeof steward.claim !== "function") throw new Error("[Keys] claimOn wants the page's KeyboardSteward");
        var release = !opts || opts.release !== false;
        function claim() { steward.claim(id); }
        function out(ev) {
            var to = ev.relatedTarget;
            if (release && !(to && typeof root.contains === "function" && root.contains(to))) steward.release(id);
        }
        root.addEventListener("pointerdown", claim, true);
        root.addEventListener("focusin", claim, true);
        root.addEventListener("focusout", out, true);
        return function () {
            root.removeEventListener("pointerdown", claim, true);
            root.removeEventListener("focusin", claim, true);
            root.removeEventListener("focusout", out, true);
        };
    }
});
