// =============================================================================
// ContextMenuSteward — one per page: the registry of kinds, the one instance
// per kind, the layer they show in, and the one menu active — the
// fundamental invariant. Lazy: it mints nothing at construction, a kind's
// instance at the kind's first open, the layer at the first open of any, and
// listens to nothing while no menu is open. A branch component: the page's
// holder makes a sub-branch for it and hands it in, and hands the steward
// to the components that offer menus.
//
//   new ContextMenuSteward(branch, { types?, onEvent? })
//     types    kind → { kind, nodes }: a Java ContextMenuRegistry's MENUS
//   steward.define(kind)             a kind declared in JS, the same record, held
//                                    to the same rules (MenuTree.check):
//       .row(id, label, { icon?, hint? }?)   a row; icon is an Icon word
//       .divider()                           the rows after are a new section
//       .sub(id, label, opts?, sub => sub.row(...))   a row with rows, three levels at most
//       .done() → the steward
//   steward.specimen(kind, host, object?) → a static instance of the kind mounted
//                                    in the host, every level open, bound to the
//                                    object through the kind's handler; for a gallery
//   steward.undefine(kind)
//   steward.has(kind)   steward.kinds()
//   steward.handle(kind, { pick?(itemId, object), state?(itemId, object), close?(reason, object) })
//   steward.open(kind, object, at, { keyboard?, anchor? }?) → true when taken:
//       the kind is known; the component prevents the browser's menu on true
//       only. anchor is the element the menu is for, when there is one: a
//       scroll of a box holding it closes the menu, as a scroll of the page does
//   steward.close(reason?)           by the owner; reason defaults to "owner"
//   steward.bound()                  the object while a menu is open, else null
//   steward.active()                 the kind open, else null
//   steward.dispose()
//
// open closes whatever is open (replaced), binds the object to the kind's
// instance, shows it at the point the geometry gives, takes the focus and
// installs the listeners. A press outside closes it (outside) and is
// swallowed whole — the press, and the release and click of the same
// gesture — so nothing under the pointer is acted on; the contextmenu that
// follows a right-press is let through, so the next menu opens in the same
// gesture. Escape closes (escape); a scroll of the page, or of a box that
// holds the anchor, closes (scroll) — a scroll elsewhere, a log filling
// behind the menu, does not; a resize closes (scroll); the window losing
// focus closes (blur). Keys are captured while open — the
// page behind sees none — and Tab is held. Closing returns the focus to
// what had it. Every open, pick and close is one MenuEvents object on the
// sink; a pick is Picked then Closed(pick), and the handler acts last, so
// what it opens keeps the focus. `css` is injected with the styles import.
// =============================================================================

const _stewardOwner = Object.freeze({ toString: () => "contextMenus" });
var _pages = new WeakSet();          // the documents that have a steward: one each
var _SWALLOW_MS = 1000;              // a swallowed gesture that never releases is forgotten after this

class ContextMenuSteward {
    constructor(branch, opts) {
        if (!branch) throw new Error("[ContextMenuSteward] a branch of its own is required");
        if (typeof document !== "undefined") {
            if (_pages.has(document)) throw new Error("[ContextMenuSteward] this page has a steward already: one per page");
            _pages.add(document);
        }
        var self = this;
        branch.activate(_stewardOwner);
        this._branch = branch;
        this._sink = opts && typeof opts.onEvent === "function" ? opts.onEvent : null;
        this._types = {};
        var given = opts && opts.types ? opts.types : {};
        for (var k in given) if (Object.prototype.hasOwnProperty.call(given, k)) this._types[k] = given[k];
        this._handlers = {};
        this._menus = {};
        this._specimens = [];
        this._specimenCount = 0;
        this._layer = null;
        this._kind = null;
        this._menu = null;
        this._object = null;
        this._restoreTo = null;
        this._anchor = null;
        this._onPress = function (e) { self._press(e); };
        this._onKey = function (e) { self._key(e); };
        this._onScroll = function (e) { if (self._scrollMoves(e.target)) self._close("scroll"); };
        this._onResize = function () { self._close("scroll"); };
        this._onBlur = function () { self._close("blur"); };
    }

