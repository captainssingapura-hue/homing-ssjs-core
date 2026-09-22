// =============================================================================
// StewardMonitor — the keyboard steward's activeness on view. A lamp: active,
// the keys are the holder's, or dormant on the element that has the native
// focus, whose keys are its own; and the evidence — the last keys the steward
// saw and what it did with each: to the holder, taken or left; native, left
// to the focused element; to no one; a Tab to the member it went to, or left
// to the browser. Redrawn on every key the steward traces and every move of
// the native focus, so it says at every moment whether the steward is
// routing keys, and to whom the last one went. Tooling for a page: it reads
// the steward's trace and the document's active element, and takes no keys.
// A branch component: the caller makes a sub-branch for it and hands it in.
//
//   new StewardMonitor(branch, { host, keep? })   keep: how many keys are shown (8)
//     .root           the element, appended to the host
//     .lamp()         the lamp's text
//     .lines()        the key lines' texts, newest first
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
        this._keep = opts.keep > 0 ? opts.keep : 8;
        this._seen = [];
        this._rows = null;
        this._n = 0;
        var root = branch.createElement("monitor", "div");
        css.addClass(root, sm_monitor);
        root.setAttribute("role", "status");
        root.setAttribute("aria-label", "Keyboard steward");
        var lamp = branch.createElement("lamp", "div");
        css.addClass(lamp, sm_lamp);
        var keys = branch.createElement("keys", "div");
        css.addClass(keys, sm_keys);
        root.appendChild(lamp);
        root.appendChild(keys);
        opts.host.appendChild(root);
        this.root = root; this._lamp = lamp; this._keys = keys;
        this._offTrace = KeyboardStewardInstance.trace(function (t) { if (t.kind === "KeyDown") { self._seen.unshift(t); if (self._seen.length > self._keep) self._seen.length = self._keep; } self.refresh(); });
        this._offSteward = KeyboardStewardInstance.on(function () { self.refresh(); });
        this._onFocus = function () { self.refresh(); };
        document.addEventListener("focusin", this._onFocus, false);
        document.addEventListener("focusout", this._onFocus, false);
        this.refresh();
    }

    /** The lamp and the key lines drawn afresh: the lines minted on a sub-branch dissolved each time. */
    refresh() {
        var f = KeyboardSteward.focused();
        this._lamp.textContent = f ? "dormant on " + StewardMonitor.describe(f) + " — its keys are its own" : "active — the keys are the holder's";
        css.toggleClass(this._lamp, sm_lamp_dormant, !!f);
        if (this._rows) this.branch.dissolveBranch(this._rows.name);
        var rows = this.branch.createBranch("keys-" + (++this._n));
        rows.activate(_stewardMonitorOwner);
        this._rows = rows;
        while (this._keys.firstChild) this._keys.removeChild(this._keys.firstChild);
        for (var i = 0; i < this._seen.length; i++) {
            var line = rows.createElement("k" + i, "div");
            css.addClass(line, sm_key);
            var k = rows.createElement("key" + i, "span");
            css.addClass(k, sm_key_name);
            k.textContent = StewardMonitor.keyName(this._seen[i].key);
            var r = rows.createElement("route" + i, "span");
            css.addClass(r, sm_key_route);
            r.textContent = StewardMonitor.routeOf(this._seen[i]);
            line.appendChild(k);
            line.appendChild(r);
            this._keys.appendChild(line);
        }
        return this;
    }

    /** What the steward did with a key, as a line. */
    static routeOf(t) {
        var m = typeof t.to === "string" ? focusParty.find(t.to) : null, who = m ? m.path : (typeof t.to === "string" ? t.to : "");
        switch (t.route) {
            case "tab": return "→ " + who + " (Tab)";
            case "browser": return "→ the browser: no member to go to";
            case "native": return "→ native: " + StewardMonitor.describe(t.to);
            case "none": return "→ no one holds";
            default: return "→ " + who + (t.taken ? " · taken" : " · left");
        }
    }
    /** A key for the eye: a space named, a letter as typed. */
    static keyName(key) { return key === " " ? "Space" : String(key); }
    /** An element for the eye: its tag, its aria-label or id when it has one. */
    static describe(el) {
        var tag = String(el && el.tagName || "?").toLowerCase();
        var name = el && el.getAttribute ? (el.getAttribute("aria-label") || el.id) : "";
        return name ? tag + " “" + name + "”" : tag;
    }

    lamp() { return this._lamp.textContent; }
    lines() {
        var out = [];
        for (var i = 0; i < this._keys.children.length; i++) {
            var parts = [], line = this._keys.children[i];
            for (var j = 0; j < line.children.length; j++) parts.push(line.children[j].textContent);
            out.push(parts.join(" "));
        }
        return out;
    }

    dispose() {
        this._offTrace();
        this._offSteward();
        document.removeEventListener("focusin", this._onFocus, false);
        document.removeEventListener("focusout", this._onFocus, false);
        if (this.root.parentNode) this.root.parentNode.removeChild(this.root);
        this.branch.dissolve();
    }
}
