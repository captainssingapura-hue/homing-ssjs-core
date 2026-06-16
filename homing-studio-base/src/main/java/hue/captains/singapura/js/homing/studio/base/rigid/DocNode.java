package hue.captains.singapura.js.homing.studio.base.rigid;

import hue.captains.singapura.js.homing.studio.base.composed.Segment;

import java.util.List;
import java.util.Objects;

/**
 * RFC 0042 — one node of a {@link RigidDoc}'s structure tree. A node is always
 * a titled section (structure); it carries its own ordered {@code content}
 * (a {@link Segment} bundle rendered as a lead-in / the leaf body) and its
 * ordered child nodes.
 *
 * <p>The structure/content split of RFC 0041 holds here: the {@code title} is
 * structure (it heads the section, appears in the TOC); the {@code content}
 * segments are content (rendered in the node's body, never tree nodes). A node
 * with no children is a <i>leaf</i>; a node with no content is a pure
 * <i>branch</i>; a node with both is a branch with a lead-in — the foldable
 * view (RFC 0039) renders the lead-in with the heading and folds the children.</p>
 *
 * <p>Built only through the leveled builder ({@link Rigid}); this record is the
 * value the builder accumulates and the normalizer walks.</p>
 *
 * @param title    the section heading (structure) — never blank
 * @param content  the node's segment bundle (content), in order; may be empty
 * @param children the node's child sections, in order; may be empty
 * @since homing-studio-base — RFC 0042 leveled tree-builder
 */
public record DocNode(String title, List<Segment> content, List<DocNode> children) {

    public DocNode {
        Objects.requireNonNull(title,    "DocNode.title");
        Objects.requireNonNull(content,  "DocNode.content");
        Objects.requireNonNull(children, "DocNode.children");
        if (title.isBlank()) {
            throw new IllegalArgumentException("DocNode.title must not be blank — every node is a heading");
        }
        content  = List.copyOf(content);
        children = List.copyOf(children);
    }

    /** A leaf is a node with no child sections. */
    public boolean isLeaf() { return children.isEmpty(); }
}
