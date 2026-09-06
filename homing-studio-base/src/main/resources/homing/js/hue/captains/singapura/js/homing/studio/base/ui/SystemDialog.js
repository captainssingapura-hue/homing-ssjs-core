// SystemDialog.js
//
// A dialog that owns the screen until dismissed — or, in non-modal mode, one
// that does not. RFC 0057, Phase 1: everything the theme picker had to do by
// hand to be a proper dialog, lifted out so the next dialog cannot be built
// without it.
//
//   openSystemDialog(opts) -> handle
//
// opts:
//   branch       (required) a DomOpsParty branch; the dialog mints a CHILD per
//                open, named by the clock, and dissolves it on close — so each
//                dialog's namespace and lifetime is its own
//   content      (required) function(branch, bodyEl) -> { onKeydown?, focusEl? }
//                builds the body INTO the dialog's branch; may return a key
//                handler (ev -> bool) and the element to focus on open
//   title        string
//   modal        boolean, default true — scrim + inert + capture keyboard
//   glow         boolean, default = modal
//   actions      [{ id, label, primary?, onClick(handle) }] — omit for no row
//   size         { w?, h? } px; default is 1/φ of each viewport axis, clamped
//   restoreFocusTo  element focused on close; default is whatever had focus
//   onClose      function() — fires on EVERY close path, exactly once
//
// handle:
//   el, bodyEl, branch
//   close()
//   setAction(id, { enabled?, label? })
//
// NOT Modal. Modal is the draggable, resizable panel that carries a widget out
// of a MultiTabPane, and it stays that. This is built on its own branch and its
// own typed sheet, which is what lets the glow be an ordinary border and
// box-shadow rather than the outline-and-filter workaround the picker needed to
// get past Modal's unlayered <style> tag.
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java SystemDialog declaration — do not add import/export lines here.

const _dialogOwner = Object.freeze({ toString: () => "systemDialog" });

var _seq = 0;
var PHI  = 1.6180339887;

function _isFormControl(el) {
    if (!el || !el.tagName) return false;
    var t = el.tagName;
    return t === "BUTTON" || t === "INPUT" || t === "TEXTAREA" || t === "SELECT" || el.isContentEditable === true;
}

/**
 * 1/φ of each viewport axis, clamped. The clamp is not decoration: a hidden or
 * not-yet-laid-out pane reports innerWidth 0, and an unclamped value reaches
 * the DOM verbatim as a dialog two pixels wide. The ceiling is the other end
 * of the same argument — past about a thousand pixels the extra width is spent
 * on nothing. Measured at open rather than tracked: a dialog that resized under
 * the pointer while you read it would be worse than one right for the viewport
 * you opened it in.
 */
function _size(opts) {
    var vw = window.innerWidth  || 1024;
    var vh = window.innerHeight || 768;
    return {
        w: (opts.size && opts.size.w) || Math.min(980, Math.max(460, Math.round(vw / PHI))),
        h: (opts.size && opts.size.h) || Math.min(720, Math.max(320, Math.round(vh / PHI)))
    };
}

