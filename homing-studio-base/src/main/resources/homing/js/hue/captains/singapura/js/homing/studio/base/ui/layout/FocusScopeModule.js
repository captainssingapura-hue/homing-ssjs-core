// =============================================================================
// FocusScopeModule — RFC 0048 give-up-focus wrapper.
//
// A container that owns a "you are inside me now" mode wraps its content in a
// FocusScope and is told, uniformly, when the content wants to hand focus back.
// It normalises the two give-up signals on `hostEl` (bubble phase) into one
// onGiveUp():
//
//   1. an un-consumed Escape  — a keydown that bubbled to hostEl because no
//      widget inside called stopPropagation; or
//   2. a release event        — a bubbling CustomEvent('homing-focus',
//      { detail: { intent: 'release' } }).
//
// The dual signal is trap-free without stealing a key: a widget that uses Escape
// keeps it (stopPropagation); a widget that swallows Escape entirely binds its
// own shortcut to dispatch the release event. Same class in every container, so
// give-up-focus is uniform — a portable DOM convention, not an injected API.
//
// Builds no DOM and applies no styling — it only attaches two listeners.
//
// API:  new FocusScope(hostEl, onGiveUp).attach();  … .dispose();
// =============================================================================

class FocusScope {

    constructor(hostEl, onGiveUp) {
        if (!hostEl) throw new Error("[FocusScope] hostEl is required");
        if (typeof onGiveUp !== "function") throw new Error("[FocusScope] onGiveUp must be a function");
        this._host     = hostEl;
        this._onGiveUp = onGiveUp;
        this._attached = false;
        var self = this;
        // Un-consumed Escape — anything inside that uses Escape would have called
        // stopPropagation, so its arrival here IS the "not consumed" signal.
        this._onKey = function (e) {
            if (e.key === "Escape" && !e.ctrlKey && !e.metaKey && !e.altKey) self._give(e);
        };
        // Voluntary release — a widget dispatches this on its own trigger (a Done
        // button, task completion, or a custom shortcut when it owns Escape).
        this._onRelease = function (e) {
            var d = e && e.detail;
            if (d && d.intent === "release") self._give(e);
        };
    }

    attach() {
        if (this._attached) return this;
        this._host.addEventListener("keydown", this._onKey);            // bubble phase
        this._host.addEventListener("homing-focus", this._onRelease);   // bubble phase
        this._attached = true;
        return this;
    }

    dispose() {
        if (!this._attached) return this;
        this._host.removeEventListener("keydown", this._onKey);
        this._host.removeEventListener("homing-focus", this._onRelease);
        this._attached = false;
        return this;
    }

    _give(e) {
        // Consume it so an outer FocusScope (nested containers) doesn't also fire.
        if (e) {
            if (typeof e.stopPropagation === "function") e.stopPropagation();
            if (typeof e.preventDefault === "function")  e.preventDefault();
        }
        try { this._onGiveUp(); }
        catch (err) { console.error("[FocusScope] onGiveUp threw:", err); }
    }
}
