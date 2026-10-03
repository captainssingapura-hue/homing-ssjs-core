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
//     and a container is never granted for a press on its child. The root
//     enrolled is also where the member's mark is worn — written by the
//     steward, never by the member — and what the browser's focus arriving
//     inside it is measured against: that is the steward's business, not the
//     convention's (RFC 0066 E3, keyboard §17.5), so the convention listens to
//     the press alone. off() removes the listener and forgets the root.
//   Keys.claim(m)                    by call: a dialog on open, a shortcut's summons
//   Keys.yield(m)                    the keys given up: they go up the focus tree to the first
//                                    ancestor that would hold them (wouldHold), else to no one
//   Keys.release(m)                  nothing, unless m holds
//   Keys.home(m)                     the root's default allocation: where a yield no one would hold
//                                    goes, and, while no one holds, the keys now - a page names it
//                                    once it is laid out; Keys.home(null) takes it off
//     m: a membership of the focus tree — or, the older way, the pair
//     (steward, id): Keys.claimOn(root, steward, id), Keys.claim(steward, id)
//   A member of a mobile focus party that reaches no stationed one - a stray, or
//   grafted into a stray - only forwards, and has nowhere to forward to: its
//   claim, yield and release are nothing, and a press in its root claims nothing.
// =============================================================================

/** A membership whose party reaches no stationed party: nothing it asks of the keys goes anywhere. */
function _stray(m) {
    var party = m && typeof m === "object" && m.in ? m.in.party : null;
    return !!party && typeof party.stationed === "function" && party.stationed() === null;
}

function _target(a, b) {
    return a && typeof a.join === "function" && typeof a.claim === "function" ? { steward: a, id: b } : { steward: KeyboardStewardInstance, id: a };
}

var Keys = Object.freeze({

    claimOn: function (root, a, b) {
        if (!root || typeof root.addEventListener !== "function") throw new Error("[Keys] claimOn wants the component's root element");
        var t = _target(a, b);
        var member = t.id && typeof t.id === "object" ? t.id : null;
        var steward = t.steward, id = member && typeof member.id === "string" ? member.id : t.id;
        if (!steward || typeof steward.claim !== "function") throw new Error("[Keys] claimOn wants the page's KeyboardSteward");
        if (id == null) throw new Error("[Keys] claimOn wants the membership to claim for");
        var forget = steward.enroll(root, id);
        function press(ev) { if (steward.memberAt(ev.target) === id && !_stray(member)) steward.claim(id); }   // a stray's press goes nowhere
        root.addEventListener("pointerdown", press, true);
        return function () {
            root.removeEventListener("pointerdown", press, true);
            forget();
        };
    },

    claim: function (a, b) { if (_stray(a)) return; var t = _target(a, b); t.steward.claim(t.id); },
    yield: function (a, b) { if (_stray(a)) return false; var t = _target(a, b); return t.steward.yield(t.id); },
    release: function (a, b) { if (_stray(a)) return; var t = _target(a, b); t.steward.release(t.id); },
    home: function (a, b) {
        if (a == null) { KeyboardStewardInstance.home(null); return; }
        if (_stray(a)) return;
        var t = _target(a, b); t.steward.home(t.id);
    }
});
