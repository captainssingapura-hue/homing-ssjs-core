// =============================================================================
// Dialog — a frame that owns the screen until dismissed, or, non-modal, one
// that does not.
//
//   openDialog(opts) → handle
//
// opts:
//   branch          (required) a DomOpsParty branch; the dialog mints a CHILD
//                   per open, named by the clock and a counter, and dissolves
//                   it on close — each dialog's namespace and lifetime is its own
//   content         (required) function(branch, bodyEl) → { onKeydown?, focusEl?, dispose? }
//                   builds the body INTO the dialog's branch; may return a key
//                   handler (ev → bool), the element to focus on open, and a
//                   dispose() called first on close, before the branch dissolves,
//                   for what dissolving cannot release - the widget's word for it
//   title           string
//   modal           boolean, default true — scrim, inert, keyboard capture
//   glow            boolean, default = modal — the focus ring on the frame
//   actions         [{ id, label, primary?, onClick(handle) }] — omit for no row.
//                   The buttons are the elements' Button: the primary one
//                   primary, the rest plain
//   size            { w?, h? } px; default 1/φ of each viewport axis, clamped
//   restoreFocusTo  element focused on close; default whatever had focus
//   onClose         function() — fires on EVERY close path, exactly once
//
// handle:
//   el, bodyEl, branch
//   close()
//   actionEl(id)                       the button, an owned reference
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

function _isFormControl(el) {
    if (!el || !el.tagName) return false;
    var t = el.tagName;
    return t === "BUTTON" || t === "INPUT" || t === "TEXTAREA" || t === "SELECT" || el.isContentEditable === true;
}

// 1/φ of each viewport axis, clamped: a pane not yet laid out reports a width
// of 0, and an unclamped value would reach the DOM as a dialog two pixels wide.
// Measured at open, not tracked. A requested size is honoured up to the
// viewport less a margin.
function _size(opts) {
    var vw = window.innerWidth  || 1024;
    var vh = window.innerHeight || 768;
    var w = opts.size && opts.size.w;
    var h = opts.size && opts.size.h;
    return {
        w: w ? Math.min(w, vw - 48) : Math.min(980, Math.max(460, Math.round(vw / PHI))),
        h: h ? Math.min(h, vh - 48) : Math.min(720, Math.max(320, Math.round(vh / PHI)))
    };
}

function openDialog(opts) {
    if (!opts || !opts.branch) throw new Error("openDialog: opts.branch is required");
    if (typeof opts.content !== "function") throw new Error("openDialog: opts.content must be a function");

    var modal  = opts.modal !== false;
    var glow   = (opts.glow != null) ? !!opts.glow : modal;
    var seq    = ++_seq;
    var branch = opts.branch.createBranch("dialog" + Date.now() + "_" + seq);
    branch.activate(_dialogOwner);

    var closed  = false;
    var buttons = {};
    var release = null;

    // ── frame ────────────────────────────────────────────────────────────────
    var frame = branch.createElement("frame", "div");
    css.addClass(frame, dl_frame);
    if (glow) css.addClass(frame, dl_glow);
    frame.setAttribute("role", "dialog");
    frame.setAttribute("aria-modal", modal ? "true" : "false");
    frame.setAttribute("tabindex", "-1");
    var sz = _size(opts);
    frame.style.setProperty("--dl-w", sz.w + "px");     // DATA, via the runtime vars (RFC 0044)
    frame.style.setProperty("--dl-h", sz.h + "px");

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
    x.addEventListener("click", function () { close(); });
    title.appendChild(x);
    frame.appendChild(title);

    var body = branch.createElement("body", "div");
    css.addClass(body, dl_body);
    frame.appendChild(body);

    // ── actions ──────────────────────────────────────────────────────────────
    var primary = null;
    if (opts.actions && opts.actions.length) {
        var row = branch.createElement("actions", "div");
        css.addClass(row, dl_actions);
        for (var i = 0; i < opts.actions.length; i++) {
            (function (a) {
                var b = Button(branch, "act_" + a.id, {
                    label: a.label,
                    kind: a.primary ? "primary" : "plain",
                    onClick: function () { if (!b.disabled) a.onClick(handle); }
                });
                if (a.primary) primary = a;
                row.appendChild(b);
                buttons[a.id] = b;
            })(opts.actions[i]);
        }
        frame.appendChild(row);
    }

    // ── on the page ──────────────────────────────────────────────────────────
    var scrim = null;
    if (modal) {
        scrim = branch.createElement("scrim", "div");
        css.addClass(scrim, dl_scrim);
        scrim.addEventListener("mousedown", function () { close(); });
        document.body.appendChild(scrim);
    }
    document.body.appendChild(frame);

    // ── content ──────────────────────────────────────────────────────────────
    var built = opts.content(branch, body) || {};

    // ── keys ─────────────────────────────────────────────────────────────────
    function keys(ev) {
        if (built.onKeydown && built.onKeydown(ev)) return true;
        if (ev.key === "Escape") { close(); return true; }
        if (ev.key === "Enter" && primary && !_isFormControl(ev.target)) {
            var pb = buttons[primary.id];
            if (pb && !pb.disabled) primary.onClick(handle);
            return true;
        }
        return false;
    }
    var restoreTo = opts.restoreFocusTo || document.activeElement;
    if (modal) {
        release = holdModality(frame, { keep: [scrim], onKeydown: keys, restoreTo: restoreTo });
    } else {
        frame.addEventListener("keydown", function (ev) {
            if (keys(ev)) { ev.preventDefault(); ev.stopPropagation(); }
        });
    }

    var focusEl = built.focusEl || frame;
    if (focusEl.focus) focusEl.focus();

    // ── close: every path, exactly once ──────────────────────────────────────
    function close() {
        if (closed) return;
        closed = true;
        if (typeof built.dispose === "function") { try { built.dispose(); } catch (e) { console.error("[dialog] content dispose threw", e); } }
        try { branch.dissolve(); } catch (e) {}     // frame, scrim and content go together
        if (release) release();
        else if (restoreTo && restoreTo.focus && document.contains(restoreTo)) restoreTo.focus();
        if (opts.onClose) opts.onClose();
    }

    var handle = {
        el: frame, bodyEl: body, branch: branch,
        close: close,
        actionEl:  function (id) { return buttons[id] || null; },
        setAction: function (id, state) {
            var b = buttons[id];
            if (!b || !state) return;
            if (state.enabled != null) setButtonOn(b, !!state.enabled);
            if (state.label != null) b.textContent = state.label;
        }
    };
    return handle;
}
