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
//   new TreeRenderer({ branch, container, data, onSelect, onActivate, onToggle, expandDepth })
//   renderer.setData(treeJson)        // (re)load a tree
//   renderer.handleKeydown(event)     // arrow-key navigation; returns true
//                                     //   if the key was consumed
//
// onToggle({ path, expanded }) fires when the USER expands/collapses a branch
// (caret click or ArrowRight/Left), never during the initial expandDepth build.
// A host uses it to keep a second view (e.g. renderDocTree's body) folded in
// sync with the TOC. Optional — omit it and expand/collapse is TOC-local.
//
// onActivate(selection) fires on an INTENTIONAL open — Enter on the selected
// row, or a double-click — distinct from the high-frequency onSelect that fires
// on every arrow-move / single click. A host wires onActivate to an expensive
// "open this node" action (e.g. render the selected doc in a detail pane) so
// that cost is paid only when the user means to open, not merely to browse.
// Optional — omit it and Enter / double-click are no-ops beyond selection.
//
// Selection is tracked by ROW-ENTRY REFERENCE, never by node id: the renderer
// builds every row, so it holds the row's entry object directly. Two nodes may
// share a display label (e.g. two catalogues with the same name) and selection
// still resolves to the clicked row — there is no id-keyed map to collide in.
// Node identity is purely positional: each row carries its child-index `path`
// (RFC 0040), which the caller uses to address the node (e.g. the leveled
// /open?l0=..&l1=.. URL). Nodes carry no id.
//
// onSelect receives a flattened selection object:
//   { path, level, kind, label, summary, hasChildren }
//
// Keyboard (handleKeydown): ArrowDown/ArrowUp move the selection through the
// VISIBLE rows (live-follow — each move fires onSelect); ArrowRight expands the
// focused branch one level, ArrowLeft folds it; Enter opens the current
// selection (fires onActivate). The host widget owns WHEN keys flow (it forwards
// keydown only while workspace-active); the renderer owns the key semantics.
// Returns true when it consumed the key so the host can preventDefault (stop
// page scroll).
// =============================================================================

class TreeRenderer {

