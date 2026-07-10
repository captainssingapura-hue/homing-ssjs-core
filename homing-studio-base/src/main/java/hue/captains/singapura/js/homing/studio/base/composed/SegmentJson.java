package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.composed.text.Line;

import java.util.List;
import java.util.function.Function;

/**
 * RFC 0039 — serialises a single {@link Segment} to its content-object JSON,
 * in the exact shape the existing per-segment renderers (RFC 0024 P1c) consume.
 * Shared by the legacy flat {@code ComposedDoc.contentsRootedAt} and the new
 * rigid-tree {@link DocTreeJsonWriter}, the only difference being how a
 * <b>resource-backed</b> segment (svg / table / image / composed) addresses its
 * bytes:
 *
 * <ul>
 *   <li>Legacy passes the <i>leveled</i> URL ({@code /doc?id=<root>&lN=..}) —
 *       the navigation-baked-into-content smell RFC 0039 retires.</li>
 *   <li>The doc tree passes the resource's <b>own stable {@code url()}</b> —
 *       "immutable external resources by their own address", per RFC 0039's
 *       carve-out. Text-shaped kinds (markdown / text / code / relation) inline
 *       regardless.</li>
 * </ul>
 *
 * <p>The {@code resourceUrl} strategy lets the one serializer serve both
 * callers without duplicating the heavy block / params helpers.</p>
 *
 * @since homing-studio-base — RFC 0039 rigid-tree doc
 */
final class SegmentJson {

    private SegmentJson() {}

