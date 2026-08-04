package hue.captains.singapura.js.homing.studio.base.composed;

import java.util.Objects;

/**
 * One entry in a relation's sparse articulation side-car ({@link
 * RelationArticulations}): an {@link Articulation} bound to a cell coordinate.
 *
 * <p>Coordinates index the relation the same way it is authored:</p>
 * <ul>
 *   <li>{@code row} — 0-based index into the relation's body {@code rows}; the
 *       sentinel {@link #HEADER} ({@code -1}) addresses the <i>header</i> cell of
 *       the column instead;</li>
 *   <li>{@code col} — 0-based column index.</li>
 * </ul>
 *
 * <p>Only cells that need enhancement appear — the side-car is sparse.</p>
 *
 * @param row          0-based body-row index, or {@link #HEADER} for the header
 * @param col          0-based column index
 * @param articulation the marks to apply to that cell
 */
public record ArticulatedCell(int row, int col, Articulation articulation) {

    /** {@code row} sentinel addressing the header cell of a column. */
    public static final int HEADER = -1;

    public ArticulatedCell {
        if (row < HEADER) {
            throw new IllegalArgumentException("ArticulatedCell.row must be >= -1 (HEADER); got " + row);
        }
        if (col < 0) {
            throw new IllegalArgumentException("ArticulatedCell.col must be >= 0; got " + col);
        }
        Objects.requireNonNull(articulation, "ArticulatedCell.articulation");
    }

    /** Articulate the body cell at {@code (row, col)}. */
    public static ArticulatedCell at(int row, int col, Articulation articulation) {
        return new ArticulatedCell(row, col, articulation);
    }

    /** Articulate the header cell of column {@code col}. */
    public static ArticulatedCell header(int col, Articulation articulation) {
        return new ArticulatedCell(HEADER, col, articulation);
    }
}
