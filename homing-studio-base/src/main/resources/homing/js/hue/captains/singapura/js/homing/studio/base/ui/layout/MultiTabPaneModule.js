// =============================================================================
// MultiTabPaneModule — second workspace primitive. Layers tabs on top of
// SplitPane with a conserved tab budget (default 16).
//
// Capacity per pane = floor(budget / 2^depth). Splitting halves it;
// merging doubles it. Total budget is conserved across any layout.
//
// API:
//   new MultiTabPane({ container, budget?, initialLayout?, onChange? })
//     .addTab(slotId, { id, title, render })
//     .removeTab(slotId, tabId)
//     .switchTab(slotId, tabId)
//     .split(slotId, orientation)                  // 'horizontal' | 'vertical'
//     .merge(slotId)                               // merges with sibling
//     .canSplit(slotId) / .canMerge(slotId)        // { allowed, reason }
//     .capacityOf(slotId)
//     .getState()
//     .destroy()
//
// Internal architecture: this class owns the layout state + per-slot
// tab arrays. It delegates rendering to a SplitPane instance whose
// renderSlot callback we provide. On split/merge we mutate our own
// layout and call sp.setLayout() to drive a re-render.
// =============================================================================

var _STYLE_TAG_ID = "homing-multitabpane-style";
// Theme-aware styling — every surface, text, border, and accent reads from
// the framework's --color-* CSS variables so the primitive blends with any
// active theme (default / forest / bauhaus / forbidden-city / letterpress /
// maple-bridge / retro-90s / jazz-drums). Hard-coded greys ruined the
// chrome against themes with patterned backdrops or dark palettes.
var _STYLE_CSS = [
    ".hmtp-leaf{display:flex;flex-direction:column;height:100%;position:relative;",
    "  background:var(--color-surface);color:var(--color-text-primary);}",
    ".hmtp-strip{display:flex;align-items:center;gap:2px;padding:4px;",
    "  background:var(--color-surface-raised);",
    "  border-bottom:1px solid var(--color-border);",
    "  overflow-x:auto;flex-shrink:0;position:relative;}",
    ".hmtp-chip{display:flex;align-items:center;gap:4px;padding:3px 8px;border-radius:3px;",
    "  font:13px sans-serif;cursor:pointer;border:1px solid transparent;flex-shrink:0;",
    "  color:var(--color-text-muted);}",
    ".hmtp-chip-active{background:var(--color-surface);",
    "  border-color:var(--color-border);color:var(--color-text-primary);}",
    ".hmtp-chip-close{cursor:pointer;opacity:0.5;padding:0 2px;}",
    ".hmtp-chip-close:hover{opacity:1;color:var(--color-accent-emphasis);}",
    ".hmtp-pill{margin-left:auto;padding:2px 8px;background:var(--color-surface);",
    "  border:1px solid var(--color-border);border-radius:10px;font:11px sans-serif;",
    "  color:var(--color-text-muted);flex-shrink:0;}",
    ".hmtp-pill-full{color:var(--color-accent-emphasis);",
    "  border-color:var(--color-accent-emphasis);}",
    ".hmtp-content{flex:1;min-height:0;position:relative;overflow:auto;",
    "  background:var(--color-surface);}",
    ".hmtp-empty{display:flex;align-items:center;justify-content:center;",
    "  color:var(--color-text-muted);font:14px sans-serif;height:100%;}",
    ".hmtp-corner{position:absolute;top:4px;right:4px;display:flex;gap:2px;z-index:6;",
    "  background:color-mix(in srgb, var(--color-surface) 90%, transparent);",
    "  border:1px solid var(--color-border);border-radius:3px;padding:1px;}",
    ".hmtp-cbtn{border:0;background:transparent;cursor:pointer;padding:2px 6px;",
    "  font:14px sans-serif;line-height:1;color:var(--color-text-primary);}",
    ".hmtp-cbtn:hover:not(:disabled){",
    "  background:color-mix(in srgb, var(--color-accent) 15%, transparent);}",
    ".hmtp-cbtn:disabled{opacity:0.3;cursor:not-allowed;}"
].join("\n");

function _ensureStyles() {
    if (document.getElementById(_STYLE_TAG_ID)) return;
    var s = document.createElement("style");
    s.id = _STYLE_TAG_ID;
    s.textContent = _STYLE_CSS;
    document.head.appendChild(s);
}

// ─── Tree helpers (mirror SplitPane's shape) ─────────────────────────────────

