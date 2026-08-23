package hue.captains.singapura.js.homing.grid.contract;

/**
 * RFC 0050 — a Relation column's stable name. The {@code j → columnName} map
 * targets these; hide/reorder are reassignments of which names show where.
 * Typed for the same reason as {@link GridKey}: named identity, never a raw
 * string in a signature.
 */
public record ColumnName(String value) {
    public ColumnName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ColumnName must be non-blank");
        }
    }
}
