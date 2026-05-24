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
//   new SplitPane({ container, layout, renderSlot, minPanePx?, onChange? })
//     .setLayout(newTree)
//     .getLayout()
//     .relayout()
//     .split(slotId, orientation, newSlotId, side?)
//     .destroy()
//
// Sizing model (b.2f — pure JS pixel sizing).
//
//   Every pane (leaf or nested split) gets an explicit pixel width and
//   height set as inline style — computed by JS from the container's
//   current clientWidth/clientHeight at render + on every ResizeObserver
//   tick. No flex. No percentages. Content can never push a pane past
//   its computed size; overflow:hidden clips anything beyond.
//
//   Divider drag: pixel delta → ratio adjustment → recompute pixels →
//   re-apply. minPanePx (default 40) bounds the smallest pane — drag
//   won't crush a pane below this.
//
// Doctrine: this primitive uses raw document.* DOM internally (no
// DomOpsParty). Lifecycle is owned by an explicit destroy() that removes
// listeners and clears the container. Inline scoped CSS is injected once
// (idempotent) on first construction.
// =============================================================================

var _STYLE_TAG_ID = "homing-splitpane-style";
var _STYLE_CSS = [
    // No display:flex anywhere. JS sets explicit pixel width/height +
    // absolute positioning on every child. Container is position:relative
    // so children's position:absolute is anchored to it.
    ".hsp-root{position:relative;overflow:hidden;}",
    ".hsp-leaf{position:absolute;overflow:hidden;}",
    ".hsp-divider{position:absolute;background:rgba(0,0,0,0.08);z-index:1;}",
    ".hsp-divider.hsp-h-div{cursor:col-resize;}",
    ".hsp-divider.hsp-v-div{cursor:row-resize;}",
    ".hsp-divider:hover,.hsp-divider.hsp-active{background:rgba(0,0,0,0.22);}",
    "body.hsp-dragging{user-select:none;-webkit-user-select:none;cursor:inherit;}"
].join("\n");

var _DIV_PX = 6;   // divider thickness

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

