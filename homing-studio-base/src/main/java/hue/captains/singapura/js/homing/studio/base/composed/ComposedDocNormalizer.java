package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.tree.DimensionKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.RigidTrees;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.TreeNormalizer;
import hue.captains.singapura.js.homing.tree.dims.NameValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * RFC 0039 — transforms a {@link ComposedDoc} into a rigid tree: the
 * {@link TreeNormalizer} for the doc front-end (the second after
 * {@code CatalogueNormalizer}, as RFC 0040 promised). Produces a {@link DocTree}
 * — pure {@link NormalizedNode} structure plus the position-keyed
 * {@link ContentProvider} seam.
 *
 * <p>The mapping (the mechanical "first candidate" of RFC 0039):</p>
 * <ul>
 *   <li>The doc is the {@code L0} root: label = title, no content provider
 *       (purely structural).</li>
 *   <li>Each top-level {@link Segment} is an {@code L1} child: label = the
 *       segment's title/caption (or its kind), and a provider yielding that
 *       segment as the node's content.</li>
 *   <li>A {@link ComposedSegment} is <b>structure, not content</b>: the
 *       embedded doc is normalized standalone and {@link RigidTrees#graftUnder
 *       grafted} under this node (its segments shift one level deeper). Its
 *       providers re-key under the embed's path.</li>
 * </ul>
 *
 * <p><b>Cycles die in the transform.</b> A by-reference re-entry is truncated
 * by a visited-set of doc UUIDs, so the emitted structure is acyclic by
 * construction (RFC 0039). In practice {@link ComposedSegment} holds a direct
 * {@code ComposedDoc} reference, so a true cycle is already unconstructible —
 * the guard is cheap defence, and a doc embedded twice (a diamond, not a cycle)
 * renders fully because the visited-set clears on exit.</p>
 *
 * @since homing-studio-base — RFC 0039 rigid-tree doc
 */
public final class ComposedDocNormalizer implements TreeNormalizer<ComposedDoc> {

    public static final ComposedDocNormalizer INSTANCE = new ComposedDocNormalizer();

    private ComposedDocNormalizer() {}

    /** Structure only (the {@link TreeNormalizer} contract). */
    @Override
    public NormalizedNode normalize(ComposedDoc doc) {
        return toDocTree(doc).structure();
    }

    /** The full transform: pure structure tree + position-keyed content providers. */
    public DocTree toDocTree(ComposedDoc doc) {
        if (doc == null) throw new IllegalArgumentException("doc");
        var providers = new LinkedHashMap<List<Integer>, ContentProvider>();
        NormalizedNode root = build(doc, TreeLevel.L0.INSTANCE, List.of(),
                new LinkedHashSet<>(), providers);
        return new DocTree(root, providers);
    }

    // ── Recursive build: structure into the return, providers into the map ──

    private NormalizedNode build(ComposedDoc doc, TreeLevel level, List<Integer> pathPrefix,
                                 Set<UUID> visiting,
                                 Map<List<Integer>, ContentProvider> providers) {
        var kids = new ArrayList<NormalizedNode>();
        TreeLevel childLevel = level.below().orElse(null);
        if (childLevel != null) {
            int idx = 0;
            for (Segment seg : doc.segments()) {
                List<Integer> childPath = append(pathPrefix, idx);
                if (seg instanceof ComposedSegment cs) {
                    kids.add(graftEmbed(cs, level, childPath, visiting, providers));
                } else {
                    // Content node: a leaf whose provider yields this one segment
                    // as a singleton ComposedLeaf bundle (RFC 0041).
                    kids.add(NormalizedNode.leaf(childLevel, labelDims(labelFor(seg))));
                    final ComposedLeaf content = new ComposedLeaf(List.of(seg));
                    providers.put(childPath, () -> content);
                }
                idx++;
            }
        }
        return new NormalizedNode(level, labelDims(doc.title()), kids);
    }

    /** Graft an embedded doc under the host, re-keying its providers by position. */
    private NormalizedNode graftEmbed(ComposedSegment cs, TreeLevel hostLevel,
                                      List<Integer> childPath, Set<UUID> visiting,
                                      Map<List<Integer>, ContentProvider> providers) {
        ComposedDoc embedded = cs.doc();
        UUID id = embedded.uuid();
        TreeLevel childLevel = hostLevel.below().orElseThrow();
        if (id != null && visiting.contains(id)) {
            // Cycle — truncate; emit a leaf marking the cut (no content).
            return NormalizedNode.leaf(childLevel, labelDims("↻ " + cs.resolvedCaption()));
        }
        if (id != null) visiting.add(id);
        // Build the embedded doc standalone at L0 (providers keyed under childPath,
        // graft-invariant since positions don't change with the level shift), then
        // graft so its root lands one level below the host.
        NormalizedNode subRoot = build(embedded, TreeLevel.L0.INSTANCE, childPath, visiting, providers);
        NormalizedNode grafted = RigidTrees.graftUnder(subRoot, hostLevel);
        if (id != null) visiting.remove(id);
        return grafted;
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private static List<Integer> append(List<Integer> prefix, int idx) {
        var out = new ArrayList<Integer>(prefix.size() + 1);
        out.addAll(prefix);
        out.add(idx);
        return List.copyOf(out);
    }

    private static Map<DimensionKey, DimensionValue> labelDims(String label) {
        return Map.of(DisplayLabel.INSTANCE, new NameValue(label == null ? "" : label));
    }

    /** A node label: the segment's title/caption, falling back to its kind. */
    private static String labelFor(Segment s) {
        String l = switch (s) {
            case MarkdownSegment m -> m.title().orElse("");
            case TextSegment t     -> t.title().orElse("");
            case CodeSegment c     -> c.title().orElse("");
            case RelationSegment r -> r.caption().orElse("");
            case SvgSegment sv     -> sv.resolvedCaption();
            case TableSegment tb   -> tb.resolvedCaption();
            case ImageSegment im   -> im.resolvedCaption();
            case ComposedSegment cs -> cs.resolvedCaption();
            case DocumentaryWidget w -> "";
        };
        return (l == null || l.isBlank()) ? kindOf(s) : l;
    }

    private static String kindOf(Segment s) {
        return switch (s) {
            case MarkdownSegment m -> "markdown";
            case TextSegment t     -> "text";
            case CodeSegment c     -> "code";
            case RelationSegment r -> "relation";
            case SvgSegment sv     -> "svg";
            case TableSegment tb   -> "table";
            case ImageSegment im   -> "image";
            case ComposedSegment cs -> "composed";
            case DocumentaryWidget w -> "widget";
        };
    }
}
