package hue.captains.singapura.js.homing.studio.base.rigid;

import hue.captains.singapura.js.homing.studio.base.composed.RigidSegment;
import hue.captains.singapura.js.homing.studio.base.composed.text.Line;
import hue.captains.singapura.js.homing.studio.base.composed.text.Title;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0042 — one node of a {@link RigidDoc}'s structure tree. A node is always
 * a titled section (structure); it carries its own ordered {@code content}
 * (a {@link RigidSegment} bundle rendered as a lead-in / the leaf body) and its
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
 * @param title    the section heading (structure) — a typed {@link Title} (≤66 chars)
 * @param caption  optional highlighted header rendered above this node's body
 *                 (one plain line); {@code empty} for none. Distinct from
 *                 {@code title}: the title is the structural heading (in the TOC),
 *                 the caption is a content-level header. The leveled DSL leaves it
 *                 empty; a content provider may set it.
 * @param content  the node's segment bundle (content), in order; may be empty
 * @param children the node's child sections, in order; may be empty
 * @since homing-studio-base — RFC 0042 leveled tree-builder
 */
public record DocNode(Title title, Optional<Line.Plain> caption,
                      List<RigidSegment> content, List<DocNode> children) {

    public DocNode {
        Objects.requireNonNull(title,    "DocNode.title");
        Objects.requireNonNull(caption,  "DocNode.caption (use Optional.empty)");
        Objects.requireNonNull(content,  "DocNode.content");
        Objects.requireNonNull(children, "DocNode.children");
        content  = List.copyOf(content);
        children = List.copyOf(children);
    }

    /** Convenience — no caption (the leveled DSL and any caption-less caller). */
    public DocNode(Title title, List<RigidSegment> content, List<DocNode> children) {
        this(title, Optional.empty(), content, children);
    }

    /** A leaf is a node with no child sections. */
    public boolean isLeaf() { return children.isEmpty(); }
}
