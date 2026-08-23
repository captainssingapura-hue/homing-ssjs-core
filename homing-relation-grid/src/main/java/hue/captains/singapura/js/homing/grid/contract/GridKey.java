package hue.captains.singapura.js.homing.grid.contract;

/**
 * RFC 0050 D3 — the <b>typed primary key</b> of a Relation row. A named type
 * with well-defined equality (record semantics), stable for the Relation's
 * life; never a raw string floating through signatures. Identity-space
 * selection and the direct {@code (PK, columnName)} update path are
 * load-bearing on this stability.
 *
 * <p>{@link #value()} is the stable string form — the form that crosses into
 * JS, keys the view maps, and addresses cells in the cells branch.</p>
 */
public record GridKey(String value) {
    public GridKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("GridKey must be non-blank");
        }
    }
}
