// =============================================================================
// WidgetSlot — hosts widgets the way the workspace does: any number
// constructed and kept, one of them in the DOM.
//
//   new WidgetSlot({ branch, host })
//       branch   the slot's own, ACTIVATED by the caller; each widget gets a
//                child of it, named from its key, handed unactivated. A key is
//                any string; the branch name is the key with what the party
//                does not allow replaced, made distinct if two keys collide
//       host     the element the shown widget's root is attached to
//
//   slot.show(key, Widget, params) → the widget
//       the first show of a key constructs `new Widget(branch, params)` and
//       keeps it; every show attaches the kept root to the host, after taking
//       the shown one out. setActive(false) is told to the one going out,
//       setActive(true) to the one coming in, when they have it.
//   slot.hide()                    takes the shown widget out; keeps it
//   slot.current()                 the key shown, or null
//   slot.has(key), slot.keys()
//   slot.widget(key)               the kept widget, for a holder that operates
//                                  it directly; null when not kept
//   slot.dispose(key)              hides it if shown; dispose() if it has one;
//                                  then its branch dissolves and the key is gone
//   slot.disposeAll()
//
// The contract a widget meets is the base's Widget: a class whose constructor
// takes (branch, params), whose instance has root, optionally setActive(bool)
// and dispose(), and whatever surface the widget offers a holder that knows
// it. The root is the only element the slot ever touches; a widget's branch is
// the only thing the slot ever dissolves. Attaching and detaching is how a
// widget is shown and hidden — not display, not visibility — so a hidden
// widget can neither be focused nor found, which is what hidden means.
// =============================================================================

class WidgetSlot {
    constructor(opts) {
        if (!opts || !opts.branch) throw new Error("[WidgetSlot] opts.branch is required");
        if (!opts.host)            throw new Error("[WidgetSlot] opts.host is required");
        this._branch = opts.branch;
        this._host = opts.host;
        this._kept = new Map();      // key → { name, widget }
        this._shown = null;          // the key in the DOM, or null
        this._names = new Set();     // branch names in use, for a key that sanitises like another
    }

    _branchNameFor(key) {
        var base = key.replace(/[^A-Za-z0-9_-]/g, "_") || "w", name = base, n = 1;
        while (this._names.has(name)) name = base + "_" + (++n);
        this._names.add(name);
        return name;
    }

    _tell(entry, active) {
        if (entry && typeof entry.widget.setActive === "function") {
            try { entry.widget.setActive(active); } catch (e) { console.error("[WidgetSlot] setActive threw", e); }
        }
    }

    show(key, Widget, params) {
        if (typeof key !== "string" || !key) throw new Error("[WidgetSlot] show: key must be a non-empty string");
        var entry = this._kept.get(key);
        if (!entry) {
            if (typeof Widget !== "function") throw new Error("[WidgetSlot] show: no widget kept as '" + key + "' and no class given");
            var name = this._branchNameFor(key);
            var own = this._branch.createBranch(name);
            var widget = new Widget(own, params || {});
            if (!widget || !widget.root) throw new Error("[WidgetSlot] widget '" + key + "': the class must construct an instance with a root");
            entry = { name: name, widget: widget };
            this._kept.set(key, entry);
        }
        if (this._shown === key) return entry.widget;
        this.hide();
        this._host.appendChild(entry.widget.root);
        this._shown = key;
        this._tell(entry, true);
        return entry.widget;
    }

    hide() {
        if (this._shown === null) return;
        var entry = this._kept.get(this._shown);
        this._tell(entry, false);
        if (entry.widget.root.parentNode === this._host) this._host.removeChild(entry.widget.root);
        this._shown = null;
    }

    current() { return this._shown; }
    has(key)  { return this._kept.has(key); }
    keys()    { return Array.from(this._kept.keys()); }
    widget(key) { var e = this._kept.get(key); return e ? e.widget : null; }

    dispose(key) {
        var entry = this._kept.get(key);
        if (!entry) return;
        if (this._shown === key) this.hide();
        if (typeof entry.widget.dispose === "function") {
            try { entry.widget.dispose(); } catch (e) { console.error("[WidgetSlot] dispose threw", e); }
        }
        try { this._branch.dissolveBranch(entry.name); } catch (e) { console.error("[WidgetSlot] dissolve threw", e); }
        this._names.delete(entry.name);
        this._kept.delete(key);
    }

    disposeAll() {
        Array.from(this._kept.keys()).forEach(this.dispose, this);
    }
}
