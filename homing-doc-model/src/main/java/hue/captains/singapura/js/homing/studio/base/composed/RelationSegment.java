package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.composed.text.Line;

import hue.captains.singapura.js.homing.studio.base.composed.text.Lines;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * RFC 0019 — typed relation (tabular) segment for {@link ComposedDoc}.
 *
 * <p>Represents a homogeneous table: a header row plus zero or more data rows,
 * derived at construction time from a typed {@code List<T>} via a
 * {@code List<Column<T>>} schema. Extractors run once at build time; the
 * segment stores only the resulting {@code headers} and {@code rows} strings —
 * no generic type parameter leaks into the record itself, so it integrates
 * cleanly with the sealed {@link Segment} hierarchy.</p>
 *
 * <p>Cell values may contain inline markdown (bold, italic, inline code,
 * links); the renderer applies {@code marked.parseInline()} to each header
 * and cell, so {@code **bold**}, `` `code` ``, and {@code [label](#ref:name)}
 * cross-references all render correctly without any extra wiring.</p>
 *
 * <p>Rendering is pure JS — {@code RelationSegmentRenderer.js} builds a
 * {@code <table>} element using the existing {@code st_table / st_th / st_td}
 * CSS tokens; no server-side HTML generation.</p>
 *
 * <p>This is the typed alternative to using a raw {@link MarkdownSegment} as
 * a GFM table escape hatch — prefer {@code RelationSegment} whenever the
 * table data is homogeneous and the schema can be expressed as a
 * {@code List<Column<T>>}.</p>
 *
 * <p><b>Articulations (side-car).</b> Beyond the plain string cells, a relation
 * may carry a sparse {@link RelationArticulations} side-car — a set of
 * {@link ArticulatedCell}s giving per-cell visual enhancement (status badge,
 * alignment, emphasis) to only the cells that need it. The main {@code rows}
 * are untouched; a plain relation has {@link RelationArticulations#NONE} and
 * serializes exactly as before. Authored via {@code .articulatedRelation(...)}.</p>
 *
 * @param headers       ordered list of column header labels
 * @param rows          ordered list of row value lists (same length as headers per row)
 * @param caption       optional table caption; contributes to TOC as a level-2
 *                      entry when present
 * @param articulations sparse per-cell visual-enhancement side-car;
 *                      {@link RelationArticulations#NONE} for a plain relation
 *
 * @since RFC 0019 (RelationSegment — typed table addition)
 */
public record RelationSegment(
        List<String>          headers,
        List<List<String>>    rows,
        Optional<Line.Plain>  caption,
        RelationArticulations articulations
) implements Listable {

    /**
     * Typed column definition. The header label and the value extractor are
     * declared together; the extractor is applied for each row item when
     * the relation is made from its columns.
     *
     * @param <T>       the row data type
     * @param header    column header label (may contain inline markdown)
     * @param extractor function from a row item to its cell string (may contain inline markdown)
     */
    public record Column<T>(String header, Function<T, String> extractor) {
        public Column {
            Objects.requireNonNull(header,    "RelationSegment.Column.header");
            Objects.requireNonNull(extractor, "RelationSegment.Column.extractor");
        }

        String extract(T item) {
            return extractor.apply(item);
        }
    }

    public RelationSegment {
        Objects.requireNonNull(headers, "RelationSegment.headers");
        Objects.requireNonNull(rows,    "RelationSegment.rows");
        Objects.requireNonNull(caption, "RelationSegment.caption (use Optional.empty)");
        articulations = (articulations == null) ? RelationArticulations.NONE : articulations;
        headers = List.copyOf(headers);
        rows    = rows.stream().map(List::copyOf).toList();
    }

    /** Plain relation — no articulation side-car (backward-compatible 3-arg form). */
    public RelationSegment(List<String> headers, List<List<String>> rows, Optional<Line.Plain> caption) {
        this(headers, rows, caption, RelationArticulations.NONE);
    }

    /** Convenience — no caption. */
    public <T> RelationSegment(List<Column<T>> columns, List<T> data) {
        this(RelationSegments.INSTANCE.headersOf(columns), RelationSegments.INSTANCE.rowsOf(columns, data), Optional.empty());
    }

    /** Convenience — with caption (contributes to TOC; blank becomes no caption). */
    public <T> RelationSegment(List<Column<T>> columns, List<T> data, String caption) {
        this(RelationSegments.INSTANCE.headersOf(columns), RelationSegments.INSTANCE.rowsOf(columns, data), Lines.INSTANCE.optionalPlain(caption));
    }
}
