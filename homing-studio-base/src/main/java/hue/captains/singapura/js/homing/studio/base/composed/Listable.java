package hue.captains.singapura.js.homing.studio.base.composed;

/**
 * The subset of {@link RigidSegment}s that may appear as an <b>item</b> of an
 * {@link UnorderedListSegment} / {@link OrderedListSegment} — the "single-layer"
 * content kinds.
 *
 * <p>A list item is {@code Listable}; the list segments themselves are
 * <b>not</b>, so a list <b>cannot contain a list</b> — it won't compile. Nested /
 * recursive structure is a deliberate non-goal for lists: it belongs to the
 * rigid tree ({@code DocNode} children), not to list items. This pushes all
 * recursion to the one place that models it (the tree), and keeps a list a flat,
 * homogeneous run of leaf content.</p>
 */
public sealed interface Listable extends RigidSegment
        permits MarkdownSegment, TextSegment, CodeSegment,
                RelationSegment, ParagraphSegment,
                SvgSegment, TableSegment, ImageSegment {
}
