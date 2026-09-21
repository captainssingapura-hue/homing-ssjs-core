// =============================================================================
// PartyModule — RFC 0028 cycle 2.
//
// One Party = one workspace-scoped messaging universe. The workspace
// declares a tree of Secretaries (internal routing nodes, each carrying
// state + a pure behaviour function) at construction; Actors (leaves
// with handler maps) join and leave dynamically. The Party owns:
//
//   - the registry (path → Secretary, path → Actor)
//   - synchronous depth-first dispatch
//   - per-member try/catch isolation
//   - inspection accessors
//
// Message shape on the wire:
//
//   envelope = { from: "workspace/ribbon/...", message: { kind: "...", ...} }
//
// Secretary behaviour signature:
//
//   behavior(state, envelope) -> { newState, actions }
//
// where actions is an array of:
//
//   { kind: "SendToParent",       message: {...} }
//   { kind: "BroadcastToMembers", message: {...} }
//   { kind: "SendToMember",       to: "path", message: {...} }
//
// Per RFC 0028 the Action ADT has no LocalEffect variant — Secretaries
// route, Actors do. Side effects live in reactor handlers.
// =============================================================================

class Party {

    constructor(opts) {
        if (!opts || !opts.name)  throw new Error("[Party] opts.name is required");
        if (!opts.root)           throw new Error("[Party] opts.root is required");

        this._name = opts.name;
        this._secretaries = new Map();   // path -> { path, parentPath, state, behavior, members:Set<path> }
        this._actors      = new Map();   // path -> { reactors:Map<kind, handler> }

        this._buildSecretaryTree(opts.root, null);
    }

    // ─── Build ──────────────────────────────────────────────────────────────

    _buildSecretaryTree(node, parentPath) {
        if (!node || !node.path)      throw new Error("[Party] secretary node missing path");
        if (typeof node.behavior !== "function") {
            throw new Error("[Party] secretary " + node.path + " missing behavior function");
        }
        if (this._secretaries.has(node.path)) {
            throw new Error("[Party] duplicate secretary path: " + node.path);
        }
        var sec = {
            path       : node.path,
            parentPath : parentPath,
            state      : node.initial,         // may be undefined; workspace's choice
            behavior   : node.behavior,
            members    : new Set()
        };
        this._secretaries.set(node.path, sec);
        if (parentPath) {
            var parent = this._secretaries.get(parentPath);
            if (!parent) throw new Error("[Party] secretary " + node.path + " parent " + parentPath + " not found");
            parent.members.add(node.path);
        }
        var kids = node.children || [];
        for (var i = 0; i < kids.length; i++) {
            this._buildSecretaryTree(kids[i], node.path);
        }
    }

    // ─── Actor lifecycle ────────────────────────────────────────────────────

    joinActor(opts) {
        if (!opts || !opts.id)                throw new Error("[Party] joinActor: id required");
        if (!opts.parentSecretary)            throw new Error("[Party] joinActor: parentSecretary required");
        if (this._actors.has(opts.id))        throw new Error("[Party] joinActor: id " + opts.id + " already registered");
        var parent = this._secretaries.get(opts.parentSecretary);
        if (!parent) throw new Error("[Party] joinActor: no Secretary at " + opts.parentSecretary);

        var reactorMap = new Map();
        var reactors = opts.reactors || {};
        for (var kind in reactors) {
            if (Object.prototype.hasOwnProperty.call(reactors, kind)) {
                reactorMap.set(kind, reactors[kind]);
            }
        }
        this._actors.set(opts.id, { reactors: reactorMap });
        parent.members.add(opts.id);
    }

    leave(actorId) {
        if (!this._actors.has(actorId)) return;
        this._actors.delete(actorId);
        // The Actor lived under exactly one Secretary (its parentSecretary
        // at join time); the membership set must drop it. Linear scan is
        // fine — workspace-scale Secretary counts are small.
        this._secretaries.forEach(function (sec) { sec.members.delete(actorId); });
    }

    // ─── Dispatch entry points ──────────────────────────────────────────────

