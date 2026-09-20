// =============================================================================
// SplitPane — the splitter: a tree of panes side by side or stacked, each
// split sharing its space by ratio, a draggable divider between neighbours.
// A branch component: the caller makes a sub-branch for it and hands it in;
// dispose() dissolves it.
//
//   new SplitPane(branch, { host, layout, minPanePx?, onEvent? })
//     branch: the splitter's own, handed unactivated
//     host:   a flex box; the splitter is its item and fills it.
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
// indexes from the root. Hovered or held, the handle is lit: it wears the
// primary surface by extent — part of the way from the design's neutral
// while hovered, at full while held — and wears nothing of its own at rest.
// The splitter sets the number; the design owns what each looks like.
// `css` is injected with the styles import.
// =============================================================================

const _splitOwner = Object.freeze({ toString: () => "splitPane" });
var _HOVER = 0.4, _HELD = 1;   // the lit handle's extent of the primary surface

class SplitPane {
    constructor(branch, opts) {
        if (!branch) throw new Error("[SplitPane] a branch of its own is required");
        if (!opts || !opts.host) throw new Error("[SplitPane] opts.host is required");
        branch.activate(_splitOwner);
        this._branch = branch;
        this._minPx = opts.minPanePx == null ? 40 : Math.max(0, opts.minPanePx | 0);
        this._sink = typeof opts.onEvent === "function" ? opts.onEvent : null;
        this._leaves = new Map();        // slotId → leaf element
        this._splits = new Map();        // path → { el, orientation, children: [{ el, node }], dividers: [el] }
        this._tree = SplitPane._validate(opts.layout, "layout", new Set());

        var root = branch.createElement("root", "div");
        css.addClass(root, sp_root);
        root.style.setProperty("--sp-min", this._minPx + "px");
        root.appendChild(this._render(this._tree, ""));
        opts.host.appendChild(root);
        this.el = root;
    }

    // ── Reporting ─────────────────────────────────────────────────────────
    _fire(ev) {
        if (!this._sink) return;
        try { this._sink(ev); }
        catch (e) { console.error("[SplitPane] onEvent threw on " + ev.kind + ":", e); }
    }

    // ── The tree ──────────────────────────────────────────────────────────
    static _validate(node, at, seen) {
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
            children.push({ pane: SplitPane._validate(c && c.pane, at + ".children[" + i + "].pane", seen), ratio: r });
            sum += r;
        }
        for (var j = 0; j < children.length; j++) children[j].ratio = children[j].ratio / sum;
        return { kind: "split", orientation: node.orientation, children: children };
    }

    _render(node, path) {
        var branch = this._branch;
        var name = path === "" ? "root" : path.replace(/\//g, "_");
        if (node.kind === "leaf") {
            var leaf = branch.createElement("leaf-" + node.slotId.replace(/[^A-Za-z0-9_-]/g, "_"), "div");
            css.addClass(leaf, sp_leaf);
            leaf.setAttribute("data-slot", node.slotId);
            this._leaves.set(node.slotId, leaf);
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
                this._armDrag(divider, path, i - 1);
                entry.dividers.push(divider);
                el.appendChild(divider);
            }
            var child = branch.createElement("child-" + name + "-" + i, "div");
            css.addClass(child, sp_child, horizontal ? sp_child_h : sp_child_v);
            child.style.setProperty("--sp-ratio", String(node.children[i].ratio));
            child.appendChild(this._render(node.children[i].pane, path === "" ? String(i) : path + "/" + i));
            entry.children.push({ el: child, node: node.children[i] });
            el.appendChild(child);
        }
        this._splits.set(path, entry);
        return el;
    }

    _apply(path, ratios) {
        var entry = this._splits.get(path);
        for (var i = 0; i < ratios.length; i++) {
            entry.children[i].node.ratio = ratios[i];
            entry.children[i].el.style.setProperty("--sp-ratio", String(ratios[i]));
        }
    }
    _ratiosOf(path) {
        var entry = this._splits.get(path), out = [];
        for (var i = 0; i < entry.children.length; i++) out.push(entry.children[i].node.ratio);
        return out;
    }

    // ── Hover and drag a divider ──────────────────────────────────────────
    _armDrag(divider, path, before) {
        var self = this;
        var hovering = false, held = false;
        function paint() {
            if (held || hovering) { css.addClass(divider, sp_divider_lit); css.extent(divider, held ? _HELD : _HOVER); }
            else { css.removeClass(divider, sp_divider_lit); css.extent(divider, null); }
        }
        divider.addEventListener("pointerenter", function () { hovering = true; paint(); });
        divider.addEventListener("pointerleave", function () { hovering = false; paint(); });
        divider.addEventListener("pointerdown", function (down) {
            if (down.button !== 0 || held) return;
            var entry = self._splits.get(path), horizontal = entry.orientation === "horizontal";
            var a = entry.children[before], b = entry.children[before + 1];
            var start = horizontal ? down.clientX : down.clientY;
            var size = horizontal ? entry.el.getBoundingClientRect().width : entry.el.getBoundingClientRect().height;
            var a0 = a.node.ratio, b0 = b.node.ratio;
            if (!(size > 0)) return;
            var minShare = Math.min(self._minPx / size, (a0 + b0) / 2);
            var moved = false;
            held = true;
            paint();
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
                held = false;
                paint();
                try { divider.releasePointerCapture(down.pointerId); } catch (err) {}
                if (e.type !== "pointerup") { self._apply(path, SplitPane._restore(entry, before, a0, b0)); return; }
                if (moved) self._fire(SplitEvents.RatioChanged(path, self._ratiosOf(path)));
            }
            divider.addEventListener("pointermove", onMove);
            divider.addEventListener("pointerup", onEnd);
            divider.addEventListener("pointercancel", onEnd);
        });
    }
    static _restore(entry, before, a0, b0) {
        var out = [];
        for (var i = 0; i < entry.children.length; i++) out.push(i === before ? a0 : i === before + 1 ? b0 : entry.children[i].node.ratio);
        return out;
    }

    // ── The surface ───────────────────────────────────────────────────────
    slot(slotId) { return this._leaves.get(slotId) || null; }
    slots() { return Array.from(this._leaves.keys()); }

    setRatios(path, ratios) {
        var entry = this._splits.get(path);
        if (!entry) throw new Error("[SplitPane] no split at '" + path + "'");
        if (!Array.isArray(ratios) || ratios.length !== entry.children.length) throw new Error("[SplitPane] setRatios: " + entry.children.length + " ratios expected at '" + path + "'");
        var sum = 0;
        for (var i = 0; i < ratios.length; i++) {
            if (typeof ratios[i] !== "number" || !(ratios[i] > 0)) throw new Error("[SplitPane] setRatios: ratios must be positive numbers");
            sum += ratios[i];
        }
        var shares = [];
        for (var j = 0; j < ratios.length; j++) shares.push(ratios[j] / sum);
        this._apply(path, shares);
        this._fire(SplitEvents.RatioChanged(path, shares));
    }

    layout() {
        return (function copy(node) {
            if (node.kind === "leaf") return { kind: "leaf", slotId: node.slotId };
            var children = [];
            for (var i = 0; i < node.children.length; i++) children.push({ pane: copy(node.children[i].pane), ratio: node.children[i].ratio });
            return { kind: "split", orientation: node.orientation, children: children };
        })(this._tree);
    }

    dispose() {
        if (this.el.parentNode) this.el.parentNode.removeChild(this.el);
        this._leaves.clear();
        this._splits.clear();
        this._branch.dissolve();
    }
}