    // ── the registry ──────────────────────────────────────────────────────
    define(kind) {
        var self = this, nodes = [];
        if (this._types[kind]) throw new Error("[ContextMenuSteward] kind declared twice: " + kind);
        function node(id, label, o, section) {
            var n = { id: id, label: label, section: section };
            if (o && o.icon) n.icon = o.icon;
            if (o && o.hint) n.hint = o.hint;
            return n;
        }
        function into(list, depth) {
            var section = 0;
            var b = {
                row: function (id, label, o) { list.push(node(id, label, o, section)); return b; },
                divider: function () { section++; return b; },
                sub: function (id, label, o, fill) {
                    if (typeof o === "function") { fill = o; o = null; }
                    if (depth >= MenuTree.MAX_DEPTH) throw new Error("[ContextMenuSteward] " + kind + ": " + id + " is at the last level and lists rows");
                    var n = node(id, label, o, section);
                    n.nodes = [];
                    fill(into(n.nodes, depth + 1));
                    list.push(n);
                    return b;
                }
            };
            return b;
        }
        var b = into(nodes, 1);
        b.done = function () { MenuTree.check(kind, nodes); self._types[kind] = { kind: kind, nodes: nodes }; return self; };
        return b;
    }
    specimen(kind, host, object) {
        if (!this.has(kind)) throw new Error("[ContextMenuSteward] no kind '" + kind + "'");
        var h = this._handlers[kind] || {};
        var sub = this._branch.createBranch("specimen-" + kind.replace(/[^A-Za-z0-9_-]/g, "_") + "-" + (++this._specimenCount));
        var menu = new ContextMenu(sub, this._types[kind], {}, { specimen: true });
        css.addClass(host, cm_specimen);
        menu.bind(object === undefined ? null : object, h.state ? function (id, o) { return h.state(id, o); } : null);
        menu.mount(host);
        this._specimens.push(menu);
        return menu;
    }
    undefine(kind) {
        if (this._kind === kind) this._close("owner");
        if (this._menus[kind]) { this._menus[kind].dispose(); delete this._menus[kind]; }
        delete this._types[kind];
        delete this._handlers[kind];
    }
    has(kind) { return Object.prototype.hasOwnProperty.call(this._types, kind); }
    kinds() { return Object.keys(this._types); }
    handle(kind, handler) { this._handlers[kind] = handler || {}; return this; }

    // ── one menu open ─────────────────────────────────────────────────────
    open(kind, object, at, opts) {
        if (!this.has(kind)) return false;
        if (this._menu) this._close("replaced");
        var menu = this._menuFor(kind), h = this._handlers[kind] || {};
        this._kind = kind;
        this._menu = menu;
        this._object = object;
        this._restoreTo = document.activeElement;
        this._anchor = opts && opts.anchor ? opts.anchor : null;
        menu.bind(object, h.state ? function (id, o) { return h.state(id, o); } : null);
        var layer = this._layerEl();
        document.body.appendChild(layer);
        var p = menu.show(layer, { x: at.x, y: at.y }, { w: window.innerWidth, h: window.innerHeight }, !!(opts && opts.keyboard));
        this._listen(true);
        this._fire(MenuEvents.Opened(kind, p.x, p.y));
        return true;
    }
    close(reason) { if (this._menu) this._close(reason || "owner"); }
    bound() { return this._object; }
    active() { return this._kind; }

