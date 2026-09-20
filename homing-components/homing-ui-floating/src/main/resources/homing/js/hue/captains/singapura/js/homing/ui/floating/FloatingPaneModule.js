// =============================================================================
// FloatingPane — one pane raised above the desk: a head that names it and
// moves it, a body that holds what it shows, a grip that sizes it. A branch
// component: the desk makes a sub-branch for it and hands it in; dispose()
// dissolves it. Container.Pane.Floating to the design — a container's corner,
// rule and ring, the overlay's shadow, the pane's air on its head — and a
// larger, movable card in the hand: its place and its measure are its user's,
// carried in --fp-x, --fp-y, --fp-w, --fp-h on the frame, never the design's.
//
//   new FloatingPane(branch, { id, title, x, y, w, h, z, closable?, onEvent?, minW?, minH? })
//     branch   the pane's own, handed unactivated
//     x, y     its place within the desk, in px; w, h its measure; z its place on the stack
//
//   pane.root                the frame, appended by the desk; focusable (tabindex −1)
//   pane.body                the bounded region a widget's root goes in
//   pane.head                the bar; the desk listens on it and on the frame
//   pane.id
//   pane.title(text?)        read, or set
//   pane.moveTo(x, y)        clamped to the desk; reports Moved when it changed
//   pane.resizeTo(w, h)      clamped to the least and the desk; reports Resized when it changed
//   pane.bounds()            { x, y, w, h }
//   pane.raise(z)            its place on the stack; the desk's to call
//   pane.setActive(on)       the ring drawn now, on the active one
//   pane.size(s)             the size axis, on the head and its parts
//   pane.dispose()           dissolves the branch
//
// The hand: a press on the head and a drag moves it, live, and reports Moved
// once when it lets go; the head is lit by extent while held. A press on the
// grip and a drag sizes it, live, and reports Resized once. A press on the
// cross reports nothing: it asks the desk, through `onClose`, to close it.
// Every report is one FloatEvents object on one sink, onEvent(ev).
// =============================================================================

const _paneOwner = Object.freeze({ toString: () => "floatingPane" });
var _HELD = 0.6;
var _MIN_W = 160, _MIN_H = 96;
var _KEEP = 48;          // how much of the head must stay within the desk, inline

class FloatingPane {
    constructor(branch, opts) {
        if (!branch) throw new Error("[FloatingPane] a branch of its own is required");
        if (!opts || !opts.id) throw new Error("[FloatingPane] opts.id is required");
        var self = this;
        branch.activate(_paneOwner);
        this.branch = branch;
        this.id = String(opts.id);
        this._sink = typeof opts.onEvent === "function" ? opts.onEvent : null;
        this._onClose = typeof opts.onClose === "function" ? opts.onClose : null;
        this._minW = opts.minW == null ? _MIN_W : Math.max(24, opts.minW | 0);
        this._minH = opts.minH == null ? _MIN_H : Math.max(24, opts.minH | 0);
        this._x = 0; this._y = 0; this._w = this._minW; this._h = this._minH;
        this._size = 0;

        var frame = branch.createElement("frame", "section");
        css.addClass(frame, fp_frame);
        frame.tabIndex = -1;
        frame.setAttribute("role", "region");

        var head = branch.createElement("head", "header");
        css.addClass(head, fp_head);
        this._title = branch.createElement("title", "span");
        css.addClass(this._title, fp_title);
        head.appendChild(this._title);
        this._close = null;
        if (opts.closable !== false) {
            var x = branch.createElement("close", "button");
            x.type = "button";
            css.addClass(x, fp_close);
            x.textContent = "×";
            x.setAttribute("aria-label", "Close");
            x.addEventListener("click", function (e) { e.stopPropagation(); if (self._onClose) self._onClose(self); });
            head.appendChild(x);
            this._close = x;
        }
        frame.appendChild(head);

        var body = branch.createElement("body", "div");
        css.addClass(body, fp_body);
        frame.appendChild(body);

        var grip = branch.createElement("grip", "div");
        css.addClass(grip, fp_grip);
        grip.setAttribute("aria-hidden", "true");
        frame.appendChild(grip);

        this.root = frame;
        this.head = head;
        this.body = body;
        this._parts = [head, this._title].concat(this._close ? [this._close] : []);
        this.title(opts.title == null ? "" : opts.title);
        this.raise(opts.z == null ? 1 : opts.z);
        this._set(opts.x == null ? 0 : opts.x, opts.y == null ? 0 : opts.y, opts.w == null ? 320 : opts.w, opts.h == null ? 220 : opts.h, false);
        this._armMove(head);
        this._armResize(grip);
    }

    title(text) {
        if (text !== undefined) { this._title.textContent = String(text); this.root.setAttribute("aria-label", String(text)); }
        return this._title.textContent;
    }

    bounds() { return { x: this._x, y: this._y, w: this._w, h: this._h }; }

    moveTo(x, y) {
        var before = this._x + "," + this._y;
        this._set(x, y, this._w, this._h, true);
        if (this._x + "," + this._y !== before) this._fire(FloatEvents.Moved(this.id, this._x, this._y));
        return this;
    }

