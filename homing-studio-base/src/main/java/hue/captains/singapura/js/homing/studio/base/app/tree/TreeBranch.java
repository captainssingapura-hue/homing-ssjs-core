package hue.captains.singapura.js.homing.studio.base.app.tree;

import java.util.List;
import java.util.Objects;

/**
 * RFC 0016 — a branching node in a {@link DynamicCatalogue}. Has children
 * (mix of {@link TreeBranch} and {@link TreeLeaf}); bears no content.
 *
 * <p>Realises DocTree T5 (branches bear no content) — the metadata
 * fields are display-only; the structural role is the children list.</p>
 *
 * @param segment URL-safe slug, unique among siblings (used for path construction)
 * @param name    display heading
 * @param summary display body
 * @param badge   short uppercase badge text
 * @param icon    optional glyph
 * @param children ordered child nodes (branches + leaves)
 *
 * @since RFC 0016
 */
public record TreeBranch(
        String         segment,
        String         name,
        String         summary,
        String         badge,
        String         icon,
        List<TreeNode> children
) implements TreeNode {

    public TreeBranch {
        Objects.requireNonNull(segment,  "TreeBranch.segment");
        Objects.requireNonNull(name,     "TreeBranch.name");
        Objects.requireNonNull(children, "TreeBranch.children");
        if (name.isBlank()) {
            throw new IllegalArgumentException("TreeBranch.name must not be blank");
        }
        if (summary == null) summary = "";
        if (badge   == null) badge   = "";
        if (icon    == null) icon    = "";
        children = List.copyOf(children);
    }

    /** Convenience — branch with metadata but slug derived from name. */
    public static TreeBranch of(String name, String summary, String badge, String icon, List<TreeNode> children) {
        return new TreeBranch(slugify(name), name, summary, badge, icon, children);
    }

    static String slugify(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9-]", "-").replaceAll("-+", "-").replaceAll("^-|-$", "");
    }
}
