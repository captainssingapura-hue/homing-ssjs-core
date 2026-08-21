// =============================================================================
// WorkspaceShallowKeyboardModule — RFC 0049's shallow-mode keyboard layer.
//
// The keyboard half of the focus coordinator, kept in its own module (Modest
// File Size — one orthogonal concern per class; this is the direct descendant
// of RFC 0048's PaneFocusNav). Owns ONLY the shallow-mode keys:
//
//   Arrows       → move the cursor pane (mtp.neighbourOf shared-edge)
//   Tab/Shift+Tab→ cycle the shown tab WITHIN the cursor pane
//   Enter        → upgrade the cursor pane to deep
//
// Inert while the selection is deep — the widget owns the keyboard then; the
// discriminator is the coordinator's mode(), not DOM focus. Scoped so a
// focused input or a second workspace is never disturbed.
//
//   new ShallowKeyboard({ coordinator, mtp, host }).attach();  … .dispose();
// =============================================================================

class ShallowKeyboard {

    constructor(opts) {
        opts = opts || {};
        if (!opts.coordinator) throw new Error("[ShallowKeyboard] opts.coordinator is required");
        if (!opts.mtp)         throw new Error("[ShallowKeyboard] opts.mtp is required");
        this._fc   = opts.coordinator;
        this._mtp  = opts.mtp;
        this._host = opts.host || null;   // workspace content element, for scoping
        this._attached = false;
        var self = this;
        this._onKeyDown = function (e) { self._handleKey(e); };
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

    _handleKey(e) {
        // Deep → the widget owns the keyboard (the tab's FM handles give-up).
        if (this._fc.mode() === "deep") return;
        if (e.ctrlKey || e.metaKey || e.altKey) return;
        if (this._isEditable(e.target)) return;
        if (!this._inScope(e)) return;

        var slot = this._fc.selectedSlotId();
        if (slot == null) return;
        switch (e.key) {
            case "ArrowLeft":  this._moveCursor(slot, "left");  break;
            case "ArrowRight": this._moveCursor(slot, "right"); break;
            case "ArrowUp":    this._moveCursor(slot, "up");    break;
            case "ArrowDown":  this._moveCursor(slot, "down");  break;
            case "Tab":        this._cycleTab(slot, e.shiftKey ? -1 : 1); break;
            case "Enter":      this._fc.enterDeep(slot); break;   // empty pane degrades to shallow
            default:           return;                            // not ours — leave it for the browser
        }
        e.preventDefault();
        e.stopPropagation();
    }

    _moveCursor(fromSlot, direction) {
        var next = this._mtp.neighbourOf ? this._mtp.neighbourOf(fromSlot, direction) : null;
        if (next != null) this._fc.selectShallow(next);
    }

    /** Cycle the shown tab WITHIN the cursor pane (never leaves the pane). */
    _cycleTab(slot, delta) {
        var state = this._mtp.getState ? this._mtp.getState() : null;
        var s = state && state.tabs ? state.tabs[slot] : null;
        if (!s || !s.tabs || s.tabs.length < 2) return;
        var i = 0;
        for (var k = 0; k < s.tabs.length; k++) {
            if (s.tabs[k].id === s.activeTabId) { i = k; break; }
        }
        var n = s.tabs.length;
        var j = ((i + delta) % n + n) % n;
        this._mtp.switchTab(slot, s.tabs[j].id);   // fires onTabActivated → coordinator reselects shallow
    }
}
