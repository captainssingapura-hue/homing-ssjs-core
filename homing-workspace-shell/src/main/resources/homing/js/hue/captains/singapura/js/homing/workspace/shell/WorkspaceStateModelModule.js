// =============================================================================
// WorkspaceStateModel — pure in-memory model of workspace effective state.
//
// Replaces physical replay (each event triggers an MTP mutation) with
// virtual replay: events fold into this model, then orchestrator projects
// the final state to MTP once. A spawn-then-close pair becomes a no-op
// at projection time; a spawn-then-move-five-times becomes one place to
// the final pane.
//
// Three pieces:
//   _layout       : MTP-native layout tree ({kind:'leaf',slotId} | split)
//   _tabsBySlot   : Map<slotId, Array<TabDescriptor>>
//   _activeUuid   : widget UUID currently workspace-active (or null)
//
// TabDescriptor shape:
//   { widgetInstanceUuid, widgetKind, params, title, pinned }
//
// Events folded via apply(eventRow). Recognised event names:
//   WidgetSpawnedPinned       — add a pinned widget
//   WidgetSpawnedFromPicker   — add a picker-spawned widget
//   TabMoved                  — relocate by widgetInstanceId
//   TabClosed                 — remove by widgetInstanceId
//   SplitCreated              — split a leaf into two leaves (parent path)
//   SplitMerged               — collapse a split back to a single leaf
//   WorkspaceActiveChanged    — set active widget UUID
// Any other event name is silently ignored.
//
// Explicit Substrate doctrine:
//   - Per-workspace instance; no INSTANCE singleton.
//   - No external collaborators (no MTP ref, no recorder, no DOM).
//   - All mutations idempotent where it makes sense.
// =============================================================================

class WorkspaceStateModel {

    constructor() {
        // Mirror MTP._defaultLayout. Stays identical to MTP's slot ids so
        // projection lines up without translation. If the framework ever
        // changes its default, this constructor needs to track.
        this._layout = {
            kind: 'split', orientation: 'vertical',
            children: [
                { ratio: 0.5, pane: {
                    kind: 'split', orientation: 'horizontal',
                    children: [
                        { ratio: 0.5, pane: { kind: 'leaf', slotId: 'tl' } },
                        { ratio: 0.5, pane: { kind: 'leaf', slotId: 'tr' } }
                    ]
                } },
                { ratio: 0.5, pane: {
                    kind: 'split', orientation: 'horizontal',
                    children: [
                        { ratio: 0.5, pane: { kind: 'leaf', slotId: 'bl' } },
                        { ratio: 0.5, pane: { kind: 'leaf', slotId: 'br' } }
                    ]
                } }
            ]
        };
        this._tabsBySlot = new Map();
        for (const id of ['tl', 'tr', 'bl', 'br']) this._tabsBySlot.set(id, []);
        this._activeUuid   = null;
        this._nextSplitId  = 1;
    }

    // ── Public API ──────────────────────────────────────────────────────

    /** Fold one event into the model. Unknown event names are ignored. */
    apply(eventRow) {
        if (!eventRow) return;
        const name = eventRow.name;
        const p    = eventRow.payload || {};
        switch (name) {
            case 'WidgetSpawnedPinned':     this._spawn(p, true);  break;
            case 'WidgetSpawnedFromPicker': this._spawn(p, false); break;
            case 'TabMoved':                this._move(p);         break;
            case 'TabClosed':               this._close(p);        break;
            case 'SplitCreated':            this._split(p);        break;
            case 'SplitMerged':             this._merge(p);        break;
            case 'SplitRatioChanged':       this._setRatio(p);     break;
            case 'WorkspaceActiveChanged':  this._setActive(p);    break;
            default: /* unknown event — ignore */                  break;
        }
    }

    /** Whether the model has any tabs at all. */
    isEmpty() {
        for (const tabs of this._tabsBySlot.values()) {
            if (tabs.length > 0) return false;
        }
        return true;
    }

