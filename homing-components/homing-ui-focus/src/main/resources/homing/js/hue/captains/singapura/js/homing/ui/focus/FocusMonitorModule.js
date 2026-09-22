// =============================================================================
// FocusMonitor — the page's logical-focus tree as a tree view, the holder of
// the keys marked. One row per node: the root, every branch (its holder), every
// leaf — the kind, the name, the component's class — indented by depth; the
// row of the member that holds the keys is lit. Redrawn on every notice of
// the focus party and every event of the steward, so it says at every moment
// exactly which component is in focus and under whom. A holder that is not
// in the tree — a member by id, the older way — is named below the tree.
// Tooling for a page: it reads the party and the steward and takes no keys;
// the steward's own state is the StewardMonitor's.
// A branch component: the caller makes a sub-branch for it and hands it in.
//
//   new FocusMonitor(branch, { host })
//     .root           the tree element, appended to the host
//     .refresh()      redrawn by call; it redraws itself on every change
//     .holderRow()    the lit row's element, or null
//     .dispose()
// =============================================================================

const _monitorOwner = Object.freeze({ toString: () => "focusMonitor" });

class FocusMonitor {
    constructor(branch, opts) {
        if (!branch) throw new Error("[FocusMonitor] a branch of its own is required");
        if (!opts || !opts.host) throw new Error("[FocusMonitor] opts.host is required");
        var self = this;
        branch.activate(_monitorOwner);
        this.branch = branch;
        this._rows = null;
        this._n = 0;
        var root = branch.createElement("tree", "div");
        css.addClass(root, fm_tree);
        root.setAttribute("role", "tree");
        root.setAttribute("aria-label", "Logical focus");
        opts.host.appendChild(root);
        this.root = root;
        this._offParty = focusParty.on(function () { self.refresh(); });
        this._offSteward = KeyboardStewardInstance.on(function () { self.refresh(); });
        this.refresh();
    }

    /** The tree read whole and drawn afresh: the rows minted on a sub-branch dissolved each time. */
    refresh() {
        var self = this, holder = KeyboardStewardInstance.holder(), seen = false;
        if (this._rows) this.branch.dissolveBranch(this._rows.name);
        var rows = this.branch.createBranch("rows-" + (++this._n));
        rows.activate(_monitorOwner);
        this._rows = rows;
        while (this.root.firstChild) this.root.removeChild(this.root.firstChild);
        this._holderRow = null;
        var seq = 0;
        function row(depth, kind, name, component, id) {
            var el = rows.createElement("r" + (++seq), "div");
            css.addClass(el, fm_row);
            el.setAttribute("role", "treeitem");
            el.setAttribute("aria-level", String(depth + 1));
            el.style.setProperty("--fm-depth", String(depth));
            var k = rows.createElement("k" + seq, "span");
            css.addClass(k, fm_kind);
            k.textContent = kind;
            var n = rows.createElement("n" + seq, "span");
            css.addClass(n, fm_name);
            n.textContent = name;
            el.appendChild(k);
            el.appendChild(n);
            if (component) {
                var c = rows.createElement("c" + seq, "span");
                css.addClass(c, fm_component);
                c.textContent = component;
                el.appendChild(c);
            }
            if (id !== null && id === holder) { css.addClass(el, fm_row_holder); el.setAttribute("aria-selected", "true"); self._holderRow = el; seen = true; }
            self.root.appendChild(el);
        }
        row(0, "root", holder === null ? "no one holds the keys" : "", null, null);
        (function draw(branch, depth) {
            for (var i = 0; i < branch.members.length; i++) {
                var m = branch.members[i];
                row(depth, m.kind === "holder" ? "branch" : "leaf", m.name, m.component, m.id);
                if (m.branch) draw(m.branch, depth + 1);
            }
        })(focusParty.inspect(), 1);
        if (holder !== null && !seen) {
            var out = rows.createElement("outside", "div");
            css.addClass(out, fm_outside);
            out.textContent = "held outside the tree: " + holder;
            this.root.appendChild(out);
        }
        return this;
    }

    holderRow() { return this._holderRow; }

    dispose() {
        this._offParty();
        this._offSteward();
        if (this.root.parentNode) this.root.parentNode.removeChild(this.root);
        this.branch.dissolve();
    }
}
