package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.tree.DimensionKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.NamePath;
import hue.captains.singapura.js.homing.tree.NodeKey;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.TreeNormalizer;
import hue.captains.singapura.js.homing.tree.dims.NameValue;

import hue.captains.singapura.js.homing.studio.base.DocNodeIdentity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Renders a <b>legacy markdown {@link Doc}</b> through the rigid-tree doc
 * pipeline (RFC 0039) without migrating it to a {@code ComposedDoc} /
 * {@code RigidDoc}. The third {@link TreeNormalizer} doc front-end, after
 * {@link ComposedDocNormalizer} and {@code RigidDocNormalizer}.
 *
 * <p>The markdown's own <b>ATX headings</b> ({@code #}…{@code ######}) become
 * the structure tree, so the doc gets a real foldable TOC: the doc is the
 * {@code L0} root (label = title), each heading a nested node, and the prose
 * between a heading and the next becomes that node's body. Heading nesting is
 * structural (a {@code ##} under a {@code #} is a child) regardless of skipped
 * levels. A leading {@code # Title} that duplicates the doc title is absorbed
 * into the root rather than repeated. A doc with no headings stays a single flat
 * node — the cheap "support the corpus without migration" path; deeper structure
 * is the {@code ComposedDoc}/{@code RigidDoc} upgrade, at the author's
 * discretion.</p>
 *
 * <h2>Fenced code is a segment, not text (RFC 0059 D8)</h2>
 *
 * <p>A section's body is a <b>list</b> of segments, not one blob: every fenced
 * block ({@code ```} / {@code ~~~}) closes the running {@link MarkdownSegment}
 * and becomes a {@link CodeSegment} carrying the fence's info string as its
 * language. So a Mermaid diagram is identifiable <i>in Java</i> — it is a code
 * segment whose language is {@code mermaid} — and the client is told what it has
 * instead of scanning rendered DOM for it. Every fence, not only the diagrams:
 * two representations of one construct in the same pipeline is the problem this
 * is fixing. An unterminated fence still yields its content rather than losing
 * it. Fences continue to hide headings, so a {@code #} inside one is a comment.</p>
 *
 * <h2>Nodes are named by their heading (RFC 0059 D9)</h2>
 *
 * <p>{@link #toDocTreeV2} names each node by the slug of its heading, which makes
 * a section's address the {@code '/'}-joined chain of those slugs — stable under
 * inserting or reordering siblings, unlike the child-index path
 * {@link #toDocTree} produces.</p>
 *
 * <p>{@link DocNodeIdentity} warns that slugging a heading "would invent
 * sibling-uniqueness that the source does not guarantee". Correct, and answered
 * rather than ignored: uniqueness is <b>enforced here</b>, not assumed. A slug
 * that collides with a sibling takes a {@code -2}, {@code -3} suffix. That
 * counter is scoped to the siblings, which is the point — the reader's
 * document-wide counter renumbers every later duplicate when an unrelated one is
 * inserted anywhere above, while this one can only move if those exact siblings
 * are reordered. Headings whose text slugs to nothing (punctuation, CJK) fall
 * back to {@code section}, and then disambiguate the same way.</p>
 *
 * <p>Stateless Functional Object — one {@code INSTANCE}.</p>
 *
 * @since homing-studio-base — markdown-in-workspace
 */
public final class MarkdownDocNormalizer implements TreeNormalizer<Doc> {

    public static final MarkdownDocNormalizer INSTANCE = new MarkdownDocNormalizer();

    private MarkdownDocNormalizer() {}

    /** Structure only (the {@link TreeNormalizer} contract). */
    @Override
    public NormalizedNode normalize(Doc doc) {
        return toDocTree(doc).structure();
    }

    /** The full transform: a heading-derived structure tree + per-section content. */
    public DocTree toDocTree(Doc doc) {
        Section root = parseDoc(doc);
        var providers = new LinkedHashMap<List<Integer>, ContentProvider>();
        NormalizedNode structure = toNode(doc.uuid(), root, TreeLevel.L0.INSTANCE, List.of(), providers);
        return new DocTree(structure, providers);
    }

    /**
     * The same transform addressed by <b>name-path</b> — each node named by its
     * heading's slug, so a section's address survives its siblings being edited.
     */
    public DocTreeV2 toDocTreeV2(Doc doc) {
        Section root = parseDoc(doc);
        var providers = new LinkedHashMap<String, ContentProvider>();
        NormalizedNode structure = toNodeV2(doc.uuid(), root, TreeLevel.L0.INSTANCE,
                                            NamePath.ROOT, "", providers);
        return new DocTreeV2(structure, providers);
    }

    // ── Parse markdown into a heading tree (root = the doc) ──────────────────

    private static Section parseDoc(Doc doc) {
        if (doc == null) throw new IllegalArgumentException("doc");
        String title = doc.title() == null ? "" : doc.title();
        String body  = doc.contents() == null ? "" : doc.contents();
        Section root = new Section(title, "doc");
        parse(body, title, root);
        root.flush();
        return root;
    }

    private static void parse(String body, String title, Section root) {
        Deque<Section> stack = new ArrayDeque<>();   // open ancestors, deepest on top
        Deque<Integer> levels = new ArrayDeque<>();  // their markdown heading levels
        stack.push(root);
        levels.push(0);                              // root sits above every heading

        boolean inFence = false;
        String fenceLang = "";
        StringBuilder code = null;
        boolean firstHeading = true;

        for (String line : body.split("\n", -1)) {
            String trimmed = line.stripLeading();

            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                if (inFence) {
                    // Closing: the collected lines become a segment of their own.
                    stack.peek().addCode(code.toString(), fenceLang);
                    inFence = false; fenceLang = ""; code = null;
                } else {
                    // Opening: the info string is the language, and the prose so
                    // far becomes its own segment.
                    stack.peek().flush();
                    fenceLang = fenceInfo(trimmed);
                    inFence = true; code = new StringBuilder();
                }
                continue;
            }
            if (inFence) { code.append(line).append('\n'); continue; }

            int hl = headingLevel(line);
            if (hl == 0) {
                stack.peek().body.append(line).append('\n');
                continue;
            }

            String text = headingText(line);

            // A leading "# Title" that duplicates the doc title is the doc itself —
            // absorb it into the root rather than nest a redundant node.
            if (firstHeading && hl == 1 && text.equalsIgnoreCase(title.strip())) {
                firstHeading = false;
                continue;   // root already represents it; following prose -> root.body
            }
            firstHeading = false;

            while (levels.peek() >= hl) { stack.peek().flush(); stack.pop(); levels.pop(); }
            stack.peek().flush();
            Section parent = stack.peek();
            Section node = new Section(text, parent.uniqueChildSlug(text));
            parent.kids.add(node);
            stack.push(node);
            levels.push(hl);
        }
        // An unterminated fence still yields its content rather than losing it.
        if (inFence && code != null) stack.peek().addCode(code.toString(), fenceLang);
        while (!stack.isEmpty()) { stack.peek().flush(); stack.pop(); }
    }

    /** The fence's info string, lower-cased and first word only ({@code ```js title=x}). */
    private static String fenceInfo(String trimmedLine) {
        int i = 0;
        while (i < trimmedLine.length()
               && (trimmedLine.charAt(i) == '`' || trimmedLine.charAt(i) == '~')) i++;
        String info = trimmedLine.substring(i).strip();
        int sp = info.indexOf(' ');
        if (sp >= 0) info = info.substring(0, sp);
        return info.toLowerCase();
    }

    /** ATX heading level (1–6), or 0 if the line is not a heading. */
    private static int headingLevel(String line) {
        int i = 0;
        while (i < line.length() && line.charAt(i) == ' ' && i < 3) i++;   // up to 3 lead spaces
        int hashes = 0;
        while (i < line.length() && line.charAt(i) == '#') { hashes++; i++; }
        if (hashes < 1 || hashes > 6) return 0;
        if (i >= line.length() || line.charAt(i) != ' ') return 0;         // need a space after
        return line.substring(i).strip().isEmpty() ? 0 : hashes;
    }

    /** The heading's display text — the #s, the gap, and any closing #s removed. */
    private static String headingText(String line) {
        int i = 0;
        while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '#')) i++;
        return line.substring(i).replaceAll("\\s*#+\\s*$", "").strip();
    }

    /**
     * The reader's slug rule, so an anchor written against the standalone page
     * still lands: lower-case, drop all but {@code [a-z0-9_]} and spaces, spaces
     * to hyphens, collapse and trim them.
     */
    /**
     * {@link NodeName} refuses anything past 48 characters, and a heading is
     * prose — long ones are ordinary. So a slug is clipped rather than allowed to
     * fail the doc, and the disambiguating suffix is clipped INTO the budget so a
     * collision can never push it back over. The cost is honest: a heading longer
     * than this gets a shorter anchor than the standalone reader gives it, since
     * that reader has no such limit.
     */
    private static final int MAX_SLUG = 48;

    /** Cut to {@code n} characters without leaving a trailing hyphen behind. */
    private static String clip(String s, int n) {
        if (s.length() > n) s = s.substring(0, n);
        s = s.replaceAll("-+$", "");
        return s.isEmpty() ? "section" : s;
    }

    static String slugify(String text) {
        String s = (text == null ? "" : text).toLowerCase()
                .replaceAll("[^a-z0-9_\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        s = s.replaceAll("^-", "").replaceAll("-$", "");
        return s.isEmpty() ? "section" : s;
    }

    // ── Builder tree → NormalizedNode + keyed providers ──────────────────────

    private static NormalizedNode toNode(UUID doc, Section s, TreeLevel level, List<Integer> path,
                                         Map<List<Integer>, ContentProvider> providers) {
        Map<DimensionKey, DimensionValue> dims = new LinkedHashMap<>();
        dims.put(DisplayLabel.INSTANCE, new NameValue(s.title));

        if (!s.segments.isEmpty()) {
            List<Segment> content = List.copyOf(s.segments);
            providers.put(path, () -> new ComposedLeaf(Optional.empty(), content));
        }

        List<NormalizedNode> kids = new ArrayList<>();
        TreeLevel childLevel = level.below().orElse(null);   // markdown caps at 6 deep — never hits L18
        if (childLevel != null) {
            for (int i = 0; i < s.kids.size(); i++) {
                List<Integer> childPath = new ArrayList<>(path);
                childPath.add(i);
                kids.add(toNode(doc, s.kids.get(i), childLevel, List.copyOf(childPath), providers));
            }
        }
        return new NormalizedNode(level, DocNodeIdentity.indexSegment(path),
                                  DocNodeIdentity.byIndex(doc, path), dims, kids);
    }

    /**
     * The name-path emitter. Two things differ from {@link #toNode}: the node
     * carries a {@link NodeKey} so the client can rebuild the same chain while
     * walking, and the provider key is that chain — {@code ""} at the root, since
     * the root's own name is not part of its descendants' addresses.
     */
    private static NormalizedNode toNodeV2(UUID doc, Section s, TreeLevel level,
                                           NamePath path, String key,
                                           Map<String, ContentProvider> providers) {
        Map<DimensionKey, DimensionValue> dims = new LinkedHashMap<>();
        dims.put(DisplayLabel.INSTANCE, new NameValue(s.title));
        dims.put(NodeKey.INSTANCE, new NameValue(s.slug));

        if (!s.segments.isEmpty()) {
            List<Segment> content = List.copyOf(s.segments);
            providers.put(key, () -> new ComposedLeaf(Optional.empty(), content));
        }

        List<NormalizedNode> kids = new ArrayList<>();
        TreeLevel childLevel = level.below().orElse(null);
        if (childLevel != null) {
            for (Section kid : s.kids) {
                NodeName seg = new NodeName(kid.slug);
                kids.add(toNodeV2(doc, kid, childLevel, path.then(seg),
                                  key.isEmpty() ? kid.slug : key + "/" + kid.slug, providers));
            }
        }
        return new NormalizedNode(level, new NodeName(s.slug),
                                  new DocNodeIdentity(doc, path), dims, kids);
    }

    /**
     * A heading node during the parse: its title, its sibling-unique slug, the
     * running prose, and the segments already closed off behind it.
     */
    private static final class Section {
        final String title;
        final String slug;
        final StringBuilder body = new StringBuilder();
        final List<Segment> segments = new ArrayList<>();
        final List<Section> kids = new ArrayList<>();
        private final Set<String> childSlugs = new HashSet<>();

        Section(String title, String slug) {
            this.title = title == null ? "" : title;
            this.slug  = slug;
        }

        /** Close the running prose into a segment, if it holds anything. */
        void flush() {
            String content = body.toString().strip();
            body.setLength(0);
            if (!content.isEmpty()) segments.add(new MarkdownSegment(content, Optional.empty()));
        }

        void addCode(String code, String language) {
            String body = code.endsWith("\n") ? code.substring(0, code.length() - 1) : code;
            segments.add(new CodeSegment(body, language));
        }

        /** A slug unique among THIS section's children — see the class javadoc. */
        String uniqueChildSlug(String headingText) {
            String base = clip(slugify(headingText), MAX_SLUG);
            String candidate = base;
            int n = 2;
            while (!childSlugs.add(candidate)) {
                String suffix = "-" + n;
                candidate = clip(base, MAX_SLUG - suffix.length()) + suffix;
                n++;
            }
            return candidate;
        }
    }
}
