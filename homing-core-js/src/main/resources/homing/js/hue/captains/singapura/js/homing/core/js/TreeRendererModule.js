// =============================================================================
// TreeRendererModule — the JS half of the rigid tree substrate.
//
// TreeRenderer draws the canonical TreeNode JSON (level + typed dimensions +
// children) as a collapsible tree and emits a selection callback on click.
// It reads ONLY the substrate's universal dimensions (displayLabel, kind,
// summary), so the same renderer draws a catalogue tree, a pivot grouping,
// or any future tree shape — zero per-tree-kind JS. That is the substrate's
// headline promise made concrete: any conforming tree renders without a
// single line of bespoke rendering code.
//
// Ownership: the caller passes a DomOpsParty branch; every element is created
// through it, so dissolving the caller's branch tears the tree down cleanly.
//
//   new TreeRenderer({ branch, container, data, onSelect, expandDepth })
//   renderer.setData(treeJson)        // (re)load a tree
//   renderer.handleKeydown(event)     // arrow-key navigation; returns true
//                                     //   if the key was consumed
//
// Selection is tracked by ROW-ENTRY REFERENCE, never by node id: the renderer
// builds every row, so it holds the row's entry object directly. Two nodes may
// share an id string (e.g. two catalogues with the same display name) and
// selection still resolves to the clicked row — there is no id-keyed map to
// collide in. The renderer is otherwise identity-agnostic; node.id is only
// passed through in the selection payload for the caller's use.
//
// onSelect receives a flattened selection object:
//   { id, level, kind, label, summary, hasChildren }
//
// Keyboard (handleKeydown): ArrowDown/ArrowUp move the selection through the
// VISIBLE rows (live-follow — each move fires onSelect); ArrowRight expands the
// focused branch one level, ArrowLeft folds it. The host widget owns WHEN keys
// flow (it forwards keydown only while workspace-active); the renderer owns the
// key semantics. Returns true when it consumed the key so the host can
// preventDefault (stop page scroll).
// =============================================================================

class TreeRenderer {

    constructor(opts) {
        if (!opts || !opts.branch)    throw new Error('[TreeRenderer] opts.branch required');
        if (!opts.container)          throw new Error('[TreeRenderer] opts.container required');
        this._branch    = opts.branch;
        this._container = opts.container;
        this._onSelect  = opts.onSelect || function () {};
        this._expandDepth = (typeof opts.expandDepth === 'number') ? opts.expandDepth : 1;
        this._n            = 0;
        this._flat         = [];    // row entries in pre-order (for keyboard nav)
        this._selectedEntry = null; // the currently-selected row entry (by ref)
        if (opts.data) this.setData(opts.data);
    }

    _el(tag) { return this._branch.createElement('tn' + (this._n++), tag); }

    // Extract a dimension's text by its substrate key tag.
    _dim(node, key) {
        var dims = node.dimensions || [];
        for (var i = 0; i < dims.length; i++) {
            if (dims[i].key === key) return dims[i].text;
        }
        return '';
    }

    _toSelection(node) {
        return {
            id:          node.id,
            level:       node.level,
            kind:        this._dim(node, 'kind'),
            label:       this._dim(node, 'displayLabel'),
            summary:     this._dim(node, 'summary'),
            hasChildren: !!(node.children && node.children.length)
        };
    }

    setData(data) {
        while (this._container.firstChild) this._container.removeChild(this._container.firstChild);
        this._flat          = [];
        this._selectedEntry = null;
        var rootWrap = this._el('div');
        this._container.appendChild(rootWrap);
        this._renderNode(data, rootWrap, 0);
    }