    /** MTP-native layout tree (a fresh deep copy is safer for projection). */
    layout() { return this._cloneNode(this._layout); }

    /** Map<slotId, TabDescriptor[]> — fresh map; tab descriptors shared. */
    tabsBySlot() {
        const out = new Map();
        this._tabsBySlot.forEach(function (arr, slotId) {
            out.set(slotId, arr.slice());
        });
        return out;
    }

    /** Currently workspace-active widgetInstanceUuid, or null. */
    activeUuid() { return this._activeUuid; }

    /** Inspect snapshot — for tests + DevTools surfacing. */
    inspect() {
        const tabs = [];
        this._tabsBySlot.forEach(function (arr, slot) {
            for (const t of arr) tabs.push({
                slot:               slot,
                widgetInstanceUuid: t.widgetInstanceUuid,
                widgetKind:         t.widgetKind,
                pinned:             !!t.pinned
            });
        });
        return {
            layout:       this._layout,
            tabs:         tabs,
            activeUuid:   this._activeUuid,
            slotCount:    this._tabsBySlot.size,
            nextSplitId:  this._nextSplitId
        };
    }

    /**
     * Serialise to a checkpoint snapshot. Plain-JSON-safe (no Maps).
     * Stores the layout, tab descriptors per slot (as an array of
     * [slotId, tabs[]] pairs), active uuid, and split counter so the
     * next mint after restore doesn't collide with restored slot ids.
     */
    toSnapshot() {
        const tabsPairs = [];
        this._tabsBySlot.forEach(function (arr, slot) {
            tabsPairs.push([slot, arr.slice()]);
        });
        return {
            schemaVersion: 1,
            layout:        this._cloneNode(this._layout),
            tabsBySlot:    tabsPairs,
            activeUuid:    this._activeUuid,
            nextSplitId:   this._nextSplitId
        };
    }

    /**
     * Restore from a snapshot produced by {@code toSnapshot()}.
     * Permissive: missing fields default to fresh-model values; an
     * unknown schemaVersion throws (caller falls back to fresh model).
     */
    static fromSnapshot(snapshot) {
        const m = new WorkspaceStateModel();
        if (!snapshot) return m;
        if (snapshot.schemaVersion !== 1) {
            throw new Error('[WorkspaceStateModel] unknown snapshot '
                          + 'schemaVersion: ' + snapshot.schemaVersion);
        }
        if (snapshot.layout) m._layout = m._cloneNode(snapshot.layout);
        m._tabsBySlot = new Map();
        if (Array.isArray(snapshot.tabsBySlot)) {
            for (const pair of snapshot.tabsBySlot) {
                if (Array.isArray(pair) && pair.length === 2) {
                    m._tabsBySlot.set(pair[0], pair[1].slice());
                }
            }
        }
        // Ensure every leaf has a tabs entry (snapshot may have pruned empties).
        m._forEachLeaf(m._layout, function (slotId) {
            if (!m._tabsBySlot.has(slotId)) m._tabsBySlot.set(slotId, []);
        });
        m._activeUuid  = snapshot.activeUuid || null;
        m._nextSplitId = (typeof snapshot.nextSplitId === 'number')
                       ? snapshot.nextSplitId : 1;
        return m;
    }

    // ── Event handlers ──────────────────────────────────────────────────

    _spawn(p, isPinned) {
        const uuid = p.widgetInstanceId;
        if (!uuid) return;
        if (this._findTab(uuid)) return;   // idempotent
        const slot = this._slotIdOfPaneId(p.to && p.to.paneId)
                  || this._defaultSlot();
        const tabs = this._tabsBySlot.get(slot);
        if (!tabs) return;                  // unknown slot — drop
        const idx = (p.to && typeof p.to.tabIndex === 'number')
                  ? Math.max(0, Math.min(p.to.tabIndex, tabs.length))
                  : tabs.length;
        tabs.splice(idx, 0, {
            widgetInstanceUuid: uuid,
            widgetKind:         p.widgetKind,
            params:             p.params || {},
            title:              p.title || null,
            pinned:             !!isPinned
        });
    }