    _close(reason) {
        if (!this._menu) return;
        var kind = this._kind, menu = this._menu, object = this._object, h = this._handlers[kind] || {};
        this._listen(false);
        menu.hide();
        menu.unbind();
        if (this._layer && this._layer.parentNode) this._layer.parentNode.removeChild(this._layer);
        this._menu = null; this._kind = null; this._object = null; this._anchor = null;
        var back = this._restoreTo;
        this._restoreTo = null;
        if (back && back.isConnected !== false && typeof back.focus === "function") { try { back.focus({ preventScroll: true }); } catch (e) {} }
        if (typeof h.close === "function") { try { h.close(reason, object); } catch (e) { console.error("[ContextMenuSteward] close handler threw:", e); } }
        this._fire(MenuEvents.Closed(kind, reason));
    }
    _pick(itemId) {
        var kind = this._kind, object = this._object, h = this._handlers[kind] || {};
        this._fire(MenuEvents.Picked(kind, itemId));
        this._close("pick");
        if (typeof h.pick === "function") { try { h.pick(itemId, object); } catch (e) { console.error("[ContextMenuSteward] pick handler threw:", e); } }
    }

    // ── minted once, lazily ───────────────────────────────────────────────
    _menuFor(kind) {
        var self = this;
        if (!this._menus[kind]) {
            var sub = this._branch.createBranch("type-" + kind.replace(/[^A-Za-z0-9_-]/g, "_"));
            this._menus[kind] = new ContextMenu(sub, this._types[kind], { pick: function (id) { self._pick(id); } });
        }
        return this._menus[kind];
    }
    _layerEl() {
        if (!this._layer) {
            this._layer = this._branch.createElement("layer", "div");
            css.addClass(this._layer, cm_layer);
        }
        return this._layer;
    }

    // ── while open: the document and the window ───────────────────────────
    _listen(on) {
        var f = on ? "addEventListener" : "removeEventListener";
        document[f]("pointerdown", this._onPress, true);
        document[f]("keydown", this._onKey, true);
        document[f]("scroll", this._onScroll, true);
        window[f]("resize", this._onResize);
        window[f]("blur", this._onBlur);
    }
    /** Whether a scroll of this target moves what the menu was opened for: the page itself, or a box that holds the anchor. */
    _scrollMoves(target) {
        if (!this._menu || this._menu.contains(target)) return false;
        if (target === document || target === document.documentElement || target === document.body) return true;
        return !!(this._anchor && target && typeof target.contains === "function" && target.contains(this._anchor));
    }
    _press(e) {
        if (this._menu && this._menu.contains(e.target)) return;
        this._close("outside");
        e.stopPropagation();
        e.preventDefault();
        ContextMenuSteward._swallow(e.pointerId);
    }
    /** The rest of the gesture that closed a menu: its release and its click reach nothing. */
    static _swallow(pointerId) {
        var done = false, timer = null;
        function off() {
            if (done) return;
            done = true;
            if (timer) clearTimeout(timer);
            ["pointerup", "mouseup", "click", "dblclick"].forEach(function (t) { document.removeEventListener(t, stop, true); });
        }
        function stop(e) {
            if (e.pointerId != null && pointerId != null && e.pointerId !== pointerId) return;
            e.stopPropagation();
            e.preventDefault();
            if (e.type === "pointerup") setTimeout(off, 0);      // the click of this release is still to come, in this task
        }
        ["pointerup", "mouseup", "click", "dblclick"].forEach(function (t) { document.addEventListener(t, stop, true); });
        timer = setTimeout(off, _SWALLOW_MS);
    }
    _key(e) {
        if (!this._menu) return;
        if (e.key === "Escape") { e.preventDefault(); e.stopPropagation(); this._close("escape"); return; }
        if (this._menu.key(e) || e.key === "Tab") { e.preventDefault(); e.stopPropagation(); }
    }
    _fire(ev) {
        if (!this._sink) return;
        try { this._sink(ev); } catch (e) { console.error("[ContextMenuSteward] onEvent threw on " + ev.kind + ":", e); }
    }

    dispose() {
        if (this._menu) this._close("owner");
        for (var k in this._menus) if (Object.prototype.hasOwnProperty.call(this._menus, k)) this._menus[k].dispose();
        this._menus = {};
        for (var s = 0; s < this._specimens.length; s++) this._specimens[s].dispose();
        this._specimens = [];
        if (typeof document !== "undefined") _pages.delete(document);
        this._branch.dissolve();
    }
}