function _isLeaf(n)  { return n && n.kind === "leaf"; }
function _isSplit(n) { return n && n.kind === "split"; }

function _depthOf(node, slotId, d) {
    d = d || 0;
    if (_isLeaf(node)) return node.slotId === slotId ? d : -1;
    for (var i = 0; i < node.children.length; i++) {
        var r = _depthOf(node.children[i].pane, slotId, d + 1);
        if (r >= 0) return r;
    }
    return -1;
}

// Returns { parent, index, sibling } for the slot, or null if at root.
function _findParent(node, slotId) {
    if (!_isSplit(node)) return null;
    for (var i = 0; i < node.children.length; i++) {
        var child = node.children[i];
        if (_isLeaf(child.pane) && child.pane.slotId === slotId) {
            if (node.children.length !== 2) return null;
            return { parent: node, index: i, sibling: node.children[1 - i].pane };
        }
        var deeper = _findParent(child.pane, slotId);
        if (deeper) return deeper;
    }
    return null;
}

// Walk `node` looking for the split whose object identity is `target`.
// When found, mutate parent's children to replace the slot containing
// `target` with `{ kind: 'leaf', slotId }`. Returns true if replaced.
function _replaceSubtree(node, target, replacement) {
    if (!_isSplit(node)) return false;
    for (var i = 0; i < node.children.length; i++) {
        if (node.children[i].pane === target) {
            node.children[i].pane = replacement;
            return true;
        }
        if (_replaceSubtree(node.children[i].pane, target, replacement)) return true;
    }
    return false;
}

function _clone(node) {
    if (_isLeaf(node)) return { kind: "leaf", slotId: node.slotId };
    return {
        kind: "split",
        orientation: node.orientation,
        children: node.children.map(function (c) {
            return { pane: _clone(c.pane), ratio: c.ratio };
        })
    };
}

function _eachLeaf(node, fn) {
    if (_isLeaf(node)) { fn(node); return; }
    node.children.forEach(function (c) { _eachLeaf(c.pane, fn); });
}

// ─── The class ───────────────────────────────────────────────────────────────

class MultiTabPane {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[MultiTabPane] container is required");
        _ensureStyles();

        this._budget   = opts.budget != null ? opts.budget : 16;
        this._onChange = opts.onChange || null;
        this._tabsBySlot = new Map();   // slotId → { tabs: [], activeTabId }
        this._stripEls   = new Map();   // slotId → strip DOM element (tracked for drag hit-testing)
        this._nextId   = 1;

        var layout = opts.initialLayout != null
            ? _clone(opts.initialLayout)
            : this._defaultLayout();

        // Initialise tab state for every leaf in the starting layout.
        var self = this;
        _eachLeaf(layout, function (leaf) {
            if (!self._tabsBySlot.has(leaf.slotId)) {
                self._tabsBySlot.set(leaf.slotId, { tabs: [], activeTabId: null });
            }
        });

        // SplitPane owns the layout from here on — including live ratios as
        // the user drags dividers. We DO NOT keep a parallel this._layout,
        // because pushing a stale copy back during _refresh() would wipe the
        // user's adjustments. Every read goes through this._sp.getLayout();
        // every mutation starts from a fresh clone of that.
        //
        // Boot-time exception: SplitPane's constructor calls renderSlot
        // synchronously during _build(), BEFORE the `new SplitPane(...)`
        // expression has returned and `this._sp` has been assigned. The
        // renderSlot callback needs to compute capacities, which need the
        // layout. _bootLayout is a one-shot fallback used only during this
        // initial synchronous render; cleared immediately after.
        this._bootLayout = layout;
        this._sp = new SplitPane({
            container : opts.container,
            layout    : layout,
            renderSlot: function (slotId, el) { self._renderLeaf(slotId, el); }
        });
        this._bootLayout = null;

