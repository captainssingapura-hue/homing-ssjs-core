// =============================================================================
// SplitPane — the splitter: a tree of panes side by side or stacked, each
// split sharing its space by ratio, a draggable divider between neighbours.
//
//   mountSplitPane({ branch, host, layout, minPanePx?, onEvent? }) → split
//
//   layout:  { kind: "leaf", slotId }
//          | { kind: "split", orientation: "horizontal" | "vertical",
//              children: [ { pane: <layout>, ratio? }, … ] }      two or more
//     "horizontal" puts the children in a row (the divider moves left and
//     right); "vertical" stacks them. Ratios are shares of the split; they
//     are normalised to sum to one, and equal when absent.
//
//   split.el
//   split.slot(slotId)         → the leaf element, the host for what fills it
//   split.slots()              → the slot ids, in tree order
//   split.layout()             → the tree with the ratios as they are now
//   split.setRatios(path, ratios)   re-share one split; reports RatioChanged
//   split.dispose()
//
// The layout is fixed for the life of the splitter: the caller decides the
// tree, the user decides the shares. Splitting and merging at runtime come
// with the multi-tab pane that needs them. Each share is a custom property
// on the child, --sp-ratio, read by its class; the minimum a pane may be is
// --sp-min on the root. Nothing is positioned by hand.
//
// A drag moves the divider between two neighbours and re-shares those two,
// each kept at the minimum; the change is reported once, on release, as
// SplitEvents.RatioChanged(path, ratios), path naming the split by child
// indexes from the root. `css` is injected with the styles import.
// =============================================================================

const _splitOwner = Object.freeze({ toString: () => "splitPane" });
var _seq = 0;

