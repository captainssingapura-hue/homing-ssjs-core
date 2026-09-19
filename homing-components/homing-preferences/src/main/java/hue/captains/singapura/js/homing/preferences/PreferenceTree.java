package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.tree.NodeName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The preferences tree, read once from its root: levelled by depth, every
 * node addressed by its path of segments, siblings' segments distinct.
 *
 * <p>The path is the node's identity in the registry and in the view — the
 * master reports it, the detail slot keeps a widget under it. It is not the
 * preference's name: a setting names the preference it writes in its
 * widget's params, and a group has no name to write.</p>
 */
public record PreferenceTree(PreferenceNode root, Map<String, PreferenceNode> byPath) {

    public PreferenceTree {
        Objects.requireNonNull(root, "PreferenceTree.root");
        byPath = Collections.unmodifiableMap(new LinkedHashMap<>(byPath));
    }

    /** Reads and checks the tree under {@code root}, levelling it from zero. */
    public static PreferenceTree of(PreferenceNode root) {
        PreferenceNode levelled = Objects.requireNonNull(root, "root").atDepth(0);
        var byPath = new LinkedHashMap<String, PreferenceNode>();
        walk(levelled, levelled.segment().value(), byPath);
        return new PreferenceTree(levelled, byPath);
    }

    private static void walk(PreferenceNode node, String path, Map<String, PreferenceNode> byPath) {
        byPath.put(path, node);
        var seen = new java.util.HashSet<NodeName>();
        for (PreferenceNode c : node.children()) {
            if (!seen.add(c.segment())) {
                throw new IllegalArgumentException("Two children of '" + path + "' share the segment '" + c.segment() + "'");
            }
            walk(c, path + "/" + c.segment().value(), byPath);
        }
    }

    /** Every path, root first, each parent before its children. */
    public List<String> paths() { return new ArrayList<>(byPath.keySet()); }

    public PreferenceNode at(String path) {
        var n = byPath.get(path);
        if (n == null) throw new IllegalArgumentException("No preference node at '" + path + "'");
        return n;
    }

    /** The paths under {@code path}, itself excluded, in tree order. */
    public List<String> below(String path) {
        var out = new ArrayList<String>();
        String prefix = path + "/";
        for (String p : byPath.keySet()) if (p.startsWith(prefix)) out.add(p);
        return out;
    }
}
