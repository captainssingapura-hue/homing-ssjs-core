// =============================================================================
// FocusParty — the logical-focus tree: structure, and nothing else. The same
// shape as the DomOpsParty and unlike it in doing no work: no DOM, no keys,
// no holder. A container holds a branch; a leaf joins one; a member can be
// moved from one branch to another as its place in the rendered UI changes.
// Its whole API is mutation and reading, and the only thing it ever says is
// what mutated.
//
// A party is STATIONED or MOBILE (RFC 0066 E3, grafting). The page has one
// stationed party, `focusParty`: the only one the keyboard steward is bound
// to, and so the only one that takes the keys from the body. Every other party
// is mobile — a widget's own, made by the party of parties, `focusParties` —
// and only forwards: it has no listeners of its own, and what mutates in it is
// said to the party it is grafted into, and on up; a stray says nothing to
// anyone. A host grafts a mobile party with branch.graft(name, party): a PROXY
// joins the branch — a member fixed to that one party for its life, holding the
// party's root — and the whole of the party is told joined. The proxy is
// transparent: never in the walk, never the one that holds the keys, and a
// container's wouldOffer is asked through it. branch.detach(name) tells the
// party's members left and dissolves the proxy; the party is whole, and a stray
// again. A mobile party dissolved takes its proxy with it, and a branch
// dissolved above a proxy dissolves the party.
//
//   branch.join(name, component)          → membership: a leaf in this branch
//   branch.createBranch(name, holder)     → the sub-branch a container holds; the holder
//                                           is a member of this branch too, the node the
//                                           sub-branch hangs from
//   branch.adopt(membership)              the member moved here, its own branch and all;
//                                           refused under its own descendant, and from another party
//   branch.graft(name, party)             → the proxy: a mobile party grafted here
//   branch.detach(name)                   → the mobile party grafted here, a stray again
//   branch.members, branch.owner, branch.path, branch.dissolve()
//   branch.walk()                         its members in pre-order: each member, then the
//                                           branch it holds walked in turn - the Tab order;
//                                           a proxy's branch walked, the proxy not
//   membership.leave()                    gone; a branch it holds is dissolved first
//   membership.component .name .id .path .kind (member | proxy) .in (the branch it is in)
//             .branch (the one it holds)
//   membership.parent()                   the member holding the branch it is in, or null
//   party.root .kind (stationed | mobile) .inspect() .walk() .find(id) .stationed()
//   focusParty.on(fn) → off               notices: { kind: joined | left | moved, id, path, parent }
//   focusParties.mobile(name) .stationed .mobiles() .strays()
//
// A member is the component itself, by the base's contract, with what the
// steward reads off it when it holds the keys: keyDown(ev), keyUp(ev),
// granted(by), taken(by), and wouldHold(from) when a descendant yields. The
// party reads none of them. Ids are stable across a move and a graft; paths
// are for the eye and change with the place.
// =============================================================================

var _seq = 0;

class FocusMembership {
    constructor(branch, name, component, kind) {
        this.id = "m" + (++_seq);
        this.name = name;
        this.component = component;
        this.kind = kind || "member";
        this.in = branch;
        this.branch = null;            // the sub-branch this member holds, when a container; a proxy's, its party's root
        this.mobile = null;            // a proxy's party, fixed for its life
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
        if (this.kind === "proxy") { if (this.mobile) this.mobile.dissolve(); return; }   // an order from above: the party goes, its proxy with it
        if (this.branch) { this.branch.dissolve(); this.branch = null; }
        var branch = this.in, party = branch.party, parent = this.parent();
        var i = branch.members.indexOf(this);
        if (i >= 0) branch.members.splice(i, 1);
        this.in = null;
        party._notify({ kind: "left", id: this.id, name: this.name, path: branch.path + "/" + this.name, parent: parent ? parent.id : null });
    }
    /** A proxy dissolved: said left, out of its branch, and its party no longer grafted. */
    _release() {
        var branch = this.in, mobile = this.mobile, parent = this.parent();
        branch.party._notify({ kind: "left", id: this.id, name: this.name, path: this.path, parent: parent ? parent.id : null });
        var i = branch.members.indexOf(this);
        if (i >= 0) branch.members.splice(i, 1);
        this.in = null;
        this.branch = null;
        this.mobile = null;
        if (mobile) { mobile.root.owner = null; mobile._proxy = null; }
    }
}

/** A proxy's face, for the steward and the walk: it holds nothing, and asks the holder of its own branch whether its party is offered. */
function _proxyFace(proxy) {
    return Object.freeze({
        toString: function () { return "proxy"; },
        wouldOffer: function () {
            var up = proxy.in ? proxy.in.owner : null, c = up ? up.component : null;
            return !c || typeof c.wouldOffer !== "function" || c.wouldOffer(proxy) !== false;
        }
    });
}

