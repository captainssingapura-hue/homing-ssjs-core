package hue.captains.singapura.js.homing.studio.base.rigid;

import hue.captains.singapura.js.homing.studio.base.composed.ComposedLeaf;
import hue.captains.singapura.js.homing.studio.base.composed.ContentProvider;
import hue.captains.singapura.js.homing.studio.base.composed.DocTree;
import hue.captains.singapura.js.homing.tree.DimensionKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.dims.NameValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RFC 0042 — transforms a {@link RigidDoc} into a {@link DocTree} (RFC 0039):
 * the pure structure tree plus the position-keyed content seam. Unlike
 * {@code ComposedDocNormalizer}, no titled-fold heuristic is needed — the author
 * already expressed the tree through the leveled builder, so this walk is a
 * direct one-to-one projection.
 *
 * <p>Each {@link DocNode} becomes a {@link NormalizedNode} at its depth's
 * {@link TreeLevel} (RFC 0040); a node's content bundle, when non-empty, becomes
 * a {@link ComposedLeaf} bound at the node's child-index path through the
 * {@link ContentProvider} seam (RFC 0041). A node may carry both content (a
 * lead-in) and children — the foldable renderer handles it.</p>
 *
 * <p>Functional Object: stateless, one {@code INSTANCE}.</p>
 *
 * @since homing-studio-base — RFC 0042 leveled tree-builder
 */
public final class RigidDocNormalizer {

    public static final RigidDocNormalizer INSTANCE = new RigidDocNormalizer();

    private RigidDocNormalizer() {}

    /** The full transform: pure structure tree + position-keyed content providers. */
    public DocTree toDocTree(RigidDoc doc) {
        if (doc == null) throw new IllegalArgumentException("doc");
        var providers = new LinkedHashMap<List<Integer>, ContentProvider>();
        NormalizedNode root = build(doc.root(), TreeLevel.L0.INSTANCE, List.of(), providers);
        return new DocTree(root, providers);
    }

    private NormalizedNode build(DocNode node, TreeLevel level, List<Integer> path,
                                 Map<List<Integer>, ContentProvider> providers) {
        // Content (if any) is this node's bundle, bound at this node's path.
        if (!node.content().isEmpty()) {
            // Widen List<RigidSegment> → List<Segment> for the general content seam;
            // carry the node's optional caption onto the leaf.
            final ComposedLeaf leaf = new ComposedLeaf(node.caption(), List.copyOf(node.content()));
            providers.put(path, () -> leaf);
        }

        var kids = new ArrayList<NormalizedNode>();
        List<DocNode> children = node.children();
        if (!children.isEmpty()) {
            TreeLevel childLevel = level.below().orElseThrow(() ->
                    new IllegalStateException("RigidDoc nesting exceeds the rigid tree's depth cap at " + level));
            for (int i = 0; i < children.size(); i++) {
                kids.add(build(children.get(i), childLevel, append(path, i), providers));
            }
        }
        return new NormalizedNode(level, labelDims(node.title().text()), kids);
    }

    private static List<Integer> append(List<Integer> prefix, int idx) {
        var out = new ArrayList<Integer>(prefix.size() + 1);
        out.addAll(prefix);
        out.add(idx);
        return List.copyOf(out);
    }

    private static Map<DimensionKey, DimensionValue> labelDims(String label) {
        return Map.of(DisplayLabel.INSTANCE, new NameValue(label == null ? "" : label));
    }
}
