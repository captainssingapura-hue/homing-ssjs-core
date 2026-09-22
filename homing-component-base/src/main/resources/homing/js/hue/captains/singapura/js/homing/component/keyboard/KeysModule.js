// =============================================================================
// Keys — what a component talks to about the keys. Never the steward: a
// component joins a focus branch and calls here; every call goes to the
// page's steward directly, none touches the party.
//
//   Keys.claimOn(root, m) → off
//     the claiming convention, as one line and one listener: a press in the
//     root claims the keys for m — when m is the innermost member whose root
//     contains the press. The roots under the convention are enrolled with
//     the steward, so nesting is a structural question and not an order: a
//     press in a widget inside a pane inside a dialog is the widget's alone,
//     and a container is never granted for a press on its child. Native focus
//     is not the convention's business — a member is never natively focused,
//     and the focus arriving in or leaving a native control inside the root
//     moves nothing. off() removes the listener and forgets the root.
//   Keys.claim(m)                    by call: a dialog on open, a shortcut's summons
//   Keys.yield(m)                    the keys given up: they go up the focus tree to the first
//                                    ancestor that would hold them (wouldHold), else to no one
//   Keys.release(m)                  nothing, unless m holds
//     m: a membership of the focus tree — or, the older way, the pair
//     (steward, id): Keys.claimOn(root, steward, id), Keys.claim(steward, id)
// =============================================================================

function _target(a, b) {
    return a && typeof a.join === "function" && typeof a.claim === "function" ? { steward: a, id: b } : { steward: KeyboardStewardInstance, id: a };
}

var Keys = Object.freeze({

    claimOn: function (root, a, b) {
        if (!root || typeof root.addEventListener !== "function") throw new Error("[Keys] claimOn wants the component's root element");
        var t = _target(a, b);
        var steward = t.steward, id = t.id && typeof t.id === "object" && typeof t.id.id === "string" ? t.id.id : t.id;
        if (!steward || typeof steward.claim !== "function") throw new Error("[Keys] claimOn wants the page's KeyboardSteward");
        if (id == null) throw new Error("[Keys] claimOn wants the membership to claim for");
        var forget = steward.enroll(root, id);
        function press(ev) { if (steward.memberAt(ev.target) === id) steward.claim(id); }
        root.addEventListener("pointerdown", press, true);
        return function () {
            root.removeEventListener("pointerdown", press, true);
            forget();
        };
    },

    claim: function (a, b) { var t = _target(a, b); t.steward.claim(t.id); },
    yield: function (a, b) { var t = _target(a, b); return t.steward.yield(t.id); },
    release: function (a, b) { var t = _target(a, b); t.steward.release(t.id); }
});