function mountSplitPane(opts) {
    if (!opts || !opts.branch) throw new Error("[SplitPane] opts.branch is required");
    if (!opts.host) throw new Error("[SplitPane] opts.host is required");
    var minPx = opts.minPanePx == null ? 40 : Math.max(0, opts.minPanePx | 0);
    var sink = typeof opts.onEvent === "function" ? opts.onEvent : null;
    var branchName = "split_" + (++_seq);
    var branch = opts.branch.createBranch(branchName);
    branch.activate(_splitOwner);

    var leaves = new Map();        // slotId → leaf element
    var splits = new Map();        // path → { el, orientation, children: [{ el, node }], dividers: [el] }
    var tree = _validate(opts.layout, "layout", new Set());

    var root = branch.createElement("root", "div");
    css.addClass(root, sp_root);
    root.style.setProperty("--sp-min", minPx + "px");
    root.appendChild(_render(tree, ""));
    opts.host.appendChild(root);

    // ── Reporting ─────────────────────────────────────────────────────────
    function _fire(ev) {
        if (!sink) return;
        try { sink(ev); }
        catch (e) { console.error("[SplitPane] onEvent threw on " + ev.kind + ":", e); }
    }

    // ── The tree ──────────────────────────────────────────────────────────
    function _validate(node, at, seen) {
        if (!node || typeof node !== "object") throw new Error("[SplitPane] " + at + " must be a leaf or a split");
        if (node.kind === "leaf") {
            if (typeof node.slotId !== "string" || !node.slotId) throw new Error("[SplitPane] " + at + ".slotId must be a non-empty string");
            if (seen.has(node.slotId)) throw new Error("[SplitPane] slot '" + node.slotId + "' appears twice");
            seen.add(node.slotId);
            return { kind: "leaf", slotId: node.slotId };
        }
        if (node.kind !== "split") throw new Error("[SplitPane] " + at + ".kind must be 'leaf' or 'split'");
        if (node.orientation !== "horizontal" && node.orientation !== "vertical") throw new Error("[SplitPane] " + at + ".orientation must be 'horizontal' or 'vertical'");
        if (!Array.isArray(node.children) || node.children.length < 2) throw new Error("[SplitPane] " + at + " needs two or more children");
        var children = [], sum = 0;
        for (var i = 0; i < node.children.length; i++) {
            var c = node.children[i];
            var r = c && c.ratio != null ? c.ratio : 1;
            if (typeof r !== "number" || !(r > 0)) throw new Error("[SplitPane] " + at + ".children[" + i + "].ratio must be a positive number");
            children.push({ pane: _validate(c && c.pane, at + ".children[" + i + "].pane", seen), ratio: r });
            sum += r;
        }
        for (var j = 0; j < children.length; j++) children[j].ratio = children[j].ratio / sum;
        return { kind: "split", orientation: node.orientation, children: children };
    }

    function _render(node, path) {
        var name = path === "" ? "root" : path.replace(/\//g, "_");
        if (node.kind === "leaf") {
            var leaf = branch.createElement("leaf-" + node.slotId.replace(/[^A-Za-z0-9_-]/g, "_"), "div");
            css.addClass(leaf, sp_leaf);
            leaf.setAttribute("data-slot", node.slotId);
            leaves.set(node.slotId, leaf);
            return leaf;
        }
        var horizontal = node.orientation === "horizontal";
        var el = branch.createElement("split-" + name, "div");
        css.addClass(el, sp_split, horizontal ? sp_split_h : sp_split_v);
        var entry = { el: el, orientation: node.orientation, children: [], dividers: [] };
        for (var i = 0; i < node.children.length; i++) {
            if (i > 0) {
                var divider = branch.createElement("divider-" + name + "-" + i, "div");
                css.addClass(divider, sp_divider, horizontal ? sp_divider_h : sp_divider_v);
                divider.setAttribute("role", "separator");
                divider.setAttribute("aria-orientation", horizontal ? "vertical" : "horizontal");
                _armDrag(divider, path, i - 1);
                entry.dividers.push(divider);
                el.appendChild(divider);
            }
            var child = branch.createElement("child-" + name + "-" + i, "div");
            css.addClass(child, sp_child, horizontal ? sp_child_h : sp_child_v);
            child.style.setProperty("--sp-ratio", String(node.children[i].ratio));
            child.appendChild(_render(node.children[i].pane, path === "" ? String(i) : path + "/" + i));
            entry.children.push({ el: child, node: node.children[i] });
            el.appendChild(child);
        }
        splits.set(path, entry);
        return el;
    }

    function _apply(path, ratios) {
        var entry = splits.get(path);
        for (var i = 0; i < ratios.length; i++) {
            entry.children[i].node.ratio = ratios[i];
            entry.children[i].el.style.setProperty("--sp-ratio", String(ratios[i]));
        }
    }
    function _ratiosOf(path) {
        var entry = splits.get(path), out = [];
        for (var i = 0; i < entry.children.length; i++) out.push(entry.children[i].node.ratio);
        return out;
    }

    // ── Drag a divider ────────────────────────────────────────────────────
    function _armDrag(divider, path, before) {
        divider.addEventListener("pointerdown", function (down) {
            if (down.button !== 0) return;
            var entry = splits.get(path), horizontal = entry.orientation === "horizontal";
            var a = entry.children[before], b = entry.children[before + 1];
            var start = horizontal ? down.clientX : down.clientY;
            var size = horizontal ? entry.el.getBoundingClientRect().width : entry.el.getBoundingClientRect().height;
            var a0 = a.node.ratio, b0 = b.node.ratio;
            if (!(size > 0)) return;
            var minShare = Math.min(minPx / size, (a0 + b0) / 2);
            var moved = false;
            css.addClass(divider, sp_divider_dragging);
            try { divider.setPointerCapture(down.pointerId); } catch (err) {}
            function onMove(e) {
                var delta = ((horizontal ? e.clientX : e.clientY) - start) / size;
                var na = Math.min(Math.max(a0 + delta, minShare), a0 + b0 - minShare);
                var nb = a0 + b0 - na;
                if (na === a.node.ratio) return;
                moved = true;
                a.node.ratio = na; b.node.ratio = nb;
                a.el.style.setProperty("--sp-ratio", String(na));
                b.el.style.setProperty("--sp-ratio", String(nb));
            }
            function onEnd(e) {
                divider.removeEventListener("pointermove", onMove);
                divider.removeEventListener("pointerup", onEnd);
                divider.removeEventListener("pointercancel", onEnd);
                css.removeClass(divider, sp_divider_dragging);
                try { divider.releasePointerCapture(down.pointerId); } catch (err) {}
                if (e.type !== "pointerup") { _apply(path, _restore(entry, before, a0, b0)); return; }
                if (moved) _fire(SplitEvents.RatioChanged(path, _ratiosOf(path)));
            }
            divider.addEventListener("pointermove", onMove);
            divider.addEventListener("pointerup", onEnd);
            divider.addEventListener("pointercancel", onEnd);
        });
    }
    function _restore(entry, before, a0, b0) {
        var out = [];
        for (var i = 0; i < entry.children.length; i++) out.push(i === before ? a0 : i === before + 1 ? b0 : entry.children[i].node.ratio);
        return out;
    }

    // ── The surface ───────────────────────────────────────────────────────
    function setRatios(path, ratios) {
        var entry = splits.get(path);
        if (!entry) throw new Error("[SplitPane] no split at '" + path + "'");
        if (!Array.isArray(ratios) || ratios.length !== entry.children.length) throw new Error("[SplitPane] setRatios: " + entry.children.length + " ratios expected at '" + path + "'");
        var sum = 0;
        for (var i = 0; i < ratios.length; i++) {
            if (typeof ratios[i] !== "number" || !(ratios[i] > 0)) throw new Error("[SplitPane] setRatios: ratios must be positive numbers");
            sum += ratios[i];
        }
        var shares = [];
        for (var j = 0; j < ratios.length; j++) shares.push(ratios[j] / sum);
        _apply(path, shares);
        _fire(SplitEvents.RatioChanged(path, shares));
    }
    function layout() {
        return (function copy(node) {
            if (node.kind === "leaf") return { kind: "leaf", slotId: node.slotId };
            var children = [];
            for (var i = 0; i < node.children.length; i++) children.push({ pane: copy(node.children[i].pane), ratio: node.children[i].ratio });
            return { kind: "split", orientation: node.orientation, children: children };
        })(tree);
    }
    function dispose() {
        if (root.parentNode) root.parentNode.removeChild(root);
        leaves.clear();
        splits.clear();
        opts.branch.dissolveBranch(branchName);
    }

    return Object.freeze({
        el: root,
        slot: function (slotId) { return leaves.get(slotId) || null; },
        slots: function () { return Array.from(leaves.keys()); },
        layout: layout,
        setRatios: setRatios,
        dispose: dispose
    });
}
