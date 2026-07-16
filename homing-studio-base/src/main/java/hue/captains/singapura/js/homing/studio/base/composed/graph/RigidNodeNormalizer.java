package hue.captains.singapura.js.homing.studio.base.composed.graph;

import hue.captains.singapura.js.homing.studio.base.composed.ComposedLeaf;
import hue.captains.singapura.js.homing.studio.base.composed.ContentProvider;
import hue.captains.singapura.js.homing.studio.base.composed.DocTreeV2;
import hue.captains.singapura.js.homing.studio.base.composed.RigidNodeContent;
import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;
import hue.captains.singapura.js.homing.tree.DimensionKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.NodeKey;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.dims.NameValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * The "bring your own tree" adapter (RFC 0039, name-path variant): assembles a flat
 * list of parent-pointer {@link RigidNode}s into a {@link DocTreeV2} — one structure
 * tree plus a name-path-keyed content seam.
 *
 * <p>Because {@link RigidNode} already makes cycles, multiple parents, and wrong
 * levels <b>unrepresentable</b> (see its doc), validation here is minimal — the
 * heavy checks aren't needed:</p>
 * <ul>
 *   <li><b>exactly one root</b> — one node with a {@code null} parent (equivalently,
 *       one level-0 node); zero means every node has a parent, more than one is a
 *       forest;</li>
 *   <li><b>every node reachable</b> — the single build-descent from the root must
 *       visit every provided node; a leftover means its parent chain leaves the
 *       provided set (incomplete input);</li>
 *   <li><b>unique sibling names</b> — so a name-path is unambiguous.</li>
 * </ul>
 *
 * <p>The descent that validates is the same one that builds the output — one pass.
 * Two of its guards (visited-once, stamped-level == depth) are cheap assertions of
 * invariants {@code RigidNode} already guarantees; they cost nothing and catch a
 * framework bug rather than a caller one.</p>
 *
 * <p>Functional Object: stateless, one {@code INSTANCE}.</p>
 *
 * @since homing-studio-base — RFC 0039 name-path doc (RigidDocV2)
 */
public final class RigidNodeNormalizer {

    public static final RigidNodeNormalizer INSTANCE = new RigidNodeNormalizer();

    private RigidNodeNormalizer() {}

    /** Explicit {@code order} ascending; nodes without one sort last, alphabetically by name. */
    private static final Comparator<RigidNode<?>> SIBLING_ORDER =
            Comparator.<RigidNode<?>>comparingInt(
                            n -> n.order().isPresent() ? n.order().getAsInt() : Integer.MAX_VALUE)
                    .thenComparing(n -> n.name().value());

    /**
     * Assemble + validate the nodes into a name-path {@link DocTreeV2}. The single
     * {@code content} function resolves each node's body from its {@code source} —
     * one provider for the whole tree, applied as the tree is built.
     */
    public <T> DocTreeV2 toDocTree(List<RigidNode<T>> nodes, Function<T, RigidNodeContent> content) {
        Objects.requireNonNull(nodes, "nodes");
        Objects.requireNonNull(content, "content provider");
        if (nodes.isEmpty()) throw new MalformedTreeException("no nodes provided");

        // Invert the upward pointers into children-by-parent (by identity), and
        // pick out the roots. Nodes are identity-distinguished throughout.
        IdentityHashMap<RigidNode<T>, List<RigidNode<T>>> childrenOf = new IdentityHashMap<>();
        Set<RigidNode<T>> provided = Collections.newSetFromMap(new IdentityHashMap<>());
        List<RigidNode<T>> roots = new ArrayList<>();
        for (RigidNode<T> n : nodes) {
            if (!provided.add(n)) {
                throw new MalformedTreeException("node listed twice: " + n.name().value());
            }
            if (n.parent() == null) roots.add(n);
            else childrenOf.computeIfAbsent(n.parent(), k -> new ArrayList<>()).add(n);
        }
        if (roots.size() != 1) {
            throw new MalformedTreeException(roots.isEmpty()
                    ? "no root: every node has a parent"
                    : "multiple roots (expected exactly one): " + roots.size());
        }

        // The one descent: builds structure + content and validates as it goes.
        Map<String, ContentProvider> providers = new LinkedHashMap<>();
        Set<RigidNode<T>> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        NormalizedNode structure = build(roots.get(0), "", 0, childrenOf, providers, visited, content);

        if (visited.size() != provided.size()) {
            throw new MalformedTreeException(
                    "nodes unreachable from the root (a parent outside the provided set): "
                    + (provided.size() - visited.size()));
        }
        return new DocTreeV2(structure, providers);
    }

    private <T> NormalizedNode build(RigidNode<T> node, String pathKey, int depth,
            IdentityHashMap<RigidNode<T>, List<RigidNode<T>>> childrenOf,
            Map<String, ContentProvider> providers, Set<RigidNode<T>> visited,
            Function<T, RigidNodeContent> content) {
        // Cheap assertions of RigidNode's own guarantees (single-parent, pinned level).
        if (!visited.add(node)) {
            throw new MalformedTreeException("revisited node (unexpected): " + node.name().value());
        }
        if (node.level() != depth) {
            throw new MalformedTreeException("node '" + node.name().value()
                    + "' stamped level " + node.level() + " but sits at depth " + depth);
        }

        // Content (if any) resolved from this node's source by the one provider.
        RigidNodeContent c = Objects.requireNonNull(content.apply(node.source()),
                () -> "content provider returned null for node " + node.name().value());
        if (!c.segments().isEmpty()) {
            ComposedLeaf leaf = new ComposedLeaf(c.caption(), List.copyOf(c.segments()));
            providers.put(pathKey, () -> leaf);
        }

        // Children: ordered, and sibling-name-unique (the name-path must be unambiguous).
        List<RigidNode<T>> kids = new ArrayList<>(childrenOf.getOrDefault(node, List.of()));
        kids.sort(SIBLING_ORDER);
        Set<String> seenNames = new HashSet<>();
        List<NormalizedNode> kidNodes = new ArrayList<>();
        for (RigidNode<T> kid : kids) {
            String kn = kid.name().value();
            if (!seenNames.add(kn)) {
                throw new MalformedTreeException("duplicate sibling name '" + kn
                        + "' under '" + node.name().value() + "'");
            }
            String childKey = pathKey.isEmpty() ? kn : pathKey + NodeName.SEPARATOR + kn;
            kidNodes.add(build(kid, childKey, depth + 1, childrenOf, providers, visited, content));
        }

        TreeLevel level;
        try {
            level = TreeLevel.atDepth(node.level());
        } catch (IllegalArgumentException ex) {
            throw new MalformedTreeException("depth exceeds the rigid cap at '"
                    + node.name().value() + "': " + ex.getMessage());
        }
        return new NormalizedNode(level, labelDims(node), kidNodes);
    }

    /** The two per-node dimensions: the human heading, then the machine identity. */
    private static Map<DimensionKey, DimensionValue> labelDims(RigidNode<?> node) {
        var dims = new LinkedHashMap<DimensionKey, DimensionValue>();
        dims.put(DisplayLabel.INSTANCE, new NameValue(node.title().text()));
        dims.put(NodeKey.INSTANCE, new NameValue(node.name().value()));
        return dims;
    }
}
