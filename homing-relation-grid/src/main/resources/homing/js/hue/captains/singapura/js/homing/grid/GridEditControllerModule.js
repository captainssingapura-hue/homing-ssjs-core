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
//   · BULK value replacement is the VIRTUAL SESSION's job (GridBulkEditSession,
//     routed first): an EffectiveType's opening char — or Enter over a
//     multi-cell homogeneous selection — opens it; while active it owns the
//     keyboard. Instantaneous types (mine tiles) commit on the opening
//     keystroke, which is how action-style grids work with the SAME
//     primitives — no onAction side-channel, the adapter is the move seam.
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
        this._session   = opts.session || null;         // the virtual bulk-edit session
        this._cbStarted   = opts.onEditStarted || null;
        this._cbCommitted = opts.onEditCommitted || null;
        this._cbRelease   = opts.onReleaseRequested || null;
        this._cbEnded     = opts.onEditEnded || null;   // fired after commit/cancel
        this._editing  = null;    // { pk, col } | null — at most ONE (deep ⇒ cell)
        this._deferred = [];      // view ops queued during an edit (D7)
    }

    /** An editing SESSION is live: a classic cell edit OR a virtual bulk one. */
    isEditing() {
        return this._editing !== null || (this._session !== null && this._session.isActive());
    }
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
        if (this._cbStarted) this._cbStarted(pk, col);
        return true;
    }

    /** Commit: the cell yields its edited value; the ADAPTER gets the write.
     *  (Bulk value replacement is the virtual session's job, not this one's.) */
    commit() {
        var ed = this._editing;
        if (!ed) return this;
        var entry = this._cells.get(ed.pk, ed.col);
        this._editing = null;
        var v = entry ? entry.cell.commitEdit() : undefined;
        if (entry && typeof this._adapter.update === "function") this._adapter.update(ed.pk, ed.col, v);
        if (this._cbCommitted) this._cbCommitted(ed.pk, ed.col, v);
        this._drain();
        if (this._cbEnded) this._cbEnded();     // the host restores grid focus
        return this;
    }

    cancel() {
        var ed = this._editing;
        if (!ed) return this;
        var entry = this._cells.get(ed.pk, ed.col);
        this._editing = null;
        if (entry) entry.cell.cancelEdit();
        this._drain();
        if (this._cbEnded) this._cbEnded();
        return this;
    }

    /** D7 guard: true = the op was queued for after the edit; run nothing now. */
    defer(thunk) {
        if (!this.isEditing()) return false;
        this._deferred.push(thunk);
        return true;
    }

    /** The session settles (commit/cancel): drain D7 + re-arm the host. */
    onSessionSettled() {
        this._drain();
        if (this._cbEnded) this._cbEnded();
        return this;
    }

    _drain() {
        var q = this._deferred;
        this._deferred = [];
        q.forEach(function (t) { t(); });
    }

    /** The single keydown dispatch point. Returns true when the key is owned. */
    routeKey(e) {
        if (this._session && this._session.isActive()) return this._session.handleKey(e);
        if (this._editing) {
            if (e.key === "Enter")  { if (e.preventDefault) e.preventDefault(); this.commit(); return true; }
            if (e.key === "Escape") { if (e.preventDefault) e.preventDefault(); this.cancel(); return true; }
            return true;    // owned: shallow keys are blocked; the editor input keeps the event
        }
        if (this._keyboard && this._keyboard.handleKey(e)) return true;
        // The virtual session sees opening keys FIRST: an EffectiveType's
        // opening char, or Enter over a multi-cell homogeneous selection
        // (single-cell Enter falls through to the classic cell editor below).
        // editable:false is a HARD read-only switch — it blocks this too.
        if (this._editable && !e.ctrlKey && !e.metaKey
                && this._session && this._session.tryOpen(e.key)) {
            if (e.preventDefault) e.preventDefault();
            return true;
        }
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
        return false;
    }
}