function openSystemDialog(opts) {
    if (!opts || !opts.branch)  throw new Error("openSystemDialog: opts.branch is required");
    if (typeof opts.content !== "function") throw new Error("openSystemDialog: opts.content must be a function");

    var modal   = opts.modal !== false;
    var glow    = (opts.glow != null) ? !!opts.glow : modal;
    var seq     = ++_seq;
    // Named by the clock AND a counter, for two opens in the same millisecond.
    var branch  = opts.branch.createBranch("sysDlg" + Date.now() + "_" + seq);
    branch.activate(_dialogOwner);

    var restoreTo = opts.restoreFocusTo || document.activeElement;
    var inerted   = [];
    var keys      = null;
    var closed    = false;
    var buttons   = {};

    // ── frame ────────────────────────────────────────────────────────────────
    var frame = branch.createElement("frame", "div");
    css.addClass(frame, sd_frame);
    if (glow) css.addClass(frame, sd_glow);
    frame.setAttribute("role", "dialog");
    frame.setAttribute("aria-modal", modal ? "true" : "false");
    frame.setAttribute("tabindex", "-1");
    var sz = _size(opts);
    frame.style.setProperty("--sd-w", sz.w + "px");     // DATA, via setProperty (RFC 0044)
    frame.style.setProperty("--sd-h", sz.h + "px");

    var title = branch.createElement("title", "div");
    css.addClass(title, sd_title);
    var label = branch.createElement("label", "span");
    css.addClass(label, sd_title_label);
    label.textContent = opts.title || "";
    title.appendChild(label);
    var x = branch.createElement("close", "button");
    x.type = "button";
    css.addClass(x, sd_close);
    x.textContent = "×";
    x.setAttribute("aria-label", "Close");
    x.addEventListener("click", function () { close(); });
    title.appendChild(x);
    frame.appendChild(title);

    var body = branch.createElement("body", "div");
    css.addClass(body, sd_body);
    frame.appendChild(body);

    // ── actions ──────────────────────────────────────────────────────────────
    var primary = null;
    if (opts.actions && opts.actions.length) {
        var row = branch.createElement("actions", "div");
        css.addClass(row, sd_actions);
        for (var i = 0; i < opts.actions.length; i++) {
            (function (a) {
                var b = branch.createElement("act_" + a.id, "button");
                b.type = "button";
                b.textContent = a.label;
                css.addClass(b, sd_action);
                if (a.primary) { css.addClass(b, sd_action_primary); primary = a; }
                b.addEventListener("click", function () { if (!b.disabled) a.onClick(handle); });
                row.appendChild(b);
                buttons[a.id] = b;
            })(opts.actions[i]);
        }
        frame.appendChild(row);
    }

    // ── modality: the scrim is the visible half, inert is the real half ─────
    var scrim = null;
    if (modal) {
        scrim = branch.createElement("scrim", "div");
        css.addClass(scrim, sd_scrim);
        scrim.addEventListener("mousedown", function () { close(); });
        document.body.appendChild(scrim);
    }
    document.body.appendChild(frame);
    if (modal) {
        var kids = document.body.children;
        for (var k = 0; k < kids.length; k++) {
            var el = kids[k];
            if (el === frame || el === scrim || el.inert) continue;
            el.inert = true;
            inerted.push(el);
        }
    }

    // ── content ──────────────────────────────────────────────────────────────
    var built = opts.content(branch, body) || {};

    // ── keyboard ─────────────────────────────────────────────────────────────
    // Modal: CAPTURE on document, and stopPropagation on anything taken — the
    // page behind may have its own tree listening on document, and without this
    // one ArrowDown walks both. Non-modal: bubble, on the frame, so keys reach
    // us only while focus is inside — which is what non-modal means.
    keys = function (ev) {
        var handled = false;
        if (ev.key === "Escape") { close(); handled = true; }
        else if (built.onKeydown && built.onKeydown(ev)) handled = true;
        else if (ev.key === "Enter" && primary && !_isFormControl(ev.target)) {
            var pb = buttons[primary.id];
            if (pb && !pb.disabled) primary.onClick(handle);
            handled = true;
        }
        if (handled) { ev.preventDefault(); ev.stopPropagation(); }
    };
    if (modal) document.addEventListener("keydown", keys, true);
    else       frame.addEventListener("keydown", keys);

    var focusEl = built.focusEl || frame;
    if (focusEl.focus) focusEl.focus();

    // ── close: every path, exactly once ──────────────────────────────────────
    function close() {
        if (closed) return;
        closed = true;
        if (modal) document.removeEventListener("keydown", keys, true);
        for (var i = 0; i < inerted.length; i++) inerted[i].inert = false;
        inerted = [];
        try { branch.dissolve(); } catch (e) {}     // frame, scrim and content go together
        if (restoreTo && restoreTo.focus && document.contains(restoreTo)) restoreTo.focus();
        if (opts.onClose) opts.onClose();
    }

    var handle = {
        el: frame, bodyEl: body, branch: branch,
        close: close,
        setAction: function (id, state) {
            var b = buttons[id];
            if (!b || !state) return;
            if (state.enabled != null) {
                b.disabled = !state.enabled;
                css.toggleClass(b, sd_action_off, !state.enabled);
            }
            if (state.label != null) b.textContent = state.label;
        }
    };
    return handle;
}
