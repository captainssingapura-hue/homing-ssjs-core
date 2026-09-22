// =============================================================================
// Keys — what a component talks to about the keys. Never the steward: a
// component joins a focus branch and calls here; every call goes to the
// page's steward directly, none touches the party.
//
//   Keys.claimOn(root, m, { release?: true }) → off
//     the claiming convention, as one line: a press in the root, or the focus
//     arriving in it, claims the keys for m; the focus leaving it for somewhere
//     outside releases (unless release: false). The party is told the result
//     and never the cause. Nesting resolves here, not in the components: every
//     root under the convention is known to Keys, so when a press lands in a
//     widget inside a pane inside a dialog, the dialog and the pane see it land
//     in a root inside their own and stay silent, and the widget alone claims —
//     a container is never granted for a press on its child. The listeners are
//     in the CAPTURE phase, so a stopPropagation inside a root cannot hide a
//     press from it. off() removes the listeners and forgets the root.
//   Keys.claim(m)                    by call: a dialog on open, a shortcut's summons
//   Keys.yield(m)                    the keys given up: they go up the focus tree to the first
//                                    ancestor that would hold them (wouldHold), else to no one
//   Keys.release(m)                  nothing, unless m holds
//     m: a membership of the focus tree — or, the older way, the pair
//     (steward, id): Keys.claimOn(root, steward, id, opts), Keys.claim(steward, id)
// =============================================================================

function _target(a, b) {
    return a && typeof a.join === "function" && typeof a.claim === "function" ? { steward: a, id: b } : { steward: KeyboardStewardInstance, id: a };
}

var _roots = new WeakMap();   // root → how many claimOn are on it: the roots under the convention

/** Whether a registered root lies strictly between the target and `root`: the press is that root's member's, not this one's. */
function _insideAnother(root, target) {
    for (var x = target; x && x !== root; x = x.parentNode) if (_roots.has(x)) return true;
    return false;
}

var Keys = Object.freeze({

    claimOn: function (root, a, b, c) {
        if (!root || typeof root.addEventListener !== "function") throw new Error("[Keys] claimOn wants the component's root element");
        var t = _target(a, b), opts = t.steward === a ? c : b;
        var steward = t.steward, id = t.id;
        if (!steward || typeof steward.claim !== "function") throw new Error("[Keys] claimOn wants the page's KeyboardSteward");
        if (id == null) throw new Error("[Keys] claimOn wants the membership to claim for");
        var release = !opts || opts.release !== false;
        function claim(ev) { if (!_insideAnother(root, ev.target)) steward.claim(id); }
        function out(ev) {
            var to = ev.relatedTarget;
            if (release && !(to && typeof root.contains === "function" && root.contains(to))) steward.release(id);
        }
        root.addEventListener("pointerdown", claim, true);
        root.addEventListener("focusin", claim, true);
        root.addEventListener("focusout", out, true);
        _roots.set(root, (_roots.get(root) || 0) + 1);
        return function () {
            root.removeEventListener("pointerdown", claim, true);
            root.removeEventListener("focusin", claim, true);
            root.removeEventListener("focusout", out, true);
            var n = (_roots.get(root) || 0) - 1;
            if (n > 0) _roots.set(root, n); else _roots.delete(root);
        };
    },

    claim: function (a, b) { var t = _target(a, b); t.steward.claim(t.id); },
    yield: function (a, b) { var t = _target(a, b); return t.steward.yield(t.id); },
    release: function (a, b) { var t = _target(a, b); t.steward.release(t.id); }
});
