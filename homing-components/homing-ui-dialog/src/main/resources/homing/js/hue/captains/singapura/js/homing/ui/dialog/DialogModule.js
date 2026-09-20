// =============================================================================
// Dialog — a frame that owns the screen until dismissed, or, non-modal, one
// that does not. A branch component: the caller makes a sub-branch for the
// dialog's life and hands it in; close dissolves it, so the frame, the scrim
// and whatever the content built go together.
//
//   new Dialog(branch, opts)
//
// opts:
//   content         (required) function(branch, bodyEl) → { onKeydown?, focusEl?, dispose? }
//                   builds the body INTO the dialog's branch; may return a key
//                   handler (ev → bool), the element to focus on open, and a
//                   dispose() called first on close, before the branch dissolves,
//                   for what dissolving cannot release - the widget's word for it
//   title           string
//   modal           boolean, default true — scrim, inert, keyboard capture
//   glow            boolean, default = modal — the focus ring on the frame
//   actions         [{ id, label, primary?, onClick(dialog) }] — omit for no row.
//                   The buttons are the elements' Button: the primary one
//                   primary, the rest plain
//   size            { w?, h? } px; default 1/φ of each viewport axis, clamped
//   restoreFocusTo  element focused on close; default whatever had focus
//   onClose         function() — fires on EVERY close path, exactly once
//
// the instance:
//   el, bodyEl, branch
//   close()
//   actionEl(id)                       the button element, an owned reference
//   setAction(id, { enabled?, label? })
//
// Keys, modal: captured on the document through Modality, so the page behind
// never sees them. The content is asked FIRST, Escape included — a popup
// nested in the dialog takes Escape to close itself, and only an Escape
// nobody inside wanted closes the dialog. Enter outside a form control fires
// the primary action. Non-modal: the same handler, bubbling on the frame, so
// keys reach it only while focus is inside — which is what non-modal means.
// =============================================================================

const _dialogOwner = Object.freeze({ toString: () => "dialog" });

var _seq = 0;
var PHI  = 1.6180339887;

class Dialog {
    constructor(branch, opts) {
        if (!branch) throw new Error("[Dialog] a branch of its own is required");
        if (!opts || typeof opts.content !== "function") throw new Error("[Dialog] opts.content must be a function");
        var self = this;
        var modal = opts.modal !== false;
        var glow  = (opts.glow != null) ? !!opts.glow : modal;
        var seq   = ++_seq;
        branch.activate(_dialogOwner);
        this.branch = branch;
        this._opts = opts;
        this._closed = false;
        this._buttons = {};
        this._primary = null;
        this._hold = null;

        // ── frame ────────────────────────────────────────────────────────────
        var frame = branch.createElement("frame", "div");
        css.addClass(frame, dl_frame);
        if (glow) css.addClass(frame, dl_glow);
        frame.setAttribute("role", "dialog");
        frame.setAttribute("aria-modal", modal ? "true" : "false");
        frame.setAttribute("tabindex", "-1");
        var sz = Dialog._size(opts);
        frame.style.setProperty("--dl-w", sz.w + "px");     // DATA, via the runtime vars (RFC 0044)
        frame.style.setProperty("--dl-h", sz.h + "px");
        this.el = frame;

        var title = branch.createElement("title", "div");
        css.addClass(title, dl_title);
        var label = branch.createElement("label", "span");
        css.addClass(label, dl_title_label);
        label.textContent = opts.title || "";
        label.id = "dialog-title-" + seq;
        frame.setAttribute("aria-labelledby", label.id);
        title.appendChild(label);
        var x = branch.createElement("close", "button");
        x.type = "button";
        css.addClass(x, dl_close);
        x.textContent = "×";
        x.setAttribute("aria-label", "Close");
        x.addEventListener("click", function () { self.close(); });
        title.appendChild(x);
        frame.appendChild(title);

        var body = branch.createElement("body", "div");
        css.addClass(body, dl_body);
        frame.appendChild(body);
        this.bodyEl = body;

        // ── actions ──────────────────────────────────────────────────────────
        if (opts.actions && opts.actions.length) {
            var row = branch.createElement("actions", "div");
            css.addClass(row, dl_actions);
            for (var i = 0; i < opts.actions.length; i++) this._action(row, opts.actions[i]);
            frame.appendChild(row);
        }

        // ── on the page ──────────────────────────────────────────────────────
        var scrim = null;
        if (modal) {
            scrim = branch.createElement("scrim", "div");
            css.addClass(scrim, dl_scrim);
            scrim.addEventListener("mousedown", function () { self.close(); });
            document.body.appendChild(scrim);
        }
        document.body.appendChild(frame);

        // ── content ──────────────────────────────────────────────────────────
        this._built = opts.content(branch, body) || {};

        // ── keys ─────────────────────────────────────────────────────────────
        this._restoreTo = opts.restoreFocusTo || document.activeElement;
        if (modal) {
            this._hold = new Modality(frame, { keep: [scrim], onKeydown: function (ev) { return self._keys(ev); }, restoreTo: this._restoreTo });
        } else {
            frame.addEventListener("keydown", function (ev) {
                if (self._keys(ev)) { ev.preventDefault(); ev.stopPropagation(); }
            });
        }

        var focusEl = this._built.focusEl || frame;
        if (focusEl.focus) focusEl.focus();
    }

