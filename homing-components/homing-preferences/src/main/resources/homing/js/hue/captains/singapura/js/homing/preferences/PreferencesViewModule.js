// =============================================================================
// PreferencesView — a master slot on the left, a detail slot on the right.
// A branch component: the caller makes a sub-branch for it and hands it in;
// dispose() dissolves it.
//
//   new PreferencesView(branch, host, registry)
//     .select(path) → Promise<widget|null>
//     .ready        → Promise<master>
//     .dispose()
//
//   branch     the view's own, handed unactivated
//   host       the element the view fills
//   registry   a site's PREFERENCES: { tree, master: {module, export, params},
//              nodes: { path: { label, summary, widget: {module, export, params} } } }
//              where `export` names the widget's CLASS in its module
//
// Both slots are WidgetSlots: a widget is constructed the first time its node
// is chosen and kept until dispose(); the shown one is in the DOM, the rest
// are not. A widget's module is imported only then, through the serving
// context, so it joins the page's chain rather than forking a second party.
//
// The master is a widget too, constructed from registry.master with the tree
// in its params, and the view drives it through the surface a tree widget
// offers: onSelect(fn) to hear a choice, select(path) to make one. A detail
// widget it drives through nothing — construct, show, hide — and what the
// widget writes it writes through the steward, which is everyone's party.
// =============================================================================

const _viewOwner = Object.freeze({ toString: () => "preferencesView" });

var _loaded = new Map();   // module url → Promise<module>; one import per address per page

function _load(entry) {
    var url = withServingContext(entry.module);
    var p = _loaded.get(url);
    if (!p) {
        p = import(url);
        _loaded.set(url, p);
    }
    return p.then(function (m) {
        var Widget = m[entry.export];
        if (typeof Widget !== "function") throw new Error("[PreferencesView] " + entry.module + " has no class " + entry.export);
        return Widget;
    });
}

class PreferencesView {
    constructor(branch, host, registry) {
        if (!branch)   throw new Error("[PreferencesView] a branch of its own is required");
        if (!host)     throw new Error("[PreferencesView] host is required");
        if (!registry || !registry.nodes || !registry.master) throw new Error("[PreferencesView] registry is required");
        var self = this;
        branch.activate(_viewOwner);
        this._branch = branch;
        this._registry = registry;
        this._disposed = false;
        this._wanted = null;        // the path last asked for, so a slow import cannot show a stale one

        var root = branch.createElement("root", "div");
        css.addClass(root, pv_root);
        var masterPane = branch.createElement("master", "div");
        css.addClass(masterPane, pv_master);
        var detailPane = branch.createElement("detail", "div");
        css.addClass(detailPane, pv_detail);
        root.appendChild(masterPane);
        root.appendChild(detailPane);
        host.appendChild(root);
        this.root = root;

        var masterBranch = branch.createBranch("masterSlot");
        masterBranch.activate(_viewOwner);
        var detailBranch = branch.createBranch("detailSlot");
        detailBranch.activate(_viewOwner);
        this._masterSlot = new WidgetSlot({ branch: masterBranch, host: masterPane });
        this._detailSlot = new WidgetSlot({ branch: detailBranch, host: detailPane });

        var firstPath = Object.keys(registry.nodes)[0];
        this.ready = _load(registry.master).then(function (Master) {
            if (self._disposed) return null;
            var master = self._masterSlot.show("master", Master, registry.master.params);
            if (typeof master.onSelect === "function") master.onSelect(function (path) { self.select(path); });
            if (typeof master.select === "function") master.select(firstPath);
            else self.select(firstPath);
            return master;
        });
        this.ready.catch(function (e) { console.error("[PreferencesView] master failed", e); });
    }

    select(path) {
        var self = this;
        var node = this._registry.nodes[path];
        if (!node) throw new Error("[PreferencesView] no node at '" + path + "'");
        this._wanted = path;
        if (!node.widget) { this._detailSlot.hide(); return Promise.resolve(null); }
        if (this._detailSlot.has(path)) { return Promise.resolve(this._detailSlot.show(path)); }
        return _load(node.widget).then(function (Widget) {
            if (self._disposed || self._wanted !== path) return null;
            return self._detailSlot.show(path, Widget, node.widget.params);
        }).catch(function (e) { console.error("[PreferencesView] widget for '" + path + "' failed", e); return null; });
    }

    dispose() {
        if (this._disposed) return;
        this._disposed = true;
        this._detailSlot.disposeAll();
        this._masterSlot.disposeAll();
        this._branch.dissolve();
    }
}
