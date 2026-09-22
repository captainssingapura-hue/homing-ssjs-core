// =============================================================================
// FocusParty — the logical-focus tree: structure, and nothing else. The same
// shape as the DomOpsParty and unlike it in doing no work: no DOM, no keys,
// no holder. A container holds a branch; a leaf joins one; a member can be
// moved from one branch to another as its place in the rendered UI changes.
// Its whole API is mutation and reading, and the only thing it ever says is
// what mutated. One per page: `focusParty`, the root branch.
//
//   branch.join(name, component)          → membership: a leaf in this branch
//   branch.createBranch(name, holder)     → the sub-branch a container holds; the holder
//                                           is a member of this branch too, the node the
//                                           sub-branch hangs from
//   branch.adopt(membership)              the member moved here, its own branch and all;
//                                           refused under its own descendant
//   branch.members, branch.owner, branch.path, branch.dissolve()
//   membership.leave()                    gone; a branch it holds is dissolved first
//   membership.component .name .id .path .in (the branch it is in) .branch (the one it holds)
//   membership.parent()                   the member holding the branch it is in, or null
//   focusParty.root                       the root branch
//   focusParty.on(fn) → off               notices: { kind: joined | left | moved, id, path, parent }
//   focusParty.inspect()                  the tree as frozen data
//
// A member is the component itself, by the base's contract, with what the
// steward reads off it when it holds the keys: keyDown(ev), keyUp(ev),
// granted(by), taken(by), and wouldHold(from) when a descendant yields. The
// party reads none of them. Ids are stable across a move; paths are for the
// eye and change with the place.
// =============================================================================

var _seq = 0;

class FocusMembership {
    constructor(branch, name, component) {
        this.id = "m" + (++_seq);
        this.name = name;
        this.component = component;
        this.in = branch;
        this.branch = null;            // the sub-branch this member holds, when a container
    }
    get path() { return this.in.path + "/" + this.name; }
    /** The member holding the branch this one is in, or null at the root. */
    parent() { return this.in.owner; }
    /** Whether `other` is this member or below it. */
    holds(other) {
        for (var b = other.in; b; b = b.owner ? b.owner.in : null) if (b.owner === this) return true;
        return false;
    }
    leave() {
        if (!this.in) return;
        if (this.branch) { this.branch.dissolve(); this.branch = null; }
        var branch = this.in, party = branch.party, parent = this.parent();
        var i = branch.members.indexOf(this);
        if (i >= 0) branch.members.splice(i, 1);
        this.in = null;
        party._notify({ kind: "left", id: this.id, name: this.name, path: branch.path + "/" + this.name, parent: parent ? parent.id : null });
    }
}

class FocusBranch {
    constructor(party, name, owner) {
        this.party = party;
        this.name = name;
        this.owner = owner || null;     // the membership holding this branch; null for the root
        this.members = [];
    }
    get path() { return this.owner ? this.owner.path : ""; }
    _check(name) {
        if (typeof name !== "string" || !name) throw new Error("[FocusParty] a member needs a name");
        for (var i = 0; i < this.members.length; i++) if (this.members[i].name === name) throw new Error("[FocusParty] '" + name + "' is already a member of " + (this.path || "the root"));
    }
    /** A leaf: the component joins this branch under `name`. */
    join(name, component) {
        this._check(name);
        if (!component || typeof component !== "object") throw new Error("[FocusParty] join wants the component");
        var m = new FocusMembership(this, name, component);
        this.members.push(m);
        this.party._notify({ kind: "joined", id: m.id, name: name, path: m.path, parent: this.owner ? this.owner.id : null });
        return m;
    }
    /** A container: the holder joins this branch under `name` and holds the sub-branch returned. */
    createBranch(name, holder) {
        var m = this.join(name, holder);
        m.branch = new FocusBranch(this.party, name, m);
        return m.branch;
    }
    /** The member moved here from wherever it is, its own branch and all; not under itself. */
    adopt(m) {
        if (!(m instanceof FocusMembership) || !m.in) throw new Error("[FocusParty] adopt wants a member");
        if (m.in === this) return m;
        if (this.owner && (this.owner === m || m.holds(this.owner))) throw new Error("[FocusParty] a member cannot be adopted under itself or its own descendant");
        for (var i = 0; i < this.members.length; i++) if (this.members[i].name === m.name) throw new Error("[FocusParty] '" + m.name + "' is already a member of " + (this.path || "the root"));
        var from = m.in, k = from.members.indexOf(m);
        if (k >= 0) from.members.splice(k, 1);
        m.in = this;
        this.members.push(m);
        this.party._notify({ kind: "moved", id: m.id, name: m.name, path: m.path, parent: this.owner ? this.owner.id : null });
        return m;
    }
    /** Every member left, leaves last first. */
    dissolve() {
        while (this.members.length) this.members[this.members.length - 1].leave();
    }
    /** This branch as frozen data. */
    inspect() {
        var owner = this.owner;
        return Object.freeze({
            name: this.name, path: this.path, holder: owner ? owner.id : null,
            members: Object.freeze(this.members.map(function (m) {
                return Object.freeze({ id: m.id, name: m.name, path: m.path, kind: m.branch ? "holder" : "leaf",
                    component: m.component && m.component.constructor ? m.component.constructor.name : "?",
                    branch: m.branch ? m.branch.inspect() : null });
            }))
        });
    }
}

class FocusParty {
    constructor() {
        this.root = new FocusBranch(this, "root", null);
        this._sinks = [];
    }
    /** Another listener for the notices; the function returned removes it. */
    on(fn) {
        if (typeof fn !== "function") throw new Error("[FocusParty] on wants a function");
        var sinks = this._sinks;
        sinks.push(fn);
        return function () { var i = sinks.indexOf(fn); if (i >= 0) sinks.splice(i, 1); };
    }
    _notify(notice) {
        var n = Object.freeze(notice), sinks = this._sinks.slice();
        for (var i = 0; i < sinks.length; i++) {
            try { sinks[i](n); } catch (e) { console.error("[FocusParty] a listener threw on " + n.kind + ":", e); }
        }
    }
    /** The member with this id, or null. */
    find(id) {
        var found = null;
        (function walk(b) { for (var i = 0; i < b.members.length && !found; i++) { if (b.members[i].id === id) found = b.members[i]; else if (b.members[i].branch) walk(b.members[i].branch); } })(this.root);
        return found;
    }
    inspect() { return this.root.inspect(); }
}

/** The page's, one per document. */
const focusParty = new FocusParty();