    _move(p) {
        const uuid = p.widgetInstanceId;
        if (!uuid) return;
        const found = this._findTab(uuid);
        if (!found) return;
        const destSlot = this._slotIdOfPaneId(p.to && p.to.paneId)
                      || this._defaultSlot();
        if (!this._tabsBySlot.has(destSlot)) return;
        // Remove from src
        const srcTabs = this._tabsBySlot.get(found.slot);
        const [tab]   = srcTabs.splice(found.index, 1);
        // Insert at dest
        const destTabs = this._tabsBySlot.get(destSlot);
        const idx = (p.to && typeof p.to.tabIndex === 'number')
                  ? Math.max(0, Math.min(p.to.tabIndex, destTabs.length))
                  : destTabs.length;
        destTabs.splice(idx, 0, tab);
    }

    _close(p) {
        const uuid = p.widgetInstanceId;
        if (!uuid) return;
        const found = this._findTab(uuid);
        if (!found) return;
        this._tabsBySlot.get(found.slot).splice(found.index, 1);
        if (this._activeUuid === uuid) this._activeUuid = null;
    }

    /**
     * Split a leaf into two leaves. Payload.paneId is the parent path
     * AFTER the split (matches orchestrator's emit which strips the
     * trailing '_1' / '_2' from the post-split first-child path). For
     * an empty paneId or '_' we split the root leaf if there is one;
     * otherwise traverse to the target leaf and replace it.
     */
    _split(p) {
        const parentPath = p.paneId || '_';
        const found = this._findNodeByPaneId(parentPath);
        if (!found || !found.node || found.node.kind !== 'leaf') return;
        const orig = found.node;
        const newSlotId = 'sp_' + (this._nextSplitId++);
        const splitNode = {
            kind: 'split',
            orientation: (p.orientation === 'vertical') ? 'vertical' : 'horizontal',
            children: [
                { ratio: 0.5, pane: { kind: 'leaf', slotId: orig.slotId } },
                { ratio: 0.5, pane: { kind: 'leaf', slotId: newSlotId } }
            ]
        };
        found.replaceWith(splitNode);
        if (!this._tabsBySlot.has(newSlotId)) this._tabsBySlot.set(newSlotId, []);
    }

    /**
     * Merge a split node back into a single leaf. Payload.paneId is the
     * parent split path. Keeps the first child (matches V1 / orchestrator
     * convention); sibling's tabs are appended to kept slot's list.
     */
    _merge(p) {
        const parentPath = p.paneId || '_';
        const found = this._findNodeByPaneId(parentPath);
        if (!found || !found.node || found.node.kind !== 'split') return;
        const kids = found.node.children;
        // We support merge only when both children are leaves; nested
        // merges are out of scope (mirrors mtp.merge contract).
        if (!kids || kids.length !== 2
            || kids[0].pane.kind !== 'leaf'
            || kids[1].pane.kind !== 'leaf') return;
        const keepSlot   = kids[0].pane.slotId;
        const removeSlot = kids[1].pane.slotId;
        const kept       = this._tabsBySlot.get(keepSlot)   || [];
        const removed    = this._tabsBySlot.get(removeSlot) || [];
        for (const t of removed) kept.push(t);
        this._tabsBySlot.set(keepSlot, kept);
        this._tabsBySlot.delete(removeSlot);
        found.replaceWith({ kind: 'leaf', slotId: keepSlot });
    }

    _setActive(p) {
        if (!p.to || !p.to.widgetInstanceId) return;
        this._activeUuid = p.to.widgetInstanceId;
    }