    // 1/φ of each viewport axis, clamped: a pane not yet laid out reports a width
    // of 0, and an unclamped value would reach the DOM as a dialog two pixels
    // wide. Measured at open, not tracked. A requested size is honoured up to
    // the viewport less a margin.
    static _size(opts) {
        var vw = window.innerWidth  || 1024;
        var vh = window.innerHeight || 768;
        var w = opts.size && opts.size.w;
        var h = opts.size && opts.size.h;
        return {
            w: w ? Math.min(w, vw - 48) : Math.min(980, Math.max(460, Math.round(vw / PHI))),
            h: h ? Math.min(h, vh - 48) : Math.min(720, Math.max(320, Math.round(vh / PHI)))
        };
    }

    static _isFormControl(el) {
        if (!el || !el.tagName) return false;
        var t = el.tagName;
        return t === "BUTTON" || t === "INPUT" || t === "TEXTAREA" || t === "SELECT" || el.isContentEditable === true;
    }

    _action(row, a) {
        var self = this;
        var btn = new Button(this.branch.createElement("act_" + a.id, Button.TAG), {
            label: a.label,
            kind: a.primary ? "primary" : "plain",
            onClick: function () { if (!btn.el.disabled) a.onClick(self); }
        });
        if (a.primary) this._primary = a;
        row.appendChild(btn.el);
        this._buttons[a.id] = btn;
    }

    _keys(ev) {
        var built = this._built;
        if (built.onKeydown && built.onKeydown(ev)) return true;
        if (ev.key === "Escape") { this.close(); return true; }
        if (ev.key === "Enter" && this._primary && !Dialog._isFormControl(ev.target)) {
            var pb = this._buttons[this._primary.id];
            if (pb && !pb.el.disabled) this._primary.onClick(this);
            return true;
        }
        return false;
    }

    /** Every path, exactly once: the content's dispose, the branch, the hold or the focus, then onClose. */
    close() {
        if (this._closed) return;
        this._closed = true;
        var built = this._built;
        if (typeof built.dispose === "function") { try { built.dispose(); } catch (e) { console.error("[dialog] content dispose threw", e); } }
        try { this.branch.dissolve(); } catch (e) {}     // frame, scrim and content go together
        if (this._hold) this._hold.release();
        else if (this._restoreTo && this._restoreTo.focus && document.contains(this._restoreTo)) this._restoreTo.focus();
        if (this._opts.onClose) this._opts.onClose();
    }

    actionEl(id) { var b = this._buttons[id]; return b ? b.el : null; }

    setAction(id, state) {
        var b = this._buttons[id];
        if (!b || !state) return;
        if (state.enabled != null) b.setOn(!!state.enabled);
        if (state.label != null) b.label(state.label);
    }
}
