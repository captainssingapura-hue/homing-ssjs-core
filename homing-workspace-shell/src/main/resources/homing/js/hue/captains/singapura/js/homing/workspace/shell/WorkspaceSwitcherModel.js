// WorkspaceSwitcherModel.js — the switcher's pure half. No DOM, no window.
//
// Everything here is a function of its arguments: the tree of kinds, the list of
// instances, the URL a choice navigates to. The view (WorkspaceSwitcher.js) owns
// the elements; this owns the decisions, which is what makes them testable.
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java WorkspaceSwitcherModel declaration — do not add import/export
// lines here.

var DEFAULT_GROUP = "Workspaces";

function _slug(s) {
    return String(s || "").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "") || "group";
}

/** [{kind, title, group?}] → TreeRenderer data: groups at L1, kinds at L2. */
function kindTreeData(kinds, currentKind) {
    var order = [], byName = {};
    for (var i = 0; i < kinds.length; i++) {
        var g = kinds[i].group || DEFAULT_GROUP;
        if (!byName[g]) { byName[g] = []; order.push(g); }
        byName[g].push(kinds[i]);
    }
    return {
        level: "L0", segment: "kinds",
        display: { label: "Workspaces", badge: "", note: "", kind: "root" },
        children: order.map(function (name) {
            return {
                level: "L1", segment: _slug(name),
                display: { label: name, badge: "", note: "", kind: "group" },
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

/** The kind id a tree selection names, or null for a group row. */
function kindOfSelection(sel) {
    if (!sel || sel.kind !== "workspaceKind") return null;
    var parts = String(sel.namePath || "").split("/");
    return parts[parts.length - 1] || null;
}

/** Positional path [group, kind] for TreeRenderer.selectPath, or null. */
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
 *   o.kind        the chosen kind
 *   o.currentKind the kind now open
 *   o.base        "/goto?app=<simpleName>" — where a KIND change must go
 *   o.instanceId  an existing instance (→ ?workspace=)
 *   o.name        a new instance to mint (→ ?name=, which the directory resolves)
 *
 * TWO SHAPES, because the two parameters are two kinds of thing. workspace and
 * name are plain query reads — the directory takes them from the address as
 * given — so a same-kind change edits the current URL and every other parameter
 * survives. ws_kind is a TYPED param: a catalogue route stamps it, and editing
 * it in the query of /cat/workspace changes nothing. So a kind change goes to
 * the goto base, which resolves the pair to its authentic path when one exists
 * and to the flat render otherwise. Without a base (an older chrome) it falls
 * back to the query edit, which is what the old modal always did.
 *
 * workspace, name and slowmo are scoped to one kind and one instance, so all
 * three are cleared before the choice is written. Never both workspace and name.
 */
function targetUrl(current, o) {
    var kindChange = !!(o.kind && o.kind !== o.currentKind);
    var path, params;
    if (kindChange && o.base) {
        var b = o.base.indexOf("?");
        path   = b < 0 ? o.base : o.base.slice(0, b);
        params = new URLSearchParams(b < 0 ? "" : o.base.slice(b + 1));
    } else {
        var q = current.indexOf("?");
        path   = q < 0 ? current : current.slice(0, q);
        params = new URLSearchParams(q < 0 ? "" : current.slice(q + 1));
    }
    params.delete("workspace"); params.delete("name"); params.delete("slowmo");
    if (kindChange) params.set("ws_kind", o.kind);
    if (o.name)            params.set("name", o.name);
    else if (o.instanceId) params.set("workspace", o.instanceId);
    var s = params.toString();
    return path + (s ? "?" + s : "");
}
