package hue.captains.singapura.js.homing.tree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The disjointness check {@link NodeResolver#union(List)} rests on (RFC 0053).
 *
 * <p>A union asks its parts in order and takes the first answer, which is a
 * well-defined <i>function</i> only when no two parts claim the same identity.
 * That precondition is not bookkeeping: two vertices carrying one identity is
 * precisely RFC 0051 Law 1's "a navigable at two positions", so a duplicate found
 * here is a law violation rather than a merge to reconcile.</p>
 *
 * <p>Which is the payoff the design was argued for — the law stops being a
 * separate pass over a separate structure and becomes a property of the tree
 * itself, checkable by one walk of the thing that was just built.</p>
 *
 * <p>Generic over any {@link NormalizedNode}: it reads identity and children and
 * nothing else, so it holds for a catalogue forest, a doc tree, or a crate
 * listing alike.</p>
 *
 * <p>Functional Object: stateless, static entry points.</p>
 *
 * @since RFC 0053
 */
public final class NodeIdentities {

    private NodeIdentities() {}

    /** Every vertex's identity, in pre-order — duplicates included. */
    public static List<NodeIdentity> allIn(NormalizedNode root) {
        var out = new ArrayList<NodeIdentity>();
        collect(root, out);
        return List.copyOf(out);
    }

    /**
     * Identities carried by more than one vertex, mapped to how many vertices
     * carry them, in first-seen order. Empty is the conforming answer.
     */
    public static Map<NodeIdentity, Integer> duplicatesIn(NormalizedNode root) {
        var counts = new LinkedHashMap<NodeIdentity, Integer>();
        for (NodeIdentity id : allIn(root)) {
            counts.merge(id, 1, Integer::sum);
        }
        var dupes = new LinkedHashMap<NodeIdentity, Integer>();
        for (var e : counts.entrySet()) {
            if (e.getValue() > 1) dupes.put(e.getKey(), e.getValue());
        }
        return Map.copyOf(dupes);
    }

    /** True when every vertex in the tree carries a distinct identity. */
    public static boolean disjoint(NormalizedNode root) {
        return duplicatesIn(root).isEmpty();
    }

    private static void collect(NormalizedNode node, List<NodeIdentity> out) {
        out.add(node.identity());
        for (NormalizedNode kid : node.children()) collect(kid, out);
    }
}
