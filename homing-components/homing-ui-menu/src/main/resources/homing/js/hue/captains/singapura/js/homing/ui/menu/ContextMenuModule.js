// =============================================================================
// ContextMenu — one kind's instance: the frame, its rows, and the submenu
// frames of its submenu rows, built once on the branch given and kept; bound
// to an object while it is shown, and to none after. The steward makes one
// per kind at the kind's first open and holds it; nothing here is minted
// twice. A branch component: the steward makes a sub-branch for it and
// hands it in.
//
//   new ContextMenu(branch, type, hooks)
//     type   { kind, items }: items { id, label, hint?, separator?, items? }
//     hooks  { pick(itemId) }
//   menu.el                       the frame, role=menu, focusable
//   menu.bind(object, state?)     state(itemId, object) → { disabled?, checked?, hidden? }
//   menu.unbind()   menu.bound()
//   menu.show(layer, at, viewport, fromKeyboard) → { x, y } where it was placed
//   menu.hide()
//   menu.contains(node)           the frame or an open submenu holds the node
//   menu.key(ev) → boolean        arrows, Home, End, Enter, Space, Left, Right
//   menu.dispose()
//
// The cursor is data-highlighted on a row — the word Selectable draws it —
// and moves by MenuGeometry.step, skipping disabled and hidden rows and
// wrapping. A submenu opens beside its row on hover, on Enter and on
// ArrowRight; it closes when another row is hovered or on ArrowLeft. A
// checked row shows its check; the labels align on a blank of the same
// width. `css` is injected with the styles import.
// =============================================================================

const _menuOwner = Object.freeze({ toString: () => "contextMenu" });

class ContextMenu {
    constructor(branch, type, hooks) {
        if (!branch) throw new Error("[ContextMenu] a branch of its own is required");
        branch.activate(_menuOwner);
        this._branch = branch;
        this.kind = type.kind;
        this._hooks = hooks || {};
        this._object = null;
        this._viewport = { w: 0, h: 0 };
        this._open = null;              // the row whose submenu is shown
        this.el = this._frame("frame", type.kind);
        this._rows = this._build(this.el, type.items, "", true);
    }

    _frame(name, label) {
        var f = this._branch.createElement(name, "div");
        css.addClass(f, cm_frame);
        f.setAttribute("role", "menu");
        f.setAttribute("aria-label", label);
        f.setAttribute("tabindex", "-1");
        f.addEventListener("contextmenu", function (e) { e.preventDefault(); });
        return f;
    }
    _build(host, items, prefix, top) {
        var self = this, rows = [];
        for (var i = 0; i < items.length; i++) {
            var it = items[i];
            if (it.separator) {
                var sep = this._branch.createElement(prefix + "sep-" + i, "div");
                css.addClass(sep, cm_separator);
                sep.setAttribute("role", "separator");
                host.appendChild(sep);
                rows.push({ separator: true, el: sep });
                continue;
            }
            var name = prefix + "row-" + it.id.replace(/[^A-Za-z0-9_-]/g, "_");
            var el = this._branch.createElement(name, "div");
            css.addClass(el, cm_item);
            el.setAttribute("role", "menuitem");
            el.setAttribute("tabindex", "-1");
            el.setAttribute("data-id", it.id);
            var check = this._branch.createElement(name + "-check", "span");
            css.addClass(check, cm_item_check);
            check.setAttribute("aria-hidden", "true");
            el.appendChild(check);
            var label = this._branch.createElement(name + "-label", "span");
            css.addClass(label, cm_item_label);
            label.textContent = it.label;
            el.appendChild(label);
            if (it.hint) {
                var hint = this._branch.createElement(name + "-hint", "span");
                css.addClass(hint, cm_item_hint);
                hint.textContent = it.hint;
                el.appendChild(hint);
            }
            var row = { id: it.id, el: el, check: check, sub: null, disabled: false, hidden: false, top: top };
            if (it.items) {
                var arrow = this._branch.createElement(name + "-arrow", "span");
                css.addClass(arrow, cm_item_arrow);
                arrow.setAttribute("aria-hidden", "true");
                arrow.textContent = "▸";
                el.appendChild(arrow);
                el.setAttribute("aria-haspopup", "menu");
                el.setAttribute("aria-expanded", "false");
                var subEl = this._frame(name + "-sub", it.label);
                row.sub = { el: subEl, rows: this._build(subEl, it.items, name + "-", false), cursor: -1 };
            }
            el.addEventListener("pointerenter", function (r) { return function () { self._hover(r); }; }(row));
            el.addEventListener("click", function (r) { return function (e) { e.preventDefault(); self._activate(r); }; }(row));
            host.appendChild(el);
            rows.push(row);
        }
        return rows;
    }

    // ── bound to an object ────────────────────────────────────────────────
    bind(object, state) {
        this._object = object;
        this._apply(this._rows, object, state);
        this._cursor = -1;
        this._closeSub();
    }
    _apply(rows, object, state) {
        for (var i = 0; i < rows.length; i++) {
            var r = rows[i];
            if (r.separator) continue;
            var s = (typeof state === "function" && state(r.id, object)) || {};
            r.disabled = !!s.disabled;
            r.hidden = !!s.hidden;
            css.toggleClass(r.el, cm_item_disabled, r.disabled);
            css.toggleClass(r.el, cm_item_hidden, r.hidden);
            r.el.setAttribute("aria-disabled", r.disabled ? "true" : "false");
            if (s.checked != null) r.el.setAttribute("aria-checked", s.checked ? "true" : "false"); else r.el.removeAttribute("aria-checked");
            r.check.textContent = s.checked ? "✓" : "";
            if (r.sub) this._apply(r.sub.rows, object, state);
        }
    }
    unbind() { this._object = null; this._closeSub(); this._highlight(this._rows, -1); this._cursor = -1; }
    bound() { return this._object; }

