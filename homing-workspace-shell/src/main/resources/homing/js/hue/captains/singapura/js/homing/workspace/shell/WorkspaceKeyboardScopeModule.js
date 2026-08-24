// =============================================================================
// WorkspaceKeyboardScopeModule — RFC 0052's keyboard-SCOPE watcher.
//
// One orthogonal fact, kept live: does this workspace own the keyboard right
// now? The keyboard AXIS always has a home (a cursor pane, or an entered
// widget), but that home only receives keystrokes while focus is inside the
// workspace — or nowhere, which is exactly when the shallow keyboard acts.
// Focus parked in page chrome (a theme picker, a header link) leaves the locus
// real but DEAF, and a locus mark that stayed lit there would be lying.
//
// So the mark is gated on this: shown iff the keyboard would actually act.
// The test mirrors ShallowKeyboard._inScope deliberately — one notion of
// "belongs to this workspace", not two that can drift apart.
//
//   new KeyboardScope({ host, onChange }).attach();   // onChange(bool)
//
// Synchronous by construction, no timers: focusin says where focus ARRIVED;
// focusout carries relatedTarget (null = heading for <body>, which IS in
// scope). Window blur/focus covers the whole document losing the keys to
// another application.
// =============================================================================

class KeyboardScope {

    constructor(opts) {
        opts = opts || {};
        this._host     = opts.host || null;
        this._onChange = typeof opts.onChange === "function" ? opts.onChange : null;
        this._in       = true;     // boot: focus is nowhere, so the keys are ours
        this._attached = false;
    }

    /** True when this workspace owns the keyboard right now. */
    has() {
        if (!this._host || typeof this._host.contains !== "function") return true;  // can't tell → assume ours
        if (typeof document === "undefined") return true;
        var a = document.activeElement;
        if (a == null || a === document.body) return true;   // focus nowhere → the shallow keyboard acts
        return this._host.contains(a);
    }

    /** The last reported answer (no recomputation). */
    value() { return this._in; }

    /** Re-derive from the live world. Focus events can be missed (a headless or
     *  unfocused document fires none at all); every workspace trigger calls this,
     *  so a stale answer is cured by the next interaction rather than persisting. */
    resync() { this._set(this.has()); return this; }

    attach() {
        if (this._attached) return this;
        var self = this;
        // Recompute from the WORLD (document.activeElement), never from the event
        // payload — the coordinator's own doctrine, one level down.
        this._onIn = function () { self._set(self.has()); };
        this._onOut = function (e) {
            var next = e && e.relatedTarget;
            if (!next) { self._set(true); return; }   // → body: the shallow keyboard resumes
            self._set(self._holds(next));
        };
        this._onWinFocus = function () { self._set(self.has()); };
        this._onWinBlur  = function () { self._set(false); };   // another application has the keys
        if (typeof document !== "undefined" && document.addEventListener) {
            document.addEventListener("focusin",  this._onIn,  true);
            document.addEventListener("focusout", this._onOut, true);
        }
        if (typeof window !== "undefined" && window.addEventListener) {
            window.addEventListener("focus", this._onWinFocus);
            window.addEventListener("blur",  this._onWinBlur);
        }
        this._attached = true;
        this._emit();                 // paint the initial truth, honest from frame one
        return this;
    }

    dispose() {
        if (!this._attached) return this;
        if (typeof document !== "undefined" && document.removeEventListener) {
            document.removeEventListener("focusin",  this._onIn,  true);
            document.removeEventListener("focusout", this._onOut, true);
        }
        if (typeof window !== "undefined" && window.removeEventListener) {
            window.removeEventListener("focus", this._onWinFocus);
            window.removeEventListener("blur",  this._onWinBlur);
        }
        this._attached = false;
        return this;
    }

    _holds(node) {
        if (!this._host || typeof this._host.contains !== "function") return true;
        return this._host.contains(node);
    }

    _set(next) {
        next = !!next;
        if (next === this._in) return;
        this._in = next;
        this._emit();
    }

    _emit() {
        if (!this._onChange) return;
        try { this._onChange(this._in); }
        catch (e) { console.error("[KeyboardScope] onChange threw:", e); }
    }
}
