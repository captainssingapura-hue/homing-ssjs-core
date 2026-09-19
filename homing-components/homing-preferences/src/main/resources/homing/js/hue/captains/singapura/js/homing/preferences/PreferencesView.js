// =============================================================================
// PreferencesView — a master slot on the left, a detail slot on the right.
//
//   mountPreferencesView(branch, host, registry) → { select(path), dispose() }
//
//   branch     the caller's, activated; the view builds on a child of it
//   host       the element the view fills
//   registry   a site's PREFERENCES: { tree, master: {module, export, params},
//              nodes: { path: { label, summary, widget: {module, export, params} } } }
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
        var fn = m[entry.export || "construct"];
        if (typeof fn !== "function") throw new Error("[PreferencesView] " + entry.module + " has no export " + (entry.export || "construct"));
        return fn;
    });
}

function mountPreferencesView(branch, host, registry) {
    if (!branch)   throw new Error("mountPreferencesView: branch is required");
    if (!host)     throw new Error("mountPreferencesView: host is required");
    if (!registry || !registry.nodes || !registry.master) throw new Error("mountPreferencesView: registry is required");

    var own = branch.createBranch("preferencesView");
    own.activate(_viewOwner);

    var root = own.createElement("root", "div");
    css.addClass(root, pv_root);
    var masterPane = own.createElement("master", "div");
    css.addClass(masterPane, pv_master);
    var detailPane = own.createElement("detail", "div");
    css.addClass(detailPane, pv_detail);
    root.appendChild(masterPane);
    root.appendChild(detailPane);
    host.appendChild(root);

    var masterBranch = own.createBranch("masterSlot");
    masterBranch.activate(_viewOwner);
    var detailBranch = own.createBranch("detailSlot");
    detailBranch.activate(_viewOwner);
    var masterSlot = createWidgetSlot({ branch: masterBranch, host: masterPane });
    var detailSlot = createWidgetSlot({ branch: detailBranch, host: detailPane });

    var disposed = false;
    var wanted = null;        // the path last asked for, so a slow import cannot show a stale one

    function select(path) {
        var node = registry.nodes[path];
        if (!node) throw new Error("[PreferencesView] no node at '" + path + "'");
        wanted = path;
        if (!node.widget) { detailSlot.hide(); return Promise.resolve(null); }
        if (detailSlot.has(path)) { return Promise.resolve(detailSlot.show(path)); }
        return _load(node.widget).then(function (construct) {
            if (disposed || wanted !== path) return null;
            return detailSlot.show(path, construct, node.widget.params);
        }).catch(function (e) { console.error("[PreferencesView] widget for '" + path + "' failed", e); return null; });
    }

    var firstPath = Object.keys(registry.nodes)[0];
    var ready = _load(registry.master).then(function (construct) {
        if (disposed) return null;
        var master = masterSlot.show("master", construct, registry.master.params);
        if (typeof master.onSelect === "function") master.onSelect(function (path) { select(path); });
        if (typeof master.select === "function") master.select(firstPath);
        else select(firstPath);
        return master;
    });
    ready.catch(function (e) { console.error("[PreferencesView] master failed", e); });

    return Object.freeze({
        select: select,
        ready: ready,
        dispose: function () {
            if (disposed) return;
            disposed = true;
            detailSlot.disposeAll();
            masterSlot.disposeAll();
            own.dissolve();
        }
    });
}
