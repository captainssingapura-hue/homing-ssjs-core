// =============================================================================
// ModalModule — third workspace primitive. A standalone draggable, resizable
// floating panel. Self-contained: no dependency on SplitPane or MultiTabPane.
//
// Reusable beyond the workspace: settings dialogs, tool palettes, picture-
// in-picture viewers, detached widgets, custom confirmation prompts. The
// MultiTabPane's detach-to-modal feature is one consumer; this primitive
// has no awareness of tabs.
//
// API:
//   new Modal({ container, title?, content?, x?, y?, width?, height?,
//               minWidth?, minHeight?, resizable?, closable?, bounds?,
//               onClose?, onMove?, onResize?, onFocus? })
//     .el                        — root DOM element
//     .setTitle(s) .setContent(el)
//     .moveTo(x, y) .resize(w, h)
//     .open() .close() .toggle() .isOpen()
//     .destroy()
//
// Container constraint: mouse clientX/Y maps directly to CSS left/top, so
// the container must have its origin at the viewport origin. Use
// document.body as the container; nested positioned containers produce a
// first-move jump equal to the container's offset.
// =============================================================================

var _STYLE_TAG_ID = "homing-modal-style";
// Theme-aware modal styling — every surface, text, and border reads from
// the framework's --color-* CSS variables so the modal blends with the
// active theme. Box-shadow stays a fixed dark tint — it's depth feedback
// (the modal floats above content) and reads consistently across themes.
var _STYLE_CSS = [
    ".hmd-panel{position:absolute;display:flex;flex-direction:column;",
    "  background:var(--color-surface);color:var(--color-text-primary);",
    "  border:1px solid var(--color-border);border-radius:6px;",
    "  box-shadow:0 8px 24px rgba(0,0,0,0.32),0 2px 6px rgba(0,0,0,0.20);",
    "  overflow:hidden;font-family:sans-serif;}",
    ".hmd-titlebar{display:flex;align-items:center;height:28px;padding:0 10px;",
    "  background:var(--color-surface-raised);",
    "  border-bottom:1px solid var(--color-border);",
    "  cursor:grab;user-select:none;flex-shrink:0;}",
    ".hmd-titlebar:active{cursor:grabbing;}",
    ".hmd-titlelabel{flex:1;font:600 11px sans-serif;letter-spacing:0.4px;",
    "  text-transform:uppercase;color:var(--color-text-muted);",
    "  overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}",
    ".hmd-close{cursor:pointer;font:16px sans-serif;line-height:1;",
    "  color:var(--color-text-muted);padding:0 4px;}",
    ".hmd-close:hover{color:var(--color-accent-emphasis);}",
    ".hmd-body{flex:1;overflow:auto;background:var(--color-surface);",
    "  color:var(--color-text-primary);}",
    ".hmd-handle{position:absolute;}",
    ".hmd-handle-n{top:0;left:6px;right:6px;height:6px;cursor:ns-resize;}",
    ".hmd-handle-s{bottom:0;left:6px;right:6px;height:6px;cursor:ns-resize;}",
    ".hmd-handle-e{right:0;top:6px;bottom:6px;width:6px;cursor:ew-resize;}",
    ".hmd-handle-w{left:0;top:6px;bottom:6px;width:6px;cursor:ew-resize;}",
    ".hmd-handle-ne{top:0;right:0;width:6px;height:6px;cursor:nesw-resize;}",
    ".hmd-handle-nw{top:0;left:0;width:6px;height:6px;cursor:nwse-resize;}",
    ".hmd-handle-se{bottom:0;right:0;width:6px;height:6px;cursor:nwse-resize;}",
    ".hmd-handle-sw{bottom:0;left:0;width:6px;height:6px;cursor:nesw-resize;}",
    "body.hmd-dragging{cursor:grabbing!important;user-select:none;}",
    "body.hmd-resizing{user-select:none;}"
].join("\n");

function _ensureStyles() {
    if (document.getElementById(_STYLE_TAG_ID)) return;
    var s = document.createElement("style");
    s.id = _STYLE_TAG_ID;
    s.textContent = _STYLE_CSS;
    document.head.appendChild(s);
}

var _HANDLE_POSITIONS = ["n", "s", "e", "w", "ne", "nw", "se", "sw"];

class Modal {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[Modal] container is required");
        _ensureStyles();

        this._container = opts.container;
        this._minW      = opts.minWidth  != null ? opts.minWidth  : 120;
        this._minH      = opts.minHeight != null ? opts.minHeight : 60;
        this._bounds    = opts.bounds   || null;
        this._onClose   = opts.onClose  || null;
        this._onMove    = opts.onMove   || null;
        this._onResize  = opts.onResize || null;
        this._onFocus   = opts.onFocus  || null;
        this._listeners = [];
        this._open      = true;

        // Build DOM
        var el = document.createElement("div");
        el.className = "hmd-panel";
        el.style.left   = (opts.x != null ? opts.x : 60) + "px";
        el.style.top    = (opts.y != null ? opts.y : 60) + "px";
        el.style.width  = (opts.width  != null ? opts.width  : 380) + "px";
        el.style.height = (opts.height != null ? opts.height : 260) + "px";
        this._el = el;

        // Title bar
        var bar = document.createElement("div");
        bar.className = "hmd-titlebar";
        var label = document.createElement("span");
        label.className = "hmd-titlelabel";
        label.textContent = opts.title || "Panel";
        bar.appendChild(label);
        this._titleBar = bar;
        this._titleLabel = label;

        var self = this;
        if (opts.closable !== false) {
            var x = document.createElement("span");
            x.className = "hmd-close";
            x.textContent = "×";
            this._on(x, "click", function (e) {
                e.stopPropagation();
                self.close();
                if (self._onClose) self._onClose();
            });
            bar.appendChild(x);
        }
        el.appendChild(bar);

