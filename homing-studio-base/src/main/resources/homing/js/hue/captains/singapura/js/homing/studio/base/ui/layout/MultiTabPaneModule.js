// =============================================================================
// MultiTabPaneModule — second workspace primitive. Layers tabs on top of
// SplitPane with a conserved GLOBAL tab budget (default 16).
//
// RFC 0047 — the budget is a single shared pool across the whole pane tree:
// an add is allowed iff the total tab count across every slot is below the
// budget. Splitting adds no tabs, so it never changes what's available; the
// "+" affordance disables on every strip together when the pool is full.
// (Was rationed per-pane by split depth, floor(budget / 2^depth).)
//
// API:
//   new MultiTabPane({ container, budget?, initialLayout?, onChange?, onAddTab? })
//     onAddTab(slotId): optional. When provided, each strip renders a "+"
//     button while the workspace is below its tab budget; click fires
//     onAddTab(slotId). The host decides what "add" means (open a picker,
//     add a default tab, prompt the user).
//     .addTab(slotId, { id, title, render, onClose?, setActive?, pinned? })
//        - onClose() fires only on real close (× click via removeTab).
//          Internal transient removals during drag detach do NOT fire it,
//          so the same tab can be re-docked elsewhere without disposing
//          caller-owned resources (DomOpsParty branches, timers, etc.).
//        - setActive(boolean) fires on workspace-active transitions —
//          old.setActive(false) then new.setActive(true). Optional; widgets
//          with nothing to gate (or that rely entirely on the overlay
//          mouse gate) omit it.
//     .setWorkspaceActiveTab(tabId) / .getWorkspaceActiveTab()
//        - Workspace-wide focus: at most one tab is workspace-active across
//          the whole pane. The active tab's content overlay is removed
//          (mouse passes through); every other pane-active tab gets an
//          invisible mouse-event cover. Click anywhere on a cover activates
//          that tab.
//     .removeTab(slotId, tabId)
//     .switchTab(slotId, tabId)
//     .split(slotId, orientation)                  // 'horizontal' | 'vertical'
//     .merge(slotId)                               // merges with sibling
//     .canSplit(slotId) / .canMerge(slotId)        // { allowed, reason }
//     .totalTabs() / .budget() / .atCapacity() / .canAdd()   // RFC 0047 global pool
//     .getState()
//     .destroy()
//
// Internal architecture: this class owns the layout state + per-slot
// tab arrays. It delegates rendering to a SplitPane instance whose
// renderSlot callback we provide. On split/merge we call SplitPane's
// in-place structural mutators (sp.split / sp.merge); we NEVER hand
// SplitPane a wholesale new layout — the banned innerHTML="" rebuild
// path is gone (b.2j: never detach widget DOM).
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
    // RFC 0048 — focus rings. The SELECTED pane (shallow cursor) gets a dashed
    // ring; the ENTERED pane (deep) gets a solid accent ring. Drawn with outline
    // (no layout shift), inset so it stays within the pane; themed tokens only.
    ".hmtp-leaf-selected{outline:2px dashed var(--color-border-emphasis);outline-offset:-2px;}",
    ".hmtp-leaf-entered{outline:2px solid var(--color-accent);outline-offset:-2px;}",
    // The entered pane also GLOWS — a soft inset accent halo. It's an ::after
    // overlay because the parent clips outer shadows, and an inset shadow on the
    // leaf itself would sit behind the pane's opaque strip/content. Always
    // present but transparent until entered, so it fades in; pointer-events:none
    // so it never intercepts clicks.
    ".hmtp-leaf::after{content:'';position:absolute;inset:0;pointer-events:none;z-index:7;",
    "  box-shadow:inset 0 0 0 transparent;transition:box-shadow 160ms ease;}",
    ".hmtp-leaf-entered::after{",
    "  box-shadow:inset 0 0 18px color-mix(in srgb, var(--color-accent) 45%, transparent);}",
    // The entered pane's content takes DOM focus (tabindex -1); suppress its own
    // focus outline — the pane ring above is the focus affordance.
    ".hmtp-content:focus{outline:none;}",
    ".hmtp-strip{display:flex;align-items:center;gap:2px;padding:4px;",
    "  background:var(--color-surface-raised);",
    "  border-bottom:1px solid var(--color-border);",
    "  overflow-x:auto;flex-shrink:0;position:relative;}",
    ".hmtp-chip{display:flex;align-items:center;gap:4px;padding:3px 8px;border-radius:3px;",
    "  font:13px sans-serif;cursor:pointer;border:1px solid transparent;flex-shrink:0;",
    "  color:var(--color-text-muted);}",
    ".hmtp-chip-active{background:var(--color-surface);",
    "  border-color:var(--color-border);color:var(--color-text-primary);}",
    // Workspace-active accent — sits on top of pane-active. A tab can be both:",
    // pane-active is 'visible in this pane', workspace-active is 'the one the",
    // user is currently with'. At most one workspace-active across the whole",
    // workspace; that tab's content overlay is hidden, all others have an",
    // invisible click-capturing overlay that on click re-activates the target.",
    ".hmtp-chip-workspace-active{box-shadow:inset 0 -2px 0 var(--color-accent);}",
    ".hmtp-chip-close{cursor:pointer;opacity:0.5;padding:0 2px;}",
    ".hmtp-chip-close:hover{opacity:1;color:var(--color-accent-emphasis);}",
    // Per-strip "+" — small inline button rendered at the strip's right edge
    // when the pane is below capacity AND the host passed an onAddTab callback.
    // Absent strips have no "+" → the capacity-full state is visually obvious.
    ".hmtp-strip-add{cursor:pointer;opacity:0.5;padding:0 6px;font:14px sans-serif;",
    "  background:transparent;border:0;color:var(--color-text-primary);}",
    ".hmtp-strip-add:hover{opacity:1;color:var(--color-accent-emphasis);}",
    ".hmtp-pill{margin-left:auto;padding:2px 8px;background:var(--color-surface);",
    "  border:1px solid var(--color-border);border-radius:10px;font:11px sans-serif;",
    "  color:var(--color-text-muted);flex-shrink:0;}",
    ".hmtp-pill-full{color:var(--color-accent-emphasis);",
    "  border-color:var(--color-accent-emphasis);}",
    // ─── Content wrapper — a positioning context only. The scrollbar lives
    //     inside the per-tab .hmtp-tab-content (one per tab), NOT here. Each
    //     tab gets its own persistent scroll container so switching tabs
    //     toggles display without detaching DOM — scroll position is
    //     preserved by construction.
    ".hmtp-content{flex:1;min-height:0;position:relative;overflow:hidden;",
    "  background:var(--color-surface);}",
    // ─── Per-tab persistent content host. Created once per tab; widget's",
    // ─── render(el) is called once into it; never detached on tab switch.",
    // ─── Switching tabs flips display:block/none on this element.",
    ".hmtp-tab-content{position:absolute;inset:0;overflow:auto;}",
    // Invisible mouse-event capture layer rendered over every tab content",
    // EXCEPT the workspace-active one. Click anywhere on it → that tab",
    // becomes workspace-active. Sits above content; doesn't tint, doesn't",
    // change layout. The widget below stops receiving mouse events while",
    // inactive — its setActive(false) handles non-mouse cleanup (keyboard",
    // listeners, audio, animations).",
    ".hmtp-inactive-cover{position:absolute;inset:0;z-index:5;cursor:pointer;",
    "  background:transparent;}",
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
        // Optional "+" affordance per strip. When provided AND the pane is
        // below capacity, the strip renders a small "+" at its right edge;
        // click fires onAddTab(slotId). The pane stays free of widget-system
        // knowledge — the host decides what "add" means (open a picker,
        // create a default tab, prompt, …).
        this._onAddTab = opts.onAddTab || null;
        // ─── RFC 0032 — typed mutation callbacks. ────────────────────────────
        // Each fires at the site of its specific mutation, BEFORE the omnibus
        // onChange. Each is optional; absent callbacks are no-ops. The pane
        // publishes its own structural events so downstream chromes (e.g. an
        // event-recording chrome under RFC 0030) don't have to reverse-engineer
        // them from a before/after onChange diff.
        //
        // Callback shapes:
        //   onTabAdded(slotId, tab, tabIndex)
        //   onTabRemoved(slotId, tab, fromIndex)
        //   onTabMoved(srcSlotId, destSlotId, tab, fromIndex, toIndex)
        //   onTabActivated(slotId, tabId)           — strip-chip click / programmatic switchTab
        //   onWorkspaceActiveChanged(fromTabId, toTabId)
        //   onSplit(sourceSlotId, orientation, newSlotId)
        //   onMerge(keptSlotId, removedSlotId)
        //
        // onRatioChanged is NOT in P1 — ratio drags live in SplitPane, which
        // doesn't (yet) expose a typed callback. A follow-up will widen
        // SplitPane the same way; until then, callers wanting ratio change
        // notification must subscribe to SplitPane.onChange directly or
        // continue using MTP's omnibus onChange.
        this._cbTabAdded         = opts.onTabAdded         || null;
        this._cbTabRemoved       = opts.onTabRemoved       || null;
        this._cbTabMoved         = opts.onTabMoved         || null;
        this._cbTabActivated     = opts.onTabActivated     || null;
        this._cbTabAttached      = opts.onTabAttached      || null;
        this._cbWsActiveChanged  = opts.onWorkspaceActiveChanged || null;
        this._cbSplit            = opts.onSplit            || null;
        this._cbMerge            = opts.onMerge            || null;
        // RFC 0032 P5 — fired once per divider-drag (on stop). Receives
        // the new layout; consumer diffs against its model to find which
        // split ratios changed. Per-mousemove repaint sync stays on the
        // internal SplitPane callback; this one is for journaling.
        this._cbRatioChanged     = opts.onRatioChanged     || null;
        // ─── Workspace-active state. ────────────────────────────────────────
        // At most ONE tab across the whole pane is "workspace-active" — the
        // tab the user is currently with. This is distinct from per-slot
        // `activeTabId` (which tab is shown in each pane). The workspace-
        // active tab is always pane-active in its pane (the only way to see
        // it). Switching workspace-active calls tab.setActive(false) on the
        // old and tab.setActive(true) on the new — widgets opt in by
        // providing setActive in their tab descriptor.
        this._workspaceActiveTabId = null;
        this._tabsBySlot = new Map();   // slotId → { tabs: [], activeTabId }
        this._stripEls   = new Map();   // slotId → strip DOM element (tracked for drag hit-testing)
        // RFC 0048 — keyboard focus navigation. _leafBySlot maps each slot to its
        // .hmtp-leaf element (for rect geometry + the focus ring); _selectedSlot is
        // the shallow-mode pane cursor. "deep" mode is derived — a pane is entered
        // exactly when its active tab is the workspace-active tab — so the only new
        // state is the cursor and the leaf-element index.
        this._leafBySlot   = new Map();
        this._selectedSlot = null;
        this._focusScope   = null;   // RFC 0048 — the entered pane's give-up-focus wrapper (deep mode)
        // ─── Persistent per-slot DOM wrappers. Created once per slot in
        //     _renderLeaf (when SplitPane mints the leaf el); reused on every
        //     subsequent _renderSlotLocal call so tab content DOM is never
        //     detached on tab switch / add / remove / move. Map of
        //     slotId → { strip, content, corner }.
        this._wrappersBySlot = new Map();
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
            renderSlot: function (slotId, el) { self._renderLeaf(slotId, el); },
            onRatioChanged: function (newLayout) {
                self._fire(self._cbRatioChanged, "onRatioChanged", [newLayout]);
            }
        });
        this._bootLayout = null;

        // Drag controller — instantiated last so all state is in place.
        this._drag = new TabDragController(this);
    }

    /** Layout source of truth — SplitPane after construction, boot layout during. */
    _layout() {
        return this._sp ? this._sp.getLayout() : this._bootLayout;
    }

    // ─── RFC 0032 P2 — pane-path identity. ─────────────────────────────────
    //
    //   Every pane in the split tree has a unique structural path:
    //     root           = "_"
    //     first child    = parent + "_1"
    //     second child   = parent + "_2"
    //
    //   Paths are intrinsic to the tree shape (not minted): two MTP instances
    //   with the same split tree have the same paths, regardless of internal
    //   slot id minting. Stable across sessions when callers reconstruct the
    //   same split sequence.
    //
    //   Mtp-minted slot ids stay the public handle for live operations
    //   (addTab/removeTab/moveTab/split/merge); paths are the durable handle
    //   for recording + replay across sessions. paneIdOf(slotId) and
    //   slotIdOfPaneId(paneId) bridge the two namespaces.

    /** Return the structural paneId path for a live mtp slot id, or null. */
    paneIdOf(slotId) {
        var found = null;
        (function walk(node, path) {
            if (found || !node) return;
            if (node.kind === "leaf") {
                if (node.slotId === slotId) found = path || "_";
                return;
            }
            walk(node.children[0].pane, path + "_1");
            walk(node.children[1].pane, path + "_2");
        })(this._layout(), "");
        return found;
    }

    /**
     * Index of a tab within its slot's strip, or -1 if the slot or tab is
     * unknown. The tab-strip counterpart to paneIdOf: recording code pairs
     * paneIdOf(slotId) (which pane) with tabIndexOf(slotId, tabId) (where in
     * that pane's strip) to persist a widget's full Location. A freshly-added
     * tab reports its live position (the strip end), so replay restores it in
     * the same order it was spawned.
     */
    tabIndexOf(slotId, tabId) {
        var s = this._tabsBySlot.get(slotId);
        if (!s || !s.tabs) return -1;
        for (var i = 0; i < s.tabs.length; i++) {
            if (s.tabs[i].id === tabId) return i;
        }
        return -1;
    }

    /** Return the live mtp slot id at a structural paneId path, or null. */
    slotIdOfPaneId(paneId) {
        if (paneId == null) return null;
        var node = this._layout();
        if (paneId === "_" || paneId === "") {
            return (node && node.kind === "leaf") ? node.slotId : null;
        }
        var parts = paneId.split("_");
        for (var i = 0; i < parts.length; i++) {
            if (parts[i].length === 0) continue;   // leading "_" splits to empty string
            if (!node || node.kind === "leaf") return null;
            var idx = parts[i] === "1" ? 0 : parts[i] === "2" ? 1 : -1;
            if (idx < 0) return null;
            node = node.children[idx].pane;
        }
        return (node && node.kind === "leaf") ? node.slotId : null;
    }

    /** Return the split-node descriptor (orientation, ratio) at a paneId path, or null. */
    splitAtPaneId(paneId) {
        var node = this._layout();
        if (paneId == null || paneId === "_" || paneId === "") {
            // root — only meaningful if root is a split node
            return (node && node.kind === "split")
                ? { orientation: node.orientation, ratio: node.children[0].ratio }
                : null;
        }
        var parts = paneId.split("_");
        for (var i = 0; i < parts.length; i++) {
            if (parts[i].length === 0) continue;
            if (!node || node.kind === "leaf") return null;
            var idx = parts[i] === "1" ? 0 : parts[i] === "2" ? 1 : -1;
            if (idx < 0) return null;
            node = node.children[idx].pane;
        }
        return (node && node.kind === "split")
            ? { orientation: node.orientation, ratio: node.children[0].ratio }
            : null;
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
    // RFC 0047 — one shared pool. The budget is a workspace total, not a
    // per-pane ration: an add anywhere is gated on the sum of tabs across
    // every slot, so the "+" and the pill read the same number on every strip.

    /** Total tabs across every slot in this workspace. */
    totalTabs() {
        var n = 0;
        this._tabsBySlot.forEach(function (state) { n += state.tabs.length; });
        return n;
    }

    /** The budget ceiling — the conserved workspace tab total. */
    budget() { return this._budget; }

    /** True when the shared pool is exhausted: no pane may add a tab. */
    atCapacity() { return this.totalTabs() >= this._budget; }

    /** True while the shared pool has room for one more tab, anywhere. */
    canAdd() { return this.totalTabs() < this._budget; }

    canSplit(slotId) {
        // RFC 0047 — a split adds no tabs, so it never touches the budget;
        // there is nothing to gate here. (Tabs stay in the originating child;
        // the shared-pool invariant "no growth by reshuffle" holds for free
        // because the total is unchanged.) Kept for API symmetry with
        // canMerge and any future structural (e.g. min-pane-size) check.
        if (_depthOf(this._layout(), slotId) < 0) {
            throw new Error("[MultiTabPane] unknown slot: " + slotId);
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
        // RFC 0047 — global gate: the shared pool, not this pane, is the limit.
        if (this.atCapacity()) {
            throw new Error("[MultiTabPane] workspace tab budget reached (" + this._budget + ")");
        }
        state.tabs.push(tab);
        var addedIndex = state.tabs.length - 1;
        if (state.activeTabId == null) state.activeTabId = tab.id;
        // Tab-only change → local per-slot re-render. No SplitPane push, no
        // cross-slot DOM disturbance, no widget DOM detach.
        this._renderSlotLocal(slotId);
        // RFC 0047 — the workspace total rose, so every OTHER strip's "+"/pill
        // (which read the global pool) must refresh, not just this slot's.
        this._refreshAllStrips();
        this._fire(this._cbTabAdded, "onTabAdded", [slotId, tab, addedIndex]);
        if (this._onChange) this._onChange(this.getState());
        return this;
    }

    removeTab(slotId, tabId) {
        var state = this._requireSlot(slotId);
        var idx = -1;
        for (var i = 0; i < state.tabs.length; i++) {
            if (state.tabs[i].id === tabId) { idx = i; break; }
        }
        if (idx < 0) return this;
        // Fire the tab's lifecycle hooks BEFORE splicing — this is the
        // "real close" signal (× click). Internal transient removals during
        // drag detach go through _removeTabFromState which deliberately
        // skips both hooks so the tab can be re-docked without disposing.
        //
        // Order per RFC 0028 lifecycle: partyDeregister FIRST (widget leaves
        // every Party it joined; no more incoming messages), then onClose
        // (general cleanup — dispose timers, dissolve DomOpsParty branches).
        // Reverse order would leave a window during which the widget could
        // receive messages with its DOM already torn down. Both hooks run
        // under try/catch; either throwing does not block the other.
        var tab = state.tabs[idx];
        if (typeof tab.partyDeregister === "function") {
            try { tab.partyDeregister(); }
            catch (e) { console.error("[MultiTabPane] tab.partyDeregister threw:", e); }
        }
        if (typeof tab.onClose === "function") {
            try { tab.onClose(); } catch (e) { console.error("[MultiTabPane] tab.onClose threw:", e); }
        }
        // If the closed tab was workspace-active, clear it. Per b.2d spec, no
        // automatic successor inheritance — workspace-active just goes null
        // until the user clicks another tab.
        if (this._workspaceActiveTabId === tabId) this._workspaceActiveTabId = null;
        state.tabs.splice(idx, 1);
        if (state.activeTabId === tabId) {
            state.activeTabId = state.tabs.length > 0
                ? state.tabs[Math.min(idx, state.tabs.length - 1)].id
                : null;
        }
        // Local re-render — the slot's persistent wrapper drops the closed
        // tab's _contentEl.
        this._renderSlotLocal(slotId);
        // RFC 0047 — the workspace total fell, so every OTHER strip's "+"/pill
        // must refresh (a pool that was full may now have room again).
        this._refreshAllStrips();
        this._fire(this._cbTabRemoved, "onTabRemoved", [slotId, tab, idx]);
        if (this._onChange) this._onChange(this.getState());
        return this;
    }

    /**
     * Get the workspace-active tab id, or null if no tab is active.
     */
    getWorkspaceActiveTab() { return this._workspaceActiveTabId; }

    /**
     * Mark a tab as workspace-active. Calls tab.setActive(false) on the
     * previously active tab (if any) and tab.setActive(true) on the new
     * one. Both wrapped in try/catch so a misbehaving widget doesn't
     * block the transition. Triggers a re-render so the chip accent and
     * per-content overlays update.
     *
     * Pass null to clear the workspace-active state (no tab is active).
     */
    setWorkspaceActiveTab(tabId) {
        if (this._workspaceActiveTabId === tabId) return this;
        // RFC 0048 — leaving or changing the entered pane drops its give-up-focus
        // wrapper; enterDeep re-creates one for the newly entered pane.
        this._disposeFocusScope();
        var prevId = this._workspaceActiveTabId;
        var prev = prevId ? this._findTab(prevId) : null;
        var next = tabId  ? this._findTab(tabId)  : null;
        if (tabId && !next) return this;   // unknown tab id — refuse silently
        this._workspaceActiveTabId = tabId;
        if (prev && typeof prev.setActive === "function") {
            try { prev.setActive(false); } catch (e) { console.error("[MultiTabPane] prev tab.setActive(false) threw:", e); }
        }
        if (next && typeof next.setActive === "function") {
            try { next.setActive(true); } catch (e) { console.error("[MultiTabPane] next tab.setActive(true) threw:", e); }
        }
        // The cover overlay depends on workspace-active state. Affects every
        // slot (one slot loses cover; previous active's slot may gain one). Render
        // all slots locally — cheap, no SplitPane involvement.
        var self2 = this;
        this._wrappersBySlot.forEach(function (_, slotId) { self2._renderSlotLocal(slotId); });
        this._renderRings();   // RFC 0048 — entered/selected rings track workspace-active
        this._fire(this._cbWsActiveChanged, "onWorkspaceActiveChanged", [prevId, tabId]);
        if (this._onChange) this._onChange(this.getState());
        return this;
    }

    // ─── RFC 0048 — keyboard focus navigation (shallow / deep) ───────────────
    // Shallow: a pane CURSOR (_selectedSlot) moves with the arrows; the pane is
    // highlighted but its widget stays covered. Deep: the cursor pane is ENTERED
    // — its active tab becomes workspace-active (cover off), so keys reach the
    // widget. "deep" is derived — a pane is entered exactly when its active tab
    // is the workspace-active tab — so there is no separate mode flag to drift.

    /** 'shallow' (navigate) or 'deep' (a pane is entered). */
    mode() { return this._workspaceActiveTabId ? "deep" : "shallow"; }

    /** The shallow-mode pane cursor slot id, or null. */
    selectedSlot() { return this._selectedSlot; }

    /**
     * Shallow-select a pane (the cursor) without entering it. Drops out of deep
     * mode if a pane was entered — every pane's cover returns.
     */
    selectPane(slotId) {
        if (!this._leafBySlot.has(slotId)) return this;
        this.setWorkspaceActiveTab(null);   // leave deep → all panes covered (no-op if already shallow)
        this._selectedSlot = slotId;
        this._renderRings();
        return this;
    }

    /**
     * Move the cursor one pane in a direction ('left'|'right'|'up'|'down') via
     * the RFC 0048 shared-edge algorithm. No-op at an outer edge.
     */
    focusPane(direction) {
        var from = this._selectedSlot || this._firstSlot();
        if (from == null) return this;
        var next = this._neighborSlot(from, direction);
        if (next != null) this.selectPane(next);
        return this;
    }

    /**
     * Cycle the active tab WITHIN the selected pane (+1 / -1), wrapping. Never
     * leaves the pane (Tab stays in the pane — RFC 0048 decision).
     */
    cycleTabInPane(delta) {
        var slot = this._selectedSlot;
        if (slot == null) return this;
        var state = this._tabsBySlot.get(slot);
        if (!state || state.tabs.length < 2) return this;
        var i = 0;
        for (var k = 0; k < state.tabs.length; k++) {
            if (state.tabs[k].id === state.activeTabId) { i = k; break; }
        }
        var n = state.tabs.length;
        var j = ((i + delta) % n + n) % n;
        this.switchTab(slot, state.tabs[j].id);
        return this;
    }

    /**
     * Enter deep on the selected (or given) pane: its active tab becomes
     * workspace-active (cover off) and DOM focus moves into the pane so keys
     * reach the widget. No-op on an empty pane (the host opens the picker).
     */
    enterDeep(slotId) {
        var slot = slotId || this._selectedSlot;
        if (slot == null) return this;
        var state = this._tabsBySlot.get(slot);
        if (!state || !state.activeTabId) return this;   // empty pane — host handles add
        this._selectedSlot = slot;
        this.setWorkspaceActiveTab(state.activeTabId);    // deep; paints rings (+ disposes any prior scope)
        var w = this._wrappersBySlot.get(slot);
        if (w && w.content) {
            try { w.content.focus(); } catch (e) {}
            // RFC 0048 — wrap the entered pane: an un-consumed Escape or a
            // homing-focus release event hands focus back to shallow.
            var self = this;
            this._focusScope = new FocusScope(w.content, function () { self.releaseToShallow(); });
            this._focusScope.attach();
        }
        return this;
    }

    /** Return to shallow: no pane entered, cursor unchanged. */
    releaseToShallow() {
        this.setWorkspaceActiveTab(null);   // disposes the scope + paints rings (leaving deep)
        this._renderRings();
        return this;
    }

    _disposeFocusScope() {
        if (this._focusScope) {
            try { this._focusScope.dispose(); } catch (e) {}
            this._focusScope = null;
        }
    }

    _firstSlot() {
        var first = null;
        this._leafBySlot.forEach(function (_, slot) { if (first === null) first = slot; });
        return first;
    }

    _axisOverlap(a0, a1, b0, b1) {
        return Math.max(0, Math.min(a1, b1) - Math.max(a0, b0));
    }

    /**
     * The neighbouring pane across the pressed edge (RFC 0048 shared-edge
     * algorithm). Collect panes on that side sharing the divider (nearest gap)
     * with positive perpendicular overlap; pick nearest divider, then max
     * overlap. Returns a slot id, or null at an outer edge.
     */
    _neighborSlot(fromSlot, direction) {
        var fromEl = this._leafBySlot.get(fromSlot);
        if (!fromEl) return null;
        var P = fromEl.getBoundingClientRect();
        var GAP = 24;   // divider tolerance in px (dividers are ~6px; be generous)
        var EPS = 2;
        var self = this;
        var best = null, bestGap = Infinity, bestOv = -1;
        this._leafBySlot.forEach(function (el, slot) {
            if (slot === fromSlot) return;
            var Q = el.getBoundingClientRect();
            var gap, ov, onSide;
            if (direction === "right")     { onSide = Q.left   >= P.right  - EPS; gap = Q.left - P.right;  ov = self._axisOverlap(P.top, P.bottom, Q.top, Q.bottom); }
            else if (direction === "left") { onSide = Q.right  <= P.left   + EPS; gap = P.left - Q.right;  ov = self._axisOverlap(P.top, P.bottom, Q.top, Q.bottom); }
            else if (direction === "down") { onSide = Q.top    >= P.bottom - EPS; gap = Q.top - P.bottom;  ov = self._axisOverlap(P.left, P.right, Q.left, Q.right); }
            else                           { onSide = Q.bottom <= P.top    + EPS; gap = P.top - Q.bottom;  ov = self._axisOverlap(P.left, P.right, Q.left, Q.right); }
            if (!onSide || ov <= 0 || gap > GAP) return;
            if (gap < bestGap - EPS || (Math.abs(gap - bestGap) <= EPS && ov > bestOv)) {
                best = slot; bestGap = gap; bestOv = ov;
            }
        });
        return best;
    }

    /**
     * Paint the focus rings: the ENTERED pane (its active tab is workspace-
     * active) gets the solid accent ring; otherwise the SELECTED cursor pane
     * gets the dashed selection ring. At most one of each.
     */
    _renderRings() {
        var self = this;
        var waid = this._workspaceActiveTabId;
        this._leafBySlot.forEach(function (el, slot) {
            var state = self._tabsBySlot.get(slot);
            var entered = !!(state && state.activeTabId && state.activeTabId === waid);
            var selected = (slot === self._selectedSlot) && !entered;
            el.classList.toggle("hmtp-leaf-entered", entered);
            el.classList.toggle("hmtp-leaf-selected", selected);
        });
    }

    /** Scan all panes for a tab with the given id. */
    _findTab(tabId) {
        var found = null;
        this._tabsBySlot.forEach(function (state) {
            if (found) return;
            for (var i = 0; i < state.tabs.length; i++) {
                if (state.tabs[i].id === tabId) { found = state.tabs[i]; return; }
            }
        });
        return found;
    }

    switchTab(slotId, tabId) {
        var state = this._requireSlot(slotId);
        for (var i = 0; i < state.tabs.length; i++) {
            if (state.tabs[i].id === tabId) {
                state.activeTabId = tabId;
                // Focus change is a per-slot local concern. Toggle display on
                // the relevant tab._contentEl, swap chip-active class, redraw
                // corner enable state. NO SplitPane involvement → no DOM
                // detach anywhere → scroll position preserved on every tab.
                this._renderSlotLocal(slotId);
                this._fire(this._cbTabActivated, "onTabActivated", [slotId, tabId]);
                if (this._onChange) this._onChange(this.getState());
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

        // RFC 0047 — no capacity gate on move: relocating an existing tab is
        // net-zero for the shared pool (removed from src, added to dest), so it
        // is always allowed, even at capacity. Still validate the dest slot.
        if (srcSlotId !== destSlotId) this._requireSlot(destSlotId);

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
        // Render both affected slots locally. The tab._contentEl is moved
        // from src's content wrapper to dest's via appendChild — single
        // intra-document move, scroll preserved on the tab's inner
        // scrollable descendants.
        this._renderSlotLocal(srcSlotId);
        if (destSlotId !== srcSlotId) this._renderSlotLocal(destSlotId);
        // adjustedIndex is destIndex normalized for the post-remove array;
        // it's the index where the tab now lives in destState.tabs.
        this._fire(this._cbTabMoved, "onTabMoved",
                   [srcSlotId, destSlotId, tab, idx, adjustedIndex]);
        if (this._onChange) this._onChange(this.getState());
        return this;
    }

    /**
     * Public attach for tabs that come back from "outside" — modal redock,
     * picker-modal docking, programmatic re-parenting. Wraps
     * _insertTabIntoState + _renderSlotLocal + switchTab so the dock site
     * publishes its mutation as a typed onTabAttached callback instead of
     * mutating internals directly (which would bypass recording — RFC 0032).
     *
     * Tab is becoming workspace-visible at (slotId, index); the tab object
     * MAY already have a _contentEl re-parented into the destination wrapper
     * (drag controller does this to preserve UI state across the transition).
     * If so, this method's render path won't disturb it.
     *
     * Fires onTabAttached(slotId, tab, tabIndex), then the omnibus onChange.
     */
    attachTab(slotId, tab, index) {
        this._requireSlot(slotId);
        this._insertTabIntoState(slotId, tab, index);
        var state = this._tabsBySlot.get(slotId);
        var attachedIndex = -1;
        for (var i = 0; i < state.tabs.length; i++) {
            if (state.tabs[i].id === tab.id) { attachedIndex = i; break; }
        }
        // switchTab handles the local re-render + sets activeTabId; we still
        // want our own typed callback (and the omnibus onChange) to fire
        // even when switchTab fires its own onTabActivated + onChange. Order:
        // onTabAttached first (the structural event), then switchTab's
        // notifications. Suppress switchTab's omnibus by inlining the render.
        state.activeTabId = tab.id;
        this._renderSlotLocal(slotId);
        this._fire(this._cbTabAttached, "onTabAttached", [slotId, tab, attachedIndex]);
        if (this._onChange) this._onChange(this.getState());
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

        // In-place mutation via SplitPane.split — the existing leaf's DOM
        // moves into a new split box via a single sync appendChild; widget
        // UI state inside (scroll, focus, animations, audio) is preserved.
        // SplitPane will call our renderSlot(newSlotId, newLeafEl) → builds
        // fresh wrappers for the new slot only.
        this._sp.split(slotId, orientation, newSlotId);

        // Refresh strip/content/corner of both: the now-split slot's corner
        // gets the merge option re-enabled; the new slot just got its empty
        // wrappers from _renderLeaf and a local refresh is harmless.
        this._renderSlotLocal(slotId);
        this._renderSlotLocal(newSlotId);
        this._fire(this._cbSplit, "onSplit", [slotId, orientation, newSlotId]);
        if (this._onChange) this._onChange(this.getState());
        return this;
    }

    merge(slotId) {
        var check = this.canMerge(slotId);
        if (!check.allowed) throw new Error("[MultiTabPane.merge] " + check.reason);

        var layout = this._layout();
        var found = _findParent(layout, slotId);
        var siblingSlotId = found.sibling.slotId;
        var siblingState = this._tabsBySlot.get(siblingSlotId);
        var myState     = this._tabsBySlot.get(slotId);

        // ─── Pre-migrate sibling's tab content elements into my wrapper
        //     BEFORE the structural DOM mutation. Each move is a single sync
        //     appendChild between two attached parents (sibling's content
        //     wrapper and mine), so each tab's UI state survives.
        var myWrappers = this._wrappersBySlot.get(slotId);
        var siblingWrappers = this._wrappersBySlot.get(siblingSlotId);
        if (myWrappers && siblingWrappers) {
            siblingState.tabs.forEach(function (tab) {
                if (tab._contentEl) myWrappers.content.appendChild(tab._contentEl);
            });
        }

        // Concatenate JS state — sibling's tabs after mine; keep my active.
        for (var i = 0; i < siblingState.tabs.length; i++) myState.tabs.push(siblingState.tabs[i]);
        this._tabsBySlot.delete(siblingSlotId);
        this._wrappersBySlot.delete(siblingSlotId);
        this._stripEls.delete(siblingSlotId);
        this._leafBySlot.delete(siblingSlotId);   // RFC 0048 — drop the merged-away pane's el
        // If the cursor was on the pane that just merged away, move it to the survivor.
        if (this._selectedSlot === siblingSlotId) this._selectedSlot = slotId;

        // Structural DOM mutation — sibling's now-empty wrappers + leaf el go
        // away; my leaf el moves up to the grandparent's position via a
        // single sync appendChild. Widget DOM inside (which we already
        // moved above) rides along, never detaching.
        this._sp.merge(slotId);

        // Refresh strip (new chips) + content (display toggles for migrated
        // tabs) + corner (merge availability may have changed).
        this._renderSlotLocal(slotId);
        this._fire(this._cbMerge, "onMerge", [slotId, siblingSlotId]);
        if (this._onChange) this._onChange(this.getState());
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
        this._disposeFocusScope();   // RFC 0048 — drop any entered-pane wrapper
        if (this._drag) { this._drag.destroy(); this._drag = null; }
        if (this._sp) { this._sp.destroy(); this._sp = null; }
        this._tabsBySlot.clear();
        this._stripEls.clear();
        this._wrappersBySlot.clear();
    }

    // ─── Internals ───────────────────────────────────────────────────────────

    _requireSlot(slotId) {
        var s = this._tabsBySlot.get(slotId);
        if (!s) throw new Error("[MultiTabPane] no such slot: " + slotId);
        return s;
    }

    /**
     * RFC 0032 — invoke a typed callback safely. Each typed callback runs
     * BEFORE the omnibus onChange, wrapped in try/catch so a misbehaving
     * subscriber can't block the mutation or block the omnibus call.
     * Mirrors the discipline already used for tab.onClose / tab.setActive.
     */
    _fire(cb, ctx, args) {
        if (!cb) return;
        try { cb.apply(null, args); }
        catch (e) { console.error("[MultiTabPane] " + ctx + " threw:", e); }
    }

    _mintSlotId() {
        var id;
        do { id = "p" + (this._nextId++); } while (this._tabsBySlot.has(id));
        return id;
    }

    /**
     * Re-render every slot (full local refresh — no structural change). The
     * structural ops (split, merge) go through SplitPane.split/merge in
     * place; this method is a fallback for "I touched something cross-slot
     * and want every pane to reflect the new state."
     */
    _refresh() {
        var self = this;
        this._wrappersBySlot.forEach(function (_, slotId) { self._renderSlotLocal(slotId); });
        if (this._onChange) this._onChange(this.getState());
    }

    // ─── Rendering ───────────────────────────────────────────────────────────
    //
    // Architecture (b.2i — scroll-preservation refactor):
    //
    //   Each slot owns a persistent triple of wrappers (strip, content, corner)
    //   created ONCE in _renderLeaf when SplitPane mints the leaf el. Every
    //   tab-only update (switch / add / remove / move / workspace-active
    //   change) flows through _renderSlotLocal, which mutates the wrappers'
    //   children in place — it NEVER detaches the persistent wrappers, and it
    //   NEVER detaches the per-tab content elements.
    //
    //   Each tab gets a persistent .hmtp-tab-content div, created on first
    //   sight in _renderContentContents. The widget's tab.render(el) is
    //   called ONCE per tab, into that persistent element. Tab visibility =
    //   display:block / display:none toggle — scroll position survives by
    //   construction because nothing leaves the DOM tree.
    //
    //   SplitPane still calls _renderLeaf for newly-minted leaves (boot, and
    //   after split/merge); those cases re-create wrappers from scratch. For
    //   the common path (tab switch / add / remove / move), _refresh is no
    //   longer involved — _renderSlotLocal is enough.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called by SplitPane when it mints a leaf div (boot + after structural
     * split/merge). Builds the three persistent wrappers for this slot and
     * delegates first-time content rendering to _renderSlotLocal.
     */
    _renderLeaf(slotId, el) {
        // Preserve SplitPane's `hsp-leaf` class — `el.className = ...` would
        // stomp it (the Ep2 case study's Round 4 lesson — destructive className
        // assignment between layers). Inline styles set by SplitPane survive
        // either way, but the class discipline matters for future CSS.
        el.classList.add("hmtp-leaf");

        var strip = document.createElement("div");
        strip.className = "hmtp-strip";
        strip.setAttribute("data-slot-id", slotId);

        var content = document.createElement("div");
        content.className = "hmtp-content";
        content.setAttribute("tabindex", "-1");   // RFC 0048 — programmatically focusable when entered

        var corner = document.createElement("div");
        corner.className = "hmtp-corner";

        el.appendChild(strip);
        el.appendChild(content);
        el.appendChild(corner);

        this._wrappersBySlot.set(slotId, { strip: strip, content: content, corner: corner });
        this._stripEls.set(slotId, strip);   // back-compat for drag controller
        this._leafBySlot.set(slotId, el);    // RFC 0048 — pane el for rect geometry + focus ring
        if (this._selectedSlot === null) this._selectedSlot = slotId;   // first pane is the initial cursor

        this._renderSlotLocal(slotId);
        // RFC 0048 — paint the rings now that this leaf (and the initial cursor)
        // exists, so the boot-shallow selection ring is visible without waiting
        // for the first interaction.
        this._renderRings();
    }

    /**
     * Per-slot local re-render. Touches only this slot's strip + content +
     * corner. Does NOT detach the persistent wrappers; does NOT detach
     * per-tab content elements. Safe to call from any tab-state mutation.
     */
    _renderSlotLocal(slotId) {
        var wrappers = this._wrappersBySlot.get(slotId);
        if (!wrappers) return;   // boot race — slot's leaf el not built yet
        var state = this._tabsBySlot.get(slotId) || { tabs: [], activeTabId: null };

        this._renderStripContents(slotId, wrappers.strip, state);
        this._renderContentContents(slotId, wrappers.content, state);
        this._renderCornerContents(slotId, wrappers.corner);
    }

    /**
     * RFC 0047 — re-render every slot's STRIP only (chips + "+" + pill),
     * leaving content and corners untouched. The "+" and pill read the global
     * tab pool, so a change to the workspace total in any one pane must update
     * the affordance on all of them. Cheap: a strip is a handful of nodes.
     */
    _refreshAllStrips() {
        var self = this;
        this._wrappersBySlot.forEach(function (wrappers, slotId) {
            var state = self._tabsBySlot.get(slotId) || { tabs: [], activeTabId: null };
            self._renderStripContents(slotId, wrappers.strip, state);
        });
    }

    /**
     * Strip rebuild — clear + re-add chips, "+", pill. Chips have no inner
     * state to preserve (no scroll, no focus past the close button) so a
     * full rebuild is fine and keeps the code simple.
     */
    _renderStripContents(slotId, strip, state) {
        var self = this;
        while (strip.firstChild) strip.removeChild(strip.firstChild);

        var workspaceActiveId = this._workspaceActiveTabId;
        state.tabs.forEach(function (tab) {
            var chip = document.createElement("div");
            var cls = "hmtp-chip";
            if (tab.id === state.activeTabId) cls += " hmtp-chip-active";
            if (tab.id === workspaceActiveId) cls += " hmtp-chip-workspace-active";
            chip.className = cls;
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
            chip.addEventListener("mousedown", function (ev) {
                self._drag.beginDrag(ev, slotId, tab.id);
            });
            strip.appendChild(chip);
        });

        // RFC 0047 — the "+" and the pill read the GLOBAL pool, so every strip
        // shows the same workspace used/max and the "+" disables on all panes
        // together the moment the pool is full.
        var used = this.totalTabs();
        var max  = this._budget;
        var full = used >= max;

        if (this._onAddTab && !full) {
            var addBtn = document.createElement("button");
            addBtn.className = "hmtp-strip-add";
            addBtn.textContent = "+";
            addBtn.title = "Add a tab";
            addBtn.addEventListener("mousedown", function (ev) { ev.stopPropagation(); });
            addBtn.addEventListener("click", function (ev) {
                ev.stopPropagation();
                try { self._onAddTab(slotId); } catch (e) { console.error("[MultiTabPane] onAddTab threw:", e); }
            });
            strip.appendChild(addBtn);
        }

        var pill = document.createElement("span");
        pill.className = "hmtp-pill" + (full ? " hmtp-pill-full" : "");
        pill.textContent = used + " / " + max;
        pill.title = "Workspace tab budget: " + used + " used of " + max;
        strip.appendChild(pill);
    }

    /**
     * Content area surgical update. Walks the existing children and removes
     * only what no longer belongs (empty-state node, stale cover, dead tab
     * contents); keeps still-current tab contents attached; appends new tab
     * contents the first time they're seen; toggles display per active.
     */
    _renderContentContents(slotId, content, state) {
        var self = this;
        var currentIds = Object.create(null);
        state.tabs.forEach(function (t) { currentIds[t.id] = true; });

        // Pass 1: prune children that are no longer relevant. Walk via index
        // because removeChild shifts the live HTMLCollection.
        var i = 0;
        while (i < content.children.length) {
            var ch = content.children[i];
            if (ch.classList && ch.classList.contains("hmtp-tab-content")) {
                var tabId = ch.getAttribute("data-tab-id");
                if (currentIds[tabId]) { i++; continue; }
                // stale tab content — drop
            }
            // empty-state messages and covers are always re-created — drop them
            content.removeChild(ch);
        }

        // Pass 2: ensure every current tab has its persistent _contentEl
        // attached here. tab.render(el) is called EXACTLY ONCE in the tab's
        // lifetime, when its content host is first minted.
        state.tabs.forEach(function (tab) {
            if (!tab._contentEl) {
                tab._contentEl = document.createElement("div");
                tab._contentEl.className = "hmtp-tab-content";
                tab._contentEl.setAttribute("data-tab-id", tab.id);
                if (typeof tab.render === "function") {
                    try { tab.render(tab._contentEl); }
                    catch (e) { console.error("[MultiTabPane] tab.render threw:", e); }
                }
            }
            // Attach if not already a child here. Cross-slot move = single
            // intra-document appendChild; the browser preserves scroll on
            // descendants for moves between two attached parents.
            if (tab._contentEl.parentNode !== content) content.appendChild(tab._contentEl);
            tab._contentEl.style.display = (tab.id === state.activeTabId) ? "block" : "none";
        });

        // Empty-state placeholder when the pane has no tabs.
        if (state.tabs.length === 0) {
            var empty = document.createElement("div");
            empty.className = "hmtp-empty";
            empty.textContent = "(empty pane)";
            content.appendChild(empty);
            return;
        }

        // Cover overlay — sits above the active tab's content when the pane
        // isn't entered (workspace-active). RFC 0048: a single click SELECTS the
        // pane (shallow cursor); a double click ENTERS it (deep). Keyboard /
        // non-mouse interactions are gated by the widget's own setActive(false).
        if (state.activeTabId && state.activeTabId !== this._workspaceActiveTabId) {
            var cover = document.createElement("div");
            cover.className = "hmtp-inactive-cover";
            cover.addEventListener("mousedown", function (ev) { ev.stopPropagation(); });
            cover.addEventListener("click", function (ev) {
                ev.stopPropagation();
                self.selectPane(slotId);
            });
            cover.addEventListener("dblclick", function (ev) {
                ev.stopPropagation();
                self.enterDeep(slotId);
            });
            content.appendChild(cover);
        }
    }

    /**
     * Corner buttons rebuild — split/merge enable state can change on any
     * tab op (e.g. merge becomes available when sibling slot empties). Cheap
     * full rebuild.
     */
    _renderCornerContents(slotId, corner) {
        var self = this;
        while (corner.firstChild) corner.removeChild(corner.firstChild);

        var canS = this.canSplit(slotId);
        corner.appendChild(this._cbtn("⇆", "split horizontally", canS,
                function () { self.split(slotId, "horizontal"); }));
        corner.appendChild(this._cbtn("⇅", "split vertically", canS,
                function () { self.split(slotId, "vertical"); }));

        var canM = this.canMerge(slotId);
        if (canM.allowed) {
            corner.appendChild(this._cbtn("⤢", "merge with sibling",
                    { allowed: true, reason: "" },
                    function () { self.merge(slotId); }));
        }
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
