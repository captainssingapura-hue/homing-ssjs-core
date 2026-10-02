package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocId;
import hue.captains.singapura.js.homing.studio.base.LazyReferences;
import hue.captains.singapura.js.homing.studio.base.Reference;
import hue.captains.singapura.js.homing.studio.base.NoOwnContentException;

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
 * <p>Data only: a composed doc has no content of its own ({@link #contents()} throws).
 * Whoever shows it walks the segments and the {@link #toc() TOC} and
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
 * @param references   typed cross-references; standard {@code [label](#ref:name)} grammar - declared
 *                     lazily ({@link LazyReferences}) when docs name each other
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
        List<Reference> references,
        NodeName        slug
) implements Doc, hue.captains.singapura.js.homing.studio.base.AuthoredName {

    /** RFC 0051 Phase 6 — the record component IS the authored name. */
    @Override public NodeName authoredSlug() { return slug; }


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
        // RFC 0051 Law 2 — every ComposedDoc shares this class, so the
        // class-derived default would have them all claim the segment
        // "composed". The title is the only per-instance datum available by
        // default; an author who wants a segment that survives re-wording
        // supplies one through withSlug.
        if (slug         == null) slug         = NodeName.conciseSlug(title);
        segments   = List.copyOf(segments);
        // lazily declared references stay lazy: reading them now would read docs still being made
        references = references instanceof LazyReferences ? references : List.copyOf(references);
    }

    /**
     * The canonical shape before RFC 0051 — same doc, slug derived from the
     * title. Kept so no existing construction site has to change.
     */
    public ComposedDoc(UUID uuid, String title, String summary, String abstractText,
                       String category, List<Segment> segments, List<Reference> references) {
        this(uuid, title, summary, abstractText, category, segments, references, null);
    }

    /**
     * RFC 0051 — this doc with an authored path segment, replacing the
     * title-derived default.
     *
     * <p>A wither rather than a wrapper: the framework's viewers and the
     * doc harvest dispatch on the concrete type ({@code instanceof
     * ComposedDoc}), so a delegating wrapper would keep the Doc protocol
     * intact and still blank the page. Identity has to be attached without
     * changing what the value <i>is</i>.</p>
     */
    public ComposedDoc withSlug(NodeName authored) {
        Objects.requireNonNull(authored, "ComposedDoc.withSlug(authored)");
        return new ComposedDoc(uuid, title, summary, abstractText, category,
                               segments, references, authored);
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
    @Override public String fileExtension() { return ""; }

    /**
     * A composed doc's data is its segments, not text: it has no content of its own.
     * Content is made from it by a {@code Function<Doc, Content>}. Nothing here builds a URL.
     */
    @Override public String contents()    { throw new NoOwnContentException(this); }
    @Override public String contentType() { throw new NoOwnContentException(this); }

    @Override public List<Reference> references() { return references; }

    /**
     * Leveled-tree descent — given the local identifier for the next level
     * (a string parsed as an integer segment index), return the embedded
     * sub-doc at that segment. Markdown / Text / Code / Relation segments
     * have no embedded Doc and yield empty.
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

    // -----------------------------------------------------------------------
    // TOC builder — segment captions + markdown heading extraction.
    // -----------------------------------------------------------------------

    /** ATX heading recognizer: 1-4 leading {@code #} chars + space + text. */
    private static final Pattern HEADING = Pattern.compile("^(#{1,4})\\s+(.+)$");

    /**
     * The doc's table of contents, derived from its segments - anchors {@code seg-N}
     * and {@code seg-N-hM}, as the viewers name them. For each segment:
     * <ul>
     *   <li>SvgSegment / future visual segments → one level-2 entry from caption</li>
     *   <li>MarkdownSegment → one level-2 entry from title (if present), plus
     *       level-1..level-4 entries extracted from the body's ATX headings</li>
     * </ul>
     */
    public List<TocEntry> toc() {
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
                case TypedCodeSegment tc -> {
                    tc.title().ifPresent(t -> out.add(new TocEntry(2, t, anchor)));
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
                case UnorderedListSegment ul -> { /* list body — no TOC contribution */ }
                case OrderedListSegment ol -> { /* list body — no TOC contribution */ }
                case ParagraphSegment p -> {
                    // A plain paragraph is body only — no title, no TOC contribution.
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
                    rs.caption().ifPresent(cap -> out.add(new TocEntry(2, cap.raw(), anchor)));
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

}
