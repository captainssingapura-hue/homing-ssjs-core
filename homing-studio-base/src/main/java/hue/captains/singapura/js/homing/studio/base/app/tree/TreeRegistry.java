package hue.captains.singapura.js.homing.studio.base.app.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RFC 0016 — boot-time registry of {@link DynamicCatalogue}s. Indexed by
 * tree id; resolves a {@code (treeId, path)} request to the addressed
 * {@link TreeNode}.
 *
 * <p>Boot-time validations:</p>
 * <ol>
 *   <li>Tree {@code id} uniqueness across all registered trees.</li>
 *   <li>Slug uniqueness among siblings within each branch.</li>
 *   <li>Max depth bound (16) — sanity cap for imported / generated trees.</li>
 *   <li>Branch / leaf invariants enforced at the record level (compact constructors).</li>
 * </ol>
 *
 * <p>Phase 1 of RFC 0016 — static trees only. Dynamic trees via
 * {@code TreeProvider} land in a follow-up phase.</p>
 *
 * @since RFC 0016
 */
public final class TreeRegistry {

    /** Maximum tree depth (root counts as 0). Sanity cap from RFC 0016. */
    public static final int MAX_DEPTH = 16;

    private final Map<String, DynamicCatalogue> byId;

    public TreeRegistry(Collection<? extends DynamicCatalogue> trees) {
        var byId = new LinkedHashMap<String, DynamicCatalogue>();
        for (DynamicCatalogue tree : trees) {
            if (tree == null) {
                throw new IllegalStateException("TreeRegistry: null DynamicCatalogue in input");
            }
            if (byId.containsKey(tree.id())) {
                throw new IllegalStateException(
                        "TreeRegistry: duplicate tree id \"" + tree.id() + "\"");
            }
            validateBranch(tree.id(), tree.root(), 0);
            byId.put(tree.id(), tree);
        }
        this.byId = Map.copyOf(byId);
    }

    private static void validateBranch(String treeId, TreeBranch branch, int depth) {
        if (depth > MAX_DEPTH) {
            throw new IllegalStateException(
                    "TreeRegistry: tree \"" + treeId + "\" exceeds max depth " + MAX_DEPTH);
        }
        var slugs = new java.util.HashSet<String>();
        for (TreeNode child : branch.children()) {
            if (!slugs.add(child.segment())) {
                throw new IllegalStateException(
                        "TreeRegistry: duplicate sibling segment \"" + child.segment()
                      + "\" in tree \"" + treeId + "\" branch \"" + branch.segment() + "\"");
            }
            if (child instanceof TreeBranch sub) {
                validateBranch(treeId, sub, depth + 1);
            }
        }
    }

    /** Resolve a registered DynamicCatalogue by id, or null if absent. */
    public DynamicCatalogue resolve(String id) {
        return byId.get(id);
    }

    /**
     * Walk the tree's root → branch chain by path (slash-separated segments).
     * Returns the addressed node or null if the path doesn't resolve.
     *
     * <p>Empty / null path resolves to the tree's root branch.</p>
     */
    public TreeNode resolvePath(String treeId, String path) {
        DynamicCatalogue tree = byId.get(treeId);
        if (tree == null) return null;
        if (path == null || path.isBlank()) return tree.root();

        TreeNode cursor = tree.root();
        for (String segment : path.split("/")) {
            if (segment.isBlank()) continue;
            if (!(cursor instanceof TreeBranch branch)) return null;  // leaves can't have children
            cursor = branch.children().stream()
                    .filter(c -> c.segment().equals(segment))
                    .findFirst()
                    .orElse(null);
            if (cursor == null) return null;
        }
        return cursor;
    }

    /**
     * Build the breadcrumb chain (root → ... → addressed node) for a given path.
     * Returns the chain of all branches encountered plus the final node.
     */
    public List<TreeNode> breadcrumbs(String treeId, String path) {
        DynamicCatalogue tree = byId.get(treeId);
        if (tree == null) return List.of();
        var chain = new ArrayList<TreeNode>();
        chain.add(tree.root());
        if (path == null || path.isBlank()) return chain;

        TreeNode cursor = tree.root();
        for (String segment : path.split("/")) {
            if (segment.isBlank()) continue;
            if (!(cursor instanceof TreeBranch branch)) break;
            TreeNode next = branch.children().stream()
                    .filter(c -> c.segment().equals(segment))
                    .findFirst()
                    .orElse(null);
            if (next == null) break;
            chain.add(next);
            cursor = next;
        }
        return List.copyOf(chain);
    }

    public Collection<DynamicCatalogue> all() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public int size() { return byId.size(); }
}
