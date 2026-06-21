package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.tree.DimensionKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.TreeNormalizer;
import hue.captains.singapura.js.homing.tree.dims.NameValue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders a <b>legacy markdown {@link Doc}</b> through the rigid-tree doc
 * pipeline (RFC 0039) without migrating it to a {@code ComposedDoc} /
 * {@code RigidDoc}. The third {@link TreeNormalizer} doc front-end, after
 * {@link ComposedDocNormalizer} and {@code RigidDocNormalizer}.
 *
 * <p>The markdown's own <b>ATX headings</b> ({@code #}…{@code ######}) become
 * the structure tree, so the doc gets a real foldable TOC: the doc is the
 * {@code L0} root (label = title), each heading a nested node, and the prose
 * between a heading and the next becomes that node's body (one
 * {@link MarkdownSegment}). Heading nesting is structural (a {@code ##} under a
 * {@code #} is a child) regardless of skipped levels. A leading {@code # Title}
 * that duplicates the doc title is absorbed into the root rather than repeated.
 * A doc with no headings stays a single flat node (its whole body as content) —
 * the cheap "support the corpus without migration" path; deeper structure is the
 * {@code ComposedDoc}/{@code RigidDoc} upgrade, at the author's discretion.</p>
 *
 * <p>Fenced code blocks ({@code ```} / {@code ~~~}) are respected — a {@code #}
 * inside a fence is a comment, not a heading. Setext (underline) headings are
 * not parsed; ATX is the dominant form in the corpus.</p>
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
        if (doc == null) throw new IllegalArgumentException("doc");

        String title = doc.title() == null ? "" : doc.title();
        String body  = doc.contents() == null ? "" : doc.contents();

        Section root = new Section(title);
        parse(body, title, root);

        var providers = new LinkedHashMap<List<Integer>, ContentProvider>();
        NormalizedNode structure = toNode(root, TreeLevel.L0.INSTANCE, List.of(), providers);
        return new DocTree(structure, providers);
    }

    // ── Parse markdown into a heading tree (root = the doc) ──────────────────

    private static void parse(String body, String title, Section root) {
        Deque<Section> stack = new ArrayDeque<>();   // open ancestors, deepest on top
        Deque<Integer> levels = new ArrayDeque<>();  // their markdown heading levels
        stack.push(root);
        levels.push(0);                              // root sits above every heading

        boolean inFence = false;
        boolean firstHeading = true;

        for (String line : body.split("\n", -1)) {
            String trimmed = line.stripLeading();
            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                inFence = !inFence;
                stack.peek().body.append(line).append('\n');
                continue;
            }

            int hl = inFence ? 0 : headingLevel(line);
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

            while (levels.peek() >= hl) { stack.pop(); levels.pop(); }
            Section node = new Section(text);
            stack.peek().kids.add(node);
            stack.push(node);
            levels.push(hl);
        }
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

    // ── Builder tree → NormalizedNode + position-keyed providers ─────────────

    private static NormalizedNode toNode(Section s, TreeLevel level, List<Integer> path,
                                         Map<List<Integer>, ContentProvider> providers) {
        Map<DimensionKey, DimensionValue> dims = new LinkedHashMap<>();
        dims.put(DisplayLabel.INSTANCE, new NameValue(s.title));

        String content = s.body.toString().strip();
        if (!content.isEmpty()) {
            providers.put(path, () -> new ComposedLeaf(List.of(new MarkdownSegment(content))));
        }

        List<NormalizedNode> kids = new ArrayList<>();
        TreeLevel childLevel = level.below().orElse(null);   // markdown caps at 6 deep — never hits L18
        if (childLevel != null) {
            for (int i = 0; i < s.kids.size(); i++) {
                List<Integer> childPath = new ArrayList<>(path);
                childPath.add(i);
                kids.add(toNode(s.kids.get(i), childLevel, List.copyOf(childPath), providers));
            }
        }
        return new NormalizedNode(level, dims, kids);
    }

    /** A mutable heading node during the parse — title + accumulating body + children. */
    private static final class Section {
        final String title;
        final StringBuilder body = new StringBuilder();
        final List<Section> kids = new ArrayList<>();
        Section(String title) { this.title = title == null ? "" : title; }
    }
}