        // Body
        var body = document.createElement("div");
        body.className = "hmd-body";
        if (opts.content) body.appendChild(opts.content);
        this._body = body;
        el.appendChild(body);

        // Resize handles
        if (opts.resizable !== false) {
            for (var i = 0; i < _HANDLE_POSITIONS.length; i++) {
                var pos = _HANDLE_POSITIONS[i];
                var h = document.createElement("div");
                h.className = "hmd-handle hmd-handle-" + pos;
                (function (p) {
                    self._on(h, "mousedown", function (e) { self._onResizeStart(e, p); });
                })(pos);
                el.appendChild(h);
            }
        }

        // Events
        this._on(bar, "mousedown", function (e) { self._onDragStart(e); });
        this._on(el, "mousedown", function ()  { if (self._onFocus) self._onFocus(); });

        // Mount
        this._container.appendChild(el);
    }

    // ─── Public API ──────────────────────────────────────────────────────

    get el() { return this._el; }

    setTitle(s)     { this._titleLabel.textContent = s; return this; }

    setContent(el)  {
        this._body.innerHTML = "";
        if (el) this._body.appendChild(el);
        return this;
    }

    moveTo(x, y) {
        if (this._bounds) {
            var br = this._bounds.getBoundingClientRect();
            var w  = this._el.offsetWidth  || 0;
            var h  = this._el.offsetHeight || 0;
            x = Math.max(br.left, Math.min(x, br.right  - w));
            y = Math.max(br.top,  Math.min(y, br.bottom - h));
        }
        this._el.style.left = x + "px";
        this._el.style.top  = y + "px";
        if (this._onMove) this._onMove(x, y);
        return this;
    }

    resize(w, h) {
        this._el.style.width  = Math.max(w, this._minW) + "px";
        this._el.style.height = Math.max(h, this._minH) + "px";
        if (this._onResize) this._onResize(w, h);
        return this;
    }

    open()    { this._open = true;  this._el.style.display = "";     if (this._onFocus) this._onFocus(); return this; }
    close()   { this._open = false; this._el.style.display = "none"; return this; }
    toggle()  { return this._open ? this.close() : this.open(); }
    isOpen()  { return this._open; }

    destroy() {
        for (var i = 0; i < this._listeners.length; i++) {
            var L = this._listeners[i];
            L[0].removeEventListener(L[1], L[2], L[3]);
        }
        this._listeners = [];
        if (this._el && this._el.parentNode) this._el.parentNode.removeChild(this._el);
        this._el = null;
    }

    // ─── Private ─────────────────────────────────────────────────────────

    _on(target, type, fn, opts) {
        target.addEventListener(type, fn, opts);
        this._listeners.push([target, type, fn, opts]);
    }

    _onDragStart(e) {
        if (e.button !== 0) return;
        e.preventDefault();
        var self = this;
        var rect = this._el.getBoundingClientRect();
        var dx = e.clientX - rect.left;
        var dy = e.clientY - rect.top;

        var onMove = function (ev) {
            var x = ev.clientX - dx;
            var y = ev.clientY - dy;
            if (self._bounds) {
                var br = self._bounds.getBoundingClientRect();
                x = Math.max(br.left, Math.min(x, br.right  - self._el.offsetWidth));
                y = Math.max(br.top,  Math.min(y, br.bottom - self._el.offsetHeight));
            }
            self._el.style.left = x + "px";
            self._el.style.top  = y + "px";
        };
        var onUp = function () {
            document.removeEventListener("mousemove", onMove);
            document.removeEventListener("mouseup",   onUp);
            document.body.classList.remove("hmd-dragging");
            if (self._onMove) self._onMove(parseFloat(self._el.style.left),
                                           parseFloat(self._el.style.top));
        };
        document.addEventListener("mousemove", onMove);
        document.addEventListener("mouseup",   onUp);
        document.body.classList.add("hmd-dragging");
    }

    _onResizeStart(e, pos) {
        if (e.button !== 0) return;
        e.preventDefault();
        e.stopPropagation();
        var self = this;
        var rect = this._el.getBoundingClientRect();
        var orig = { x: rect.left, y: rect.top, w: rect.width, h: rect.height,
                     mx: e.clientX, my: e.clientY };

        var onMove = function (ev) {
            var dx = ev.clientX - orig.mx;
            var dy = ev.clientY - orig.my;
            var x = orig.x, y = orig.y, w = orig.w, h = orig.h;
            if (pos.indexOf("e") >= 0) w = orig.w + dx;
            if (pos.indexOf("w") >= 0) { w = orig.w - dx; x = orig.x + dx; }
            if (pos.indexOf("s") >= 0) h = orig.h + dy;
            if (pos.indexOf("n") >= 0) { h = orig.h - dy; y = orig.y + dy; }
            if (w < self._minW) { if (pos.indexOf("w") >= 0) x = orig.x + orig.w - self._minW; w = self._minW; }
            if (h < self._minH) { if (pos.indexOf("n") >= 0) y = orig.y + orig.h - self._minH; h = self._minH; }
            self._el.style.left   = x + "px";
            self._el.style.top    = y + "px";
            self._el.style.width  = w + "px";
            self._el.style.height = h + "px";
        };
        var onUp = function () {
            document.removeEventListener("mousemove", onMove);
            document.removeEventListener("mouseup",   onUp);
            document.body.classList.remove("hmd-resizing");
            if (self._onResize) self._onResize(parseFloat(self._el.style.width),
                                               parseFloat(self._el.style.height));
        };
        document.addEventListener("mousemove", onMove);
        document.addEventListener("mouseup",   onUp);
        document.body.classList.add("hmd-resizing");
    }
}
