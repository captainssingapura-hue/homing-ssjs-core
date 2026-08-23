package hue.captains.singapura.js.homing.grid.contract;

import java.util.List;

/**
 * RFC 0050 — the domain-facing contract: what a Relation supplies to the grid
 * and how commands/updates flow. The domain never sees {@code (i, j)} —
 * everything here is identity-space ({@link GridKey}, {@link ColumnName}).
 *
 * <p>Update direction: the domain pushes changes to the <b>ActualCell
 * directly</b> by {@code (PK, columnName)} through the change subscription —
 * no layout involvement, no lookup (rAF-batched when dense). Bulk operations
 * come back as <b>identity sets</b>: delete is "these PKs", never row
 * indices.</p>
 *
 * <p>Scale (RFC 0050 D6): v1 adapters are in-memory small/medium; a virtual /
 * streaming Relation later implements this same contract — the layout never
 * changes, and detach-not-destroy is already the windowing mechanism.</p>
 */
public interface RelationAdapterContract {

    // ─── The Relation's axes + values ────────────────────────────────────
    List<GridKey>    pks();                                // stable, unique row keys
    List<ColumnName> columns();                            // stable column names
    Object           get(GridKey pk, ColumnName column);   // one cell's domain value

    // ─── Change flow (domain → cells, identity-addressed) ────────────────
    void subscribe(Object onCellChanged);                  // (pk, column, newValue) push
    void unsubscribe(Object onCellChanged);

    // ─── Edits + bulk ops (grid → domain, identity-addressed) ────────────
    void update(GridKey pk, ColumnName column, Object newValue);   // an edit commit lands here
    void deleteRows(List<GridKey> pks);                            // bulk delete — an identity SET

    /** The method names the JS adapter object must expose (conformance-gated). */
    String[] METHOD_NAMES = { "pks", "columns", "get", "subscribe", "unsubscribe",
                              "update", "deleteRows" };
}
