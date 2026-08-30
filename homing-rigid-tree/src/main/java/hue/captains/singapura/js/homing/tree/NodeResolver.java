package hue.captains.singapura.js.homing.tree;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Identity to answer — the second half of RFC 0053's sequence
 * ({@code Path -> Node -> Identity -> …}), and the thing subtrees bring along
 * with their vertices.
 *
 * <p>A tree is assembled from heterogeneous subtrees, each of which knows how to
 * answer for its own identity kind and nothing else. Grafting merges them by
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
 * <p>So the union does not detect collisions, and should not: a collision is not
 * a merge failure to be resolved but a law violation to be reported, and it is
 * reported where the law is checked ({@link NodeIdentities#duplicatesIn}). What
 * follows is that the union is <b>commutative</b> over a conforming forest — if
 * merge order ever changes an answer, the disjointness contract has already been
 * broken, and that makes a fine test.</p>
 *
 * <h2>Open on the kind, closed on the answer</h2>
 *
 * <p>{@code D} is the answer type, and it is a parameter rather than {@code
 * Object} because every subtree answers in the <i>same</i> shape even though each
 * brings its own identity kind. The substrate cannot name that shape — display
 * belongs to the consumer, not here — so it does not try; but neither does it
 * force a cast on the access path, because {@code D} is uniform across the union.
 * That is the difference from CONTENT, which is genuinely heterogeneous: a
 * parameter works precisely because the answer is not.</p>
 *
 * <p>In {@code studio-base} that answer is {@code Details} — an illustration,
 * plus a navigable when the vertex is a leaf. The substrate stays free of it.</p>
 *
 * <p>An eager identity-keyed map is a perfectly good <i>representation</i> of the
 * same union — it makes lookup constant-time and collisions cheap to find in one
 * pass. It is an operational optimisation, not the design, and nothing here
 * mandates either shape.</p>
 *
 * @param <D> what a resolver answers with; uniform across a union
 * @since RFC 0053
 */
@FunctionalInterface
public interface NodeResolver<D> {

    /**
     * The answer for {@code identity}, or empty when this resolver does not own
     * that identity. Empty means "not mine", never "mine but absent" — a resolver
     * that owns an identity and has nothing for it is a bug in that subtree, not
     * a miss to be passed along.
     */
    Optional<D> resolve(NodeIdentity identity);

    /** Owns nothing. The identity element of {@link #union(List)}. */
    static <D> NodeResolver<D> none() {
        return identity -> Optional.empty();
    }

    /** This resolver, then {@code next} — first match wins. */
    default NodeResolver<D> or(NodeResolver<D> next) {
        java.util.Objects.requireNonNull(next, "next");
        return identity -> {
            Optional<D> mine = resolve(identity);
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
    static <D> NodeResolver<D> union(List<? extends NodeResolver<D>> parts) {
        java.util.Objects.requireNonNull(parts, "parts");
        List<? extends NodeResolver<D>> copy = List.copyOf(parts);
        return identity -> {
            for (NodeResolver<D> part : copy) {
                Optional<D> hit = part.resolve(identity);
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
    static <I extends NodeIdentity, D> NodeResolver<D> forKind(Class<I> kind, Function<I, D> lookup) {
        java.util.Objects.requireNonNull(kind, "kind");
        java.util.Objects.requireNonNull(lookup, "lookup");
        return identity -> kind.isInstance(identity)
                ? Optional.ofNullable(lookup.apply(kind.cast(identity)))
                : Optional.empty();
    }
}
