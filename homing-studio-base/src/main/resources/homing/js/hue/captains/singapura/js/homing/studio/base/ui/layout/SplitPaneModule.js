// =============================================================================
// SplitPaneModule — first independent workspace primitive.
//
// Exports a single class, SplitPane, that lays out an arbitrarily-nested
// 2D split-pane tree inside a container. Each leaf is identified by a
// stable `slotId`; the consumer fills the leaf DOM via a `renderSlot`
// callback. SplitPane knows nothing about tabs, widgets, or modals —
// those are separate primitives that compose on top.
//
// Pane tree shape (plain JS object literals at runtime):
//
//   Leaf:  { kind: 'leaf',  slotId: string }
//   Split: { kind: 'split', orientation: 'horizontal'|'vertical',
//            children: [{ pane: <Leaf|Split>, ratio: number }, ...] }
//
// `orientation: 'horizontal'` ⇒ children are arranged left-to-right (a
//   horizontal row of vertical bands; the divider drags horizontally).
// `orientation: 'vertical'`   ⇒ children are arranged top-to-bottom (a
//   vertical stack of horizontal bands; the divider drags vertically).
// `ratio` is the child's share of its parent split (0..1). Ratios within
//   a split should sum to ~1.0 but the layout walker normalises them
//   defensively on every apply.
//
// Public JS API (constructor returns the instance — methods on it):
//   new SplitPane({ container, layout, renderSlot, minPx?, onChange? })
//     .setLayout(newTree)
//     .getLayout()
//     .relayout()
//     .split(slotId, orientation, newSlotId, side?)
//     .destroy()
//
// Doctrine: this primitive uses raw document.* DOM internally (no
// DomOpsParty). Lifecycle is owned by an explicit destroy() that removes
// listeners and clears the container. Inline scoped CSS is injected once
// (idempotent) on first construction.
// =============================================================================

var _STYLE_TAG_ID = "homing-splitpane-style";
var _STYLE_CSS = [
    ".hsp-root{position:relative;width:100%;height:100%;overflow:hidden;display:flex;}",
    ".hsp-root.hsp-h{flex-direction:row;}",
    ".hsp-root.hsp-v{flex-direction:column;}",
    ".hsp-leaf{position:relative;overflow:hidden;min-width:0;min-height:0;}",
    ".hsp-divider{flex:0 0 6px;background:rgba(0,0,0,0.08);position:relative;z-index:1;}",
    ".hsp-divider.hsp-h-div{cursor:col-resize;width:6px;}",
    ".hsp-divider.hsp-v-div{cursor:row-resize;height:6px;}",
    ".hsp-divider:hover,.hsp-divider.hsp-active{background:rgba(0,0,0,0.22);}",
    "body.hsp-dragging{user-select:none;-webkit-user-select:none;cursor:inherit;}"
].join("\n");

function _ensureStyles() {
    if (document.getElementById(_STYLE_TAG_ID)) return;
    var s = document.createElement("style");
    s.id = _STYLE_TAG_ID;
    s.textContent = _STYLE_CSS;
    document.head.appendChild(s);
}

function _isLeaf(node)  { return node && node.kind === "leaf"; }
function _isSplit(node) { return node && node.kind === "split"; }

function _cloneLayout(node) {
    if (_isLeaf(node)) return { kind: "leaf", slotId: node.slotId };
    return {
        kind: "split",
        orientation: node.orientation,
        children: node.children.map(function (c) {
            return { pane: _cloneLayout(c.pane), ratio: c.ratio };
        })
    };
}

// Walks a split-tree, replaces the leaf identified by slotId by the result
// of `mutator(leaf)`. Returns true if found+replaced, false otherwise.
// Operates in-place on `node`'s `children` array (slot replacement).
function _replaceLeaf(node, slotId, mutator) {
    if (!_isSplit(node)) return false;
    for (var i = 0; i < node.children.length; i++) {
        var child = node.children[i];
        if (_isLeaf(child.pane) && child.pane.slotId === slotId) {
            child.pane = mutator(child.pane);
            return true;
        }
        if (_replaceLeaf(child.pane, slotId, mutator)) return true;
    }
    return false;
}

function _normaliseRatios(children) {
    var sum = 0;
    for (var i = 0; i < children.length; i++) sum += (children[i].ratio || 0);
    if (sum <= 0) {
        var even = 1 / children.length;
        for (var j = 0; j < children.length; j++) children[j].ratio = even;
        return;
    }
    for (var k = 0; k < children.length; k++) children[k].ratio = children[k].ratio / sum;
}

