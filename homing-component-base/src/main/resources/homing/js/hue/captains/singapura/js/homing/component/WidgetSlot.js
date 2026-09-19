// =============================================================================
// WidgetSlot — hosts widgets the way the workspace does: any number
// constructed and kept, one of them in the DOM.
//
//   createWidgetSlot({ branch, host }) → slot
//       branch   the slot's own, ACTIVATED by the caller; each widget gets a
//                child of it, named from its key, handed unactivated. A key is
//                any string; the branch name is the key with what the party
//                does not allow replaced, made distinct if two keys collide
//       host     the element the shown widget's root is attached to
//
//   slot.show(key, construct, params) → controller
//       the first show of a key calls construct(branch, params) and keeps
//       what comes back; every show attaches the kept root to the host, after
//       taking the shown one out. setActive(false) is told to the one going
//       out, setActive(true) to the one coming in, when they have it.
//   slot.hide()                    takes the shown widget out; keeps it
//   slot.current()                 the key shown, or null
//   slot.has(key), slot.keys()
//   slot.controller(key)           the kept controller, for a holder that operates
//                                  its widget directly; null when not kept
//   slot.dispose(key)              hides it if shown; dispose() if it has one;
//                                  then its branch dissolves and the key is gone
//   slot.disposeAll()
//
// The contract a widget meets is the base's Widget: construct(branch, params)
// returns a plain object with root, optionally setActive(bool) and dispose(),
// and whatever surface the widget offers a holder that knows it. The root is
// the only element the slot ever touches; a widget's branch is the only thing
// the slot ever dissolves. Attaching and detaching is how a widget is shown
// and hidden — not display, not visibility — so a hidden widget can neither
// be focused nor found, which is what hidden means.
// =============================================================================

function createWidgetSlot(opts) {
    if (!opts || !opts.branch) throw new Error("createWidgetSlot: opts.branch is required");
    if (!opts.host)            throw new Error("createWidgetSlot: opts.host is required");
    var branch = opts.branch, host = opts.host;
    var kept = new Map();      // key → { name, controller }
    var shown = null;          // the key in the DOM, or null
    var names = new Set();     // branch names in use, for a key that sanitises like another

    function branchNameFor(key) {
        var base = key.replace(/[^A-Za-z0-9_-]/g, "_") || "w", name = base, n = 1;
        while (names.has(name)) name = base + "_" + (++n);
        names.add(name);
        return name;
    }

    function validate(key, controller) {
        if (!controller || !controller.root) {
            throw new Error("[WidgetSlot] widget '" + key + "': construct must return { root, setActive?, dispose? } — got " + controller);
        }
        return controller;
    }

    function tell(entry, active) {
        if (entry && typeof entry.controller.setActive === "function") {
            try { entry.controller.setActive(active); } catch (e) { console.error("[WidgetSlot] setActive threw", e); }
        }
    }

    function hide() {
        if (shown === null) return;
        var entry = kept.get(shown);
        tell(entry, false);
        if (entry.controller.root.parentNode === host) host.removeChild(entry.controller.root);
        shown = null;
    }

    function show(key, construct, params) {
        if (typeof key !== "string" || !key) throw new Error("[WidgetSlot] show: key must be a non-empty string");
        var entry = kept.get(key);
        if (!entry) {
            if (typeof construct !== "function") throw new Error("[WidgetSlot] show: no widget kept as '" + key + "' and no construct given");
            var name = branchNameFor(key);
            var own = branch.createBranch(name);
            var controller = validate(key, construct(own, params || {}));
            entry = { name: name, controller: controller };
            kept.set(key, entry);
        }
        if (shown === key) return entry.controller;
        hide();
        host.appendChild(entry.controller.root);
        shown = key;
        tell(entry, true);
        return entry.controller;
    }

    function dispose(key) {
        var entry = kept.get(key);
        if (!entry) return;
        if (shown === key) hide();
        if (typeof entry.controller.dispose === "function") {
            try { entry.controller.dispose(); } catch (e) { console.error("[WidgetSlot] dispose threw", e); }
        }
        try { branch.dissolveBranch(entry.name); } catch (e) { console.error("[WidgetSlot] dissolve threw", e); }
        names.delete(entry.name);
        kept.delete(key);
    }

    function disposeAll() {
        Array.from(kept.keys()).forEach(dispose);
    }

    return Object.freeze({
        show: show,
        hide: hide,
        current: function () { return shown; },
        has: function (key) { return kept.has(key); },
        keys: function () { return Array.from(kept.keys()); },
        controller: function (key) { var e = kept.get(key); return e ? e.controller : null; },
        dispose: dispose,
        disposeAll: disposeAll
    });
}
