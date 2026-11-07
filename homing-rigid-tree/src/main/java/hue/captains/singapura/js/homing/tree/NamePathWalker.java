package hue.captains.singapura.js.homing.tree;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Walks a name path down a tree, a rung at a time ({@link NamePathWalk}). */
public record NamePathWalker() implements StatelessFunctionalObject {

    public static final NamePathWalker INSTANCE = new NamePathWalker();

    /**
     * Walk {@code path} from {@code root}. An empty path names the root itself,
     * so a bare route prefix is the front door rather than a miss.
     */
    public NamePathWalk walk(NormalizedNode root, NamePath path) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(path, "path");
        return step(root, path.segments(), 0, NamePath.ROOT);
    }

    /**
     * One rung, then the rest. Recursion is safe by construction: the substrate
     * caps depth at {@code L18}, so a path cannot drive this deeper than the tree
     * is allowed to be.
     */
    private NamePathWalk step(NormalizedNode at, List<NodeName> segments,
                                     int index, NamePath matched) {
        if (index == segments.size()) {
            return new NamePathWalk.Found(matched, at.identity());
        }
        NodeName wanted = segments.get(index);

        for (NormalizedNode kid : at.children()) {
            if (kid.segment().equals(wanted)) {
                return step(kid, segments, index + 1, matched.then(wanted));
            }
        }

        if (at.children().isEmpty()) {
            var rest = new ArrayList<NodeName>(segments.subList(index, segments.size()));
            return new NamePathWalk.PastALeaf(matched, wanted, at.identity(), new NamePath(rest));
        }

        var available = new ArrayList<NodeName>(at.children().size());
        for (NormalizedNode kid : at.children()) available.add(kid.segment());
        return new NamePathWalk.NoSuchChild(matched, wanted, available);
    }
}