    constructor(opts) {
        if (!opts || !opts.branch)    throw new Error('[TreeRenderer] opts.branch required');
        if (!opts.container)          throw new Error('[TreeRenderer] opts.container required');
        this._branch    = opts.branch;
        this._container = opts.container;
        this._onSelect  = opts.onSelect || function () {};
        // Fired on an INTENTIONAL open — Enter on the selected row or a
        // double-click — NOT on every selection move. The host pays the
        // expensive open cost (e.g. rendering the doc) only when the user
        // means to open. Optional; default no-op leaves Enter/dblclick as
        // pure selection.
        this._onActivate = opts.onActivate || function () {};
        // Fired when the USER expands/collapses a branch (caret click or
        // ArrowRight/Left) — NOT during the initial expandDepth build. Carries
        // { path, expanded } so a host (e.g. renderDocTree) can fold the body
        // in sync with the TOC. Initial render stays silent so the host's body
        // starts in the matching all-expanded state without a toggle storm.
        this._onToggle  = opts.onToggle || function () {};
        // Optional: path -> href string. When set, each row's label is an <a>
        // anchor with that href, so TOC navigation survives a static HTML export
        // (no JS) via the fragment link. Live, the row handler preventDefaults
        // and uses onSelect (smooth scroll) instead of the raw anchor jump.
        this._hrefForPath = opts.hrefForPath || null;
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

    // No node id is surfaced to the UI — identity is the structural `path`
    // added by _emitSelection. (The wire JSON may still carry an id field;
    // the UI simply doesn't read it.)
    _toSelection(node) {
        return {
            level:       node.level,
            kind:        this._dim(node, 'kind'),
            label:       this._dim(node, 'displayLabel'),
            summary:     this._dim(node, 'summary'),
            hasChildren: !!(node.children && node.children.length)
        };
    }

    // Emit the selection for a row entry, carrying its leveled child-index
    // `path` (the structural position) alongside the node's display fields.
    // Consumers address the node by path, not by any stamped id.
    _emitSelection(entry) {
        var sel = this._toSelection(entry.node);
        sel.path = entry.path;
        this._onSelect(sel);
    }

    // Emit an ACTIVATION (intentional open) for a row entry — same flattened
    // shape as selection, carrying the node's leveled `path`. Distinct callback
    // from onSelect so a host can gate the expensive open on real intent.
    _emitActivation(entry) {
        var sel = this._toSelection(entry.node);
        sel.path = entry.path;
        this._onActivate(sel);
    }

    // Activate a row (Enter / double-click): make sure it's the selection, then
    // fire onActivate. Selection is emitted by the originating click/keymove, so
    // here we only ensure the highlight and raise the open intent.
    _activate(entry) {
        if (!entry) return;
        this._markSelected(entry);
        this._emitActivation(entry);
    }

    setData(data) {
        while (this._container.firstChild) this._container.removeChild(this._container.firstChild);
        this._flat          = [];
        this._selectedEntry = null;
        var rootWrap = this._el('div');
        this._container.appendChild(rootWrap);
        this._renderNode(data, rootWrap, 0, []);
    }

    // `path` is the leveled child-index path from the root ([] at the root,
    // parent.path.concat([childIndex]) below). It is purely structural — the
    // node carries no id — and is what the leveled "open" URL encodes.
    _renderNode(node, parentEl, depth, path) {
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

        var label;
        if (this._hrefForPath) {
            // Anchor label — clicking jumps to the target's id via the fragment
            // href, which works in a static HTML export with no JavaScript.
            label = this._el('a');
            label.setAttribute('href', this._hrefForPath(path) || '#');
            label.style.cssText = 'flex:1;white-space:nowrap;overflow:hidden;'
                + 'text-overflow:ellipsis;color:inherit;text-decoration:none;';
        } else {
            label = this._el('span');
            label.style.cssText = 'flex:1;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;';
        }
        label.textContent = sel.label || '';
        row.appendChild(label);

        parentEl.appendChild(row);
        // Record before recursing so _flat lands in pre-order (parent then
        // its subtree) — the natural top-to-bottom visual order arrow nav walks.
        var entry = {
            rowEl: row, node: node, caretEl: caret,
            kidsEl: null, hasChildren: isBranch, depth: depth, path: path
        };
        this._flat.push(entry);

        var kids = null;
        if (isBranch) {
            kids = this._el('div');
            entry.kidsEl = kids;
            parentEl.appendChild(kids);
            for (var i = 0; i < node.children.length; i++) {
                this._renderNode(node.children[i], kids, depth + 1, path.concat([i]));
            }
            this._setExpanded(entry, depth < this._expandDepth);
        }

        row.addEventListener('click', function (e) {
            e.stopPropagation();
            // Live: suppress the anchor's native fragment jump in favour of the
            // smooth scroll + highlight onSelect drives. (In a static export the
            // handler is gone, so the anchor href navigates natively.)
            if (self._hrefForPath) e.preventDefault();
            if (isBranch) self._userSetExpanded(entry, !self._isExpanded(entry));
            self._markSelected(entry);
            self._emitSelection(entry);
        });
        // Double-click = intentional open. The two underlying single-clicks
        // already selected this row (so a detail/summary pane updated cheaply);
        // the dblclick raises onActivate so the host opens it. preventDefault
        // suppresses the anchor's native jump / text selection on the gesture.
        row.addEventListener('dblclick', function (e) {
            e.stopPropagation();
            if (self._hrefForPath) e.preventDefault();
            self._activate(entry);
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

    // User-initiated expand/collapse — toggles the TOC row AND notifies the
    // host (onToggle), so the body folds in sync. The silent _setExpanded is
    // reserved for the initial expandDepth build.
    _userSetExpanded(entry, expanded) {
        this._setExpanded(entry, expanded);
        if (entry) this._onToggle({ path: entry.path, expanded: !!expanded });
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
        this._emitSelection(entry);
    }

    // Arrow-key handler. Returns true iff the key was consumed.
    handleKeydown(ev) {
        var key = ev && ev.key;
        if (key !== 'ArrowDown' && key !== 'ArrowUp'
            && key !== 'ArrowRight' && key !== 'ArrowLeft'
            && key !== 'Enter') {
            return false;
        }
        // Enter = intentional open of the current selection (no list walk).
        if (key === 'Enter') {
            if (this._selectedEntry) this._activate(this._selectedEntry);
            return true;
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
                this._userSetExpanded(e, true);
            }
            return true;
        }
        // ArrowLeft
        if (e && e.hasChildren && this._isExpanded(e)) {
            this._userSetExpanded(e, false);
        }
        return true;
    }
}
