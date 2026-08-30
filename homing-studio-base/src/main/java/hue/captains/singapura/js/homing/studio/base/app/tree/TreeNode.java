package hue.captains.singapura.js.homing.studio.base.app.tree;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * RFC 0016 — sealed sum for the two node kinds in a {@link DynamicCatalogue}.
 * Realises the DocTree T3 ontology axiom (leaf-or-branch closure): every
 * node is either a {@link TreeBranch} (has children, bears no content)
 * or a {@link TreeLeaf} (bears content, has no children). No node is
 * both; no node is neither.
 *
 * @since RFC 0016
 */
public sealed interface TreeNode extends ValueObject permits TreeBranch, TreeLeaf {

    /** URL-safe slug — unique among siblings. Used to construct the node's path. */
    String segment();

    /** Display heading on tiles / breadcrumbs. */
    String name();

    /** Display body shown beneath the heading. */
    String summary();

    /** Optional badge label (e.g. "ANIMAL", "HALLOWEEN"). */
    String badge();

    /** Optional glyph prefixed into breadcrumb crumbs. */
    String icon();
}
