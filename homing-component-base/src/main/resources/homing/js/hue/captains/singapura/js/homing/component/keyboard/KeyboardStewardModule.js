// =============================================================================
// KeyboardSteward — the keyboard party's face to the DOM, one per page. It
// owns the party (one secretary, KeyboardSecretary, members flat), captures
// keys on the document while someone holds the keyboard, and turns them into
// messages the secretary routes to the holder and nowhere else. Lazy: no
// listener while no one holds. One per page, as `KeyboardStewardInstance`,
// bound to the focus party (FocusParty): a member of the tree is a member
// here by its membership, and the party's `left` drops it. A page — the log,
// a monitor — may know the steward; a component never does: it joins a focus
// branch and calls Keys.
//
//   KeyboardStewardInstance          the page's, one per document
//   new KeyboardSteward(branch?, { onEvent?, party? })   the class, for a test
//   steward.on(fn) → off           another listener for the events; off() removes it
//   steward.join(membership)         a member of the focus tree: its component's
//       keyDown?(ev), keyUp?(ev), granted?(by), taken?(by) are its reactors. Done by
//       the steward itself on the party's `joined` notice: a component that joins
//       a focus branch is a member here without a word to the steward
//   steward.join(id, { keyDown?(ev), keyUp?(ev), granted?(), taken?(by) }) → id
//       a member by id, the older way. keyDown/keyUp return true to TAKE the key —
//       the steward then defaults and stops it, and nothing below the document
//       sees it — or anything else to leave it, and it travels on its normal
//       way: to the focused element, then up through its ancestors, so the
//       containers the holder sits in hear it by bubbling. Neither is called
//       unless the member holds.
//   steward.leave(m)                 the member is gone; it releases if it held
//   steward.claim(m)                 a fact: m holds now, whoever held is told
//   steward.release(m)               nothing, unless m holds
//   steward.holder()                 the id, or null; a tree member's id is its membership's
//   steward.has(m)                   whether m is a member
//       m: an id, or a membership of the focus tree
//   steward.dispose()
//
// One rule is the steward's, since only it sees the target: with physical
// focus in a text field — an input, a textarea, an editable — plain keys and
// arrows are the field's and are not forwarded; a chord with a modifier, and
// Escape, are. Physical focus is never touched: no focus() is called here.
// Every change of holder is one KeyboardEvents object to every listener:
// Granted, Taken (by whom), Released. The chrome that makes the steward may
// hand it to the pages it hosts; each listens for what it shows. The convention by which components claim —
// a press or the focus arriving in their root — is Keys.claimOn, theirs.
// =============================================================================

const _keyboardOwner = Object.freeze({ toString: () => "keyboard" });
var _pages = new WeakSet();          // the documents that have a steward: one each
var _STEWARD = "steward", _ROOT = "keyboard";

class KeyboardSteward {
    constructor(branch, opts) {
        if (typeof document !== "undefined") {
            if (_pages.has(document)) throw new Error("[KeyboardSteward] this page has a steward already: one per page");
            _pages.add(document);
        }
        var self = this;
        if (branch) branch.activate(_keyboardOwner);
        this._branch = branch || null;
        this._sinks = [];
        if (opts && typeof opts.onEvent === "function") this.on(opts.onEvent);
        this._holder = null;
        this._members = {};
        this._listening = false;
        this._party = new Party({ name: "keyboard", root: { path: _ROOT, initial: KeyboardSecretary.initial, behavior: KeyboardSecretary.behavior } });
        this._party.joinActor({ id: _STEWARD, parentSecretary: _ROOT, reactors: {} });
        this._onDown = function (ev) { self._forward("KeyDown", ev); };
        this._onUp = function (ev) { self._forward("KeyUp", ev); };
        // the focus tree: whoever joins it is a member here, by its membership; whoever leaves it leaves here too
        this._focus = opts && opts.party ? opts.party : null;
        this._offFocus = this._focus ? this._focus.on(function (n) {
            if (n.kind === "joined") { var m = self._focus.find(n.id); if (m && !self._members[m.id]) self.join(m); }
            else if (n.kind === "left" && self._members[n.id]) self.leave(n.id);
        }) : null;
    }
    /** A member's id: a membership's, or the string given. */
    static idOf(m) { return m && typeof m === "object" && typeof m.id === "string" ? m.id : m; }