    /**
     * An Actor sends a message UP to its parent Secretary. The framework
     * looks up which Secretary owns this Actor (membership scan), wraps the
     * message in an envelope tagged with the sender's id, and dispatches.
     */
    tellFrom(fromPath, message) {
        if (!fromPath) throw new Error("[Party] tellFrom: fromPath required");
        if (message == null) throw new Error("[Party] tellFrom: message required");
        var parent = this._findParentSecretary(fromPath);
        if (!parent) {
            console.warn("[Party " + this._name + "] tellFrom: no parent Secretary for " + fromPath);
            return;
        }
        this._dispatchToSecretary(parent, { from: fromPath, message: message });
    }

    _findParentSecretary(memberPath) {
        var found = null;
        this._secretaries.forEach(function (sec) {
            if (found) return;
            if (sec.members.has(memberPath)) found = sec;
        });
        return found;
    }

    // ─── Core dispatch loop ─────────────────────────────────────────────────

    _dispatchToSecretary(sec, envelope) {
        var step;
        try {
            step = sec.behavior(sec.state, envelope);
        } catch (t) {
            console.error("[Party " + this._name + "] secretary " + sec.path + " threw on "
                + envelope.message.kind + ":", t);
            return;   // state unchanged, no actions propagated
        }
        if (!step || typeof step !== "object") {
            console.warn("[Party " + this._name + "] secretary " + sec.path
                + " returned non-object — ignoring");
            return;
        }
        // newState may be the same reference (no-op); commit unconditionally.
        sec.state = step.newState;
        var actions = step.actions || [];
        for (var i = 0; i < actions.length; i++) {
            this._executeAction(sec, actions[i]);
        }
    }

    _executeAction(originSec, action) {
        if (!action || !action.kind) {
            console.warn("[Party " + this._name + "] action missing kind:", action);
            return;
        }
        switch (action.kind) {
            case "SendToParent": {
                if (!originSec.parentPath) return;   // root secretary — no parent to send up to
                var parent = this._secretaries.get(originSec.parentPath);
                if (!parent) return;
                this._dispatchToSecretary(parent,
                    { from: originSec.path, message: action.message });
                return;
            }
            case "BroadcastToMembers": {
                var self = this;
                originSec.members.forEach(function (memberPath) {
                    self._deliverToMember(memberPath,
                        { from: originSec.path, message: action.message });
                });
                return;
            }
            case "SendToMember": {
                this._deliverToMember(action.to,
                    { from: originSec.path, message: action.message });
                return;
            }
            default:
                console.warn("[Party " + this._name + "] unknown action kind: " + action.kind);
        }
    }

    /**
     * A member is either an Actor (leaf — invoke its reactor for the message
     * kind, if any) or a sub-Secretary (internal — re-enter dispatch). The
     * per-member try/catch lives here so that one ill-behaved reactor cannot
     * break delivery to siblings.
     */
    _deliverToMember(memberPath, envelope) {
        if (this._actors.has(memberPath)) {
            var actor = this._actors.get(memberPath);
            var handler = actor.reactors.get(envelope.message.kind);
            if (!handler) return;   // no reactor for this kind — drop silently (by design)
            try {
                handler(envelope.message, envelope.from);
            } catch (t) {
                console.error("[Party " + this._name + "] actor " + memberPath
                    + " reactor for " + envelope.message.kind + " threw:", t);
            }
            return;
        }
        if (this._secretaries.has(memberPath)) {
            try {
                this._dispatchToSecretary(this._secretaries.get(memberPath), envelope);
            } catch (t) {
                console.error("[Party " + this._name + "] sub-secretary " + memberPath
                    + " dispatch threw:", t);
            }
            return;
        }
        console.warn("[Party " + this._name + "] _deliverToMember: unknown member " + memberPath);
    }

    // ─── Inspection ─────────────────────────────────────────────────────────

    /**
     * Snapshot the whole Party — every Secretary's state + members and every
     * Actor's registered reactor kinds. Read-only; for dev-tools surfaces.
     */
    inspect() {
        var snapshot = { name: this._name, secretaries: [], actors: [] };
        this._secretaries.forEach(function (sec) {
            snapshot.secretaries.push({
                path       : sec.path,
                parentPath : sec.parentPath,
                state      : sec.state,
                members    : Array.from(sec.members)
            });
        });
        this._actors.forEach(function (actor, id) {
            snapshot.actors.push({
                id       : id,
                reactors : Array.from(actor.reactors.keys())
            });
        });
        return snapshot;
    }

    /** Workspace-local Party name (for logging / inspection display). */
    get name() { return this._name; }
}
