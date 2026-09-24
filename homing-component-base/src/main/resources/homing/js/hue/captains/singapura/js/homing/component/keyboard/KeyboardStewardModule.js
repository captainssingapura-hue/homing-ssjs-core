// =============================================================================
// KeyboardSteward — the keyboard party's face to the DOM, one per document.
// It owns the holder: the flat secretary, one field, a claim evicts. It reads
// the focus party's structure and never mutates it. It routes by state, per
// key, from one listener on the document in the BUBBLE phase, always on — so
// a component that stops a key's propagation keeps it, and the steward acts
// only on what the page let through:
//
//   THE PAGE'S OWN KEYS come before all of it: a chord or a function key the
//   page claimed with shortcut(fn) is tried first, wherever the hand is, so a
//   command that summons something works while a grid's editor has the focus.
//   Then, by state:
//
//   target is the body — nothing focused anywhere — to the holder's
//   keyDown/keyUp, defaulted and stopped when taken; target is a focused
//   element — the native world had it, EXCEPT for a chord, which is offered
//   up the chain from that element: memberAt for where to start, the party
//   for who is above, chord(ev) on each until one takes it. A letter typed in
//   a field is nobody else's business; Ctrl+Tab is. A chord the HOLDER had no
//   use for goes up the same way, since a holder is not always the outermost
//   thing that cares. The holder is untouched by native focus and resumes the
//   moment the focused thing blurs.
//
//   THE WALK, while nothing is natively focused, is KeyboardWalk's: it moves
//   a CANDIDATE — the cursor the steward keeps beside the holder, offered and
//   withdrawn here — over the focus party tree, and the holder does not
//   change until a key confirms it. Anything else goes to the holder as
//   usual. A claim by any other means, native focus arriving, or the
//   candidate leaving the tree, all withdraw the offer: the walk exists only
//   while the keyboard has the page — the steward's one focusin listener
//   withdraws, and never claims.
//
// A member is also told when the keys are INSIDE it — within(on), on the
// holder's ancestors in the focus tree — so a container can show where the
// work is going on. Nothing is handed on: it is told, not routed.
//
// Members: a membership of the focus tree, joined on the party's `joined`
// notice, its component's keyDown/keyUp/granted/taken the reactors; or, the
// older way, an id with handlers. The roots under the convention are enrolled
// here (Keys.claimOn does it), so `memberAt(el)` answers the innermost member
// whose root contains an element — the structural query the press and the
// traversal both rest on. Claim, yield, release; events out by on(fn); and
// for tooling, trace(fn): every key the steward saw and what it did with it.
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
        this._traces = [];
        if (opts && typeof opts.onEvent === "function") this.on(opts.onEvent);
        this._holder = null;
        this._candidate = null;      // the walk's cursor: a member id, or null
        this._shortcuts = [];        // the page's own keys, tried before anyone
        this._members = {};
        this._roots = new WeakMap();     // root element → the id enrolled on it
        this._party = new Party({ name: "keyboard", root: { path: _ROOT, initial: KeyboardSecretary.initial, behavior: KeyboardSecretary.behavior } });
        this._party.joinActor({ id: _STEWARD, parentSecretary: _ROOT, reactors: {} });
        this._onDown = function (ev) { self._forward("KeyDown", ev); };
        this._onUp = function (ev) { self._forward("KeyUp", ev); };
        this._onFocusIn = function () { self.withdraw(); };   // a walk is the keyboard's alone: native focus ends it. It never claims
        this._listen(true);
        // the focus tree: whoever joins it is a member here, by its membership; whoever leaves it leaves here too
        this._focus = opts && opts.party ? opts.party : null;
        this._offFocus = this._focus ? this._focus.on(function (n) {
            if (n.kind === "joined") { var m = self._focus.find(n.id); if (m && !self._members[m.id]) self.join(m); }
            else if (n.kind === "left" && self._members[n.id]) {
                if (self._candidate === n.id) self.withdraw();   // nothing is offered to a member that has gone
                // the leaver yields on its way out, from the parent it had: the first ancestor that would hold, else no one
                if (self._holder === n.id) self._handOn(self._catcher(n.parent ? self._focus.find(n.parent) : null, null), n.id, "left");
                self._party.leave(n.id);
                delete self._members[n.id];
            }
        }) : null;
    }
    /** The first of `p` and its ancestors whose component would hold the keys yielded by `from`, or null. */
    _catcher(p, from) {
        for (; p; p = p.parent()) {
            var c = p.component;
            if (c && typeof c.wouldHold === "function" && c.wouldHold(from) === true) return p;
        }
        return null;
    }
    /** The keys to `target` with the reason `by`, or, with no target, released by `from`. */
    _handOn(target, from, by) {
        if (target) this._party.tellFrom(_STEWARD, { kind: "Claim", id: target.id, by: by });
        else this._party.tellFrom(_STEWARD, { kind: "Release", id: from });
    }
    /** A member's id: a membership's, or the string given. */
    static idOf(m) { return m && typeof m === "object" && typeof m.id === "string" ? m.id : m; }

    // ── the roots under the convention ────────────────────────────────────
    /** The root enrolled for a member; the function returned forgets it. Keys.claimOn does this. */
    enroll(root, id) {
        id = KeyboardSteward.idOf(id);
        if (!root || typeof root !== "object") throw new Error("[KeyboardSteward] enroll wants the root element");
        if (typeof id !== "string" || !id) throw new Error("[KeyboardSteward] enroll wants the member's id");
        var roots = this._roots;
        roots.set(root, id);
        return function () { if (roots.get(root) === id) roots.delete(root); };
    }
    /** The id of the innermost member whose enrolled root contains `el`, or null. */
    memberAt(el) {
        for (var x = el; x; x = x.parentNode) { var id = this._roots.get(x); if (id) return id; }
        return null;
    }

    // ── the members ───────────────────────────────────────────────────────
    join(id, handlers) {
        if (id && typeof id === "object" && typeof id.id === "string" && id.component) {   // a membership of the focus tree: the component's methods are the reactors
            var c = id.component;
            handlers = { keyDown: typeof c.keyDown === "function" ? function (ev) { return c.keyDown(ev); } : null, keyUp: typeof c.keyUp === "function" ? function (ev) { return c.keyUp(ev); } : null,
                         granted: typeof c.granted === "function" ? function (by) { c.granted(by); } : null, taken: typeof c.taken === "function" ? function (by) { c.taken(by); } : null,
                         offered: typeof c.offered === "function" ? function () { c.offered(); } : null, withdrawn: typeof c.withdrawn === "function" ? function () { c.withdrawn(); } : null,
                         within: typeof c.within === "function" ? function (on) { c.within(on); } : null,
                         chord: typeof c.chord === "function" ? function (ev) { return c.chord(ev); } : null };
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
            Granted: function (m) { self._held(id, m.by); if (typeof h.granted === "function") h.granted(m.by == null ? "claim" : m.by); },
            Taken: function (m) { self._lost(id, m.by); if (typeof h.taken === "function") h.taken(m.by); }
        } });
        this._members[id] = h;
        return id;
    }
    leave(id) {
        id = KeyboardSteward.idOf(id);
        if (!this._members[id]) return;
        if (this._candidate === id) this.withdraw();
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
    /** Only from the holder: up the tree to the first ancestor that would hold, else to no one. True when it was the holder. */
    yield(id) {
        id = KeyboardSteward.idOf(id);
        if (this._holder !== id) return false;
        var m = this._focus ? this._focus.find(id) : null;
        this._handOn(m ? this._catcher(m.parent(), m) : null, id, "yield");
        return true;
    }
    /** A key of the PAGE's, tried before the native world and before the holder: fn(ev) → true when it took it. A chord or a function key only; the function returned takes it off. */
    shortcut(fn) { return KeyboardShortcuts.add(this._shortcuts, fn); }
    holder() { return this._holder; }
    has(id) { return !!this._members[KeyboardSteward.idOf(id)]; }
    /** Another listener for the events; the function returned removes it. */
    on(fn) {
        if (typeof fn !== "function") throw new Error("[KeyboardSteward] on wants a function");
        var sinks = this._sinks;
        sinks.push(fn);
        return function () { var i = sinks.indexOf(fn); if (i >= 0) sinks.splice(i, 1); };
    }

    // ── where a walk starts ───────────────────────────────────────────────
    /** What is natively focused, or null when nothing is: the document's active element unless it is the body. */
    static focused() {
        if (typeof document === "undefined") return null;
        var a = document.activeElement;
        return a && a !== document.body && a !== document.documentElement ? a : null;
    }
    /** The member a walk starts from: the candidate while one is on, else the innermost member containing the focused element, else the holder; or null. */
    from() {
        if (this._candidate) return this._candidate;
        var f = KeyboardSteward.focused();
        var at = f ? this.memberAt(f) : null;
        return at || this._holder;
    }
    // ── the walk's cursor: KeyboardWalk moves it, the steward keeps it ────
    /** The member the walk's cursor rests on, or null. */
    candidate() { return this._candidate; }
    /** The walk one step on (dir 1) or back (-1), by call: the member offered, or null when it ended. */
    walk(dir) { return KeyboardWalk.move(this, dir); }
    /** The keys offered to a member: the cursor rests on it, and a confirming key would claim it. The holder is never offered. */
    offer(m) {
        var id = KeyboardSteward.idOf(m);
        if (!this._members[id] || id === this._holder || id === this._candidate) return false;
        this.withdraw();
        this._candidate = id;
        var h = this._members[id];
        if (h && typeof h.offered === "function") { try { h.offered(); } catch (e) { console.error("[KeyboardSteward] offered threw:", e); } }
        this._fire(KeyboardEvents.Offered(id));
        return true;
    }
    /** The offer off: the walk moved on, was confirmed, or was called off. True when there was one. */
    withdraw() {
        var id = this._candidate;
        if (!id) return false;
        this._candidate = null;
        var h = this._members[id];
        if (h && typeof h.withdrawn === "function") { try { h.withdrawn(); } catch (e) { console.error("[KeyboardSteward] withdrawn threw:", e); } }
        this._fire(KeyboardEvents.Withdrawn(id));
        return true;
    }
    /** The offer taken up: the candidate claims, and the walk is over. True when there was a candidate. */
    confirm() {
        var id = this._candidate;
        if (!id) return false;
        this.withdraw();
        this.claim(id);
        return true;
    }

    // ── the trace, for tooling ────────────────────────────────────────────
    /**
     * Another listener for every key the steward saw and what it did: { kind, key, route, to, taken } —
     * route "native" (to: the focused element; left), "none" (no holder; left), "holder" (to: the holder's
     * id; taken says whether it took it). The function returned removes it.
     */
    trace(fn) {
        if (typeof fn !== "function") throw new Error("[KeyboardSteward] trace wants a function");
        var traces = this._traces;
        traces.push(fn);
        return function () { var i = traces.indexOf(fn); if (i >= 0) traces.splice(i, 1); };
    }
    _traced(kind, ev, route, to, taken) {
        if (!this._traces.length) return;
        var t = Object.freeze({ kind: kind, key: ev.key, route: route, to: to, taken: taken === true }), sinks = this._traces.slice();
        for (var i = 0; i < sinks.length; i++) {
            try { sinks[i](t); } catch (e) { console.error("[KeyboardSteward] trace threw on " + kind + ":", e); }
        }
    }

    // ── what the secretary decided, mirrored for the listeners and the sink ──
    _held(id, by) {
        this._holder = id;
        this.withdraw();   // the keys have moved: whatever was offered, the walk is over
        this._told();
        this._fire(KeyboardEvents.Granted(id, by == null ? "claim" : by));
    }
    _lost(id, by) {
        if (this._holder === id) { this._holder = null; this._told(); }
        this._fire(by == null ? KeyboardEvents.Released(id) : KeyboardEvents.Taken(id, by));
    }

    // ── the document ──────────────────────────────────────────────────────
    _listen(on) {
        if (typeof document === "undefined") return;
        var f = on ? "addEventListener" : "removeEventListener";
        document[f]("keydown", this._onDown, false);
        document[f]("keyup", this._onUp, false);
        document[f]("focusin", this._onFocusIn, false);
    }
    /**
     * Who the keys are inside: the holder's ancestors in the focus tree, told
     * when that changes and only if they ask — within(true) as the keys come
     * into them, within(false) as they leave. Nothing is handed on and no key
     * is routed: a container that shows where the work is going on wants this,
     * and it is a fact about state, not a tier of bubbling.
     */
    _told() {
        var now = {}, m = this._holder && this._focus ? this._focus.find(this._holder) : null, id;
        for (var p = m ? m.parent() : null; p; p = p.parent()) now[p.id] = true;
        var was = this._inside || {};
        for (id in was) if (!now[id]) this._tell(id, false);
        for (id in now) if (!was[id]) this._tell(id, true);
        this._inside = now;
    }
    _tell(id, on) {
        var h = this._members[id];
        if (h && typeof h.within === "function") { try { h.within(on); } catch (e) { console.error("[KeyboardSteward] within threw:", e); } }
    }

    /** By state: the walk's keys are the steward's; a key from the body goes to the holder; a key from a focused element goes nowhere. Each traced. */
    _forward(kind, ev) {
        if (kind === "KeyDown" && KeyboardShortcuts.took(this._shortcuts, ev)) {   // the page's own, before everything: a command must work wherever the hand is
            ev.preventDefault(); ev.stopPropagation();
            this._traced(kind, ev, "page", null, true);
            return;
        }
        if (KeyboardSteward.fromAFocusedElement(ev)) { this.withdraw(); return this._native(kind, ev); }
        if (kind === "KeyDown" && KeyboardWalk.keyDown(this, ev)) { ev.preventDefault(); ev.stopPropagation(); this._traced(kind, ev, "walk", this._candidate, true); return; }
        var holder = this._holder;
        if (holder === null) { this._traced(kind, ev, "none", null, false); return; }
        return this._toHolder(kind, ev, holder);
    }
    /** The holder gets it; and a chord it had no use for goes on up, because a holder is not always the outermost thing that cares. */
    _toHolder(kind, ev, holder) { this._party.tellFrom(_STEWARD, { kind: kind, ev: ev }); if (kind === "KeyDown" && ev.defaultPrevented !== true) KeyboardChords.above(this, holder, ev); this._traced(kind, ev, "holder", holder, ev.defaultPrevented === true); }

    /** The native world had it — unless it was a chord, which KeyboardChords offers up the chain from where it was pressed. */
    _native(kind, ev) { var by = kind === "KeyDown" ? KeyboardChords.took(this, ev) : null; this._traced(kind, ev, by ? "chord" : "native", by || ev.target, !!by); }

    /** Whether the key came from a focused element — anything but the body, the root element or the document itself. */
    static fromAFocusedElement(ev) {
        var t = ev.target;
        if (!t || typeof document === "undefined") return false;
        return t !== document && t !== document.body && t !== document.documentElement;
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
        this._candidate = null;
        this._members = {};
        this._sinks = [];
        this._traces = [];
        if (this._offFocus) { this._offFocus(); this._offFocus = null; }
        if (typeof document !== "undefined") _pages.delete(document);
        if (this._branch) this._branch.dissolve();
    }
}

/** The page's steward, one per document, bound to the focus party. */
const KeyboardStewardInstance = new KeyboardSteward(null, { party: focusParty });
