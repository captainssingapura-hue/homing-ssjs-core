package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.composed.text.Block;
import hue.captains.singapura.js.homing.studio.base.composed.text.Inline;
import hue.captains.singapura.js.homing.studio.base.composed.text.Line;

import java.util.List;

/**
 * A composed doc as the legacy composed viewer reads it: the JSON bundling its framing,
 * its {@link ComposedDoc#toc() TOC} and its segments - each segment kind in the shape
 * its renderer walks, and each embedded doc (svg, table, image, composed) addressed by
 * the leveled {@code /doc?id=<root>&l1=..} URL the viewer fetches it by.
 *
 * <p>The legacy wire's, moved out of {@link ComposedDoc} when the doc model became data
 * (RFC 0039's point one: the doc builds no URLs). Byte-for-byte what
 * {@code ComposedDoc.contents()} returned before; retired with the viewer.</p>
 */
public final class ComposedDocJson {

    private ComposedDocJson() {}

    /**
     * Render the JSON payload with URLs rooted at {@code rootId} prefixed
     * by {@code pathPrefix}. Each embedded sub-doc emits a URL of shape
     * {@code /doc?id=<rootId>&l1=<p[0]>&l2=<p[1]>&...&l<N+1>=<localIndex>}
     * — the local index of the segment within this doc appended one level
     * deeper than the prefix the request walked to reach here.
     *
     * <p>The framework's leveled-tree URL scheme means the embedded doc's
     * URL identifies its <em>containment path from the root</em>, not its
     * own UUID. The embedded doc never needs separate UUID registration;
     * its addressability is its position inside its parent.</p>
     */
    public static String write(ComposedDoc doc, String rootId, List<String> pathPrefix) {
        var sb = new StringBuilder("{");
        sb.append("\"title\":")        .append(jstr(doc.title())).append(',');
        sb.append("\"summary\":")      .append(jstr(doc.summary())).append(',');
        sb.append("\"abstractText\":") .append(jstr(doc.abstractText())).append(',');
        sb.append("\"category\":")     .append(jstr(doc.category())).append(',');

        // ---- TOC ----
        sb.append("\"toc\":[");
        boolean firstToc = true;
        for (TocEntry te : doc.toc()) {
            if (!firstToc) sb.append(',');
            firstToc = false;
            sb.append("{\"level\":").append(te.level())
              .append(",\"text\":").append(jstr(te.text()))
              .append(",\"anchor\":").append(jstr(te.anchor()))
              .append('}');
        }
        sb.append("],");

        // ---- Segments ----
        sb.append("\"segments\":[");
        boolean firstSeg = true;
        int segIndex = 0;
        for (Segment s : doc.segments()) {
            if (!firstSeg) sb.append(',');
            firstSeg = false;
            sb.append('{');
            switch (s) {
                case MarkdownSegment m -> {
                    sb.append("\"kind\":\"markdown\",");
                    sb.append("\"anchor\":").append(jstr("seg-" + segIndex)).append(',');
                    sb.append("\"title\":") .append(jstr(m.title().orElse(""))).append(',');
                    sb.append("\"body\":")  .append(jstr(m.body()));
                }
                case TextSegment tx -> {
                    sb.append("\"kind\":\"text\",");
                    sb.append("\"anchor\":").append(jstr("seg-" + segIndex)).append(',');
                    sb.append("\"title\":") .append(jstr(tx.title().orElse(""))).append(',');
                    sb.append("\"blocks\":");
                    appendBlocks(sb, tx.parsed());
                }
                case CodeSegment cs -> {
                    sb.append("\"kind\":\"code\",");
                    sb.append("\"anchor\":")  .append(jstr("seg-" + segIndex)).append(',');
                    sb.append("\"title\":")   .append(jstr(cs.title().orElse(""))).append(',');
                    sb.append("\"language\":").append(jstr(cs.language())).append(',');
                    sb.append("\"body\":")    .append(jstr(cs.body()));
                }
                // The typed mirror: same object, language read from the type.
                case TypedCodeSegment tc -> {
                    sb.append("\"kind\":\"code\",");
                    sb.append("\"anchor\":")  .append(jstr("seg-" + segIndex)).append(',');
                    sb.append("\"title\":")   .append(jstr(tc.title().orElse(""))).append(',');
                    sb.append("\"language\":").append(jstr(tc.language().tag())).append(',');
                    sb.append("\"body\":")    .append(jstr(tc.body()));
                }
                case SvgSegment v -> {
                    sb.append("\"kind\":\"svg\",");
                    sb.append("\"anchor\":")  .append(jstr("seg-" + segIndex)).append(',');
                    sb.append("\"caption\":") .append(jstr(v.resolvedCaption())).append(',');
                    sb.append("\"svgUrl\":")  .append(jstr(buildLeveledUrl(rootId, pathPrefix, segIndex)));
                }
                case TableSegment t -> {
                    sb.append("\"kind\":\"table\",");
                    sb.append("\"anchor\":")   .append(jstr("seg-" + segIndex)).append(',');
                    sb.append("\"caption\":")  .append(jstr(t.resolvedCaption())).append(',');
                    sb.append("\"tableUrl\":") .append(jstr(buildLeveledUrl(rootId, pathPrefix, segIndex)));
                }
                case ImageSegment im -> {
                    sb.append("\"kind\":\"image\",");
                    sb.append("\"anchor\":")   .append(jstr("seg-" + segIndex)).append(',');
                    sb.append("\"caption\":")  .append(jstr(im.resolvedCaption())).append(',');
                    sb.append("\"imageUrl\":") .append(jstr(buildLeveledUrl(rootId, pathPrefix, segIndex)));
                }
                case RelationSegment rs -> {
                    sb.append("\"kind\":\"relation\",");
                    sb.append("\"anchor\":") .append(jstr("seg-" + segIndex)).append(',');
                    sb.append("\"caption\":").append(jstr(rs.caption().map(Line.Plain::raw).orElse(""))).append(',');
                    sb.append("\"headers\":");
                    appendStringList(sb, rs.headers());
                    sb.append(",\"rows\":[");
                    boolean firstRow = true;
                    for (List<String> row : rs.rows()) {
                        if (!firstRow) sb.append(',');
                        firstRow = false;
                        appendStringList(sb, row);
                    }
                    sb.append(']');
                }
                case UnorderedListSegment ul -> appendListSegment(sb, "ulist", segIndex, ul.items(), rootId, pathPrefix);
                case OrderedListSegment ol -> appendListSegment(sb, "olist", segIndex, ol.items(), rootId, pathPrefix);
                case ParagraphSegment p -> {
                    sb.append("\"kind\":\"paragraph\",");
                    sb.append("\"anchor\":").append(jstr("seg-" + segIndex)).append(',');
                    sb.append("\"lines\":[");
                    boolean firstLine = true;
                    for (Line.Plain ln : p.lines()) {
                        if (!firstLine) sb.append(',');
                        firstLine = false;
                        sb.append(jstr(ln.raw()));
                    }
                    sb.append(']');
                }
                case ComposedSegment cd -> {
                    // RFC 0024 P1c — recursive composedDoc reference.
                    // The renderer fetches the leveled URL and mounts a fresh
                    // ComposedWidget into a sub-branch. The embedded doc's UUID
                    // is also emitted for the client's cycle-detection stack.
                    sb.append("\"kind\":\"composed\",");
                    sb.append("\"anchor\":")       .append(jstr("seg-" + segIndex)).append(',');
                    sb.append("\"caption\":")      .append(jstr(cd.resolvedCaption())).append(',');
                    sb.append("\"composedUrl\":")  .append(jstr(buildLeveledUrl(rootId, pathPrefix, segIndex))).append(',');
                    sb.append("\"composedDocId\":").append(jstr(cd.doc().uuid().toString()));
                }
                case DocumentaryWidget<?, ?> w -> {
                    sb.append("\"kind\":\"documentary-widget\",");
                    sb.append("\"anchor\":")    .append(jstr("seg-" + segIndex)).append(',');
                    sb.append("\"caption\":")   .append(jstr(w.resolvedCaption())).append(',');
                    // Typed module URL — the wrapped AppModule's JS module.
                    // Identical for every instance of the same widget *type*; the
                    // browser caches once. Per-instance variation flows through
                    // `params` below, not through the URL.
                    sb.append("\"moduleUrl\":")
                      .append(jstr("/module?class=" + w.widget().getClass().getCanonicalName())).append(',');
                    // Typed Params, serialised as a JSON object via record-component
                    // reflection. Passed to appMain at call site, not encoded in URL
                    // — the EsModule stays cacheable; param shape varies per segment.
                    sb.append("\"params\":");
                    appendParamsJson(sb, w.params());
                }
                // An embed only the studio could name: DocumentaryWidget is the one with a wire shape.
                case EmbeddedSegment e -> throw new IllegalStateException(
                        "The legacy composed viewer has no wire shape for " + e.getClass().getName());
            }
            sb.append('}');
            segIndex++;
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * Build the URL for an embedded segment at the given local index. The
     * URL carries {@code rootId} plus the existing {@code pathPrefix} levels,
     * with the local index appended one level deeper.
     */
    private static String buildLeveledUrl(String rootId, List<String> pathPrefix, int segIndex) {
        var sb = new StringBuilder("/doc?id=").append(rootId);
        for (int i = 0; i < pathPrefix.size(); i++) {
            sb.append("&l").append(i + 1).append('=').append(pathPrefix.get(i));
        }
        sb.append("&l").append(pathPrefix.size() + 1).append('=').append(segIndex);
        return sb.toString();
    }

    /**
     * Serialize a list segment ({@code ulist}/{@code olist}) in the flat path.
     * Delegates each {@link Listable} item to {@link SegmentJson}; nested resource
     * items reuse the list's own leveled URL (best-effort — flat composed docs
     * don't author lists; the rigid-tree path is the supported one).
     */
    private static void appendListSegment(StringBuilder sb, String kind, int segIndex,
            List<? extends Segment> items, String rootId, List<String> pathPrefix) {
        sb.append("\"kind\":\"").append(kind).append("\",");
        sb.append("\"anchor\":").append(jstr("seg-" + segIndex)).append(',');
        sb.append("\"items\":[");
        for (int j = 0; j < items.size(); j++) {
            if (j > 0) sb.append(',');
            SegmentJson.write(sb, items.get(j), "seg-" + segIndex + "-" + j,
                    s -> buildLeveledUrl(rootId, pathPrefix, segIndex));
        }
        sb.append(']');
    }

    // -----------------------------------------------------------------------
    // TextSegment AST → JSON. Mirrors the Block / Inline ADT shape so the
    // client renderer is a pure data walk (no second parser).
    // -----------------------------------------------------------------------

    static void appendBlocks(StringBuilder sb, List<Block> blocks) {
        sb.append('[');
        for (int i = 0; i < blocks.size(); i++) {
            if (i > 0) sb.append(',');
            appendBlock(sb, blocks.get(i));
        }
        sb.append(']');
    }

    private static void appendBlock(StringBuilder sb, Block b) {
        switch (b) {
            case Block.Para p -> {
                sb.append("{\"kind\":\"p\",\"inlines\":");
                appendInlines(sb, p.inlines());
                sb.append('}');
            }
            case Block.Bullets bl -> {
                sb.append("{\"kind\":\"ul\",\"items\":[");
                for (int i = 0; i < bl.items().size(); i++) {
                    if (i > 0) sb.append(',');
                    appendInlines(sb, bl.items().get(i));
                }
                sb.append("]}");
            }
            case Block.Numbered nl -> {
                sb.append("{\"kind\":\"ol\",\"items\":[");
                for (int i = 0; i < nl.items().size(); i++) {
                    if (i > 0) sb.append(',');
                    appendInlines(sb, nl.items().get(i));
                }
                sb.append("]}");
            }
            case Block.Quote q -> {
                sb.append("{\"kind\":\"quote\",\"inlines\":");
                appendInlines(sb, q.inlines());
                sb.append('}');
            }
        }
    }

    private static void appendInlines(StringBuilder sb, List<Inline> inlines) {
        sb.append('[');
        for (int i = 0; i < inlines.size(); i++) {
            if (i > 0) sb.append(',');
            appendInline(sb, inlines.get(i));
        }
        sb.append(']');
    }

    private static void appendInline(StringBuilder sb, Inline in) {
        switch (in) {
            case Inline.Text t   -> sb.append("{\"kind\":\"text\",\"text\":").append(jstr(t.text())).append('}');
            case Inline.Code c   -> sb.append("{\"kind\":\"code\",\"text\":").append(jstr(c.text())).append('}');
            case Inline.Bold b   -> {
                sb.append("{\"kind\":\"b\",\"inlines\":");
                appendInlines(sb, b.inlines());
                sb.append('}');
            }
            case Inline.Italic i -> {
                sb.append("{\"kind\":\"i\",\"inlines\":");
                appendInlines(sb, i.inlines());
                sb.append('}');
            }
            case Inline.Ref r    -> sb.append("{\"kind\":\"ref\",\"label\":")
                                      .append(jstr(r.label()))
                                      .append(",\"anchor\":").append(jstr(r.anchor()))
                                      .append('}');
        }
    }

    // -----------------------------------------------------------------------
    // JSON string escaping
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // DocumentaryWidget params → JSON. Reflects over the record's components;
    // each component becomes a key in the emitted object. Supports the same
    // scalar types ParamsWriter handles for the URL-derived case: String,
    // boxed/unboxed numerics, boolean, enum (emitted as name), Optional<T>
    // (emitted as null when empty), List<T> (emitted as array). Other shapes
    // throw at request time — the caller is constructing the segment in code
    // anyway, so the failure is loud and immediate.
    // -----------------------------------------------------------------------

    static void appendParamsJson(StringBuilder sb, Object params) {
        if (params == null) { sb.append("null"); return; }
        var cls = params.getClass();
        if (cls.getRecordComponents() == null) {
            // _None or non-record — emit empty object
            sb.append("{}");
            return;
        }
        var components = cls.getRecordComponents();
        sb.append('{');
        boolean first = true;
        for (var rc : components) {
            if (!first) sb.append(',');
            first = false;
            sb.append(jstr(rc.getName())).append(':');
            try {
                Object value = rc.getAccessor().invoke(params);
                appendParamValue(sb, value);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "Failed reading DocumentaryWidget params component "
                                + rc.getName() + " on " + cls.getName(), e);
            }
        }
        sb.append('}');
    }

    private static void appendParamValue(StringBuilder sb, Object v) {
        if (v == null) { sb.append("null"); return; }
        if (v instanceof String s)               { sb.append(jstr(s)); return; }
        if (v instanceof Boolean b)              { sb.append(b ? "true" : "false"); return; }
        if (v instanceof Number n)               { sb.append(n.toString()); return; }
        if (v instanceof Enum<?> e)              { sb.append(jstr(e.name())); return; }
        if (v instanceof java.util.Optional<?> o) {
            if (o.isEmpty()) { sb.append("null"); return; }
            appendParamValue(sb, o.get());
            return;
        }
        if (v instanceof java.util.List<?> list) {
            sb.append('[');
            boolean first = true;
            for (var item : list) {
                if (!first) sb.append(',');
                first = false;
                appendParamValue(sb, item);
            }
            sb.append(']');
            return;
        }
        throw new IllegalStateException(
                "Unsupported DocumentaryWidget param value type: "
                        + v.getClass().getName() + " (value=" + v + ")");
    }

    static void appendStringList(StringBuilder sb, List<String> items) {
        sb.append('[');
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(jstr(items.get(i)));
        }
        sb.append(']');
    }

    static String jstr(String v) {
        if (v == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