    // ── the members ───────────────────────────────────────────────────────
    join(id, handlers) {
        if (id && typeof id === "object" && typeof id.id === "string" && id.component) {   // a membership of the focus tree: the component's methods are the reactors
            var c = id.component;
            handlers = { keyDown: typeof c.keyDown === "function" ? function (ev) { return c.keyDown(ev); } : null, keyUp: typeof c.keyUp === "function" ? function (ev) { return c.keyUp(ev); } : null,
                         granted: typeof c.granted === "function" ? function (by) { c.granted(by); } : null, taken: typeof c.taken === "function" ? function (by) { c.taken(by); } : null };
            id = id.id;
        }
        if (typeof id !== "string" || !id) throw new Error("[KeyboardSteward] a member needs an id");
        if (id === _STEWARD || this._members[id]) throw new Error("[KeyboardSteward] member joined twice: " + id);
        var self = this, h = handlers || {};
        function key(kind) {
            return function (m) {
                var fn = kind === "KeyDown" ? h.keyDown : h.keyUp;
                if (typeof fn === "function" && fn(m.ev) === true) { m.ev.preventDefault(); m.ev.stopPropagation(); }
            };
        }
        this._party.joinActor({ id: id, parentSecretary: _ROOT, reactors: {
            KeyDown: key("KeyDown"),
            KeyUp: key("KeyUp"),
            Granted: function (m) { self._held(id); if (typeof h.granted === "function") h.granted(m.by == null ? "claim" : m.by); },
            Taken: function (m) { self._lost(id, m.by); if (typeof h.taken === "function") h.taken(m.by); }
        } });
        this._members[id] = true;
        return id;
    }
    leave(id) {
        id = KeyboardSteward.idOf(id);
        if (!this._members[id]) return;
        this._party.tellFrom(_STEWARD, { kind: "Left", id: id });
        this._party.leave(id);
        delete this._members[id];
    }
    claim(id) {
        id = KeyboardSteward.idOf(id);
        if (!this._members[id]) throw new Error("[KeyboardSteward] no member '" + id + "'");
        this._party.tellFrom(_STEWARD, { kind: "Claim", id: id });
    }
    release(id) {
        id = KeyboardSteward.idOf(id);
        if (!this._members[id]) return;
        this._party.tellFrom(_STEWARD, { kind: "Release", id: id });
    }
    holder() { return this._holder; }
    has(id) { return !!this._members[KeyboardSteward.idOf(id)]; }
    /** Another listener for the events; the function returned removes it. */
    on(fn) {
        if (typeof fn !== "function") throw new Error("[KeyboardSteward] on wants a function");
        var sinks = this._sinks;
        sinks.push(fn);
        return function () { var i = sinks.indexOf(fn); if (i >= 0) sinks.splice(i, 1); };
    }

    // ── what the secretary decided, mirrored for the listeners and the sink ──
    _held(id) {
        this._holder = id;
        this._listen(true);
        this._fire(KeyboardEvents.Granted(id));
    }
    _lost(id, by) {
        if (this._holder === id) this._holder = null;
        if (by == null) this._listen(false);               // evicted: the new holder's Granted follows at once
        this._fire(by == null ? KeyboardEvents.Released(id) : KeyboardEvents.Taken(id, by));
    }

    // ── the document, while someone holds ─────────────────────────────────
    _listen(on) {
        if (on === this._listening || typeof document === "undefined") return;
        var f = on ? "addEventListener" : "removeEventListener";
        document[f]("keydown", this._onDown, true);
        document[f]("keyup", this._onUp, true);
        this._listening = on;
    }
    _forward(kind, ev) {
        if (this._holder === null) return;
        if (KeyboardSteward.editable(ev.target) && !(ev.ctrlKey || ev.metaKey || ev.altKey) && ev.key !== "Escape") return;
        this._party.tellFrom(_STEWARD, { kind: kind, ev: ev });
    }
    /** A text field: its plain keys are its own. */
    static editable(el) {
        if (!el || typeof el.tagName !== "string") return false;
        var tag = el.tagName.toUpperCase();
        if (tag === "TEXTAREA") return true;
        if (tag === "INPUT") { var t = String(el.type || "text").toLowerCase(); return ["button", "checkbox", "radio", "range", "submit", "reset", "color", "file", "image"].indexOf(t) < 0; }
        return el.isContentEditable === true;
    }
    _fire(ev) {
        var sinks = this._sinks.slice();
        for (var i = 0; i < sinks.length; i++) {
            try { sinks[i](ev); } catch (e) { console.error("[KeyboardSteward] onEvent threw on " + ev.kind + ":", e); }
        }
    }

    dispose() {
        this._listen(false);
        this._holder = null;
        this._members = {};
        this._sinks = [];
        if (this._offFocus) { this._offFocus(); this._offFocus = null; }
        if (typeof document !== "undefined") _pages.delete(document);
        if (this._branch) this._branch.dissolve();
    }
}

/** The page's steward, one per document, bound to the focus party. */
const KeyboardStewardInstance = new KeyboardSteward(null, { party: focusParty });
