package hue.captains.singapura.js.homing.studio.base.table;

import hue.captains.singapura.tao.ontology.Scalar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RFC 0020 — typed shape for {@link TableDoc} content. Slim by design:
 * headers, rows, cells with optional colspan / rowspan / cell badge /
 * cell alignment. No formulas, no interactivity, no editing
 * (RFC 0020 §2.2 — "tables, not spreadsheets").
 *
 * <p>Data only: the table viewers' JSON envelope is made from it by {@code TableJson},
 * in the studio. Constructible from CSV via
 * {@link #fromCsv(String)} for the lazy authoring case.</p>
 *
 * @param headers the header row's cells; may be empty for header-less tables
 * @param rows    the data rows, each a list of cells
 *
 * @since RFC 0020
 */
public record TableData(List<Cell> headers, List<List<Cell>> rows) implements Scalar {

    public TableData {
        Objects.requireNonNull(headers, "TableData.headers");
        Objects.requireNonNull(rows,    "TableData.rows");
        // Defensive copies — records freeze their components but the
        // collections themselves are still mutable views without copyOf().
        headers = List.copyOf(headers);
        var copiedRows = new ArrayList<List<Cell>>(rows.size());
        for (List<Cell> r : rows) {
            Objects.requireNonNull(r, "TableData.rows entry");
            copiedRows.add(List.copyOf(r));
        }
        rows = List.copyOf(copiedRows);
    }

    /** Cell badge tokens — match the RFC 0020 §2.2 status colour set. */
    public enum Badge { SUCCESS, WARNING, ERROR }

    /** Horizontal cell alignment. */
    public enum Align { LEFT, CENTER, RIGHT }

    /**
     * One cell of a {@link TableData}. Slim feature surface only:
     * {@code text} (required), {@code colspan} / {@code rowspan} (default 1),
     * optional {@code badge}, optional {@code align}.
     *
     * @param text     display text; may be empty but not null
     * @param colspan  column span (≥ 1)
     * @param rowspan  row span (≥ 1)
     * @param badge    optional status colour token; null when unset
     * @param align    optional horizontal alignment; null falls through to defaults
     */
    public record Cell(String text, int colspan, int rowspan, Badge badge, Align align)
            implements Scalar {

        public Cell {
            Objects.requireNonNull(text, "Cell.text (use \"\" for empty)");
            if (colspan < 1) {
                throw new IllegalArgumentException("Cell.colspan must be ≥ 1; got " + colspan);
            }
            if (rowspan < 1) {
                throw new IllegalArgumentException("Cell.rowspan must be ≥ 1; got " + rowspan);
            }
        }

        /** Convenience — plain cell with default colspan/rowspan and no styling. */
        public Cell(String text) {
            this(text, 1, 1, null, null);
        }

        /** Convenience — cell with the given badge. */
        public Cell(String text, Badge badge) {
            this(text, 1, 1, badge, null);
        }
    }

    // -----------------------------------------------------------------------
    // CSV ingestion — simple comma-split, no quoting beyond escapes-the-delimiter.
    // -----------------------------------------------------------------------
}