    resizeTo(w, h) {
        var before = this._w + "," + this._h;
        this._set(this._x, this._y, w, h, true);
        if (this._w + "," + this._h !== before) this._fire(FloatEvents.Resized(this.id, this._w, this._h));
        return this;
    }

    raise(z) {
        this._z = z | 0;
        this.root.style.setProperty("--fp-z", String(this._z));
        return this;
    }

    setActive(on) { css.toggleClass(this.root, fp_active, !!on); return this; }

    size(s) {
        var n = Math.max(-1, Math.min(1, Number(s)));
        this._size = Number.isFinite(n) ? n : 0;
        var v = this._size === 0 ? null : this._size;
        this._parts.forEach(function (el) { css.size(el, v); });
        return this;
    }

    dispose() { try { this.branch.dissolve(); } catch (e) {} }

    // ── the frame's place and measure, clamped to the desk it is on ─────────
    _deskSize() {
        var p = this.root.parentNode;
        var w = p && typeof p.clientWidth === "number" ? p.clientWidth : 0;
        var h = p && typeof p.clientHeight === "number" ? p.clientHeight : 0;
        return { w: w > 0 ? w : Infinity, h: h > 0 ? h : Infinity };
    }

    _set(x, y, w, h, clamp) {
        var d = clamp ? this._deskSize() : { w: Infinity, h: Infinity };
        w = Math.max(this._minW, Math.round(Number(w) || 0));
        h = Math.max(this._minH, Math.round(Number(h) || 0));
        if (d.w !== Infinity) w = Math.min(w, d.w);
        if (d.h !== Infinity) h = Math.min(h, d.h);
        x = Math.round(Number(x) || 0);
        y = Math.round(Number(y) || 0);
        if (d.w !== Infinity) x = Math.max(_KEEP - w, Math.min(x, d.w - _KEEP));
        if (d.h !== Infinity) y = Math.max(0, Math.min(y, d.h - _KEEP));
        y = Math.max(0, y);
        this._x = x; this._y = y; this._w = w; this._h = h;
        var s = this.root.style;
        s.setProperty("--fp-x", x + "px");
        s.setProperty("--fp-y", y + "px");
        s.setProperty("--fp-w", w + "px");
        s.setProperty("--fp-h", h + "px");
    }

    _fire(ev) { if (this._sink) this._sink(ev); }

    // ── the hand on the head: move ──────────────────────────────────────────
    _armMove(head) {
        var self = this;
        head.addEventListener("pointerdown", function (down) {
            if (down.button !== 0) return;
            if (self._close && (down.target === self._close || (self._close.contains && self._close.contains(down.target)))) return;
            var x0 = self._x, y0 = self._y, cx = down.clientX, cy = down.clientY, moved = false;
            css.addClass(head, fp_head_held);
            css.extent(head, _HELD);
            try { head.setPointerCapture(down.pointerId); } catch (err) {}
            function onMove(e) {
                var nx = x0 + (e.clientX - cx), ny = y0 + (e.clientY - cy);
                self._set(nx, ny, self._w, self._h, true);
                moved = true;
            }
            function onEnd(e) {
                head.removeEventListener("pointermove", onMove);
                head.removeEventListener("pointerup", onEnd);
                head.removeEventListener("pointercancel", onEnd);
                css.removeClass(head, fp_head_held);
                css.extent(head, null);
                try { head.releasePointerCapture(down.pointerId); } catch (err) {}
                if (e.type !== "pointerup") { self._set(x0, y0, self._w, self._h, false); return; }
                if (moved && (self._x !== x0 || self._y !== y0)) self._fire(FloatEvents.Moved(self.id, self._x, self._y));
            }
            head.addEventListener("pointermove", onMove);
            head.addEventListener("pointerup", onEnd);
            head.addEventListener("pointercancel", onEnd);
            if (down.preventDefault) down.preventDefault();
        });
    }

    // ── the hand on the grip: resize ────────────────────────────────────────
    _armResize(grip) {
        var self = this;
        grip.addEventListener("pointerdown", function (down) {
            if (down.button !== 0) return;
            var w0 = self._w, h0 = self._h, cx = down.clientX, cy = down.clientY, sized = false;
            try { grip.setPointerCapture(down.pointerId); } catch (err) {}
            function onMove(e) {
                self._set(self._x, self._y, w0 + (e.clientX - cx), h0 + (e.clientY - cy), true);
                sized = true;
            }
            function onEnd(e) {
                grip.removeEventListener("pointermove", onMove);
                grip.removeEventListener("pointerup", onEnd);
                grip.removeEventListener("pointercancel", onEnd);
                try { grip.releasePointerCapture(down.pointerId); } catch (err) {}
                if (e.type !== "pointerup") { self._set(self._x, self._y, w0, h0, false); return; }
                if (sized && (self._w !== w0 || self._h !== h0)) self._fire(FloatEvents.Resized(self.id, self._w, self._h));
            }
            grip.addEventListener("pointermove", onMove);
            grip.addEventListener("pointerup", onEnd);
            grip.addEventListener("pointercancel", onEnd);
            if (down.preventDefault) down.preventDefault();
        });
    }
}