    /**
     * Append the {@code {…}} content object for {@code s}.
     *
     * @param sb          target
     * @param s           the segment
     * @param anchor      in-page anchor id for this content
     * @param resourceUrl URL for resource-backed kinds (svg/table/image/composed)
     */
    static void write(StringBuilder sb, Segment s, String anchor, Function<Segment, String> resourceUrl) {
        sb.append('{');
        switch (s) {
            case MarkdownSegment m -> {
                sb.append("\"kind\":\"markdown\",");
                sb.append("\"anchor\":").append(ComposedDoc.jstr(anchor)).append(',');
                sb.append("\"title\":") .append(ComposedDoc.jstr(m.title().orElse(""))).append(',');
                sb.append("\"body\":")  .append(ComposedDoc.jstr(m.body()));
            }
            case TextSegment tx -> {
                sb.append("\"kind\":\"text\",");
                sb.append("\"anchor\":").append(ComposedDoc.jstr(anchor)).append(',');
                sb.append("\"title\":") .append(ComposedDoc.jstr(tx.title().orElse(""))).append(',');
                sb.append("\"blocks\":");
                ComposedDoc.appendBlocks(sb, tx.parsed());
            }
            case CodeSegment cs -> {
                sb.append("\"kind\":\"code\",");
                sb.append("\"anchor\":")  .append(ComposedDoc.jstr(anchor)).append(',');
                sb.append("\"title\":")   .append(ComposedDoc.jstr(cs.title().orElse(""))).append(',');
                sb.append("\"language\":").append(ComposedDoc.jstr(cs.language())).append(',');
                sb.append("\"body\":")    .append(ComposedDoc.jstr(cs.body()));
            }
            case SvgSegment v -> {
                sb.append("\"kind\":\"svg\",");
                sb.append("\"anchor\":")  .append(ComposedDoc.jstr(anchor)).append(',');
                sb.append("\"caption\":") .append(ComposedDoc.jstr(v.resolvedCaption())).append(',');
                sb.append("\"svgUrl\":")  .append(ComposedDoc.jstr(resourceUrl.apply(s)));
            }
            case TableSegment t -> {
                sb.append("\"kind\":\"table\",");
                sb.append("\"anchor\":")   .append(ComposedDoc.jstr(anchor)).append(',');
                sb.append("\"caption\":")  .append(ComposedDoc.jstr(t.resolvedCaption())).append(',');
                sb.append("\"tableUrl\":") .append(ComposedDoc.jstr(resourceUrl.apply(s)));
            }
            case ImageSegment im -> {
                sb.append("\"kind\":\"image\",");
                sb.append("\"anchor\":")   .append(ComposedDoc.jstr(anchor)).append(',');
                sb.append("\"caption\":")  .append(ComposedDoc.jstr(im.resolvedCaption())).append(',');
                sb.append("\"imageUrl\":") .append(ComposedDoc.jstr(resourceUrl.apply(s)));
            }
            case UnorderedListSegment ul -> writeList(sb, "ulist", anchor, ul.items(), resourceUrl);
            case OrderedListSegment ol -> writeList(sb, "olist", anchor, ol.items(), resourceUrl);
            case ParagraphSegment p -> {
                sb.append("\"kind\":\"paragraph\",");
                sb.append("\"anchor\":").append(ComposedDoc.jstr(anchor)).append(',');
                sb.append("\"lines\":[");
                boolean firstLine = true;
                for (Line.Plain ln : p.lines()) {
                    if (!firstLine) sb.append(',');
                    firstLine = false;
                    sb.append(ComposedDoc.jstr(ln.raw()));
                }
                sb.append(']');
            }
            case RelationSegment rs -> {
                sb.append("\"kind\":\"relation\",");
                sb.append("\"anchor\":") .append(ComposedDoc.jstr(anchor)).append(',');
                sb.append("\"caption\":").append(ComposedDoc.jstr(rs.caption().map(Line.Plain::raw).orElse(""))).append(',');
                sb.append("\"headers\":");
                ComposedDoc.appendStringList(sb, rs.headers());
                sb.append(",\"rows\":[");
                boolean firstRow = true;
                for (List<String> row : rs.rows()) {
                    if (!firstRow) sb.append(',');
                    firstRow = false;
                    ComposedDoc.appendStringList(sb, row);
                }
                sb.append(']');
            }
            case ComposedSegment cd -> {
                // In the rigid-tree model a ComposedSegment is STRUCTURE (a graft),
                // never a content provider — so the doc tree never serializes one.
                // Retained for exhaustiveness / the legacy caller's parity.
                sb.append("\"kind\":\"composed\",");
                sb.append("\"anchor\":")       .append(ComposedDoc.jstr(anchor)).append(',');
                sb.append("\"caption\":")      .append(ComposedDoc.jstr(cd.resolvedCaption())).append(',');
                sb.append("\"composedUrl\":")  .append(ComposedDoc.jstr(resourceUrl.apply(s))).append(',');
                sb.append("\"composedDocId\":").append(ComposedDoc.jstr(cd.doc().uuid().toString()));
            }
            case DocumentaryWidget<?, ?> w -> {
                sb.append("\"kind\":\"documentary-widget\",");
                sb.append("\"anchor\":") .append(ComposedDoc.jstr(anchor)).append(',');
                sb.append("\"caption\":").append(ComposedDoc.jstr(w.resolvedCaption())).append(',');
                sb.append("\"moduleUrl\":")
                  .append(ComposedDoc.jstr("/module?class=" + w.widget().getClass().getCanonicalName())).append(',');
                sb.append("\"params\":");
                ComposedDoc.appendParamsJson(sb, w.params());
            }
        }
        sb.append('}');
    }

    /**
     * Serialize a list segment ({@code ulist}/{@code olist}) — its items are
     * {@link Listable} segments, written recursively with a nested anchor
     * ({@code <anchor>-<i>}). Items are never lists (compile-time), so recursion
     * is depth-1.
     */
    private static void writeList(StringBuilder sb, String kind, String anchor,
                                  List<Listable> items, Function<Segment, String> resourceUrl) {
        sb.append("\"kind\":\"").append(kind).append("\",");
        sb.append("\"anchor\":").append(ComposedDoc.jstr(anchor)).append(',');
        sb.append("\"items\":[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(',');
            write(sb, items.get(i), anchor + "-" + i, resourceUrl);
        }
        sb.append(']');
    }
}