class FocusBranch {
    constructor(party, name, owner) {
        this.party = party;
        this.name = name;
        this.owner = owner || null;     // the membership holding this branch; null for the root; a grafted party's root, its proxy
        this.members = [];
    }
    get path() { return this.owner ? this.owner.path : ""; }
    _check(name) {
        if (typeof name !== "string" || !name) throw new Error("[FocusParty] a member needs a name");
        for (var i = 0; i < this.members.length; i++) if (this.members[i].name === name) throw new Error("[FocusParty] '" + name + "' is already a member of " + (this.path || "the root"));
    }
    _told(kind, m) {
        var parent = m.parent();
        this.party._notify({ kind: kind, id: m.id, name: m.name, path: m.path, parent: parent ? parent.id : null });
    }
    /** A leaf: the component joins this branch under `name`. */
    join(name, component) {
        this._check(name);
        if (!component || typeof component !== "object") throw new Error("[FocusParty] join wants the component");
        var m = new FocusMembership(this, name, component);
        this.members.push(m);
        this._told("joined", m);
        return m;
    }
    /** A container: the holder joins this branch under `name` and holds the sub-branch returned. */
    createBranch(name, holder) {
        var m = this.join(name, holder);
        m.branch = new FocusBranch(this.party, name, m);
        return m.branch;
    }
    /** The member moved here from wherever it is in this party, its own branch and all; not under itself. */
    adopt(m) {
        if (!(m instanceof FocusMembership) || !m.in) throw new Error("[FocusParty] adopt wants a member");
        if (m.in === this) return m;
        if (m.in.party !== this.party) throw new Error("[FocusParty] a member is adopted within its party; another party's is grafted");
        if (this.owner && (this.owner === m || m.holds(this.owner))) throw new Error("[FocusParty] a member cannot be adopted under itself or its own descendant");
        for (var i = 0; i < this.members.length; i++) if (this.members[i].name === m.name) throw new Error("[FocusParty] '" + m.name + "' is already a member of " + (this.path || "the root"));
        var from = m.in, k = from.members.indexOf(m);
        if (k >= 0) from.members.splice(k, 1);
        m.in = this;
        this.members.push(m);
        this._told("moved", m);
        return m;
    }
    /**
     * A mobile party grafted here, under `name`: a proxy joins this branch, fixed to the party for its life,
     * holding its root; the proxy and every member of the party are told joined. Refused: anything but a mobile
     * party; one grafted already, or dissolved; a party grafted inside itself; a name taken.
     */
    graft(name, mobile) {
        this._check(name);
        if (!(mobile instanceof MobileFocusParty)) throw new TypeError("[FocusParty] graft: only a mobile party is grafted");
        if (mobile._dissolved) throw new Error("[FocusParty] graft: mobile party '" + mobile.name + "' is dissolved");
        if (mobile._proxy) throw new Error("[FocusParty] graft: mobile party '" + mobile.name + "' is grafted already - detach it first");
        for (var b = this; b; b = b.owner ? b.owner.in : (b.party._proxy ? b.party._proxy.in : null)) {
            if (b === mobile.root) throw new Error("[FocusParty] graft: mobile party '" + mobile.name + "' cannot be grafted inside itself");
        }
        var proxy = new FocusMembership(this, name, null, "proxy");
        proxy.component = _proxyFace(proxy);
        proxy.branch = mobile.root;
        proxy.mobile = mobile;
        this.members.push(proxy);
        mobile.root.owner = proxy;
        mobile._proxy = proxy;
        this._told("joined", proxy);
        var all = mobile.root._all();
        for (var i = 0; i < all.length; i++) all[i].in._told("joined", all[i]);
        return proxy;
    }
    /** The mobile party grafted here as `name`, detached: its members told left, leaves last, and its proxy dissolved. */
    detach(name) {
        var proxy = null;
        for (var i = 0; i < this.members.length; i++) if (this.members[i].name === name && this.members[i].kind === "proxy") proxy = this.members[i];
        if (!proxy) throw new Error("[FocusParty] detach: no mobile party is grafted here as '" + name + "'");
        var mobile = proxy.mobile, all = mobile.root._all();
        for (var j = all.length - 1; j >= 0; j--) all[j].in._told("left", all[j]);
        proxy._release();
        return mobile;
    }
    /** Every membership under this branch in pre-order, proxies included. */
    _all() {
        var out = [];
        for (var i = 0; i < this.members.length; i++) {
            var m = this.members[i];
            out.push(m);
            if (m.branch) out = out.concat(m.branch._all());
        }
        return out;
    }
    /** The members in pre-order: each member, then the branch it holds, walked in turn; a proxy is not one, its party's are. */
    walk() {
        var out = [];
        for (var i = 0; i < this.members.length; i++) {
            var m = this.members[i];
            if (m.kind !== "proxy") out.push(m);
            if (m.branch) out = out.concat(m.branch.walk());
        }
        return out;
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
                return Object.freeze({ id: m.id, name: m.name, path: m.path, kind: m.kind === "proxy" ? "proxy" : m.branch ? "holder" : "leaf",
                    mobile: m.mobile ? m.mobile.name : null,
                    component: m.kind === "proxy" ? "proxy" : m.component && m.component.constructor ? m.component.constructor.name : "?",
                    branch: m.branch ? m.branch.inspect() : null });
            }))
        });
    }
}