// NOTE: no explicit `export` line — the framework's ExportWriter appends
// `export {SplitPane};` based on SplitPaneModule.java's exports().
class SplitPane {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container)   throw new Error("[SplitPane] opts.container is required");
        if (!opts.layout)      throw new Error("[SplitPane] opts.layout is required");
        if (!opts.renderSlot)  throw new Error("[SplitPane] opts.renderSlot is required");

        this._container  = opts.container;
        this._layout     = _cloneLayout(opts.layout);
        this._renderSlot = opts.renderSlot;
        this._minPx      = opts.minPx != null ? opts.minPx : 60;
        this._onChange   = opts.onChange || null;

        this._listeners = [];   // [target, type, fn, opts]
        this._rendered  = false;

        _ensureStyles();
        this._build();
        this._bindResize();
    }

    // ─── Public API ────────────────────────────────────────────────────────

    setLayout(newTree) {
        this._layout = _cloneLayout(newTree);
        this._build();
        this._notify();
        return this;
    }

    getLayout() { return _cloneLayout(this._layout); }

    relayout()  { this._applySizes(this._rootEl, this._layout); return this; }

    /**
     * Split an existing leaf in two. The leaf identified by `slotId` is
     * replaced by a split node containing [original, new] (or [new, original]
     * when `side === "before"`). The new leaf has `newSlotId`; the caller's
     * `renderSlot` callback is invoked for it on next render.
     *
     * @param {string} slotId       — slotId of the leaf to split
     * @param {string} orientation  — "horizontal" or "vertical" (same axis-
     *                                semantics as the constructor: horizontal
     *                                splits arrange children left-to-right;
     *                                vertical arranges top-to-bottom)
     * @param {string} newSlotId    — slotId for the spawned leaf
     * @param {string} [side]       — "after" (default) or "before"
     * @returns {SplitPane} this
     *
     * State-preservation note: split() calls _build() which clears the
     * container and re-runs renderSlot for every leaf. The SplitPane
     * primitive does not preserve per-leaf DOM state across re-render —
     * that's the consumer's responsibility (a MultiTabPane atop SplitPane
     * uses detach + re-anchor to keep widget state alive). For a simple
     * demo with stateless content, re-render is fine.
     */
    split(slotId, orientation, newSlotId, side) {
        if (orientation !== "horizontal" && orientation !== "vertical") {
            throw new Error("[SplitPane.split] orientation must be 'horizontal' or 'vertical'");
        }
        if (!newSlotId) {
            throw new Error("[SplitPane.split] newSlotId is required");
        }
        var placeAfter = side !== "before";
        var newLeaf = { kind: "leaf", slotId: newSlotId };

        // Special case: tree IS the leaf (top-level layout is a single leaf).
        if (_isLeaf(this._layout) && this._layout.slotId === slotId) {
            this._layout = {
                kind: "split",
                orientation: orientation,
                children: placeAfter
                    ? [{ pane: this._layout, ratio: 0.5 }, { pane: newLeaf, ratio: 0.5 }]
                    : [{ pane: newLeaf, ratio: 0.5 }, { pane: this._layout, ratio: 0.5 }]
            };
            this._build();
            this._notify();
            return this;
        }

        var found = _replaceLeaf(this._layout, slotId, function (leaf) {
            return {
                kind: "split",
                orientation: orientation,
                children: placeAfter
                    ? [{ pane: leaf, ratio: 0.5 }, { pane: newLeaf, ratio: 0.5 }]
                    : [{ pane: newLeaf, ratio: 0.5 }, { pane: leaf, ratio: 0.5 }]
            };
        });
        if (!found) {
            throw new Error("[SplitPane.split] slotId not found: " + slotId);
        }
        this._build();
        this._notify();
        return this;
    }

    destroy() {
        for (var i = 0; i < this._listeners.length; i++) {
            var L = this._listeners[i];
            L[0].removeEventListener(L[1], L[2], L[3]);
        }
        this._listeners = [];
        if (this._container) this._container.innerHTML = "";
        this._rootEl = null;
    }

    // ─── Build / render ────────────────────────────────────────────────────

    _build() {
        this._container.innerHTML = "";
        this._rootEl = this._renderNode(this._layout);
        this._container.appendChild(this._rootEl);
        // Defer size application until the container has measurable size.
        var self = this;
        requestAnimationFrame(function () { self._applySizes(self._rootEl, self._layout); });
        this._rendered = true;
    }

    _renderNode(node) {
        if (_isLeaf(node)) {
            var leaf = document.createElement("div");
            leaf.className = "hsp-leaf";
            leaf.setAttribute("data-slot-id", node.slotId);
            this._renderSlot(node.slotId, leaf);
            return leaf;
        }
        // Split.
        _normaliseRatios(node.children);
        var box = document.createElement("div");
        var isHorizontal = node.orientation === "horizontal";
        box.className = "hsp-root " + (isHorizontal ? "hsp-h" : "hsp-v");

        for (var i = 0; i < node.children.length; i++) {
            var child = node.children[i];
            var childEl = this._renderNode(child.pane);
            child._el = childEl;            // stash for sizing
            box.appendChild(childEl);
            if (i < node.children.length - 1) {
                var divider = document.createElement("div");
                divider.className = "hsp-divider " + (isHorizontal ? "hsp-h-div" : "hsp-v-div");
                box.appendChild(divider);
                this._bindDivider(divider, box, node, i, isHorizontal);
            }
        }
        node._el = box;
        return box;
    }

    _applySizes(el, node) {
        if (_isLeaf(node)) return;
        _normaliseRatios(node.children);
        var isHorizontal = node.orientation === "horizontal";
        var dividerCount = node.children.length - 1;
        var DIV_PX = 6;
        var avail = (isHorizontal ? el.clientWidth : el.clientHeight) - dividerCount * DIV_PX;
        if (avail <= 0) return;

        // Clamp each child to >= minPx, then re-distribute leftover.
        var sizes = new Array(node.children.length);
        var rem = avail;
        for (var i = 0; i < node.children.length; i++) {
            var raw = avail * node.children[i].ratio;
            var clamped = Math.max(this._minPx, Math.round(raw));
            sizes[i] = clamped;
            rem -= clamped;
        }
        // Absorb remainder (positive or negative) into the largest pane.
        if (rem !== 0) {
            var largest = 0;
            for (var j = 1; j < sizes.length; j++) if (sizes[j] > sizes[largest]) largest = j;
            sizes[largest] = Math.max(this._minPx, sizes[largest] + rem);
        }
        // Write ratios back from clamped sizes so getLayout() reflects reality.
        var total = 0;
        for (var k = 0; k < sizes.length; k++) total += sizes[k];
        for (var m = 0; m < node.children.length; m++) {
            node.children[m].ratio = sizes[m] / total;
            var cEl = node.children[m]._el;
            cEl.style.flex = "0 0 " + sizes[m] + "px";
            this._applySizes(cEl, node.children[m].pane);
        }
    }

    // ─── Divider drag ──────────────────────────────────────────────────────

    _bindDivider(divider, splitEl, splitNode, leftIdx, isHorizontal) {
        var self = this;
        var start = function (e) {
            e.preventDefault();
            divider.classList.add("hsp-active");
            document.body.classList.add("hsp-dragging");
            var rect = splitEl.getBoundingClientRect();

            var move = function (ev) {
                var src = (ev.touches && ev.touches[0]) || ev;
                var pos = isHorizontal
                    ? (src.clientX - rect.left) / rect.width
                    : (src.clientY - rect.top)  / rect.height;
                // Sum of ratios up to and including leftIdx after drag.
                var before = 0;
                for (var i = 0; i < leftIdx; i++) before += splitNode.children[i].ratio;
                var pair = splitNode.children[leftIdx].ratio + splitNode.children[leftIdx + 1].ratio;
                var newLeft = Math.max(0.01, Math.min(pair - 0.01, pos - before));
                splitNode.children[leftIdx].ratio     = newLeft;
                splitNode.children[leftIdx + 1].ratio = pair - newLeft;
                self._applySizes(splitEl, splitNode);
                self._notify();
            };
            var stop = function () {
                divider.classList.remove("hsp-active");
                document.body.classList.remove("hsp-dragging");
                document.removeEventListener("mousemove", move);
                document.removeEventListener("touchmove", move);
                document.removeEventListener("mouseup",   stop);
                document.removeEventListener("touchend",  stop);
            };
            document.addEventListener("mousemove", move);
            document.addEventListener("touchmove", move, { passive: false });
            document.addEventListener("mouseup",   stop);
            document.addEventListener("touchend",  stop);
        };
        this._on(divider, "mousedown",  start);
        this._on(divider, "touchstart", start, { passive: false });
    }

    _bindResize() {
        var self = this;
        var fn = function () { self.relayout(); };
        this._on(window, "resize", fn);
    }

    _on(target, type, fn, opts) {
        target.addEventListener(type, fn, opts);
        this._listeners.push([target, type, fn, opts]);
    }

    _notify() {
        if (this._onChange) this._onChange(this.getLayout());
    }
}
