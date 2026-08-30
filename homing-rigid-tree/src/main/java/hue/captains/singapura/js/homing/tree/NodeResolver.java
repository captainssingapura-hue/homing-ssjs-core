package hue.captains.singapura.js.homing.tree;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Identity to content — the second half of RFC 0053's sequence
 * ({@code Path -> Node -> Identity -> Content}), and the thing subtrees bring
 * along with their vertices.
 *
 * <p>A tree is assembled from heterogeneous subtrees, each of which knows how to
 * resolve its own identity kind and nothing else. Grafting merges them by
 * {@link #union(List) union}: ask each in turn, take the first that answers. That
 * is all a union is, and it is deliberately that simple — a union is itself a
 * {@code NodeResolver}, so unions nest and a graft of a graft needs no special
 * case.</p>
 *
 * <h2>Why first-match is enough, and what it assumes</h2>
 *
 * <p>First-match is only <b>order-independent</b> when the resolvers' domains are
 * disjoint. That is not a caveat to work around; it is the contract
 * {@link NodeIdentity} already demands, because an identity is minted from
 * something its producing subtree owns globally. Two resolvers answering the same
 * identity means two vertices claiming one identity, which is exactly RFC 0051
 * Law 1's "a navigable at two positions".</p>
 *
 * <p>So the union does not detect collisions, and should not: a collision is not a
 * merge failure to be resolved but a law violation to be reported, and it is
 * reported where the law is checked. What follows is that the union is
 * <b>commutative</b> over a conforming forest — if merge order ever changes an
 * answer, the disjointness contract has already been broken, and that makes a fine
 * test.</p>
 *
 * <h2>Type safety sits at two levels</h2>
 *
 * <p>The generic level is necessarily untyped in the content: the substrate cannot
 * name the kinds its consumers will bring, and threading a content type parameter
 * through would put wildcards and capture helpers on the uniform access path —
 * defeating the uniform access that is the whole point. Each subtree secures its
 * own safety by its own means and the union upcasts at the boundary;
 * {@link #forKind(Class, Function)} is that boundary written once, so a subtree
 * author writes a typed lambda and never casts.</p>
 *
 * <p>An eager identity-keyed map is a perfectly good <i>representation</i> of the
 * same union — it makes lookup constant-time and collisions cheap to find in one
 * pass. It is an operational optimisation, not the design, and nothing here
 * mandates either shape.</p>
 *
 * @since RFC 0053
 */
@FunctionalInterface
public interface NodeResolver {

    /**
     * The content behind {@code identity}, or empty when this resolver does not
     * own that identity. Empty means "not mine", never "mine but absent" — a
     * resolver that owns an identity and has nothing for it is a bug in that
     * subtree, not a miss to be passed along.
     */
    Optional<Object> resolve(NodeIdentity identity);

    /** Owns nothing. The identity element of {@link #union(List)}. */
    NodeResolver NONE = identity -> Optional.empty();

    /** This resolver, then {@code next} — first match wins. */
    default NodeResolver or(NodeResolver next) {
        java.util.Objects.requireNonNull(next, "next");
        return identity -> {
            Optional<Object> mine = resolve(identity);
            return mine.isPresent() ? mine : next.resolve(identity);
        };
    }

    /**
     * The union of many resolvers — asked in order, first match wins.
     *
     * <p>The result is itself a {@code NodeResolver}, which is what lets grafts
     * compose to any depth: a subtree hands up one resolver whether it is a leaf
     * subtree or a union of a dozen.</p>
     */
    static NodeResolver union(List<? extends NodeResolver> parts) {
        java.util.Objects.requireNonNull(parts, "parts");
        List<NodeResolver> copy = List.copyOf(parts);
        return identity -> {
            for (NodeResolver part : copy) {
                Optional<Object> hit = part.resolve(identity);
                if (hit.isPresent()) return hit;
            }
            return Optional.empty();
        };
    }

    /**
     * A resolver for one identity kind, typed at the lambda and erased at the
     * boundary — the two-level safety made concrete.
     *
     * <p>{@code forKind(NavKey.class, key -> index.get(key))} gives the author a
     * {@code NavKey} in hand and no cast anywhere; the union sees only a
     * {@code NodeResolver}. A {@code null} from the lookup is a miss.</p>
     */
    static <I extends NodeIdentity> NodeResolver forKind(Class<I> kind, Function<I, Object> lookup) {
        java.util.Objects.requireNonNull(kind, "kind");
        java.util.Objects.requireNonNull(lookup, "lookup");
        return identity -> kind.isInstance(identity)
                ? Optional.ofNullable(lookup.apply(kind.cast(identity)))
                : Optional.empty();
    }
}