        // Drag controller — instantiated last so all state is in place.
        this._drag = new TabDragController(this);
    }

    /** Layout source of truth — SplitPane after construction, boot layout during. */
    _layout() {
        return this._sp ? this._sp.getLayout() : this._bootLayout;
    }

    // Default 2x2 (depth 2, capacity 4 per pane). Slot ids are stable.
    _defaultLayout() {
        return {
            kind: "split", orientation: "vertical",
            children: [
                { ratio: 0.5, pane: {
                    kind: "split", orientation: "horizontal",
                    children: [
                        { ratio: 0.5, pane: { kind: "leaf", slotId: "tl" } },
                        { ratio: 0.5, pane: { kind: "leaf", slotId: "tr" } }
                    ]
                } },
                { ratio: 0.5, pane: {
                    kind: "split", orientation: "horizontal",
                    children: [
                        { ratio: 0.5, pane: { kind: "leaf", slotId: "bl" } },
                        { ratio: 0.5, pane: { kind: "leaf", slotId: "br" } }
                    ]
                } }
            ]
        };
    }

    // ─── Capacity + predicates ───────────────────────────────────────────────

    capacityOf(slotId) {
        var d = _depthOf(this._layout(), slotId);
        if (d < 0) throw new Error("[MultiTabPane] unknown slot: " + slotId);
        return Math.floor(this._budget / Math.pow(2, d));
    }

    canSplit(slotId) {
        // Splits are gated only by minimum-capacity (children must get cap ≥ 1).
        // Tab count is NOT a gate — existing tabs stay in the originating child
        // even when that puts it over its new capacity. The pill turns red to
        // surface this; the user can close tabs to clear it. addTab() remains
        // gated by capacity, so the budget invariant (no growth by reshuffle)
        // survives where it matters.
        var cap = this.capacityOf(slotId);
        if (cap < 2) {
            return { allowed: false, reason: "Minimum capacity reached (already at depth max)" };
        }
        return { allowed: true, reason: "" };
    }

    canMerge(slotId) {
        var found = _findParent(this._layout(), slotId);
        if (!found) return { allowed: false, reason: "Root pane — nothing to merge with" };
        if (!_isLeaf(found.sibling)) {
            return { allowed: false, reason: "Sibling is split — merge inside it first" };
        }
        return { allowed: true, reason: "" };
    }

    // ─── Tab operations ──────────────────────────────────────────────────────

    addTab(slotId, tab) {
        var state = this._requireSlot(slotId);
        var cap = this.capacityOf(slotId);
        if (state.tabs.length >= cap) {
            throw new Error("[MultiTabPane] pane " + slotId + " at capacity " + cap);
        }
        state.tabs.push(tab);
        if (state.activeTabId == null) state.activeTabId = tab.id;
        this._refresh();
        return this;
    }

    removeTab(slotId, tabId) {
        var state = this._requireSlot(slotId);
        var idx = -1;
        for (var i = 0; i < state.tabs.length; i++) {
            if (state.tabs[i].id === tabId) { idx = i; break; }
        }
        if (idx < 0) return this;
        state.tabs.splice(idx, 1);
        if (state.activeTabId === tabId) {
            state.activeTabId = state.tabs.length > 0
                ? state.tabs[Math.min(idx, state.tabs.length - 1)].id
                : null;
        }
        this._refresh();
        return this;
    }

    switchTab(slotId, tabId) {
        var state = this._requireSlot(slotId);
        for (var i = 0; i < state.tabs.length; i++) {
            if (state.tabs[i].id === tabId) {
                state.activeTabId = tabId;
                this._refresh();
                return this;
            }
        }
        return this;
    }

    /**
     * Move a tab between (or within) panes. Used by the drag controller; also
     * available programmatically. Same-pane reorder: srcSlotId === destSlotId,
     * destIndex is interpreted relative to the strip BEFORE removal. Capacity
     * gate: refuses if destination at capacity (cross-pane move only).
     */
    moveTab(srcSlotId, tabId, destSlotId, destIndex) {
        var src = this._requireSlot(srcSlotId);
        var idx = -1;
        for (var i = 0; i < src.tabs.length; i++) {
            if (src.tabs[i].id === tabId) { idx = i; break; }
        }
        if (idx < 0) throw new Error("[MultiTabPane.moveTab] tab not found: " + tabId);

        // Capacity gate (cross-pane only — same-pane reorder never grows the pane).
        if (srcSlotId !== destSlotId) {
            var dest = this._requireSlot(destSlotId);
            var cap = this.capacityOf(destSlotId);
            if (dest.tabs.length >= cap) {
                throw new Error("[MultiTabPane.moveTab] destination at capacity: "
                              + destSlotId + " (" + cap + ")");
            }
        }

        // Same-pane reorder — adjust destIndex if it would land after the
        // hole left by removal.
        var adjustedIndex = destIndex;
        if (srcSlotId === destSlotId && destIndex > idx) adjustedIndex = destIndex - 1;

        var tab = src.tabs[idx];
        this._removeTabFromState(srcSlotId, tabId);
        this._insertTabIntoState(destSlotId, tab, adjustedIndex);

        // Moved tab becomes active in destination.
        var destState = this._tabsBySlot.get(destSlotId);
        destState.activeTabId = tab.id;
        this._refresh();
        return this;
    }

    // ─── Internal helpers (used by TabDragController) ────────────────────

    /** Remove a tab from a pane's state without refreshing. */
    _removeTabFromState(slotId, tabId) {
        var state = this._requireSlot(slotId);
        var idx = -1;
        for (var i = 0; i < state.tabs.length; i++) {
            if (state.tabs[i].id === tabId) { idx = i; break; }
        }
        if (idx < 0) return;
        state.tabs.splice(idx, 1);
        if (state.activeTabId === tabId) {
            state.activeTabId = state.tabs.length > 0
                ? state.tabs[Math.min(idx, state.tabs.length - 1)].id
                : null;
        }
    }

    /** Insert a tab into a pane's state at the given index without refreshing. */
    _insertTabIntoState(slotId, tab, index) {
        var state = this._requireSlot(slotId);
        var clamped = Math.max(0, Math.min(index == null ? state.tabs.length : index, state.tabs.length));
        state.tabs.splice(clamped, 0, tab);
        if (state.activeTabId == null) state.activeTabId = tab.id;
    }

    // ─── Layout operations ───────────────────────────────────────────────────

    split(slotId, orientation) {
        var check = this.canSplit(slotId);
        if (!check.allowed) throw new Error("[MultiTabPane.split] " + check.reason);

        var newSlotId = this._mintSlotId();
        this._tabsBySlot.set(newSlotId, { tabs: [], activeTabId: null });

        // Pull a fresh clone from SplitPane (preserves user-dragged ratios on
        // every sibling), mutate, push back.
        var layout = this._layout();
        var newLeaf = { kind: "leaf", slotId: newSlotId };
        if (_isLeaf(layout) && layout.slotId === slotId) {
            layout = {
                kind: "split", orientation: orientation,
                children: [
                    { pane: layout,  ratio: 0.5 },
                    { pane: newLeaf, ratio: 0.5 }
                ]
            };
        } else {
            var found = _findParent(layout, slotId);
            var origLeaf = found.parent.children[found.index].pane;
            found.parent.children[found.index].pane = {
                kind: "split", orientation: orientation,
                children: [
                    { pane: origLeaf, ratio: 0.5 },
                    { pane: newLeaf,  ratio: 0.5 }
                ]
            };
        }
        this._push(layout);
        return this;
    }

    merge(slotId) {
        var check = this.canMerge(slotId);
        if (!check.allowed) throw new Error("[MultiTabPane.merge] " + check.reason);

        var layout = this._layout();
        var found = _findParent(layout, slotId);
        var siblingState = this._tabsBySlot.get(found.sibling.slotId);
        var myState     = this._tabsBySlot.get(slotId);

        // Concatenate sibling's tabs after mine; keep my active.
        for (var i = 0; i < siblingState.tabs.length; i++) myState.tabs.push(siblingState.tabs[i]);
        this._tabsBySlot.delete(found.sibling.slotId);

        // Replace the parent split with a single leaf.
        var mergedLeaf = { kind: "leaf", slotId: slotId };
        if (found.parent === layout) {
            layout = mergedLeaf;
        } else {
            _replaceSubtree(layout, found.parent, mergedLeaf);
        }
        this._push(layout);
        return this;
    }

    // ─── State + lifecycle ───────────────────────────────────────────────────

    getState() {
        var tabs = {};
        this._tabsBySlot.forEach(function (v, k) {
            tabs[k] = { tabs: v.tabs.map(function (t) { return { id: t.id, title: t.title }; }),
                        activeTabId: v.activeTabId };
        });
        return { layout: this._layout(), tabs: tabs };
    }

    destroy() {
        if (this._drag) { this._drag.destroy(); this._drag = null; }
        if (this._sp) { this._sp.destroy(); this._sp = null; }
        this._tabsBySlot.clear();
        this._stripEls.clear();
    }

    // ─── Internals ───────────────────────────────────────────────────────────

    _requireSlot(slotId) {
        var s = this._tabsBySlot.get(slotId);
        if (!s) throw new Error("[MultiTabPane] no such slot: " + slotId);
        return s;
    }

    _mintSlotId() {
        var id;
        do { id = "p" + (this._nextId++); } while (this._tabsBySlot.has(id));
        return id;
    }

    /**
     * Push a (possibly mutated) layout back to SplitPane. Used by split /
     * merge after structural changes.
     */
    _push(layout) {
        this._stripEls.clear();
        this._sp.setLayout(layout);
        if (this._onChange) this._onChange(this.getState());
    }

    /**
     * Re-render leaves after a tab-only change (add / remove / switch / move).
     * Pull SplitPane's CURRENT layout (with live divider ratios intact) and
     * push it back — triggering renderSlot for every leaf without disturbing
     * the user's adjustments. Pushing a stale parallel layout would reset
     * the ratios on every tab change; this is what caused divider-drag to
     * snap back after closing a tab.
     */
    _refresh() {
        this._push(this._layout());
    }

    // ─── Rendering ───────────────────────────────────────────────────────────

    _renderLeaf(slotId, el) {
        el.className = "hmtp-leaf";
        var state = this._tabsBySlot.get(slotId) || { tabs: [], activeTabId: null };

        el.appendChild(this._renderStrip(slotId, state));

        var content = document.createElement("div");
        content.className = "hmtp-content";
        if (state.activeTabId != null) {
            var active = state.tabs.find(function (t) { return t.id === state.activeTabId; });
            if (active && typeof active.render === "function") active.render(content);
        } else {
            content.innerHTML = '<div class="hmtp-empty">(empty pane)</div>';
        }
        el.appendChild(content);
        el.appendChild(this._renderCorner(slotId));
    }

    _renderStrip(slotId, state) {
        var self = this;
        var strip = document.createElement("div");
        strip.className = "hmtp-strip";
        strip.setAttribute("data-slot-id", slotId);
        this._stripEls.set(slotId, strip);

        state.tabs.forEach(function (tab) {
            var chip = document.createElement("div");
            chip.className = "hmtp-chip" + (tab.id === state.activeTabId ? " hmtp-chip-active" : "");
            chip.setAttribute("data-tab-id", tab.id);
            chip.style.cursor = "grab";
            var title = document.createElement("span");
            title.textContent = tab.title;
            chip.appendChild(title);
            var close = document.createElement("span");
            close.className = "hmtp-chip-close";
            close.textContent = "×";
            close.addEventListener("mousedown", function (ev) { ev.stopPropagation(); });
            close.addEventListener("click", function (ev) {
                ev.stopPropagation();
                self.removeTab(slotId, tab.id);
            });
            chip.appendChild(close);
            // Drag controller arbitrates click-vs-drag: on click (no move past
            // threshold) it calls switchTab on mouseup; on drag, it manages
            // ghost / drop indicator / detach / re-dock.
            chip.addEventListener("mousedown", function (ev) {
                self._drag.beginDrag(ev, slotId, tab.id);
            });
            strip.appendChild(chip);
        });

        var cap = this.capacityOf(slotId);
        var pill = document.createElement("span");
        pill.className = "hmtp-pill" + (state.tabs.length >= cap ? " hmtp-pill-full" : "");
        pill.textContent = state.tabs.length + " / " + cap;
        pill.title = "Tab budget: " + state.tabs.length + " used of " + cap + " (depth "
                   + _depthOf(this._layout(), slotId) + ", total workspace budget " + this._budget + ")";
        strip.appendChild(pill);

        return strip;
    }

    _renderCorner(slotId) {
        var self = this;
        var box = document.createElement("div");
        box.className = "hmtp-corner";

        var canS = this.canSplit(slotId);
        box.appendChild(this._cbtn("⇆", "split horizontally", canS,
                function () { self.split(slotId, "horizontal"); }));
        box.appendChild(this._cbtn("⇅", "split vertically", canS,
                function () { self.split(slotId, "vertical"); }));

        var canM = this.canMerge(slotId);
        if (canM.allowed) {
            box.appendChild(this._cbtn("⤢", "merge with sibling",
                    { allowed: true, reason: "" },
                    function () { self.merge(slotId); }));
        }
        return box;
    }

    _cbtn(glyph, baseTitle, gate, onClick) {
        var btn = document.createElement("button");
        btn.className = "hmtp-cbtn";
        btn.textContent = glyph;
        btn.title = gate.allowed ? baseTitle : (baseTitle + " — " + gate.reason);
        btn.disabled = !gate.allowed;
        if (gate.allowed) btn.addEventListener("click", function (ev) {
            ev.stopPropagation();
            onClick();
        });
        return btn;
    }
}
