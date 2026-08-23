// =============================================================================
// GridBulkEditSessionModule — RFC 0050's VIRTUAL editor: bulk value
// replacement over a homogeneous selection with no input element at all.
// The session is pure state — { type, targets, buffer, caret } — so the
// table never loses native focus. Cells stay SHALLOW throughout; the session
// (not a cell) owns the keyboard while active.
//
//   · ENTRY (tryOpen): the selection's EffectiveTypes must share a NAME;
//     a printable char the type opens() (or Enter on a multi selection)
//     starts the session. A name mismatch on such a key REPORTS AN ERROR
//     (onRejected) instead of silently doing nothing. Cells without a type
//     stay out of the feature silently.
//   · INSTANTANEOUS types (mine tiles): the opening char IS the whole edit —
//     open, commit to every selected cell, close, in one keystroke.
//   · BUFFERED types: printable chars insert at the CARET; arrows move the
//     caret (Left/Right/Home/End — the cells display it, no textbox
//     involved); Backspace/Delete edit around it. Every change parses:
//     valid clears the error paint, invalid fans it (the host paints the
//     selected tds). Enter commits only a VALID buffer (invalid: no-op,
//     error showing); Escape cancels; both restore shallow normality.
//   · PREVIEW: buffer + caret marker fan to every selected cell via
//     cell.preview(text) — raw text while typing; formatting reappears on
//     commit, when real values flow through the adapter echo.
//
// Commits are ONE adapter.update(pk, col, value) per target — the domain
// seam; a game adapter interprets the value as a move (Minesweeper).
// =============================================================================

var _HGR_CARET = "▏";   // the visible caret the cells render

class GridBulkEditSession {

    /** deps: { cells, adapter, bulk, onPaintInvalid(ids|null),
     *          onRejected({reason, names}), onCommitted(ids, value),
     *          onSettled() — fired after commit/cancel (drain + focus). */
    constructor(deps) {
        deps = deps || {};
        if (!deps.cells || !deps.adapter || !deps.bulk)
            throw new Error("[GridBulkEditSession] cells, adapter, bulk are required");
        this._cells = deps.cells;
        this._adapter = deps.adapter;
        this._bulk = deps.bulk;
        this._cbInvalid   = deps.onPaintInvalid || function () {};
        this._cbRejected  = deps.onRejected || null;
        this._cbCommitted = deps.onCommitted || null;
        this._cbSettled   = deps.onSettled || null;
        this._active = null;   // { type, targets, buffer, caret } | null
    }

    isActive() { return this._active !== null; }
    targets()  { return this._active ? this._active.targets : []; }

    /** The homogeneous EffectiveType of the ids, or a rejection descriptor. */
    _typeOf(ids) {
        var names = new Set(), type = null, typed = 0;
        for (var k = 0; k < ids.length; k++) {
            var e = this._cells.get(ids[k].pk, ids[k].column);
            var t = (e && typeof e.cell.effectiveType === "function") ? e.cell.effectiveType() : null;
            if (t) { typed++; names.add(t.name); type = t; }
        }
        if (typed === 0) return { silent: true };                       // feature not in play
        if (typed < ids.length || names.size > 1)
            return { reject: { reason: "mixed-types", names: Array.from(names) } };
        return { type: type };
    }

    /**
     * Idle-key entry. Returns true when the key was consumed (a session
     * opened, an instantaneous commit fired, or a mismatch was reported).
     */
    tryOpen(key) {
        var ids = this._bulk.selectedCellIds();
        if (!ids.length) return false;
        var res = this._typeOf(ids);
        var isEnter = key === "Enter" || key === "F2";
        var printable = typeof key === "string" && key.length === 1;
        if (res.silent) return false;
        if (res.reject) {
            if ((printable || isEnter) && ids.length > 1) {
                if (this._cbRejected) this._cbRejected(res.reject);
                return true;
            }
            return false;
        }
        var type = res.type;
        if (printable && type.opens(key)) {
            if (type.instantaneous) return this._commitValue(ids, type.parse(key).value);
            this._active = { type: type, targets: ids, buffer: key, caret: 1 };
            this._sync();
            return true;
        }
        if (isEnter) {
            if (type.instantaneous && type.opens(key)) return this._commitValue(ids, key);
            if (ids.length > 1) {                     // single-cell Enter = classic editor
                this._active = { type: type, targets: ids, buffer: "", caret: 0 };
                this._sync();
                return true;
            }
        }
        return false;
    }

    /** While active the session owns the keyboard entirely. */
    handleKey(e) {
        var s = this._active;
        if (!s) return false;
        if (e.preventDefault) e.preventDefault();
        var k = e.key;
        if (k === "Escape") return this.cancel();
        if (k === "Enter") {
            var p = s.type.parse(s.buffer);
            if (!p.ok) return true;                   // invalid: no-op, error showing
            return this._commitValue(s.targets, p.value);
        }
        if (k === "ArrowLeft")       s.caret = Math.max(0, s.caret - 1);
        else if (k === "ArrowRight") s.caret = Math.min(s.buffer.length, s.caret + 1);
        else if (k === "Home")       s.caret = 0;
        else if (k === "End")        s.caret = s.buffer.length;
        else if (k === "Backspace" && s.caret > 0) {
            s.buffer = s.buffer.slice(0, s.caret - 1) + s.buffer.slice(s.caret);
            s.caret--;
        }
        else if (k === "Delete") {
            s.buffer = s.buffer.slice(0, s.caret) + s.buffer.slice(s.caret + 1);
        }
        else if (typeof k === "string" && k.length === 1 && !e.ctrlKey && !e.metaKey) {
            s.buffer = s.buffer.slice(0, s.caret) + k + s.buffer.slice(s.caret);
            s.caret++;
        }
        this._sync();
        return true;
    }

    /** Fan buffer + caret to every target; paint or clear the error. */
    _sync() {
        var s = this._active;
        var text = s.buffer.slice(0, s.caret) + _HGR_CARET + s.buffer.slice(s.caret);
        var self = this;
        s.targets.forEach(function (id) {
            var e = self._cells.get(id.pk, id.column);
            if (e && typeof e.cell.preview === "function") e.cell.preview(text);
        });
        this._cbInvalid(s.type.parse(s.buffer).ok ? null : s.targets);
    }

    /** Click-away settles: commit when valid, cancel otherwise. */
    settle() {
        var s = this._active;
        if (!s) return this;
        var p = s.type.parse(s.buffer);
        if (p.ok) this._commitValue(s.targets, p.value); else this.cancel();
        return this;
    }

    _commitValue(ids, value) {
        this._close();
        var self = this;
        if (typeof this._adapter.update === "function")
            ids.forEach(function (id) { self._adapter.update(id.pk, id.column, value); });
        if (this._cbCommitted) this._cbCommitted(ids, value);
        if (this._cbSettled) this._cbSettled();
        return true;
    }

    cancel() {
        this._close();
        if (this._cbSettled) this._cbSettled();
        return true;
    }

    /** End the preview: cells fall back to their own (tick-current) values. */
    _close() {
        var s = this._active;
        this._active = null;
        if (!s) return;
        var self = this;
        s.targets.forEach(function (id) {
            var e = self._cells.get(id.pk, id.column);
            if (e && typeof e.cell.preview === "function") e.cell.preview(null);
        });
        this._cbInvalid(null);
    }
}
