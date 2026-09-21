// =============================================================================
// MenuTree — the shape of a context menu's tree, and the walk over it, with
// no DOM: the same record a Java ContextMenuRegistry stamps as MENUS and
// the steward's builder yields. A type is { kind, nodes }; a node is
// { id, label, icon?, hint?, section?, nodes? }, three levels deep at most
// — the Java tree's levels, held here for a type declared in JS.
//
//   MenuTree.MAX_DEPTH               3
//   MenuTree.check(kind, nodes)      throws on what Java would refuse: an id or
//                                    a label missing, an id repeated across
//                                    levels, a fourth level; returns nodes
//   MenuTree.rows(type)              every node, parents before children
//   MenuTree.find(type, id)          the node, or null
//   MenuTree.path(type, id)          the nodes from the first level down to it, or null
//   MenuTree.divided(nodes)          [{ node, divider }]: divider true where the
//                                    section changes from the row before
// =============================================================================

var MenuTree = Object.freeze({
    MAX_DEPTH: 3,

    check: function (kind, nodes) {
        var ids = {};
        function walk(list, depth, under) {
            if (!Array.isArray(list)) throw new Error("[MenuTree] " + kind + ": " + under + " lists no rows");
            if (depth > MenuTree.MAX_DEPTH) { if (list.length) throw new Error("[MenuTree] " + kind + ": " + under + " is at the last level and lists " + list.length); return; }
            for (var i = 0; i < list.length; i++) {
                var n = list[i];
                if (!n || typeof n.id !== "string" || !n.id) throw new Error("[MenuTree] " + kind + ": a row under " + under + " needs an id");
                if (typeof n.label !== "string" || !n.label) throw new Error("[MenuTree] " + kind + ": " + n.id + " has no label");
                if (ids[n.id]) throw new Error("[MenuTree] " + kind + ": row id repeated: " + n.id);
                ids[n.id] = true;
                if (n.icon != null && typeof n.icon !== "string") throw new Error("[MenuTree] " + kind + ": " + n.id + " names an icon that is not a word");
                if (n.nodes) walk(n.nodes, depth + 1, n.id);
            }
        }
        if (!Array.isArray(nodes) || !nodes.length) throw new Error("[MenuTree] " + kind + ": a kind lists no rows");
        walk(nodes, 1, kind);
        return nodes;
    },

    rows: function (type) {
        var out = [];
        (function walk(list) { for (var i = 0; i < list.length; i++) { out.push(list[i]); if (list[i].nodes) walk(list[i].nodes); } })(type.nodes || []);
        return out;
    },

    find: function (type, id) {
        var rows = MenuTree.rows(type);
        for (var i = 0; i < rows.length; i++) if (rows[i].id === id) return rows[i];
        return null;
    },

    path: function (type, id) {
        function walk(list, trail) {
            for (var i = 0; i < list.length; i++) {
                var n = list[i], t = trail.concat([n]);
                if (n.id === id) return t;
                if (n.nodes) { var deeper = walk(n.nodes, t); if (deeper) return deeper; }
            }
            return null;
        }
        return walk(type.nodes || [], []);
    },

    divided: function (nodes) {
        var out = [];
        for (var i = 0; i < nodes.length; i++) {
            var s = nodes[i].section || 0, prev = i > 0 ? (nodes[i - 1].section || 0) : s;
            out.push({ node: nodes[i], divider: i > 0 && s !== prev });
        }
        return out;
    }
});