    /**
     * Update the first-child ratio of the split at paneId. Second
     * child's ratio is derived as 1 - newRatio. No-op for missing
     * path or non-split target.
     */
    _setRatio(p) {
        const parentPath = p.paneId || '_';
        const r = (typeof p.ratio === 'number') ? p.ratio : null;
        if (r == null || !isFinite(r) || r <= 0 || r >= 1) return;
        const found = this._findNodeByPaneId(parentPath);
        if (!found || !found.node || found.node.kind !== 'split') return;
        const kids = found.node.children;
        if (!kids || kids.length !== 2) return;
        kids[0].ratio = r;
        kids[1].ratio = 1 - r;
    }

    // ── Path / lookup helpers ───────────────────────────────────────────

    /** Returns {tab, slot, index} for a uuid, or null. */
    _findTab(uuid) {
        let hit = null;
        this._tabsBySlot.forEach(function (arr, slot) {
            if (hit) return;
            for (let i = 0; i < arr.length; i++) {
                if (arr[i].widgetInstanceUuid === uuid) {
                    hit = { tab: arr[i], slot: slot, index: i };
                    return;
                }
            }
        });
        return hit;
    }

    /**
     * Walk the layout tree following the paneId path
     * ('_' = root, '_1' = first child of root, '_1_2' = second child of
     * first child of root) and return a handle:
     *   { node, replaceWith(newNode), parent, indexInParent }
     */
    _findNodeByPaneId(paneId) {
        // Walk segments. Root case handled outside the loop.
        const parts = String(paneId || '_').split('_').filter(s => s.length > 0);
        const self  = this;
        if (parts.length === 0) {
            // Root: no parent, replace by mutating this._layout.
            return {
                node:          this._layout,
                replaceWith:   function (n) { self._layout = n; },
                parent:        null,
                indexInParent: -1
            };
        }
        let parent  = this._layout;
        let cur     = this._layout;
        let parentSlot = null;   // {parentNode, childIndex} for replaceWith
        for (const part of parts) {
            if (!cur || cur.kind !== 'split') return null;
            const idx = (part === '1') ? 0 : (part === '2') ? 1 : -1;
            if (idx < 0) return null;
            parent     = cur;
            parentSlot = { parentNode: cur, childIndex: idx };
            cur        = cur.children[idx].pane;
        }
        if (!parentSlot) return null;
        return {
            node:          cur,
            replaceWith:   function (n) { parentSlot.parentNode.children[parentSlot.childIndex].pane = n; },
            parent:        parent,
            indexInParent: parentSlot.childIndex
        };
    }

    /** Returns the live slotId at a paneId path, or null. */
    _slotIdOfPaneId(paneId) {
        if (paneId == null) return null;
        if (paneId === '' || paneId === '_') {
            // Root only resolves if it's a leaf.
            return (this._layout && this._layout.kind === 'leaf')
                ? this._layout.slotId : null;
        }
        const hit = this._findNodeByPaneId(paneId);
        return (hit && hit.node && hit.node.kind === 'leaf')
                ? hit.node.slotId : null;
    }

    /** First leaf's slotId — used as default spawn target. */
    _defaultSlot() {
        const leaf = this._firstLeaf(this._layout);
        return leaf ? leaf.slotId : null;
    }

    _firstLeaf(node) {
        if (!node) return null;
        if (node.kind === 'leaf') return node;
        return this._firstLeaf(node.children[0].pane);
    }

    /** Walk every leaf in the layout, invoking fn(slotId). */
    _forEachLeaf(node, fn) {
        if (!node) return;
        if (node.kind === 'leaf') { fn(node.slotId); return; }
        for (const c of node.children) this._forEachLeaf(c.pane, fn);
    }

    /** Deep clone a layout tree node — defensive snapshot. */
    _cloneNode(node) {
        if (!node) return null;
        if (node.kind === 'leaf') return { kind: 'leaf', slotId: node.slotId };
        return {
            kind: 'split',
            orientation: node.orientation,
            children: node.children.map(c => ({
                ratio: c.ratio,
                pane:  this._cloneNode(c.pane)
            }))
        };
    }
}
