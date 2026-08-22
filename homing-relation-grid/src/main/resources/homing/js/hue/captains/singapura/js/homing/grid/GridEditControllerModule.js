// =============================================================================
// GridEditControllerModule — RFC 0050's deep mode: the editing lifecycle and
// the single keyboard dispatch point. Deep ⇒ cell, and AT MOST ONE cell is
// ever editing (the RFC 0049 invariant's deep half); beginning an edit while
// another is active commits the first.
//
// Keyboard ownership (the RFC 0049 layering, one level down):
//   · while EDITING the controller owns the keyboard — Enter commits, Escape
//     cancels, everything else stays with the editor input (the shallow
//     keyboard never sees it);
//   · while IDLE keys go to the shallow keyboard first; unconsumed Enter/F2
//     begins an edit (editable grids), Delete CLEARS the selected cells'
//     contents (Excel semantics, via GridBulkOps), idle Escape requests
//     release to the host (RFC 0049 citizenship — the pane downgrades, not
//     this widget). Ending an edit fires onEditEnded so the host can restore
//     native focus to the grid (the editor input WAS the focus — removing it
//     silently drops focus to body otherwise);
//   · the Excel BULK EDIT: when the selection at beginEdit is multi-cell and
//     homogeneous (every cell the same editor type), the committed value
//     fans out to every selected cell; heterogeneous → single-cell edit;
//   · with editing DISABLED, unconsumed action keys dispatch onAction(key,
//     pk, column) and return to shallow — the Minesweeper variant.
//
// D7: view ops arriving during an edit DEFER (the re-sort rule) — they queue
// through defer() and drain on commit/cancel, so the editor is never torn
// out from under the user. No DOM in this module — cells render their own
// editors; the adapter is the sole write target.
// =============================================================================

class GridEditController {

    constructor(opts) {
        opts = opts || {};
        if (!opts.cells || !opts.selection || !opts.adapter)
            throw new Error("[GridEditController] cells, selection, adapter are required");
        this._cells     = opts.cells;
        this._selection = opts.selection;
        this._adapter   = opts.adapter;
        this._keyboard  = opts.keyboard || null;
        this._editable  = opts.editable !== false;
        this._bulk      = opts.bulk || null;
        this._cbAction    = opts.onAction || null;
        this._cbStarted   = opts.onEditStarted || null;
        this._cbCommitted = opts.onEditCommitted || null;
        this._cbRelease   = opts.onReleaseRequested || null;
        this._cbEnded     = opts.onEditEnded || null;   // fired after commit/cancel
        this._editing  = null;    // { pk, col } | null — at most ONE (deep ⇒ cell)
        this._bulkTargets = null; // homogeneous selection captured at beginEdit
        this._deferred = [];      // view ops queued during an edit (D7)
    }

    isEditing() { return this._editing !== null; }
    editingAt() { return this._editing ? { pk: this._editing.pk, col: this._editing.col } : null; }

    beginEditAtCursor() {
        var cur = this._selection.cursorId();
        return cur ? this.beginEdit(cur.pk, cur.column) : false;
    }

    beginEdit(pk, col) {
        if (!this._editable) return false;
        if (this._editing) this.commit();          // at most one editing cell
        var entry = this._cells.get(pk, col);
        if (!entry || typeof entry.cell.beginEdit !== "function") return false;
        entry.cell.beginEdit(this._adapter.get(pk, col));   // adapter = source of truth
        this._editing = { pk: pk, col: col };
        this._bulkTargets = this._homogeneousSelection(entry);
        if (this._cbStarted) this._cbStarted(pk, col);
        return true;
    }

    /**
     * The Excel bulk-edit precondition: the multi-cell selection captured at
     * beginEdit, but ONLY when every selected cell shares the edited cell's
     * editor type (same constructor). Heterogeneous → null → single-cell edit.
     */
    _homogeneousSelection(baseEntry) {
        if (!this._bulk) return null;
        var ids = this._bulk.selectedCellIds();
        if (ids.length < 2) return null;
        var proto = baseEntry.cell.constructor, cells = this._cells;
        for (var k = 0; k < ids.length; k++) {
            var e = cells.get(ids[k].pk, ids[k].column);
            if (!e || e.cell.constructor !== proto) return null;
        }
        return ids;
    }

    /**
     * Commit: the cell yields its edited value; the ADAPTER gets the write —
     * fanned out to every captured homogeneous target (the Excel bulk edit).
     */
    commit() {
        var ed = this._editing;
        if (!ed) return this;
        var entry = this._cells.get(ed.pk, ed.col);
        var targets = this._bulkTargets;
        this._editing = null; this._bulkTargets = null;
        var v = entry ? entry.cell.commitEdit() : undefined;
        if (entry && typeof this._adapter.update === "function") {
            var self = this;
            this._adapter.update(ed.pk, ed.col, v);
            if (targets) targets.forEach(function (t) {
                if (t.pk !== ed.pk || t.column !== ed.col) self._adapter.update(t.pk, t.column, v);
            });
        }
        if (this._cbCommitted) this._cbCommitted(ed.pk, ed.col, v);
        this._drain();
        if (this._cbEnded) this._cbEnded();     // the host restores grid focus
        return this;
    }

    cancel() {
        var ed = this._editing;
        if (!ed) return this;
        var entry = this._cells.get(ed.pk, ed.col);
        this._editing = null; this._bulkTargets = null;
        if (entry) entry.cell.cancelEdit();
        this._drain();
        if (this._cbEnded) this._cbEnded();
        return this;
    }

    /** D7 guard: true = the op was queued for after the edit; run nothing now. */
    defer(thunk) {
        if (!this._editing) return false;
        this._deferred.push(thunk);
        return true;
    }

    _drain() {
        var q = this._deferred;
        this._deferred = [];
        q.forEach(function (t) { t(); });
    }

    /** The single keydown dispatch point. Returns true when the key is owned. */
    routeKey(e) {
        if (this._editing) {
            if (e.key === "Enter")  { if (e.preventDefault) e.preventDefault(); this.commit(); return true; }
            if (e.key === "Escape") { if (e.preventDefault) e.preventDefault(); this.cancel(); return true; }
            return true;    // owned: shallow keys are blocked; the editor input keeps the event
        }
        if (this._keyboard && this._keyboard.handleKey(e)) return true;
        if ((e.key === "Enter" || e.key === "F2") && this._editable) {
            if (e.preventDefault) e.preventDefault();
            this.beginEditAtCursor();
            return true;
        }
        if (e.key === "Delete" && this._editable && this._bulk) {
            if (e.preventDefault) e.preventDefault();
            this._bulk.clearSelected();          // Excel Delete: clear contents
            return true;
        }
        if (e.key === "Escape") {
            if (this._cbRelease) this._cbRelease();     // idle Escape releases the pane
            return true;
        }
        if (!this._editable && this._cbAction && !e.ctrlKey && !e.metaKey
                && (e.key === "Enter" || e.key === " " || e.key.length === 1)) {
            var cur = this._selection.cursorId();
            if (cur) {
                if (e.preventDefault) e.preventDefault();
                this._cbAction(e.key, cur.pk, cur.column);   // fire and RETURN TO SHALLOW
                return true;
            }
        }
        return false;
    }
}
