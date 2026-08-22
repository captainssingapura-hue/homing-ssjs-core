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
// Phase 2 surface: static render + refresh + the direct update path. View
// commands (sort/filter/hide/reorder), selection, editing, and bulk ops land
// in Phases 3–6 per the journey.
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
            return (typeof value === "number") ? new NumberCell() : new TextCell();
        };

        var self = this;
        this._maps = new GridViewMaps({
            pks:     this._adapter.pks(),
            columns: this._adapter.columns(),
            onViewChanged: function (kind) { self._refresh(kind); }
        });
        this._layout = new GridLayout({ container: opts.container });
        this._cells  = new GridCells({ branch: opts.branch });

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
        if (this._cbViewChanged) {
            try { this._cbViewChanged(kind); }
            catch (e) { console.error("[RelationGrid] onViewChanged threw:", e); }
        }
    }

    // ── the direct update path (domain → cell; no layout, no lookup) ────────

    updateCell(pk, col, newValue) {
        return this._cells.update(pk, col, newValue);
    }

    // ── access for the phases above (commands land in Phase 3+) ─────────────

    /** The identity/position seam — Phase 3's view commands drive it. */
    viewMaps() { return this._maps; }

    /** The row left the Relation: base out, view out, cells DIE (the only death). */
    removeRow(pk) {
        this._cells.disposeRow(pk);
        this._maps.removePk(pk);
        return this;
    }

    /** A row entered the Relation (appended to base + view; cells mint on place). */
    addRow(pk) {
        this._maps.addPk(pk);
        return this;
    }

    destroy() {
        if (typeof this._adapter.unsubscribe === "function") {
            try { this._adapter.unsubscribe(this._onCellChanged); } catch (e) {}
        }
        this._cells.destroy();
        this._layout.destroy();
    }
}