class SplitPane {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container)   throw new Error("[SplitPane] opts.container is required");
        if (!opts.layout)      throw new Error("[SplitPane] opts.layout is required");
        if (!opts.renderSlot)  throw new Error("[SplitPane] opts.renderSlot is required");

        this._container  = opts.container;
        this._layout     = _cloneLayout(opts.layout);
        this._renderSlot = opts.renderSlot;
        this._minPanePx  = opts.minPanePx != null ? opts.minPanePx : 40;
        this._onChange   = opts.onChange || null;

        this._listeners = [];
        this._resizeObserver = null;
        this._rendered  = false;

        _ensureStyles();
        this._initialBuild();
        this._observeContainer();
    }

    // ─── JSON-tree navigation helpers (used by split/merge) ────────────────

    /**
     * Find a leaf by slotId. Returns the leaf JSON node, its DOM element,
     * its containing split (parentSplit) if any, the {pane,ratio,_el,_divEl}
     * entry in parentSplit.children for this leaf, and an isRoot flag.
     * Returns null if not found.
     */
    _findLeafContext(slotId) {
        if (_isLeaf(this._layout) && this._layout.slotId === slotId) {
            return {
                leafNode    : this._layout,
                leafEl      : this._rootEl,
                parentSplit : null,
                parentChild : null,
                isRoot      : true
            };
        }
        return this._walkLeafContext(this._layout, slotId);
    }

    _walkLeafContext(node, slotId) {
        if (!_isSplit(node)) return null;
        for (var i = 0; i < node.children.length; i++) {
            var child = node.children[i];
            if (_isLeaf(child.pane) && child.pane.slotId === slotId) {
                return {
                    leafNode    : child.pane,
                    leafEl      : child._el,
                    parentSplit : node,
                    parentChild : child,
                    isRoot      : false
                };
            }
            var deeper = this._walkLeafContext(child.pane, slotId);
            if (deeper) return deeper;
        }
        return null;
    }

    /**
     * Find the position of a given split node in the layout tree. Returns
     * the containing split's child entry, or { isRoot: true } if the
     * target IS the root. null if not found.
     */
    _findSplitContext(target) {
        if (this._layout === target) return { parentChild: null, isRoot: true };
        return this._walkSplitContext(this._layout, target);
    }

    _walkSplitContext(node, target) {
        if (!_isSplit(node)) return null;
        for (var i = 0; i < node.children.length; i++) {
            var child = node.children[i];
            if (child.pane === target) {
                return { parentChild: child, isRoot: false };
            }
            var deeper = this._walkSplitContext(child.pane, target);
            if (deeper) return deeper;
        }
        return null;
    }

    // ─── Public API ────────────────────────────────────────────────────────

    /**
     * BANNED. Wholesale layout replacement requires destroying the existing
     * leaf elements (via container.innerHTML = "" or equivalent), which
     * detaches every widget DOM tree inside — and detach-then-reattach
     * resets browser UI state (scroll position, focus, text selection,
     * in-flight CSS animations, audio playback, iframe documents,
     * IntersectionObserver entries…). DomOpsParty branch ownership keeps the
     * elements alive in JS but cannot protect against detach.
     *
     * Use the in-place structural mutators instead:
     *   .split(slotId, orientation, newSlotId, side?)
     *   .merge(slotId)
     * Each mutator transitions the affected leaf el between two attached
     * parents via a single synchronous appendChild — the only browser
     * primitive that preserves UI state across structural change.
     *
     * @since b.2i — the "never detach widget DOM" doctrine.
     */
    setLayout(/* newTree */) {
        throw new Error(
            "[SplitPane.setLayout] removed — wholesale layout replacement is banned " +
            "because it detaches widget DOM (resets scroll/focus/animation/audio/iframe " +
            "state). Use the in-place mutators split() and merge() instead. See the " +
            "b.2i case study for the full rationale."
        );
    }

    getLayout() { return _cloneLayout(this._layout); }

    /** Recompute pixel sizes from current container dimensions + re-apply. */
    relayout() { this._applySizes(this._rootEl, this._layout); return this; }

    /**
     * Split a leaf in place. The existing leaf's DOM element moves from its
     * current parent (P) into a newly-created split box that takes its place
     * inside P — a single sync appendChild between two attached parents, so
     * the existing leaf's UI state (scroll, focus, animations) is preserved.
     * A fresh leaf el is created for newSlotId and the consumer's renderSlot
     * callback is invoked exactly once on it.
     *
     * @param {string} slotId       the existing slot to split
     * @param {'horizontal'|'vertical'} orientation
     * @param {string} newSlotId    id for the new leaf
     * @param {'before'|'after'} [side='after']  where the new leaf sits
     */
    split(slotId, orientation, newSlotId, side) {
        if (orientation !== "horizontal" && orientation !== "vertical") {
            throw new Error("[SplitPane.split] orientation must be 'horizontal' or 'vertical'");
        }
        if (!newSlotId) {
            throw new Error("[SplitPane.split] newSlotId is required");
        }
        var ctx = this._findLeafContext(slotId);
        if (!ctx) throw new Error("[SplitPane.split] slotId not found: " + slotId);
        var placeAfter = side !== "before";

        // ─── Mint the new leaf el + render its content (consumer fills it).
        //     Built off-DOM; immediately attached into the split box below.
        var newLeafEl = document.createElement("div");
        newLeafEl.className = "hsp-leaf";
        newLeafEl.setAttribute("data-slot-id", newSlotId);

        // ─── Mint the split box (off-DOM) and the divider.
        var splitBox = document.createElement("div");
        splitBox.className = "hsp-root " + (orientation === "horizontal" ? "hsp-h" : "hsp-v");
        var divider = document.createElement("div");
        divider.className = "hsp-divider " + (orientation === "horizontal" ? "hsp-h-div" : "hsp-v-div");

        // ─── Insert splitBox where origLeafEl currently sits in DOM. After
        //     this, splitBox is attached AND origLeafEl is still attached
        //     (next sibling). origLeafEl's UI state is intact.
        var parentDom = ctx.leafEl.parentNode;
        parentDom.insertBefore(splitBox, ctx.leafEl);

        // ─── Move origLeafEl into splitBox. THIS is the single move between
        //     two attached parents — the browser preserves scroll/focus/etc.
        //     Same goes for newLeafEl's first-time attach (no prior state).
        var newLeafJson = { kind: "leaf", slotId: newSlotId };
        var newSplitJson;
        if (placeAfter) {
            splitBox.appendChild(ctx.leafEl);   // origLeaf moves: parentDom → splitBox
            splitBox.appendChild(divider);
            splitBox.appendChild(newLeafEl);
            newSplitJson = {
                kind: "split", orientation: orientation,
                children: [
                    { pane: ctx.leafNode, ratio: 0.5, _el: ctx.leafEl, _divEl: divider },
                    { pane: newLeafJson,  ratio: 0.5, _el: newLeafEl }
                ],
                _el: splitBox
            };
        } else {
            splitBox.appendChild(newLeafEl);
            splitBox.appendChild(divider);
            splitBox.appendChild(ctx.leafEl);   // origLeaf moves: parentDom → splitBox
            newSplitJson = {
                kind: "split", orientation: orientation,
                children: [
                    { pane: newLeafJson,  ratio: 0.5, _el: newLeafEl, _divEl: divider },
                    { pane: ctx.leafNode, ratio: 0.5, _el: ctx.leafEl }
                ],
                _el: splitBox
            };
        }

        this._bindDivider(divider, splitBox, newSplitJson, 0);

        // ─── Splice newSplitJson into the JSON tree.
        if (ctx.isRoot) {
            this._layout = newSplitJson;
            this._rootEl = splitBox;
        } else {
            ctx.parentChild.pane = newSplitJson;
            ctx.parentChild._el  = splitBox;
            // parentChild._divEl is the divider AFTER this child in its parent split
            // (between ctx.parentChild and its sibling) — unchanged by the split here.
        }

        // ─── Render the new slot's content. Consumer's renderSlot runs once
        //     on newLeafEl. newLeafEl is already attached to splitBox which
        //     is attached to parentDom — consumer can use clientWidth/Height
        //     reliably if it wants to (sizes finalised by _applySizes next).
        this._renderSlot(newSlotId, newLeafEl);

        this._applySizes(this._rootEl, this._layout);
        this._notify();
        return this;
    }

    /**
     * Merge a leaf with its sibling — the surviving slotId absorbs DOM space
     * from the sibling, the parent split box collapses. The surviving leaf
     * el moves up to the grandparent's position via a single sync
     * appendChild (between two attached parents) so its UI state survives.
     * The sibling leaf el is removed; consumers must migrate any state out
     * of the sibling slot BEFORE calling merge (the MultiTabPane layer does
     * this for tab content — see MultiTabPane.merge).
     *
     * @param {string} slotId   the slot to keep
     */
    merge(slotId) {
        var ctx = this._findLeafContext(slotId);
        if (!ctx) throw new Error("[SplitPane.merge] slotId not found: " + slotId);
        if (ctx.isRoot) throw new Error("[SplitPane.merge] root leaf cannot merge — no sibling");
        var parentSplit = ctx.parentSplit;
        if (parentSplit.children.length !== 2) {
            throw new Error("[SplitPane.merge] parent split has " + parentSplit.children.length
                + " children (expected exactly 2 for binary merge)");
        }

        // ─── Locate parentSplit's position in the layout (its containing split).
        var grandCtx = this._findSplitContext(parentSplit);
        if (!grandCtx) throw new Error("[SplitPane.merge] internal error: parent split not in layout");

        // ─── DOM: move surviving leaf el up to grandparent's box, then remove
        //     the parent split box (which carries the sibling leaf + divider).
        //     The single move is leafEl → grandDom (both attached); UI state ✓.
        var splitBoxEl = parentSplit._el;
        var grandDom = splitBoxEl.parentNode;
        grandDom.insertBefore(ctx.leafEl, splitBoxEl);   // single move
        grandDom.removeChild(splitBoxEl);                // sibling + divider go away

        // ─── JSON: replace parentSplit with ctx.leafNode in the tree.
        if (grandCtx.isRoot) {
            this._layout = ctx.leafNode;
            this._rootEl = ctx.leafEl;
        } else {
            grandCtx.parentChild.pane = ctx.leafNode;
            grandCtx.parentChild._el  = ctx.leafEl;
            // _divEl on grandCtx.parentChild stays — it's the divider after
            // this slot in the grandparent split, unrelated to the merge.
        }

        this._applySizes(this._rootEl, this._layout);
        this._notify();
        return this;
    }

    destroy() {
        for (var i = 0; i < this._listeners.length; i++) {
            var L = this._listeners[i];
            L[0].removeEventListener(L[1], L[2], L[3]);
        }
        this._listeners = [];
        if (this._resizeObserver) { try { this._resizeObserver.disconnect(); } catch (e) {} this._resizeObserver = null; }
        // Targeted removal — never innerHTML="". The SplitPane owns exactly
        // one child of the container (its _rootEl); detach it and let GC
        // handle the rest. Branch-owned widget DOM inside survives in JS
        // (caller's branches keep the references alive); when those branches
        // dissolve, the DOM dies cleanly.
        if (this._container && this._rootEl && this._rootEl.parentNode === this._container) {
            this._container.removeChild(this._rootEl);
        }
        this._rootEl = null;
    }

    // ─── Build / render ────────────────────────────────────────────────────

    /**
     * One-shot initial build. Called only from the constructor — at that
     * point the container is guaranteed empty (consumers haven't had a
     * chance to attach anything), so no innerHTML="" wipe is needed. All
     * subsequent structural changes go through split() / merge() which
     * mutate the existing DOM in place. setLayout is banned.
     */
    _initialBuild() {
        this._rootEl = this._renderNode(this._layout);
        this._container.appendChild(this._rootEl);
        // Defer first sizing one frame so container's clientWidth/Height is
        // settled (parent flex chain may not have resolved on the first
        // synchronous read).
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
        box.className = "hsp-root " + (node.orientation === "horizontal" ? "hsp-h" : "hsp-v");
        for (var i = 0; i < node.children.length; i++) {
            var child = node.children[i];
            var childEl = this._renderNode(child.pane);
            child._el = childEl;
            box.appendChild(childEl);
            if (i < node.children.length - 1) {
                var divider = document.createElement("div");
                divider.className = "hsp-divider " + (node.orientation === "horizontal" ? "hsp-h-div" : "hsp-v-div");
                box.appendChild(divider);
                this._bindDivider(divider, box, node, i);
                child._divEl = divider;
            }
        }
        node._el = box;
        return box;
    }

    /**
     * Recursively size a split node's children in pixels. el = the split
     * box (or container for top-level); reads el's clientWidth/Height as
     * the available pixel budget; assigns explicit left/top/width/height
     * to every child + divider.
     *
     * For the top-level call, el is _rootEl which fills the container
     * (style.position:absolute inset:0). For nested splits, el has its
     * own position:absolute box from its parent's _applySizes pass.
     */
    _applySizes(el, node) {
        if (_isLeaf(node)) return;
        _normaliseRatios(node.children);
        var isHorizontal = node.orientation === "horizontal";

        // Top-level root: size it to fill the container.
        if (el === this._rootEl) {
            el.style.position = "absolute";
            el.style.left   = "0px";
            el.style.top    = "0px";
            el.style.width  = this._container.clientWidth  + "px";
            el.style.height = this._container.clientHeight + "px";
        }

        var totalW = el.clientWidth;
        var totalH = el.clientHeight;
        var avail  = (isHorizontal ? totalW : totalH) - (node.children.length - 1) * _DIV_PX;
        if (avail <= 0) return;

        // Compute child pixel sizes from ratios, clamp to minPanePx, absorb
        // remainder into the largest. Write back ratios so getLayout()
        // reflects the actual pixel distribution.
        var sizes = new Array(node.children.length);
        var rem = avail;
        for (var i = 0; i < node.children.length; i++) {
            var raw = avail * node.children[i].ratio;
            var clamped = Math.max(this._minPanePx, Math.round(raw));
            sizes[i] = clamped;
            rem -= clamped;
        }
        if (rem !== 0) {
            var largest = 0;
            for (var j = 1; j < sizes.length; j++) if (sizes[j] > sizes[largest]) largest = j;
            sizes[largest] = Math.max(this._minPanePx, sizes[largest] + rem);
        }
        var total = 0;
        for (var k = 0; k < sizes.length; k++) total += sizes[k];
        for (var m = 0; m < node.children.length; m++) {
            node.children[m].ratio = sizes[m] / total;
        }

        // Apply pixel positions/sizes. Children + dividers all position:absolute
        // within the split box; we walk left-to-right (horizontal) or top-to-
        // bottom (vertical), assigning left/top by accumulating offset.
        var offset = 0;
        for (var n = 0; n < node.children.length; n++) {
            var cEl   = node.children[n]._el;
            var cSize = sizes[n];
            // position:absolute inline — consumers (e.g. MultiTabPane) may
            // overwrite className on the leaf div, losing the .hsp-leaf rule.
            // Inline style wins and keeps the pixel-explicit positioning safe.
            cEl.style.position = "absolute";
            cEl.style.overflow = "hidden";
            if (isHorizontal) {
                cEl.style.left   = offset + "px";
                cEl.style.top    = "0px";
                cEl.style.width  = cSize  + "px";
                cEl.style.height = totalH + "px";
            } else {
                cEl.style.left   = "0px";
                cEl.style.top    = offset + "px";
                cEl.style.width  = totalW + "px";
                cEl.style.height = cSize  + "px";
            }
            offset += cSize;

            // Divider after this child (if not the last).
            if (n < node.children.length - 1) {
                var dEl = node.children[n]._divEl;
                dEl.style.position = "absolute";
                if (isHorizontal) {
                    dEl.style.left   = offset + "px";
                    dEl.style.top    = "0px";
                    dEl.style.width  = _DIV_PX + "px";
                    dEl.style.height = totalH  + "px";
                } else {
                    dEl.style.left   = "0px";
                    dEl.style.top    = offset + "px";
                    dEl.style.width  = totalW  + "px";
                    dEl.style.height = _DIV_PX + "px";
                }
                offset += _DIV_PX;
            }

            // Recurse into nested splits — the child now has its own pixel
            // box for its descendants to read clientWidth/Height from.
            this._applySizes(cEl, node.children[n].pane);
        }
    }

    // ─── Divider drag ──────────────────────────────────────────────────────

    _bindDivider(divider, splitEl, splitNode, leftIdx) {
        var self = this;
        var isHorizontal = splitNode.orientation === "horizontal";
        var start = function (e) {
            e.preventDefault();
            divider.classList.add("hsp-active");
            document.body.classList.add("hsp-dragging");

            var src0 = (e.touches && e.touches[0]) || e;
            var startPos = isHorizontal ? src0.clientX : src0.clientY;
            var startLeftSize  = isHorizontal ? splitNode.children[leftIdx]._el.clientWidth      : splitNode.children[leftIdx]._el.clientHeight;
            var startRightSize = isHorizontal ? splitNode.children[leftIdx + 1]._el.clientWidth  : splitNode.children[leftIdx + 1]._el.clientHeight;
            var pairTotal      = startLeftSize + startRightSize;

            var move = function (ev) {
                var src = (ev.touches && ev.touches[0]) || ev;
                var pos = isHorizontal ? src.clientX : src.clientY;
                var delta = pos - startPos;
                var newLeft = Math.max(self._minPanePx,
                               Math.min(pairTotal - self._minPanePx, startLeftSize + delta));
                var newRight = pairTotal - newLeft;
                // Update only the two affected children's ratios in-place
                // (others stay the same; _applySizes will re-normalise).
                var pairRatio = splitNode.children[leftIdx].ratio + splitNode.children[leftIdx + 1].ratio;
                splitNode.children[leftIdx].ratio     = (newLeft  / pairTotal) * pairRatio;
                splitNode.children[leftIdx + 1].ratio = (newRight / pairTotal) * pairRatio;
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

    // ─── ResizeObserver — recompute pixel sizes when container changes ─────

    _observeContainer() {
        if (typeof ResizeObserver === "undefined") {
            // Fallback: window resize only. Modern browsers all have RO.
            var self0 = this;
            this._on(window, "resize", function () { self0.relayout(); });
            return;
        }
        var self = this;
        this._resizeObserver = new ResizeObserver(function () { self.relayout(); });
        this._resizeObserver.observe(this._container);
    }

    _on(target, type, fn, opts) {
        target.addEventListener(type, fn, opts);
        this._listeners.push([target, type, fn, opts]);
    }

    _notify() {
        if (this._onChange) this._onChange(this.getLayout());
    }
}
