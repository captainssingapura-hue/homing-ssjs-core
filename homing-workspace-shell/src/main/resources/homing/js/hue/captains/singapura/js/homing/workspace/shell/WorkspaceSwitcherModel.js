// WorkspaceSwitcherModel.js — the switcher's pure half. No DOM, no window.
//
// Everything here is a function of its arguments: the tree of kinds, the list of
// instances, the URL a choice navigates to. The view (WorkspaceSwitcher.js) owns
// the elements; this owns the decisions, which is what makes them testable.
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java WorkspaceSwitcherModel declaration — do not add import/export
// lines here.

var DEFAULT_SECTION = "Workspaces";

/**
 * The group's kinds — [{kind, title, section, sectionSlug}] as the server
 * served them, section order then spec order — → TreeRenderer data: sections at
 * L1, kinds at L2. RFC 0058: this IS the group's inner catalogue, and the L1
 * segment is the slug the server minted, so the tree the switcher draws and
 * the path the anchor names are one derivation.
 */
function kindTreeData(kinds, currentKind) {
    var order = [], byName = {}, slugOf = {};
    for (var i = 0; i < kinds.length; i++) {
        var s = kinds[i].section || DEFAULT_SECTION;
        if (!byName[s]) { byName[s] = []; order.push(s); slugOf[s] = kinds[i].sectionSlug || "n"; }
        byName[s].push(kinds[i]);
    }
    return {
        level: "L0", segment: "ws",
        display: { label: "Workspaces", badge: "", note: "", kind: "root" },
        children: order.map(function (name) {
            return {
                level: "L1", segment: slugOf[name],
                display: { label: name, badge: "", note: "", kind: "section" },
                children: byName[name].map(function (k) {
                    return {
                        level: "L2", segment: k.kind,
                        display: { label: k.title || k.kind,
                                   badge: k.kind === currentKind ? "current" : "",
                                   note: "", kind: "workspaceKind" },
                        children: []
                    };
                })
            };
        })
    };
}

/** The kind id a tree selection names, or null for a section row. */
function kindOfSelection(sel) {
    if (!sel || sel.kind !== "workspaceKind") return null;
    var parts = String(sel.namePath || "").split("/");
    return parts[parts.length - 1] || null;
}

/** Positional path [section, kind] for TreeRenderer.selectPath, or null. */
function pathOfKind(kinds, kind) {
    var groups = kindTreeData(kinds, null).children;
    for (var g = 0; g < groups.length; g++) {
        var kids = groups[g].children;
        for (var k = 0; k < kids.length; k++) {
            if (kids[k].segment === kind) return [g, k];
        }
    }
    return null;
}

/** Catalogue rows → TreeRenderer data: a flat list under a hidden root. */
function instanceListData(rows, currentId) {
    return {
        level: "L0", segment: "instances",
        display: { label: "Instances", badge: "", note: "", kind: "root" },
        children: (rows || []).map(function (r) {
            return {
                level: "L1", segment: r.id,
                display: { label: r.name || "(unnamed)",
                           badge: r.id === currentId ? "open" : (r.isDefault ? "default" : ""),
                           note: "", kind: "workspaceInstance" },
                children: []
            };
        })
    };
}

/** The instance id a list selection names, or null. */
function instanceOfSelection(sel) {
    return (sel && sel.kind === "workspaceInstance") ? (sel.namePath || null) : null;
}

/** Positional path [i] for TreeRenderer.selectPath, or null. */
function pathOfInstance(rows, id) {
    for (var i = 0; i < (rows || []).length; i++) if (rows[i].id === id) return [i];
    return null;
}

/** A row may be deleted when it is neither the default nor the one open now. */
function canDelete(row, currentId) {
    return !!row && !row.isDefault && row.id !== currentId;
}

/**
 * The URL a choice navigates to.
 *
 *   o.kinds       the group's kinds as served — [{kind, title, section, sectionSlug}]
 *   o.groupId     the group's id (null for a kind no group holds)
 *   o.kind        the chosen kind
 *   o.instanceId  an existing instance (→ ?workspace=)
 *   o.name        a new instance to mint (→ ?name=, which the directory resolves)
 *
 * RFC 0058 — the kind is the ANCHOR. The path and query are the current page's,
 * because a kind lives inside its group and the group is where the page already
 * is: a kind change is `#ws/<section>/<kind>` on the same address, and the
 * chrome reloads on hashchange. On a path address nothing else moves. On the
 * legacy flat address a `ws_kind` in the query would contradict the anchor, so
 * it is rewritten to the canonical `ws_group` — the anchor names the kind, the
 * query names the group, and the two agree.
 *
 * workspace and name are plain query reads — the directory takes them from the
 * address as given — and, with slowmo, are scoped to one kind and one instance,
 * so all three are cleared before the choice is written. Never both workspace
 * and name. The anchor is ALWAYS written, a same-kind change included: current()
 * carries no fragment, and a reload without one would open the group's default.
 */
function targetUrl(current, o) {
    var q = current.indexOf("?");
    var path   = q < 0 ? current : current.slice(0, q);
    var params = new URLSearchParams(q < 0 ? "" : current.slice(q + 1));
    params.delete("workspace"); params.delete("name"); params.delete("slowmo");
    if (params.has("ws_kind")) {
        params.delete("ws_kind");
        if (o.groupId) params.set("ws_group", o.groupId);
        else           params.set("ws_kind", o.kind);     // no group: the kind stays the address
    }
    if (o.name)            params.set("name", o.name);
    else if (o.instanceId) params.set("workspace", o.instanceId);
    var s = params.toString();
    var anchor = anchorOf(o.kinds, o.kind);
    return path + (s ? "?" + s : "") + (anchor ? "#" + anchor : "");
}
