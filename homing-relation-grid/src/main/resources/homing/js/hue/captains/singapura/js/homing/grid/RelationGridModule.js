// =============================================================================
// RelationGridModule — RFC 0050's facade: composes the grid family and is the
// ONLY place the two branches meet. Orchestration only — it holds no map
// logic (GridViewMaps), builds no chrome (GridLayout), owns no cell
// (GridCells); it reads the adapter, threads the pieces, and re-places cell
// content into freshly-minted slots whenever the view changes.
//
//   new RelationGrid({
//       container,          // where the layout mounts
//       branch,             // the CELLS branch (DomOpsParty) — handed in
//       adapter,            // RelationAdapterContract shape (JS): pks(),
//                           //   columns(), get(pk, col), subscribe(fn), …
//       cellFactory?,       // (column, value, meta) → cell; default:
//                           //   number → NumberCell, else TextCell
//       onViewChanged?      // ('rows' | 'columns' | 'base') — after re-place
//   });
//
// Surface: render + view commands (D7: deferred while editing) + the
// rAF-batched direct path + selection + single-cell deep edit + bulk ops
// (raw-value TSV copy, delete by PK set, Delete-clears) + the VIRTUAL bulk
// edit session (EffectiveType-gated value replacement, buffer + caret, live
// preview, instantaneous types for game grids — the adapter is the move
// seam; onAction is gone). Options: editable, onEditStarted /
// onEditCommitted(pk, column, v), onBulkEditRejected({reason, names}),
// onBulkEditCommitted(ids, value), onReleaseRequested (idle Escape),
// onCopy(tsv), onCursorMoved, onSelectionChanged, onViewChanged.
// =============================================================================

class RelationGrid {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[RelationGrid] opts.container is required");
        if (!opts.branch)    throw new Error("[RelationGrid] opts.branch is required");
        if (!opts.adapter)   throw new Error("[RelationGrid] opts.adapter is required");
        this._adapter = opts.adapter;
        this._cbViewChanged = opts.onViewChanged || null;
        this._factory = opts.cellFactory || function (column, value) {
            return (typeof value === "number") ? new NumberCell() : new TextCell(); };

        var self = this;
        this._squelch = false;   // suppresses interim refreshes inside atomic commands
        this._maps = new GridViewMaps({
            pks:     this._adapter.pks(),
            columns: this._adapter.columns(),
            onViewChanged: function (kind) { if (!self._squelch) self._refresh(kind); }
        });
        this._layout = new GridLayout({
            container: opts.container,
            label: opts.label || null,
            readOnly: opts.editable === false,
            onCellClick: function (i, j, mods) { self._onCellClick(i, j, mods); },
            onColResize: function (j, px) {                  // staged commit lands here
                var c = self._maps.columnAt(j);
                if (c !== undefined && c !== -1 && c !== null) self.setColumnWidth(c, px);
            },
            onColReorder: function (fromJ, toJ) {            // drag-reorder (ext1 row 17)
                var c = self._maps.columnAt(fromJ);
                if (c === undefined || c === -1 || c === null) return;
                if (self._edit && self._edit.defer(function () { self._vs.reorderVisible(c, toJ); })) return;
                self._vs.reorderVisible(c, toJ);             // D7: defers while editing
            }
        });
        this._cells  = new GridCells({ branch: opts.branch });
        // ext2 — the virtual layer as state: order / hidden / widths / sort /
        // filters live in GridViewState; the facade only routes commands.
        this._vs = new GridViewState({ maps: this._maps, adapter: this._adapter, host: this });
        this._selection = new GridSelection({
            maps: this._maps,
            onPaint: function (resolved) { self._layout.paintSelection(resolved); },
            onCursorMoved: opts.onCursorMoved || null,
            onSelectionChanged: opts.onSelectionChanged || null
        });
        this._cbCopy = opts.onCopy || null;
        this._keyboard = new GridKeyboard({
            selection: this._selection,
            onCopy: this._cbCopy ? function () { self._cbCopy(self.copySelection()); } : null,
            // ext2 keyboard path: Alt+arrows resize the CURSOR's column.
            onColumnResize: function (dir) {
                var cur = self._selection.cursorId();
                if (!cur) return;
                self.setColumnWidth(cur.column, self._vs.widthOr(cur.column, 120) + dir * 10);
            }
        });
        this._bulk = new GridBulkOps({ cells: this._cells, maps: this._maps,
            selection: this._selection, adapter: this._adapter, host: this });
        this._session = new GridBulkEditSession({
            cells: this._cells, adapter: this._adapter, bulk: this._bulk,
            // ids → view positions at the seam; the layout paints the error
            onPaintInvalid: function (ids) {
                self._layout.paintInvalid(ids ? ids.map(function (id) {
                    return self._maps.locate(id.pk, id.column); }).filter(Boolean) : null);
            },
            onRejected: opts.onBulkEditRejected || null,
            onCommitted: opts.onBulkEditCommitted || null,
            onSettled: function () { self._edit.onSessionSettled(); }
        });
        this._edit = new GridEditController({
            cells: this._cells, selection: this._selection, adapter: this._adapter,
            keyboard: this._keyboard, bulk: this._bulk, session: this._session,
            editable: opts.editable !== false,
            onEditStarted: opts.onEditStarted || null,
            onEditCommitted: opts.onEditCommitted || null,
            onReleaseRequested: opts.onReleaseRequested || null,
            // Ending an edit removes the focused editor input — native focus
            // would silently fall to body (the RFC 0049 finding). Re-arm here.
            onEditEnded: function () { self.focus(); }
        });
        // ONE keydown dispatch point: the edit controller owns the keyboard
        // while editing; idle keys flow through it to the shallow keyboard.
        this._keydown = function (e) { self._edit.routeKey(e); };
        this._layout.el().addEventListener("keydown", this._keydown);


