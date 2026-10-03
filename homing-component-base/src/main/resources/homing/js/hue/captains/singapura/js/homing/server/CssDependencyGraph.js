// =============================================================================
// CssDependencyGraph — the CSS manager's affiliate: what depends on what,
// and in what order it loads (RFC 0064).
//
// Pure. No DOM, no fetch, no theme: a graph of CSS group names with three
// facts per node —
//
//   deps    the groups this one's classes depend on, declared in Java per
//           CssClass and derived per group, carried to the client as data
//           inside the served group module;
//   varies  whether its sheet changes with the theme - a node the server marks
//           varies:false is served once and left alone by a switch;
//   known   whether the node has been declared at all, or is only referred
//           to by another's deps — a node the manager will have to load by
//           name, without its handles.
//
// and one operation: a PLAN. Given the nodes to load, the plan is a list of
// WAVES, by Kahn's rule: a wave is every remaining node whose dependencies all
// sit in earlier waves. A wave may load in parallel precisely because nothing
// in it is waiting on anything pending; waves run one after another. A cycle
// leaves nodes that never qualify, and is refused with their names.
//
// The graph is merged from subgraphs as group modules arrive, so by the time a
// theme switch happens it holds everything on the page. snapshot() is the
// frozen projection the workbench draws (RFC 0063: data, never handles).
// =============================================================================


function createCssDependencyGraph() {
    // id → { deps: string[], varies: boolean, known: boolean, seq: number }
    const nodes = new Map();
    let seq = 0;

    function node(id) {
        let n = nodes.get(id);
        if (!n) { n = { deps: [], varies: true, known: false, seq: seq++ }; nodes.set(id, n); }
        return n;
    }

    /**
     * Merge a subgraph: { <id>: { deps: [...], varies?: bool }, ... }. A node
     * declared twice keeps the first declaration's order and the union of its
     * dependencies; a dependency named but not declared becomes an unknown
     * node until its own module arrives.
     */
    function merge(subgraph) {
        if (!subgraph || typeof subgraph !== "object") {
            throw new TypeError("CssDependencyGraph.merge: subgraph must be an object");
        }
        for (const id of Object.keys(subgraph)) {
            const decl = subgraph[id] || {};
            const n = node(id);
            n.known = true;
            // A node the server marks varies:false is served once, without a
            // theme, and left alone by a switch: its sheet does not change.
            if (decl.varies === false) n.varies = false;
            for (const d of decl.deps || []) {
                if (d === id) throw new Error("CssDependencyGraph: " + id + " depends on itself");
                node(d);
                if (n.deps.indexOf(d) < 0) n.deps.push(d);
            }
        }
    }

    function has(id)   { return nodes.has(id); }
    function deps(id)  { return nodes.has(id) ? nodes.get(id).deps.slice() : []; }
    function varies(id) { return !nodes.has(id) || nodes.get(id).varies; }
    function known(id) { return nodes.has(id) && nodes.get(id).known; }

    /** Every node reachable from the ids, the ids included. */
    function closureOf(ids) {
        const out = [];
        const seen = new Set();
        const walk = function (id) {
            if (seen.has(id)) return;
            seen.add(id);
            for (const d of deps(id)) walk(d);
            out.push(id);
        };
        for (const id of ids) walk(id);
        return out;
    }

    /**
     * The waves for loading exactly these group ids, by Kahn's rule over the ids
     * given — a dependency outside the set is treated as already satisfied,
     * which is what "load only the missing ones" needs. Within a wave, nodes are
     * in arrival order.
     */
    function plan(ids) {
        const set = new Set(ids);
        const waves = [];
        const placed = new Set();
        let remaining = Array.from(set);
        while (remaining.length) {
            const wave = remaining.filter(function (id) {
                return deps(id).every(function (d) { return placed.has(d) || !set.has(d); });
            });
            if (!wave.length) {
                throw new Error("CssDependencyGraph: cycle among " + remaining.slice().sort().join(", "));
            }
            wave.sort(function (a, b) { return nodes.get(a).seq - nodes.get(b).seq; });
            waves.push(wave);
            for (const id of wave) placed.add(id);
            remaining = remaining.filter(function (id) { return !placed.has(id); });
        }
        return waves;
    }

    /** Frozen, plain data — the workbench's whole view of the graph. */
    function snapshot() {
        const out = [];
        for (const [id, n] of nodes) {
            out.push(Object.freeze({ id: id, deps: Object.freeze(n.deps.slice()), varies: n.varies, known: n.known }));
        }
        out.sort(function (a, b) { return nodes.get(a.id).seq - nodes.get(b.id).seq; });
        return Object.freeze(out);
    }

    return Object.freeze({ merge, has, deps, varies, known, closureOf, plan, snapshot });
}
