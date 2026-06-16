package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocId;
import hue.captains.singapura.js.homing.studio.base.Reference;
import hue.captains.singapura.js.homing.studio.base.SvgDoc;
import hue.captains.singapura.js.homing.studio.base.composed.text.Block;
import hue.captains.singapura.js.homing.studio.base.composed.text.Inline;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RFC 0019 — the composed document Doc kind. Body is an ordered
 * sequence of typed {@link Segment}s (markdown + SVG in Phase 1;
 * extends to table + image in Phase 2/3).
 *
 * <p>The new default Doc shape per RFC 0019. Replaces the
 * "markdown-with-inline-HTML-escapes" pattern with typed segments;
 * each segment kind has its own typed renderer; no HTML escape hatch
 * anywhere.</p>
 *
 * <p>{@link #contents()} returns a JSON payload bundling the segments
 * and the server-derived TOC; {@code ComposedViewer} consumes it and
 * dispatches per segment kind.</p>
 *
 * <p>Realises Doc ontology axioms A1–A8 and Viewer ontology V11/V12 via
 * the typed {@code DocViewer} base. Theme participation is automatic:
 * MarkdownSegment renders through the framework's marked.js pipeline
 * into themed HTML; SvgSegment inherits theme via RFC 0017's
 * currentColor / var(--color-*) discipline.</p>
 *
 * @param uuid         durable UUID (per Doc A1)
 * @param title        display title
 * @param summary      short one-line summary; shown in catalogue outer cards. Keep brief — 1-2 sentences.
 * @param abstractText longer prose describing the doc; shown on the detail view alongside the doc content. Defaults to {@code summary} when not separately authored, preserving today's behavior.
 * @param category     badge category (e.g. "RFC", "CASE STUDY", "DOCTRINE")
 * @param segments     ordered list of typed segments
 * @param references   typed cross-references; standard {@code [label](#ref:name)} grammar
 *
 * @since RFC 0019 Phase 1; abstract/summary split added post-RFC-0034 session.
 */
public record ComposedDoc(
        UUID            uuid,
        String          title,
        String          summary,
        String          abstractText,
        String          category,
        List<Segment>   segments,
        List<Reference> references
) implements Doc {

    public ComposedDoc {
        Objects.requireNonNull(uuid,       "ComposedDoc.uuid");
        Objects.requireNonNull(title,      "ComposedDoc.title");
        Objects.requireNonNull(segments,   "ComposedDoc.segments");
        Objects.requireNonNull(references, "ComposedDoc.references");
        if (title.isBlank()) {
            throw new IllegalArgumentException("ComposedDoc.title must not be blank");
        }
        if (summary      == null) summary      = "";
        if (abstractText == null) abstractText = summary;
        if (category     == null) category     = "DOC";
        segments   = List.copyOf(segments);
        references = List.copyOf(references);
    }

    /**
     * Backward-compatible constructor — every pre-split ComposedDoc passed
     * one summary string. That single string becomes both summary AND
     * abstractText; the visual behavior of every existing doc is identical
     * to today. Authors who want the split provide both via the canonical
     * 7-arg constructor.
     */
    public ComposedDoc(UUID uuid, String title, String summary, String category,
                       List<Segment> segments, List<Reference> references) {
        this(uuid, title, summary, summary, category, segments, references);
    }

    // -----------------------------------------------------------------------
    // Doc protocol
    // -----------------------------------------------------------------------

    @Override public DocId  id()          { return new DocId.ByUuid(uuid); }
    @Override public String kind()        { return "composed"; }
    @Override public String url()         { return "/app?app=composed-viewer&id=" + uuid; }
    @Override public String contentType() { return "application/json; charset=utf-8"; }
    @Override public String fileExtension() { return ""; }

    /**
     * JSON payload — bundles the segments and the server-derived TOC. The
     * ComposedViewer fetches this via {@code /doc?id=<uuid>} and dispatches
     * per segment kind on the client side.
     *
     * <p>This top-level call uses the doc's own UUID as the URL root with
     * no level prefix — appropriate for direct navigation requests like
     * {@code /doc?id=<thisUuid>}. The leveled-URL emission goes through
     * {@link #contentsRootedAt(String, List)} which threads the {@code (rootId,
     * pathPrefix)} context for nested ComposedDoc emission.</p>
     */
    @Override public String contents() {
        return contentsRootedAt(uuid.toString(), List.of());
    }

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
    @Override public String contentsRootedAt(String rootId, List<String> pathPrefix) {
        var sb = new StringBuilder("{");
        sb.append("\"title\":")        .append(jstr(title)).append(',');
        sb.append("\"summary\":")      .append(jstr(summary)).append(',');
        sb.append("\"abstractText\":") .append(jstr(abstractText)).append(',');
        sb.append("\"category\":")     .append(jstr(category)).append(',');

        // ---- TOC ----
        sb.append("\"toc\":[");
        boolean firstToc = true;
        for (TocEntry te : buildToc()) {
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
        for (Segment s : segments) {
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
                    sb.append("\"caption\":").append(jstr(rs.caption().orElse(""))).append(',');
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
            }
            sb.append('}');
            segIndex++;
        }
        sb.append("]}");
        return sb.toString();
    }

    @Override public List<Reference> references() { return references; }

    /**
     * Leveled-tree descent — given the local identifier for the next level
     * (a string parsed as an integer segment index), return the embedded
     * sub-doc at that segment. Markdown / Text / Code / Relation /
     * DocumentaryWidget segments have no embedded Doc and yield empty.
     *
     * <p>The framework's {@code /doc?id=<root>&l1=...&l2=...} walker calls
     * this for each {@code lN} value in sequence. ComposedSegment yields
     * its embedded ComposedDoc (which can be descended further via
     * {@code lN+1}); SvgSegment / TableSegment / ImageSegment yield their
     * terminal docs (further descent fails cleanly because those doc
     * kinds inherit the default {@code resolveChild} returning empty).</p>
     */
    @Override
    public Optional<Doc> resolveChild(String levelId) {
        int idx;
        try { idx = Integer.parseInt(levelId); }
        catch (NumberFormatException e) { return Optional.empty(); }
        if (idx < 0 || idx >= segments.size()) return Optional.empty();
        return switch (segments.get(idx)) {
            case SvgSegment v       -> Optional.of(v.doc());
            case TableSegment t     -> Optional.of(t.doc());
            case ImageSegment im    -> Optional.of(im.doc());
            case ComposedSegment cs -> Optional.of(cs.doc());
            default -> Optional.empty();
        };
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

    // -----------------------------------------------------------------------
    // TOC builder — segment captions + markdown heading extraction.
    // -----------------------------------------------------------------------

    /** ATX heading recognizer: 1-4 leading {@code #} chars + space + text. */
    private static final Pattern HEADING = Pattern.compile("^(#{1,4})\\s+(.+)$");

    /**
     * Walk the segments and build the TOC. For each segment:
     * <ul>
     *   <li>SvgSegment / future visual segments → one level-2 entry from caption</li>
     *   <li>MarkdownSegment → one level-2 entry from title (if present), plus
     *       level-1..level-4 entries extracted from the body's ATX headings</li>
     * </ul>
     */
    List<TocEntry> buildToc() {
        var out = new ArrayList<TocEntry>();
        int segIndex = 0;
        for (Segment s : segments) {
            String anchor = "seg-" + segIndex;
            switch (s) {
                case TextSegment tx -> {
                    // T0..T4 grammar has no headings inside body; title is
                    // the only TOC contribution.
                    tx.title().ifPresent(t -> out.add(new TocEntry(2, t, anchor)));
                }
                case CodeSegment cs -> {
                    // Code body is opaque; only the optional title contributes
                    // to the TOC.
                    cs.title().ifPresent(t -> out.add(new TocEntry(2, t, anchor)));
                }
                case MarkdownSegment m -> {
                    m.title().ifPresent(t -> out.add(new TocEntry(2, t, anchor)));
                    // Heading extraction from the markdown body.
                    int headingIdx = 0;
                    for (String line : m.body().split("\n", -1)) {
                        Matcher mh = HEADING.matcher(line);
                        if (mh.matches()) {
                            int level = mh.group(1).length();
                            String text = mh.group(2).trim();
                            String hAnchor = anchor + "-h" + headingIdx++;
                            out.add(new TocEntry(level, text, hAnchor));
                        }
                    }
                }
                case SvgSegment v -> {
                    String cap = v.resolvedCaption();
                    if (!cap.isBlank()) {
                        out.add(new TocEntry(2, cap, anchor));
                    }
                }
                case TableSegment t -> {
                    String cap = t.resolvedCaption();
                    if (!cap.isBlank()) {
                        out.add(new TocEntry(2, cap, anchor));
                    }
                }
                case ImageSegment im -> {
                    String cap = im.resolvedCaption();
                    if (!cap.isBlank()) {
                        out.add(new TocEntry(2, cap, anchor));
                    }
                }
                case ComposedSegment cd -> {
                    // RFC 0024 P1c — recursive composed segment. TOC entry uses
                    // the resolved caption (the segment's caption-override
                    // takes precedence over the embedded doc's title).
                    String cap = cd.resolvedCaption();
                    if (!cap.isBlank()) {
                        out.add(new TocEntry(2, cap, anchor));
                    }
                    // Note: we do NOT recursively expand the embedded doc's
                    // TOC into the parent's TOC. Each ComposedDoc owns its
                    // own TOC; the embedded doc renders its TOC inside its
                    // own sub-tree when mounted. Flattening would
                    // structurally conflict with the recursive mount model
                    // (each level's TOC sidebar lives next to its own body).
                }
                case RelationSegment rs -> {
                    rs.caption().ifPresent(cap -> out.add(new TocEntry(2, cap, anchor)));
                }
                case DocumentaryWidget<?, ?> w -> {
                    String cap = w.resolvedCaption();
                    if (!cap.isBlank()) {
                        out.add(new TocEntry(2, cap, anchor));
                    }
                }
            }
            segIndex++;
        }
        return out;
    }

    // -----------------------------------------------------------------------
    // Convenience factories
    // -----------------------------------------------------------------------

    /** Deterministic UUID derivation for code-defined ComposedDocs. */
    public static UUID deterministicUuid(String seed) {
        return UUID.nameUUIDFromBytes(("composed:" + seed).getBytes(StandardCharsets.UTF_8));
    }

    /** Build a ComposedDoc with no references — common case. */
    public static ComposedDoc of(UUID uuid, String title, String summary, String category, List<Segment> segments) {
        return new ComposedDoc(uuid, title, summary, summary, category, segments, List.of());
    }

    /** Build a ComposedDoc with a separate abstract and no references. */
    public static ComposedDoc of(UUID uuid, String title, String summary, String abstractText,
                                 String category, List<Segment> segments) {
        return new ComposedDoc(uuid, title, summary, abstractText, category, segments, List.of());
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
