// =============================================================================
// Docking — the protocol between a desk and its docks. A branch component: the
// caller makes a sub-branch for it and hands it in; it owns the desk, as a
// layer over the docks' host, and a dock is a multi-tab pane given to it.
//
//   new Docking(branch, { host, onEvent?, minW?, minH?, keyboard?, keyboardId? })
//     keyboard  the page's KeyboardSteward, handed on to the desk, which joins as
//               keyboardId (the desk's branch's name, unless said)
//     host   a positioned box holding the docks; the desk lies over it, the
//            hand passing through except on a floating pane. A pane floats
//            only within it.
//
//   docking.desk                 the desk; open a floating tab on it as usual
//   docking.addDock(pane)        a multi-tab pane becomes a dock
//   docking.removeDock(pane)
//   docking.undock(dock, tab, e, grab?)
//                                by call, with a pointer event: the tab leaves the
//                                dock, NOT disposed, and floats under the hand —
//                                the pane opens where the pointer is, its head
//                                under the hand at the grab, an offset within the
//                                chip, when given — and takes the drag over. The
//                                strip does not pull a tab off yet; a holder may.
//                                Undocked(tabId, slotId), after the desk's Opened
//   docking.undockAt(dock, tab, at)
//                                by call, with no hand — a menu's pick: the tab
//                                leaves the dock, NOT disposed, and floats with
//                                its head at the point, within the desk; nothing
//                                is grabbed. Undocked(tabId, slotId), after Opened
//   docking.dock(paneId, dock, index?)
//                                the floating tab leaves the desk (Released) and
//                                is attached to the dock (TabAttached), then
//                                Docked(tabId, slotId, index)
//   docking.dispose()            the desk and everything on it; the docks stay
//
// The hand: while a floating pane is dragged, every dock under the pointer is
// offered the tab — the dock wears the drop-target word and marks where the
// tab would land, over its strip at the mark, over its content at the end —
// and the one still under the pointer when the hand lets go takes it. The
// dock's events and the desk's come through the same sink as Docking's own.
// =============================================================================

const _dockingOwner = Object.freeze({ toString: () => "docking" });
var _GRIP_X = 60, _GRIP_Y = 14;      // where the head sits under the hand that pulled the tab
var _FLOAT_W = 320, _FLOAT_H = 220;

class Docking {
    constructor(branch, opts) {
        if (!branch) throw new Error("[Docking] a branch of its own is required");
        if (!opts || !opts.host) throw new Error("[Docking] opts.host is required");
        var self = this;
        branch.activate(_dockingOwner);
        this.branch = branch;
        this._sink = typeof opts.onEvent === "function" ? opts.onEvent : null;
        this._docks = [];
        this._target = null;
        this._index = -1;
        this.desk = new Desk(branch.createBranch("desk"), {
            host: opts.host, layer: true, minW: opts.minW, minH: opts.minH, keyboard: opts.keyboard, keyboardId: opts.keyboardId,
            onEvent: function (ev) { self._fire(ev); },
            onDragMove: function (pane, x, y) { self._offer(x, y); },
            onDragEnd: function (pane, x, y, ok) { self._drop(pane, ok); }
        });
    }

    addDock(pane) { if (this._docks.indexOf(pane) < 0) this._docks.push(pane); return this; }
    removeDock(pane) { var i = this._docks.indexOf(pane); if (i >= 0) this._docks.splice(i, 1); return this; }
    docks() { return this._docks.slice(); }

    /** A chip pulled off a dock's strip: the tab floats under the same hand. */
    undock(dock, tab, e, grab) {
        var t = dock.detachTab(tab.id);
        var r = Docking._rect(this.desk.root);
        var gx = grab && grab.x >= 0 ? grab.x : _GRIP_X, gy = grab && grab.y >= 0 ? grab.y : _GRIP_Y;
        var pane = this.desk.open({
            id: t.id, title: t.title == null ? t.id : t.title, widget: t.widget, closable: t.closable !== false,
            x: e.clientX - r.left - gx, y: e.clientY - r.top - gy, w: _FLOAT_W, h: _FLOAT_H
        });
        this._fire(DockEvents.Undocked(t.id, dock.slotId));
        pane.grab(e.pointerId, e.clientX, e.clientY);
        return pane;
    }

    /** A tab detached by call with no hand — a menu's pick — floating with its head at the point, kept within the desk. */
    undockAt(dock, tab, at) {
        var t = dock.detachTab(tab.id);
        var r = Docking._rect(this.desk.root);
        var x = Math.max(0, (at && at.x != null ? at.x : r.left) - r.left - _GRIP_X);
        var y = Math.max(0, (at && at.y != null ? at.y : r.top) - r.top - _GRIP_Y);
        if (r.width) x = Math.min(x, Math.max(0, r.width - _FLOAT_W));
        if (r.height) y = Math.min(y, Math.max(0, r.height - _FLOAT_H));
        var pane = this.desk.open({
            id: t.id, title: t.title == null ? t.id : t.title, widget: t.widget, closable: t.closable !== false,
            x: x, y: y, w: _FLOAT_W, h: _FLOAT_H
        });
        this._fire(DockEvents.Undocked(t.id, dock.slotId));
        return pane;
    }

    /** A floating tab into a dock, at an index or the end. */
    dock(paneId, dock, index) {
        var tab = this.desk.release(paneId);
        if (!tab) throw new Error("[Docking] no floating pane '" + paneId + "'");
        var at = dock.attachTab(tab, index == null ? null : index);
        this._fire(DockEvents.Docked(tab.id, dock.slotId, at));
        return at;
    }

    dispose() {
        this.desk.dispose();
        this._docks = [];
        try { this.branch.dissolve(); } catch (e) {}
    }

    // ── the offer under the hand ────────────────────────────────────────────
    _offer(x, y) {
        var target = null, index = -1;
        for (var i = 0; i < this._docks.length; i++) {
            var at = target ? -1 : this._docks[i].dropAt(x, y);
            if (at >= 0) { target = this._docks[i]; index = at; }
            else this._docks[i].dropClear();
        }
        this._target = target;
        this._index = index;
    }
    _drop(pane, ok) {
        var target = this._target, index = this._index;
        for (var i = 0; i < this._docks.length; i++) this._docks[i].dropClear();
        this._target = null;
        this._index = -1;
        if (ok && target) this.dock(pane.id, target, index);
    }

    static _rect(el) {
        return el && typeof el.getBoundingClientRect === "function" ? el.getBoundingClientRect() : { left: 0, top: 0, width: 0, height: 0 };
    }

    _fire(ev) {
        if (!this._sink) return;
        try { this._sink(ev); }
        catch (e) { console.error("[Docking] onEvent threw on " + ev.kind + ":", e); }
    }
}
