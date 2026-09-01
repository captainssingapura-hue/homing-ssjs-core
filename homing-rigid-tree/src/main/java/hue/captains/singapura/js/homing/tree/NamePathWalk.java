package hue.captains.singapura.js.homing.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Walking a {@link NamePath} down a tree to the vertex it names — the
 * {@code Path -> Node} half of RFC 0053's sequence.
 *
 * <p>The whole algorithm is the obvious one, and that is the point: split the
 * path into segments, then advance one level per segment. Nothing is unwrapped
 * mid-walk and no node kind is inspected, because a vertex is a vertex — a graft
 * was already resolved when the tree was built, so there is no portal left to
 * step through at resolve time.</p>
 *
 * <p>It ends at a {@link NodeIdentity}, never at content. Content is the
 * {@link NodeResolver}'s job, and keeping the two apart is what lets one walk
 * serve a catalogue, a doc tree, or a crate listing: the walk knows structure,
 * the resolver knows payloads, and neither needs the other's vocabulary.</p>
 *
 * <h2>Each way of failing is its own type</h2>
 *
 * <p>A single {@code Missing} record with a reason tag would force every failure
 * to carry the same fields, so it could only ever carry what they have in common —
 * which is why the enum this replaced could say a path ran past a leaf but not
 * <i>which</i> leaf, and could say no child matched but not what the children
 * actually were. Giving each case its own constructor lets it hold exactly the
 * evidence it has:</p>
 *
 * <ul>
 *   <li>{@link NoSuchChild} carries the segments that <b>do</b> exist there, so a
 *       caller can say "try one of these" rather than only "not found".</li>
 *   <li>{@link PastALeaf} carries the leaf's own identity and path, which is what
 *       makes redirecting to the leaf implementable — a stale deep link with
 *       something appended is usually worth answering, not 404ing.</li>
 * </ul>
 *
 * <p>The grouping stays useful: switch on {@link Missing} to handle any failure
 * uniformly, or on the leaves to use what each one knows.</p>
 *
 * <p>The root's own segment is <b>not</b> part of any name-path — the empty path
 * names the root itself — which matches how a catalogue path is spelled and makes
 * a catalogue vertex's name-path exactly its URL after the route prefix.</p>
 *
 * @since RFC 0053
 */
public sealed interface NamePathWalk {

    /** The path named a vertex; {@code identity} is what to resolve content by. */
    record Found(NamePath path, NodeIdentity identity) implements NamePathWalk {
        public Found {
            Objects.requireNonNull(path,     "Found.path");
            Objects.requireNonNull(identity, "Found.identity");
        }
    }

    /**
     * The walk stopped short. Every failure knows how far it got — the partial
     * path is the useful half of an answer — and each kind knows more besides.
     */
    sealed interface Missing extends NamePathWalk {

        /** How far the walk got before stopping. A real path, and resolvable. */
        NamePath matched();

        /** The segment that could not be followed. */
        NodeName wanted();

        /** The depth at which the walk stopped — how many segments did match. */
        default int depth() { return matched().depth(); }
    }

    /**
     * The vertex reached has children, and none of them is named {@code wanted}.
     * Usually a typo or a rename.
     *
     * @param matched   the path that did resolve
     * @param wanted    the segment that matched nothing
     * @param available the segments that are there — what to suggest instead
     */
    record NoSuchChild(NamePath matched, NodeName wanted, List<NodeName> available)
            implements Missing {
        public NoSuchChild {
            Objects.requireNonNull(matched, "NoSuchChild.matched");
            Objects.requireNonNull(wanted,  "NoSuchChild.wanted");
            available = List.copyOf(available);
        }
    }

    /**
     * The path ran past a leaf: the vertex reached has no children at all, yet
     * segments remained. Usually a real address with something appended — a stale
     * deep link rather than a typo — which is why the leaf itself travels here.
     *
     * @param matched   the leaf's own path; where a caller would redirect
     * @param wanted    the first segment beyond the leaf
     * @param leaf      the leaf's identity, so its content can still be resolved
     * @param remaining everything beyond the leaf, unconsumed
     */
    record PastALeaf(NamePath matched, NodeName wanted, NodeIdentity leaf, NamePath remaining)
            implements Missing {
        public PastALeaf {
            Objects.requireNonNull(matched,   "PastALeaf.matched");
            Objects.requireNonNull(wanted,    "PastALeaf.wanted");
            Objects.requireNonNull(leaf,      "PastALeaf.leaf");
            Objects.requireNonNull(remaining, "PastALeaf.remaining");
        }
    }

    /**
     * Walk {@code path} from {@code root}. An empty path names the root itself,
     * so a bare route prefix is the front door rather than a miss.
     */
    static NamePathWalk from(NormalizedNode root, NamePath path) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(path, "path");
        return step(root, path.segments(), 0, NamePath.ROOT);
    }

    /**
     * One rung, then the rest. Recursion is safe by construction: the substrate
     * caps depth at {@code L18}, so a path cannot drive this deeper than the tree
     * is allowed to be.
     */
    private static NamePathWalk step(NormalizedNode at, List<NodeName> segments,
                                     int index, NamePath matched) {
        if (index == segments.size()) {
            return new Found(matched, at.identity());
        }
        NodeName wanted = segments.get(index);

        for (NormalizedNode kid : at.children()) {
            if (kid.segment().equals(wanted)) {
                return step(kid, segments, index + 1, matched.then(wanted));
            }
        }

        if (at.children().isEmpty()) {
            var rest = new ArrayList<NodeName>(segments.subList(index, segments.size()));
            return new PastALeaf(matched, wanted, at.identity(), new NamePath(rest));
        }

        var available = new ArrayList<NodeName>(at.children().size());
        for (NormalizedNode kid : at.children()) available.add(kid.segment());
        return new NoSuchChild(matched, wanted, available);
    }
}