        // rAF batch for the direct update path (last write per cell wins).
        this._pendingUpdates = new Map(); this._flushScheduled = false;

        // The domain's push channel — identity-addressed, straight to the cell.
        this._onCellChanged = function (pk, col, newValue) { self.updateCell(pk, col, newValue); };
        if (typeof this._adapter.subscribe === "function") this._adapter.subscribe(this._onCellChanged);

        this._refresh("base");
    }

    // ── the composition step: structure, then re-place content ──────────────

    _refresh(kind) {
        var maps = this._maps;
        var headers = [];
        for (var j0 = 0; j0 < maps.cols(); j0++) headers.push(maps.columnAt(j0));
        this._layout.render({ headers: headers, rows: maps.rows() });
        if (this._vs) this._layout.setColWidths(this._vs.widthsPositional());   // widths ride identity

        // Re-place every visible cell into its (possibly new) slot. ensure() is
        // idempotent — existing cells keep their element + state; only cells
        // never seen before are minted. Filtered-out cells simply are not
        // placed (their elements stay detached, alive, in the cells branch).
        for (var i = 0; i < maps.rows(); i++) {
            for (var j = 0; j < maps.cols(); j++) {
                var id = maps.resolve(i, j);
                this._cells.ensure(id.pk, id.column, this._factory,
                                   this._adapter.get(id.pk, id.column));
                this._cells.place(id.pk, id.column, this._layout.slotAt(i, j));
            }
        }
        // Filter DETACHES, never destroys: whatever the view no longer shows
        // leaves the tree with element, instance, and state intact.
        this._cells.detachInvisible(function (pk, col) { return maps.locate(pk, col) !== null; });
        // Selection recomputes over the fresh slots (D5: remaps reset ranges).
        if (this._selection) this._selection.onViewChanged(kind);
        if (this._cbViewChanged) {
            try { this._cbViewChanged(kind); }
            catch (e) { console.error("[RelationGrid] onViewChanged threw:", e); }
        }
    }

    // ── view commands: thin routes into GridViewState (held intent there) ───

    /** Sort by a column ('asc' | 'desc'); sortBy(null) restores base order. */
    sortBy(column, direction) {
        if (this._edit && this._edit.defer(() => this.sortBy(column, direction))) return this;   // D7
        this._vs.sortBy(column, direction); return this;
    }

    /** EPHEMERAL raw filter — works, never saved, never exported (ext2). */
    filterRows(predicate) {
        if (typeof predicate !== "function") throw new Error("[RelationGrid] filterRows needs a function");
        if (this._edit && this._edit.defer(() => this.filterRows(predicate))) return this;       // D7
        this._vs.setRawFilter(predicate); return this;
    }

    /** DECLARATIVE per-column filter: {op, operand} criteria — these persist. */
    setColumnFilter(column, op, operand) {
        if (this._edit && this._edit.defer(() => this.setColumnFilter(column, op, operand))) return this;
        this._vs.setCriterion(column, op, operand); return this;
    }

    clearColumnFilter(column) {
        if (this._edit && this._edit.defer(() => this.clearColumnFilter(column))) return this;
        this._vs.clearCriterion(column); return this;
    }

    clearFilter() {
        if (this._edit && this._edit.defer(() => this.clearFilter())) return this;               // D7
        this._vs.clearAllFilters(); return this;
    }

    hideColumn(column) {
        if (this._edit && this._edit.defer(() => this.hideColumn(column))) return this;          // D7
        this._vs.hide(column); return this;
    }

    /** Un-hide: the column returns at its place in the HELD order. */
    showColumn(column) {
        if (this._edit && this._edit.defer(() => this.showColumn(column))) return this;          // D7
        this._vs.show(column); return this;
    }

    /** Move a column to toIndex within the full (hidden-inclusive) order. */
    reorderColumn(column, toIndex) {
        if (this._edit && this._edit.defer(() => this.reorderColumn(column, toIndex))) return this;
        this._vs.reorder(column, toIndex); return this;
    }

    // ── ext2: sizing + the ViewState round-trip ─────────────────────────────

    /** Width keys on IDENTITY, so it survives hide/show/reorder. Never
     *  deferred: D7 — a resize must not disturb an active edit, and it
     *  cannot, because it touches only the layout's colgroup. */
    setColumnWidth(column, width) {
        this._vs.setWidth(column, width);
        this._layout.setColWidths(this._vs.widthsPositional());
        if (this._cbViewChanged) this._cbViewChanged("widths");
        return this;
    }

    /** Everything the user arranged, as one JSON-able object (raw filters
     *  excluded — ephemeral by contract). */
    viewState() { return this._vs.snapshot(); }

    /** Drift-tolerant restore: intent in, views re-derived against current
     *  data, one refresh out. */
    applyViewState(vs) {
        if (this._edit && this._edit.defer(() => this.applyViewState(vs))) return this;          // D7
        this._vs.apply(vs);
        this._refresh("base");
        return this;
    }

    // ── the direct update path (domain → cell; no layout, no lookup) ────────

    /**
     * Batched per animation frame, LAST WRITE PER CELL WINS — a hot feed (the
     * popularity tick) collapses to one cell.update() per frame. Without a
     * requestAnimationFrame (headless), the flush runs synchronously.
     */
    updateCell(pk, col, newValue) {
        if (!this._cells.get(pk, col)) return false;
        this._pendingUpdates.set(pk + " " + col, { pk: pk, col: col, value: newValue });
        this._scheduleFlush(); return true;
    }

    _scheduleFlush() {
        if (this._flushScheduled) return;
        this._flushScheduled = true;
        var self = this;
        var raf = (typeof requestAnimationFrame === "function") ? requestAnimationFrame
                : function (fn) { fn(); };
        raf(function () { self._flushScheduled = false; self._flushUpdates(); });
    }

    _flushUpdates() {
        var self = this, pending = this._pendingUpdates;
        this._pendingUpdates = new Map();
        pending.forEach(function (u) { self._cells.update(u.pk, u.col, u.value); });
        return this;
    }

    /**
     * Drain the batch NOW. A hidden page gets no animation frames, so pending
     * updates can sit until the next reveal — harmless for painting (the map
     * is bounded, one entry per cell, last write wins), but any read-path
     * that goes through cell state (copySelection, getValue sweeps) MUST call
     * this first or it reads stale values.
     */
    flushNow() { return this._flushUpdates(); }

    // ── selection (identity-space; Phase 4) ─────────────────────────────────

    /** Click routing: position → identity at the seam, then intent. */
    _onCellClick(i, j, mods) {
        var id = this._maps.resolve(i, j);
        if (!id || !this._selection) return;
        if (this._session.isActive()) this._session.settle();   // valid commits, invalid cancels
        if (this._edit.isEditing()) {
            var ed = this._edit.editingAt();
            if (ed && ed.pk === id.pk && ed.col === id.column) return;   // the editor's own cell
            this._edit.commit();                                         // clicking away commits
        }
        if (mods && mods.ctrl)       this._selection.addRange(id.pk, id.column);
        else if (mods && mods.shift) this._selection.extendTo(id.pk, id.column);
        else                         this._selection.setCursor(id.pk, id.column);
    }

    /** Programmatic shallow cursor (the contract's selectCell). */
    selectCell(pk, column) {
        this._selection.setCursor(pk, column);
        return this;
    }

    /** Deep mode: begin editing at the cursor (editable grids only). */
    beginEditAtCursor() { return this._edit.beginEditAtCursor(); }
    isEditing() { return this._edit.isEditing(); }

    /** Citizenship: the default activation target (RFC 0049 hands focus here). */
    focus() { var el = this._layout.el(); if (el.focus) el.focus(); return this; }

    /** Bulk ops — the active rect as raw-value TSV; rows retired by PK set. */
    copySelection() { return this._bulk.copyTsv(); }
    deleteSelectedRows() { return this._bulk.deleteSelectedRows(); }
    viewMaps() { return this._maps; }

    /** The cursor as { pk, column } JSON, or "null". */
    cursor() { return JSON.stringify(this._selection.cursorId()); }

    /** The identity-anchored range list as JSON (D5: reset on remap). */
    selection() { return JSON.stringify(this._selection.rangeList()); }

    /** The row left the Relation: base out, view out, cells DIE (the only death). */
    removeRow(pk) {
        this._cells.disposeRow(pk);
        this._maps.removePk(pk);
        return this;
    }

    /**
     * A row entered the Relation — ATOMICALLY obeying any held filter/sort:
     * the interim state where the base append reaches the view unfiltered is
     * squelched, so a row the filter rejects is never rendered (nor minted).
     */
    addRow(pk) {
        this._squelch = true;
        try { this._maps.addPk(pk); } finally { this._squelch = false; }
        if (this._vs.hasRowIntent()) this._vs.applyRowView();
        else this._refresh("base");
        return this;
    }

    destroy() {
        if (typeof this._adapter.unsubscribe === "function") {
            try { this._adapter.unsubscribe(this._onCellChanged); } catch (e) {}
        }
        if (this._session.isActive()) this._session.cancel();
        if (this._edit.isEditing()) this._edit.cancel();
        this._layout.el().removeEventListener("keydown", this._keydown);
        this._cells.destroy();
        this._layout.destroy();
    }
}
