// =============================================================================
// FocusManagerModule — RFC 0049 per-tab focus manager.
//
// 1:1 with a TAB. It owns the tab's IMMUTABLE (position-independent) focus
// mechanics — everything that operates on the tab's own content element and so
// behaves identically no matter which pane the tab currently sits in:
//
//   · inert on its content (the focusability firewall for this tab),
//   · placing native focus into the content,
//   · the setActive(bool) lifecycle call,
//   · reconcile() drift-repair,
//   · the widget-ORIGINATED events: give-up (un-consumed Escape / homing-focus
//     release) and intendedFocusIn (a request to become active).
//
// Because its listeners live on the tab's content element, the manager TRAVELS
// with the tab (a DOM subtree re-parent preserves listeners) and is disposed
// with it. The WORKSPACE owns everything positional/mutable — the single
// selection, exclusivity, visuals, clicks/arrows/Enter — and drives enter /
// release; the manager never decides selection, it enacts and reports.
//
//   new FocusManager(contentEl, {
//       tab,                       // descriptor: optional setActive(bool) + defaultActivation(el)
//       onGiveUp,                  // () => …  widget handed focus back
//       onIntendedFocus            // (activationFn|null) => …  widget requests activation
//   }).attach();
//
// Activation focus is one function resolved by precedence (request > the tab's
// registered default > system fallback: focus the content if it isn't already
// in the tab). setActive is PURE LIFECYCLE — it never focuses — so there is no
// inert-vs-setActive ordering to reason about.
// =============================================================================

class FocusManager {

    constructor(contentEl, opts) {
        if (!contentEl) throw new Error("[FocusManager] contentEl is required");
        opts = opts || {};
        this._content         = contentEl;
        this._tab             = opts.tab || null;
        this._onGiveUp        = typeof opts.onGiveUp === "function" ? opts.onGiveUp : null;
        this._onIntendedFocus = typeof opts.onIntendedFocus === "function" ? opts.onIntendedFocus : null;
        this._deep            = false;   // is THIS tab currently the deep-selected one
        this._reconciling     = false;   // set while we move focus programmatically
        this._attached        = false;

        // The content must be programmatically focusable for the default landing,
        // and starts inert — a tab is not deep until the workspace enters it.
        if (!this._content.hasAttribute || !this._content.hasAttribute("tabindex")) {
            try { this._content.setAttribute("tabindex", "-1"); } catch (e) {}
        }
        this._content.inert = true;

        var self = this;
        // give-up — only meaningful while deep (an inert tab holds no focus).
        this._onKey = function (e) {
            if (self._deep && e.key === "Escape" && !e.ctrlKey && !e.metaKey && !e.altKey) self._give(e);
        };
        this._onRelease = function (e) {
            var d = e && e.detail;
            if (self._deep && d && d.intent === "release") self._give(e);
        };
        // take-focus request — a covered (inert) widget cannot .focus() itself, so
        // it dispatches intendedFocusIn, optionally carrying its activation function.
        this._onIntended = function (e) {
            if (!self._onIntendedFocus) return;
            if (e && typeof e.stopPropagation === "function") e.stopPropagation();
            var fn = (e && e.detail && typeof e.detail.onGranted === "function") ? e.detail.onGranted : null;
            try { self._onIntendedFocus(fn); }
            catch (err) { console.error("[FocusManager] onIntendedFocus threw:", err); }
        };
    }

    attach() {
        if (this._attached) return this;
        this._content.addEventListener("keydown", this._onKey);
        this._content.addEventListener("homing-focus", this._onRelease);
        this._content.addEventListener("intendedFocusIn", this._onIntended);
        this._attached = true;
        return this;
    }

    dispose() {
        if (!this._attached) return this;
        this._content.removeEventListener("keydown", this._onKey);
        this._content.removeEventListener("homing-focus", this._onRelease);
        this._content.removeEventListener("intendedFocusIn", this._onIntended);
        this._attached = false;
        return this;
    }

    /** Whether this tab is currently the deep-selected one. */
    isDeep()        { return this._deep; }
    /** True while the manager is moving focus programmatically (loop guard). */
    isReconciling() { return this._reconciling; }

    /**
     * Enter deep (workspace-driven): un-inert, fire setActive(true) (lifecycle),
     * then run the activation function to place focus — the per-request one if
     * given, else the tab's registered default, else the system fallback (focus
     * the content if focus isn't already inside it). The un-inert→focus order is
     * internal here, so callers never sequence it.
     */
    enter(activationFn) {
        this._deep = true;
        this._content.inert = false;
        this._setActive(true);
        this._reconciling = true;
        try {
            var fn = (typeof activationFn === "function")
                        ? activationFn
                        : (this._tab && typeof this._tab.defaultActivation === "function")
                            ? this._tab.defaultActivation
                            : null;
            if (fn) {
                try { fn(this._content); }
                catch (e) { console.error("[FocusManager] activation fn threw:", e); }
            }
            if (!this._contains(this._active())) {
                try { this._content.focus(); } catch (e) {}
            }
        } finally { this._reconciling = false; }
        return this;
    }

    /**
     * Release (immutable local work): re-inert the content (which blurs a focused
     * descendant), belt-and-suspenders blur any focus still inside, and fire
     * setActive(false). The workspace does the positional follow-up on its own
     * intent after this returns / is notified.
     */
    release() {
        this._deep = false;
        this._reconciling = true;
        try {
            this._content.inert = true;   // making a focused subtree inert blurs it
            var a = this._active();
            if (a && this._contains(a) && typeof a.blur === "function") {
                try { a.blur(); } catch (e) {}
            }
        } finally { this._reconciling = false; }
        this._setActive(false);
        return this;
    }

    /**
     * Drift repair (RFC 0049 / Issue 0003): if this tab is deep but focus left its
     * content (e.g. a re-render rebuilt the focused node, dropping focus to
     * <body>), pull it back. Narrow — never yanks focus off a legitimate element
     * elsewhere, so it can't fight assistive tech.
     */
    reconcile() {
        if (!this._deep) return this;
        if (this._contains(this._active())) return this;
        this._reconciling = true;
        try { this._content.focus(); } catch (e) {}
        finally { this._reconciling = false; }
        return this;
    }

    // ── internals ─────────────────────────────────────────────────────────────

    _give(e) {
        if (e) {
            if (typeof e.stopPropagation === "function") e.stopPropagation();
            if (typeof e.preventDefault === "function")  e.preventDefault();
        }
        if (this._onGiveUp) {
            try { this._onGiveUp(); }
            catch (err) { console.error("[FocusManager] onGiveUp threw:", err); }
        }
    }

    _setActive(on) {
        if (this._tab && typeof this._tab.setActive === "function") {
            try { this._tab.setActive(on); }
            catch (e) { console.error("[FocusManager] tab.setActive threw:", e); }
        }
    }

    _active()      { return (typeof document !== "undefined") ? document.activeElement : null; }
    _contains(node){ return !!(node && this._content.contains && this._content.contains(node)); }
}