/** A party's tree: never itself - Stationed, the page's, or Mobile, any other. */
class FocusParty {
    constructor(kind) {
        if (new.target === FocusParty) throw new TypeError("[FocusParty] a party is Stationed or Mobile: the page's is focusParty, and a mobile one comes from focusParties.mobile(name)");
        this.kind = kind;
        this.root = new FocusBranch(this, "root", null);
    }
    /** The member with this id, or null: a grafted party's members found through its proxy. */
    find(id) {
        var found = null;
        (function walk(b) { for (var i = 0; i < b.members.length && !found; i++) { if (b.members[i].id === id) found = b.members[i]; else if (b.members[i].branch) walk(b.members[i].branch); } })(this.root);
        return found;
    }
    inspect() { return this.root.inspect(); }
    /** Every member in pre-order: the traversal, and the Tab order. */
    walk() { return this.root.walk(); }
}

var _stationedMade = false;

/** The page's party: the one the steward is bound to, and so the one that takes the keys from the body. One per page. */
class StationedFocusParty extends FocusParty {
    constructor() {
        if (_stationedMade) throw new Error("[FocusParty] the page has its stationed party already: focusParty");
        super("stationed");
        _stationedMade = true;
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
    /** The stationed party this one reaches: itself. */
    stationed() { return this; }
}

const _MOBILE = Symbol("mobile");

/** Any other party: a widget's own, made by the party of parties; it only forwards, to the party it is grafted into. */
class MobileFocusParty extends FocusParty {
    constructor(mark, name) {
        if (mark !== _MOBILE) throw new TypeError("[FocusParty] a mobile party comes from focusParties.mobile(name)");
        if (typeof name !== "string" || !/^[A-Za-z0-9_-]+$/.test(name)) throw new TypeError("[FocusParty] a mobile party's name: letters, digits, hyphen, underscore");
        super("mobile");
        this.name = name;
        this._proxy = null;
        this._dissolved = false;
    }
    /** A mobile party has no listeners of its own: what mutates in it is said where it is grafted. */
    on() { throw new Error("[FocusParty] a mobile party only forwards: listen to the page's, focusParty"); }
    _notify(notice) { if (this._proxy && this._proxy.in) this._proxy.in.party._notify(notice); }
    get isGrafted() { return this._proxy !== null; }
    /** The stationed party this one reaches through its grafts, or null while it, or a party it is grafted into, is a stray. */
    stationed() { return this._proxy && this._proxy.in ? this._proxy.in.party.stationed() : null; }
    /** Every member left, its proxy dissolved, and out of the party of parties. */
    dissolve() {
        if (this._dissolved) return;
        this.root.dissolve();
        if (this._proxy) this._proxy._release();
        this._dissolved = true;
        focusParties._left(this);
    }
}

/** The party of parties: the page's stationed party, and every mobile party made on the page, until it is dissolved. */
class FocusParties {
    constructor(stationed) { this._stationed = stationed; this._mobiles = new Map(); }
    get stationed() { return this._stationed; }
    /** A mobile party, new: a stray until a host grafts it. Its name is its own; no other alive has it. */
    mobile(name) {
        if (this._mobiles.has(name)) throw new RangeError("[FocusParty] a mobile party named '" + name + "' is alive already");
        var party = new MobileFocusParty(_MOBILE, name);
        this._mobiles.set(name, party);
        return party;
    }
    mobiles() { return Array.from(this._mobiles.values()); }
    /** The mobile parties no one has grafted, as data. */
    strays() { return Object.freeze(this.mobiles().filter(function (m) { return !m.isGrafted; }).map(function (m) { return Object.freeze({ name: m.name, tree: m.inspect() }); })); }
    _left(party) { if (this._mobiles.get(party.name) === party) this._mobiles.delete(party.name); }
}

/** The page's, one per document: the party the steward is bound to. */
const focusParty = new StationedFocusParty();

/** The page's party of parties. */
const focusParties = new FocusParties(focusParty);
