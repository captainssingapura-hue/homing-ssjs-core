package hue.captains.singapura.js.homing.studio.base.app.tree;

import hue.captains.singapura.js.homing.studio.base.Doc;

import java.util.Objects;

/**
 * RFC 0016 — a leaf node in a {@link DynamicCatalogue}. Wraps a {@link Doc}
 * instance directly; the Doc supplies its own viewer URL, content kind,
 * title, summary, references — all via the unified Doc polymorphism (RFC
 * 0015). The tree position adds structure; the Doc supplies content.
 *
 * <p>This is the structural mirror of {@code Entry.OfDoc} on the
 * Catalogue side: both wrap a typed Doc, both render as a standard
 * catalogue Card, both route to the Doc's registered viewer on click.
 * The tree's contribution is the path-based addressing and the
 * hierarchical context; the Doc is the same kind of artefact catalogue
 * leaves carry.</p>
 *
 * <p>The tree-leaf metadata fields ({@code name}, {@code summary},
 * {@code badge}, {@code icon}) override the wrapped Doc's display data
 * when the same Doc appears in multiple contexts with different framings.
 * Empty values fall through to the Doc's own metadata at serialization
 * time (see {@code TreeGetAction.serialize}).</p>
 *
 * <p>Realises DocTree T3 (leaf-or-branch closure) and T4 (leaf-kind
 * closure — every leaf is a content leaf bearing a Doc; the future
 * structural-leaf carve-out for cross-tree references is a separate
 * sealed extension).</p>
 *
 * @param segment URL-safe slug, unique among siblings
 * @param name    display heading override; empty → use doc.title()
 * @param summary display body override; empty → use doc.summary()
 * @param badge   badge override; empty → use doc.category()
 * @param icon    optional glyph
 * @param doc     the wrapped Doc (any subtype — ProseDoc, PlanDoc, AppDoc, SvgDoc, …)
 *
 * @since RFC 0016
 */
public record TreeLeaf(
        String segment,
        String name,
        String summary,
        String badge,
        String icon,
        Doc    doc
) implements TreeNode {

    public TreeLeaf {
        Objects.requireNonNull(segment, "TreeLeaf.segment");
        Objects.requireNonNull(name,    "TreeLeaf.name");
        Objects.requireNonNull(doc,     "TreeLeaf.doc");
        if (name.isBlank()) {
            throw new IllegalArgumentException("TreeLeaf.name must not be blank");
        }
        if (summary == null) summary = "";
        if (badge   == null) badge   = "";
        if (icon    == null) icon    = "";
    }

    /** Convenience — leaf with slug derived from name; no badge/icon override. */
    public static TreeLeaf of(String name, String summary, Doc doc) {
        return new TreeLeaf(TreeBranch.slugify(name), name, summary, "", "", doc);
    }
}