    // ── shown and hidden ──────────────────────────────────────────────────
    show(layer, at, viewport, fromKeyboard) {
        this._viewport = viewport;
        layer.appendChild(this.el);
        var r = this.el.getBoundingClientRect();
        var p = MenuGeometry.place(at, { w: r.width, h: r.height }, viewport);
        this.el.style.setProperty("--cm-x", p.x + "px");
        this.el.style.setProperty("--cm-y", p.y + "px");
        try { this.el.focus({ preventScroll: true }); } catch (e) {}
        if (fromKeyboard) this._move(1);
        return p;
    }
    hide() {
        this._closeSub();
        if (this.el.parentNode) this.el.parentNode.removeChild(this.el);
    }
    contains(node) {
        if (!node) return false;
        if (this.el.contains(node)) return true;
        return !!(this._open && this._open.sub.el.contains(node));
    }

    // ── the cursor ────────────────────────────────────────────────────────
    _level() { return this._open ? this._open.sub : null; }
    _highlight(rows, i) {
        for (var k = 0; k < rows.length; k++) if (!rows[k].separator) {
            if (k === i) rows[k].el.setAttribute("data-highlighted", "true"); else rows[k].el.removeAttribute("data-highlighted");
        }
    }
    _enabled(rows) {
        var out = [];
        for (var k = 0; k < rows.length; k++) out.push(!rows[k].separator && !rows[k].disabled && !rows[k].hidden);
        return out;
    }
    _move(dir) {
        var sub = this._level();
        if (sub) { sub.cursor = MenuGeometry.step(this._enabled(sub.rows), sub.cursor, dir); this._highlight(sub.rows, sub.cursor); }
        else { this._cursor = MenuGeometry.step(this._enabled(this._rows), this._cursor, dir); this._highlight(this._rows, this._cursor); }
    }
    _end(last) {
        var sub = this._level(), rows = sub ? sub.rows : this._rows;
        var i = MenuGeometry.step(this._enabled(rows), -1, last ? -1 : 1);
        if (sub) { sub.cursor = i; } else { this._cursor = i; }
        this._highlight(rows, i);
    }
    _current() {
        var sub = this._level();
        return sub ? (sub.cursor >= 0 ? sub.rows[sub.cursor] : null) : (this._cursor >= 0 ? this._rows[this._cursor] : null);
    }
    _hover(row) {
        if (row.top) {
            this._cursor = this._rows.indexOf(row);
            this._highlight(this._rows, this._cursor);
            if (row.sub && !row.disabled) this._openSub(row); else this._closeSub();
        } else if (this._open) {
            this._open.sub.cursor = this._open.sub.rows.indexOf(row);
            this._highlight(this._open.sub.rows, this._open.sub.cursor);
        }
    }
    _activate(row) {
        if (row.disabled) return;
        if (row.sub) { this._openSub(row); this._move(1); return; }
        if (this._hooks.pick) this._hooks.pick(row.id);
    }

    // ── the submenu ───────────────────────────────────────────────────────
    _openSub(row) {
        if (this._open === row) return;
        this._closeSub();
        var layer = this.el.parentNode;
        if (!layer) return;
        layer.appendChild(row.sub.el);
        var a = row.el.getBoundingClientRect(), r = row.sub.el.getBoundingClientRect(), f = this.el.getBoundingClientRect();
        var first = ContextMenu._firstRow(row.sub.rows), inset = first ? first.el.getBoundingClientRect().top - r.top : 0;
        var p = MenuGeometry.beside({ left: f.left, top: a.top, right: f.right, bottom: a.bottom }, { w: r.width, h: r.height }, this._viewport, inset);
        row.sub.el.style.setProperty("--cm-x", p.x + "px");
        row.sub.el.style.setProperty("--cm-y", p.y + "px");
        row.el.setAttribute("aria-expanded", "true");
        row.sub.cursor = -1;
        this._highlight(row.sub.rows, -1);
        this._open = row;
    }
    _closeSub() {
        var row = this._open;
        if (!row) return;
        this._open = null;
        row.el.setAttribute("aria-expanded", "false");
        if (row.sub.el.parentNode) row.sub.el.parentNode.removeChild(row.sub.el);
    }

    // ── the keys, forwarded by the steward ────────────────────────────────
    key(ev) {
        switch (ev.key) {
            case "ArrowDown": this._move(1); return true;
            case "ArrowUp": this._move(-1); return true;
            case "Home": this._end(false); return true;
            case "End": this._end(true); return true;
            case "ArrowRight": { var r = this._current(); if (r && r.top && r.sub && !r.disabled) { this._openSub(r); this._move(1); } return true; }
            case "ArrowLeft": { if (this._open) { var back = this._open; this._closeSub(); this._cursor = this._rows.indexOf(back); this._highlight(this._rows, this._cursor); } return true; }
            case "Enter": case " ": { var c = this._current(); if (c) this._activate(c); return true; }
            default: return false;
        }
    }

    static _firstRow(rows) { for (var k = 0; k < rows.length; k++) if (!rows[k].separator) return rows[k]; return null; }
    dispose() { this.hide(); this._branch.dissolve(); }
}
