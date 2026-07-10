package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.composed.text.Line;

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
 * @param headers  ordered list of column header labels
 * @param rows     ordered list of row value lists (same length as headers per row)
 * @param caption  optional table caption; contributes to TOC as a level-2
 *                 entry when present
 *
 * @since RFC 0019 (RelationSegment — typed table addition)
 */
public record RelationSegment(
        List<String>        headers,
        List<List<String>>  rows,
        Optional<Line.Plain> caption
) implements Listable {

    /**
     * Typed column definition. The header label and the value extractor are
     * declared together; the extractor is applied for each row item at
     * {@link #of} / {@link #of(List, List, String)} call time.
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
        headers = List.copyOf(headers);
        rows    = rows.stream().map(List::copyOf).toList();
    }

    /** Convenience — no caption. */
    public static <T> RelationSegment of(List<Column<T>> columns, List<T> data) {
        return build(columns, data, Optional.empty());
    }

    /** Convenience — with caption (contributes to TOC; blank becomes no caption). */
    public static <T> RelationSegment of(List<Column<T>> columns, List<T> data, String caption) {
        return build(columns, data, Line.optionalPlain(caption));
    }

    private static <T> RelationSegment build(
            List<Column<T>> columns, List<T> data, Optional<Line.Plain> caption) {
        Objects.requireNonNull(columns, "RelationSegment.of: columns");
        Objects.requireNonNull(data,    "RelationSegment.of: data");
        var headers = columns.stream().map(Column::header).toList();
        var rows    = data.stream()
                .map(item -> columns.stream().map(col -> col.extract(item)).toList())
                .toList();
        return new RelationSegment(headers, rows, caption);
    }
}
