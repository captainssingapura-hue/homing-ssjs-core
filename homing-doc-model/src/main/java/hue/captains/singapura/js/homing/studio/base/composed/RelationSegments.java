package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.composed.text.Line;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** What relations are read from: authoring cells, and columns over data ({@link RelationSegment}). */
public record RelationSegments() implements StatelessFunctionalObject {

    public static final RelationSegments INSTANCE = new RelationSegments();

    /**
     * Build an articulated relation from authoring {@link RelationCell}s — the
     * cells carry their marks <i>in place</i>, and the sparse
     * {@link RelationArticulations} side-car is derived from each cell's position:
     * column index for {@code col}, body-row index for {@code row}, and
     * {@link ArticulatedCell#HEADER} ({@code -1}) for the header row. Callers never
     * track coordinates by hand — position <i>is</i> the coordinate. The rigid DSL
     * {@code .articulatedRelation(...)} is sugar over this factory.
     */
    public RelationSegment articulated(List<RelationCell> headers,
                                              List<List<RelationCell>> rows,
                                              Optional<Line.Plain> caption) {
        Objects.requireNonNull(headers, "RelationSegment.articulated: headers");
        Objects.requireNonNull(rows,    "RelationSegment.articulated: rows");
        var headerText = new ArrayList<String>(headers.size());
        var articulations = new ArrayList<ArticulatedCell>();
        for (int c = 0; c < headers.size(); c++) {
            RelationCell hc = headers.get(c);
            headerText.add(hc.text());
            Articulation a = hc.articulation();
            if (!a.isEmpty()) articulations.add(new ArticulatedCell(c, a));
        }
        var rowText = new ArrayList<List<String>>(rows.size());
        for (int r = 0; r < rows.size(); r++) {
            List<RelationCell> row = rows.get(r);
            var cells = new ArrayList<String>(row.size());
            for (int c = 0; c < row.size(); c++) {
                RelationCell rc = row.get(c);
                cells.add(rc.text());
                Articulation a = rc.articulation();
                if (!a.isEmpty()) articulations.add(new ArticulatedCell(r, c, a));
            }
            rowText.add(cells);
        }
        return new RelationSegment(headerText, rowText, caption,
                new RelationArticulations(articulations));
    }

    /** The header of each column. */
    public <T> List<String> headersOf(List<RelationSegment.Column<T>> columns) {
        Objects.requireNonNull(columns, "RelationSegment: columns");
        return columns.stream().map(RelationSegment.Column::header).toList();
    }

    /** Each item as a row, a cell for each column. */
    public <T> List<List<String>> rowsOf(List<RelationSegment.Column<T>> columns, List<T> data) {
        Objects.requireNonNull(data, "RelationSegment: data");
        return data.stream()
                .map(item -> columns.stream().map(col -> col.extract(item)).toList())
                .toList();
    }
}
