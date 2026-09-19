package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.tree.DimensionKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.TreeNode;
import hue.captains.singapura.js.homing.tree.dims.NameValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A node of the preferences tree: a rigid {@link TreeNode} — level, segment,
 * a display label as its one dimension, children — with a
 * {@link WidgetProvider} attached. A group and a setting are the same node;
 * what distinguishes them is only which widget shows them, and that the
 * setting's widget names a preference in its params.
 *
 * <p>Built without levels: {@link #of} takes children and the tree assigns
 * levels by depth when it is read ({@link PreferenceTree#of}), so a subtree
 * declared on its own — a component's contribution — can be placed anywhere
 * and takes the level of where it lands.</p>
 *
 * @param level    the rigid level, assigned by the tree
 * @param segment  the path segment under the parent
 * @param label    what the master shows
 * @param summary  one line, or empty
 * @param provider the widget that shows this node
 * @param children in display order
 */
public record PreferenceNode(TreeLevel level, NodeName segment, String label, String summary,
                             WidgetProvider provider, List<PreferenceNode> children)
        implements TreeNode<TreeLevel> {

    public PreferenceNode {
        Objects.requireNonNull(level, "PreferenceNode.level");
        Objects.requireNonNull(segment, "PreferenceNode.segment");
        Objects.requireNonNull(provider, "PreferenceNode.provider");
        if (label == null || label.isBlank()) throw new IllegalArgumentException("PreferenceNode.label must not be blank");
        if (summary == null) summary = "";
        children = List.copyOf(children == null ? List.of() : children);
    }

    /** A node at no level yet; the tree levels it by depth. */
    public static PreferenceNode of(String segment, String label, String summary, WidgetProvider provider,
                                    PreferenceNode... children) {
        return new PreferenceNode(TreeLevel.L0.INSTANCE, NodeName.slug(segment), label, summary, provider, List.of(children));
    }

    @Override
    public Map<DimensionKey, DimensionValue> dimensions() {
        return Map.of(DisplayLabel.INSTANCE, new NameValue(label));
    }

    /** This subtree re-levelled so that this node is at {@code depth}. */
    PreferenceNode atDepth(int depth) {
        var kids = new ArrayList<PreferenceNode>();
        for (PreferenceNode c : children) kids.add(c.atDepth(depth + 1));
        return new PreferenceNode(TreeLevel.atDepth(depth), segment, label, summary, provider, kids);
    }
}
