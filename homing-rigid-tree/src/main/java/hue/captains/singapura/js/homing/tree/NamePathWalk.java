package hue.captains.singapura.js.homing.tree;

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
 * <p>It ends at an {@link NodeIdentity}, never at content. Content is the
 * {@link NodeResolver}'s job, and keeping the two apart is what lets the same walk
 * serve a catalogue, a doc tree, or a crate listing: the walk knows structure, the
 * resolver knows payloads, and neither needs the other's vocabulary.</p>
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
     * The walk stopped short.
     *
     * @param matched how far it got — a real path, and the useful half of a 404
     * @param segment the segment that matched nothing, at the level after
     *                {@code matched}
     * @param reason  which kind of dead end
     */
    record Missing(NamePath matched, NodeName segment, Reason reason) implements NamePathWalk {
        public Missing {
            Objects.requireNonNull(matched, "Missing.matched");
            Objects.requireNonNull(segment, "Missing.segment");
            Objects.requireNonNull(reason,  "Missing.reason");
        }

        /** The depth at which the walk stopped — how many segments did match. */
        public int depth() { return matched.depth(); }
    }

    /**
     * Why a walk stopped. Both are "no child matched", and telling them apart is
     * worth the enum because they make materially different answers: one says a
     * branch has no such child, the other says the path ran past a leaf.
     */
    enum Reason {
        /** The vertex has children, none named by the segment. */
        NO_SUCH_CHILD,
        /** The vertex is a leaf — nothing lives below it at all. */
        PAST_A_LEAF
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
        return new Missing(matched, wanted,
                at.children().isEmpty() ? Reason.PAST_A_LEAF : Reason.NO_SUCH_CHILD);
    }
}
