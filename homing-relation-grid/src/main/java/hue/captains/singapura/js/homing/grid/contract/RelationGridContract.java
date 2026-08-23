package hue.captains.singapura.js.homing.grid.contract;

/**
 * RFC 0050 — the public contract of the {@code RelationGrid} facade: what a
 * host (a workspace widget, a demo app) drives and observes. Documentation in
 * code, RFC 0035 style; the conformance test gates names + arities as the
 * facade lands (Phase 2+).
 *
 * <p>Boundaries this contract encodes (the module-map rules): selection is
 * identity-space and recompute-resolved (RFC 0049 one level down — exactly one
 * cursor, ranges all shallow, at most one editing cell, deep ⇒ cell); view
 * commands are remaps handed to {@code GridViewMaps}; bulk operations
 * materialise identity sets. The host never sees {@code (i, j)}.</p>
 */
public interface RelationGridContract {

    // ─── View commands (remaps — Phase 3) ────────────────────────────────
    void sortBy(ColumnName column, String direction);      // 'asc' | 'desc' — an i→PK permutation
    void filterRows(Object predicate);                     // an i→PK subset; cells DETACH, not destroy
    void clearFilter();
    void hideColumn(ColumnName column);
    void showColumn(ColumnName column);
    void reorderColumn(ColumnName column, int toIndex);    // j→columnName remap

    // ─── Selection (identity-space; recompute-resolved — Phase 4) ────────
    String cursor();                                       // { pk, column } JSON, or null pre-boot
    String selection();                                    // the range list, identity-anchored per D5
    void   selectCell(GridKey pk, ColumnName column);      // programmatic shallow cursor

    // ─── Editing (Phase 5) ───────────────────────────────────────────────
    void beginEditAtCursor();
    boolean isEditing();

    // ─── Bulk ops (Phase 6 — identity sets out) ──────────────────────────
    String copySelection();                                // TSV via getValueToCopy
    void   deleteSelectedRows();                           // → adapter.deleteRows(pks)

    // ─── Lifecycle ───────────────────────────────────────────────────────
    void destroy();

    /** Name of the JS facade class (conformance lookup). */
    String JS_CLASS_NAME = "RelationGrid";

    /**
     * The typed event callback options the facade's constructor accepts —
     * single-consumer, host fans out (the RFC 0032 / workspace-chrome pattern).
     */
    String[] CALLBACK_OPTION_NAMES = {
            "onCursorMoved",         // (pk, column)
            "onSelectionChanged",    // (rangeList)
            "onEditStarted",         // (pk, column)
            "onEditCommitted",       // (pk, column, newValue)
            "onBulkEditRejected",    // ({reason: 'mixed-types', names}) — the table reports the error
            "onBulkEditCommitted",   // (targetIds, value) — one virtual-session commit
            "onCopy",                // (tsv) — Ctrl+C payload
            "onReleaseRequested",    // idle Escape — RFC 0049 citizenship
            "onViewChanged"          // ('rows' | 'columns' | 'base')
    };
}
