// =============================================================================
// PaneFocusNavModule — RFC 0048 keyboard pane navigation (shallow mode).
//
// A single-purpose controller the shell composes over MultiTabPane. It owns the
// SHALLOW-mode keyboard only:
//
//   ArrowLeft/Right/Up/Down  → mtp.focusPane(dir)      (shared-edge cursor move)
//   Tab / Shift+Tab          → mtp.cycleTabInPane(±1)  (tabs within the pane)
//   Enter                    → mtp.enterDeep()         (enter the cursor pane)
//
// In DEEP mode it is inert — the entered widget owns the keyboard. The
// discriminator is mtp.mode() (derived from the workspace-active tab), NOT DOM
// focus, so there is no focus bookkeeping here. The deep→shallow release is a
// separate concern (FocusRelease), keeping this class about one thing.
//
// Scoping: a single keydown listener on `document`. It acts only when this
// workspace's MTP is shallow AND the focus is either nowhere (body) or inside
// this workspace's content element — so a second workspace on the page, or a
// focused input, is never disturbed. It never builds DOM and never styles.
//
// API:  new PaneFocusNav({ mtp, host }).attach();  … .dispose();
// =============================================================================

class PaneFocusNav {

    constructor(opts) {
        opts = opts || {};
        if (!opts.mtp) throw new Error("[PaneFocusNav] opts.mtp is required");
        this._mtp  = opts.mtp;
        this._host = opts.host || null;   // workspace content element, for scoping
        this._attached = false;
        var self = this;
        this._onKeyDown = function (e) { self._handle(e); };
    }

    attach() {
        if (this._attached) return this;
        document.addEventListener("keydown", this._onKeyDown, true);
        this._attached = true;
        return this;
    }

    dispose() {
        if (!this._attached) return this;
        document.removeEventListener("keydown", this._onKeyDown, true);
        this._attached = false;
        return this;
    }

    // ── internals ────────────────────────────────────────────────────────────

    _isEditable(t) {
        if (!t || !t.tagName) return false;
        var tag = t.tagName;
        return tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT" || t.isContentEditable === true;
    }

    /** True when the keystroke belongs to this workspace (focus here or nowhere). */
    _inScope(e) {
        if (!this._host) return true;
        var a = document.activeElement;
        if (a == null || a === document.body) return true;
        return this._host.contains(a) || this._host.contains(e.target);
    }

    _handle(e) {
        // Deep mode → the widget owns the keyboard. Modifier chords (Ctrl/Meta/
        // Alt) are left to the browser/OS. Editable targets are never disturbed.
        if (this._mtp.mode() !== "shallow") return;
        if (e.ctrlKey || e.metaKey || e.altKey) return;
        if (this._isEditable(e.target)) return;
        if (!this._inScope(e)) return;

        var mtp = this._mtp;
        switch (e.key) {
            case "ArrowLeft":  mtp.focusPane("left");  break;
            case "ArrowRight": mtp.focusPane("right"); break;
            case "ArrowUp":    mtp.focusPane("up");    break;
            case "ArrowDown":  mtp.focusPane("down");  break;
            case "Tab":        mtp.cycleTabInPane(e.shiftKey ? -1 : 1); break;
            case "Enter":      mtp.enterDeep(); break;   // no-op on an empty pane (picker: follow-up)
            default:           return;                   // not ours — leave it for the browser
        }
        e.preventDefault();
        e.stopPropagation();
    }
}
