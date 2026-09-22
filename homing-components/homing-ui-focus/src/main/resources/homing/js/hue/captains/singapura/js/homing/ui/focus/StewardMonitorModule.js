// =============================================================================
// StewardMonitor — the keyboard steward's activeness on view, as one lamp:
// active, the keys are the holder's; or dormant on the element that has the
// native focus, whose keys are its own. Redrawn on every event of the steward
// and every move of the native focus, so it says at every moment whether the
// steward is routing keys. Tooling for a page: it reads the document's active
// element and takes no keys. A branch component: the caller makes a
// sub-branch for it and hands it in.
//
//   new StewardMonitor(branch, { host })
//     .root           the lamp, appended to the host
//     .lamp()         its text
//     .refresh()      redrawn by call; it redraws itself on every change
//     .dispose()
// =============================================================================

const _stewardMonitorOwner = Object.freeze({ toString: () => "stewardMonitor" });

class StewardMonitor {
    constructor(branch, opts) {
        if (!branch) throw new Error("[StewardMonitor] a branch of its own is required");
        if (!opts || !opts.host) throw new Error("[StewardMonitor] opts.host is required");
        var self = this;
        branch.activate(_stewardMonitorOwner);
        this.branch = branch;
        var lamp = branch.createElement("lamp", "div");
        css.addClass(lamp, sm_lamp);
        lamp.setAttribute("role", "status");
        lamp.setAttribute("aria-label", "Keyboard steward");
        opts.host.appendChild(lamp);
        this.root = lamp;
        this._offSteward = KeyboardStewardInstance.on(function () { self.refresh(); });
        this._onFocus = function () { self.refresh(); };
        document.addEventListener("focusin", this._onFocus, false);
        document.addEventListener("focusout", this._onFocus, false);
        this.refresh();
    }

    /** The lamp by state. */
    refresh() {
        var f = KeyboardSteward.focused();
        this.root.textContent = f ? "dormant on " + StewardMonitor.describe(f) + " — its keys are its own" : "active — the keys are the holder's";
        css.toggleClass(this.root, sm_lamp_dormant, !!f);
        return this;
    }

    /** An element for the eye: its tag, its aria-label or id when it has one. */
    static describe(el) {
        var tag = String(el && el.tagName || "?").toLowerCase();
        var name = el && el.getAttribute ? (el.getAttribute("aria-label") || el.id) : "";
        return name ? tag + " “" + name + "”" : tag;
    }

    lamp() { return this.root.textContent; }

    dispose() {
        this._offSteward();
        document.removeEventListener("focusin", this._onFocus, false);
        document.removeEventListener("focusout", this._onFocus, false);
        if (this.root.parentNode) this.root.parentNode.removeChild(this.root);
        this.branch.dissolve();
    }
}
