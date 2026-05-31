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
//   renderer.selectById(id) -> bool   // programmatic selection
//
// onSelect receives a flattened selection object:
//   { id, level, kind, label, summary, hasChildren }
// =============================================================================

class TreeRenderer {

    constructor(opts) {
        if (!opts || !opts.branch)    throw new Error('[TreeRenderer] opts.branch required');
        if (!opts.container)          throw new Error('[TreeRenderer] opts.container required');
        this._branch    = opts.branch;
        this._container = opts.container;
        this._onSelect  = opts.onSelect || function () {};
        this._expandDepth = (typeof opts.expandDepth === 'number') ? opts.expandDepth : 1;
        this._n          = 0;
        this._rowsById   = {};      // id -> { rowEl, node }
        this._selectedId = null;
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
        this._rowsById   = {};
        this._selectedId = null;
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
        this._rowsById[node.id] = { rowEl: row, node: node };

        var kids = null;
        if (isBranch) {
            kids = this._el('div');
            parentEl.appendChild(kids);
            for (var i = 0; i < node.children.length; i++) {
                this._renderNode(node.children[i], kids, depth + 1);
            }
            var expanded = depth < this._expandDepth;
            kids.style.display = expanded ? 'block' : 'none';
            caret.textContent  = expanded ? '▾' : '▸';   // ▾ / ▸
        }

        row.addEventListener('click', function (e) {
            e.stopPropagation();
            if (isBranch && kids) {
                var nowHidden = kids.style.display === 'none';
                kids.style.display = nowHidden ? 'block' : 'none';
                caret.textContent  = nowHidden ? '▾' : '▸';
            }
            self._markSelected(node.id);
            self._onSelect(self._toSelection(node));
        });
        row.addEventListener('mouseenter', function () {
            if (self._selectedId !== node.id) row.style.background = 'rgba(0,0,0,0.05)';
        });
        row.addEventListener('mouseleave', function () {
            if (self._selectedId !== node.id) row.style.background = '';
        });
    }

    _markSelected(id) {
        if (this._selectedId && this._rowsById[this._selectedId]) {
            this._rowsById[this._selectedId].rowEl.style.background = '';
        }
        this._selectedId = id;
        if (this._rowsById[id]) {
            this._rowsById[id].rowEl.style.background = 'rgba(59,130,246,0.20)';
        }
    }

    selectById(id) {
        var entry = this._rowsById[id];
        if (!entry) return false;
        this._markSelected(id);
        this._onSelect(this._toSelection(entry.node));
        return true;
    }
}