    _renderNode(node, parentEl, depth) {
        var sel      = this._toSelection(node);
        var isBranch = sel.hasChildren;
        var self     = this;

        var row = this._el('div');
        row.style.cssText = 'display:flex;align-items:center;gap:6px;padding:3px 6px;'
            + 'cursor:pointer;border-radius:4px;font-size:13px;line-height:1.4;'
            + 'padding-left:' + (6 + depth * 16) + 'px;';

        var caret = this._el('span');
        caret.style.cssText = 'width:12px;display:inline-block;color:#888;font-size:10px;flex:0 0 auto;';
        row.appendChild(caret);

        var label = this._el('span');
        label.textContent = sel.label || sel.id;
        label.style.cssText = 'flex:1;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;';
        row.appendChild(label);

        parentEl.appendChild(row);
        // Record before recursing so _flat lands in pre-order (parent then
        // its subtree) — the natural top-to-bottom visual order arrow nav walks.
        var entry = {
            rowEl: row, node: node, caretEl: caret,
            kidsEl: null, hasChildren: isBranch, depth: depth
        };
        this._flat.push(entry);

        var kids = null;
        if (isBranch) {
            kids = this._el('div');
            entry.kidsEl = kids;
            parentEl.appendChild(kids);
            for (var i = 0; i < node.children.length; i++) {
                this._renderNode(node.children[i], kids, depth + 1);
            }
            this._setExpanded(entry, depth < this._expandDepth);
        }

        row.addEventListener('click', function (e) {
            e.stopPropagation();
            if (isBranch) self._setExpanded(entry, !self._isExpanded(entry));
            self._markSelected(entry);
            self._onSelect(self._toSelection(node));
        });
        row.addEventListener('mouseenter', function () {
            if (self._selectedEntry !== entry) row.style.background = 'rgba(0,0,0,0.05)';
        });
        row.addEventListener('mouseleave', function () {
            if (self._selectedEntry !== entry) row.style.background = '';
        });
    }

    _markSelected(entry) {
        if (this._selectedEntry) {
            this._selectedEntry.rowEl.style.background = '';
        }
        this._selectedEntry = entry;
        if (entry) {
            entry.rowEl.style.background = 'rgba(59,130,246,0.20)';
        }
    }

    // ── Expand / collapse (shared by click + keyboard) ──────────────────────

    _isExpanded(entry) {
        return !!(entry && entry.kidsEl && entry.kidsEl.style.display !== 'none');
    }

    _setExpanded(entry, expanded) {
        if (!entry || !entry.hasChildren || !entry.kidsEl) return;
        entry.kidsEl.style.display = expanded ? 'block' : 'none';
        entry.caretEl.textContent  = expanded ? '▾' : '▸';   // ▾ / ▸
    }

    // ── Keyboard navigation ─────────────────────────────────────────────────

    // Visible row entries in top-to-bottom order: walk the pre-order _flat list,
    // skipping any row deeper than a collapsed ancestor.
    _visibleEntries() {
        var out = [];
        var hideDepth = Infinity;   // rows deeper than this are hidden
        for (var i = 0; i < this._flat.length; i++) {
            var e = this._flat[i];
            if (e.depth > hideDepth) continue;       // under a collapsed node
            hideDepth = Infinity;                    // back in a visible region
            out.push(e);
            if (e.hasChildren && !this._isExpanded(e)) {
                hideDepth = e.depth;                 // hide this node's subtree
            }
        }
        return out;
    }

    // Move selection to a row entry: highlight, scroll into view, fire onSelect.
    _focusTo(entry) {
        if (!entry) return;
        this._markSelected(entry);
        if (entry.rowEl.scrollIntoView) {
            try { entry.rowEl.scrollIntoView({ block: 'nearest' }); }
            catch (x) { entry.rowEl.scrollIntoView(); }
        }
        this._onSelect(this._toSelection(entry.node));
    }

    // Arrow-key handler. Returns true iff the key was consumed.
    handleKeydown(ev) {
        var key = ev && ev.key;
        if (key !== 'ArrowDown' && key !== 'ArrowUp'
            && key !== 'ArrowRight' && key !== 'ArrowLeft') {
            return false;
        }
        var vis = this._visibleEntries();
        if (vis.length === 0) return false;
        var idx = this._selectedEntry ? vis.indexOf(this._selectedEntry) : -1;

        if (key === 'ArrowDown') {
            this._focusTo(vis[idx < 0 ? 0 : Math.min(idx + 1, vis.length - 1)]);
            return true;
        }
        if (key === 'ArrowUp') {
            this._focusTo(vis[idx < 0 ? 0 : Math.max(idx - 1, 0)]);
            return true;
        }
        // Right / Left: act on the focused branch; if nothing is focused yet,
        // the first key just lands focus on the first row.
        if (idx < 0) { this._focusTo(vis[0]); return true; }
        var e = this._selectedEntry;
        if (key === 'ArrowRight') {
            if (e && e.hasChildren && !this._isExpanded(e)) {
                this._setExpanded(e, true);
            }
            return true;
        }
        // ArrowLeft
        if (e && e.hasChildren && this._isExpanded(e)) {
            this._setExpanded(e, false);
        }
        return true;
    }
}
