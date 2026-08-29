package hue.captains.singapura.js.homing.tree;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The one canonical normalized node — the single shape every source encoding
 * maps to (via a {@link TreeNormalizer}) and the only shape the serializer and
 * renderer consume (RFC 0040).
 *
 * <p>Explicitly leveled: the node carries its {@link TreeLevel} as a runtime
 * value. Depth lives in {@link #level()} alone — never duplicated as a
 * dimension — so a graft (a pure recursive level shift, see {@link RigidTrees})
 * touches exactly one field per node. Immutable value object: dimensions are
 * order-preserving and unmodifiable; children are an immutable copy.</p>
 *
 * <p><b>A node carries two addresses, and they are different questions</b>
 * (RFC 0053, revising RFC 0040's original "no id, position is identity"):</p>
 *
 * <ul>
 *   <li>{@link #segment()} — <i>where</i>. The sibling-unique name this node is
 *       known by <b>here</b>; the chain of segments from the root is its
 *       {@link NamePath}, which for a catalogue vertex is also its URL. It is a
 *       mandatory field rather than an optional dimension precisely so a producer
 *       cannot omit it: a dimension is display, a segment is the address.</li>
 *   <li>{@link #identity()} — <i>who</i>. The link to the real content, minted
 *       from something the producing subtree already owns globally and therefore
 *       unchanged when this node is grafted elsewhere. See {@link NodeIdentity}
 *       for why it must never be positional.</li>
 * </ul>
 *
 * <p>The two are independent by construction, which is what lets a consumer
 * <i>check</i> that they agree rather than assume it — and what lets a graft be a
 * pure level shift, since neither field needs rebasing.</p>
 *
 * @since homing-rigid-tree (RFC 0040); segment and identity added by RFC 0053
 */
public record NormalizedNode(TreeLevel level,
                             NodeName segment,
                             NodeIdentity identity,
                             Map<DimensionKey, DimensionValue> dimensions,
                             List<NormalizedNode> children)
        implements TreeNode<TreeLevel> {

    public NormalizedNode {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(segment, "segment");
        Objects.requireNonNull(identity, "identity");
        dimensions = Collections.unmodifiableMap(
                new LinkedHashMap<>(dimensions == null ? Map.of() : dimensions));
        children = List.copyOf(children == null ? List.of() : children);
    }

    /** A leaf node — no children. */
    public static NormalizedNode leaf(TreeLevel level,
                                      NodeName segment,
                                      NodeIdentity identity,
                                      Map<DimensionKey, DimensionValue> dimensions) {
        return new NormalizedNode(level, segment, identity, dimensions, List.of());
    }
}
