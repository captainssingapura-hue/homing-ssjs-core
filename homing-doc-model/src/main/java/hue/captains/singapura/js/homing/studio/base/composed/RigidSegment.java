package hue.captains.singapura.js.homing.studio.base.composed;

/**
 * The subset of {@link Segment}s a {@code RigidDoc} node may hold — every inline
 * content kind <b>except</b> {@link ComposedSegment}, doc-in-doc recursion, which the
 * rigid-tree model excludes by design: redundant in a RigidDoc, which already nests through
 * its own {@code DocNode} children (structure), so a grafted sub-doc buys nothing; the
 * inline-summary use it was reached for is served by {@link SimpleListSegment}.
 *
 * <p>Because {@code DocNode.content()} is typed {@code List<RigidSegment>}, a
 * {@code ComposedSegment} <b>will not compile</b> inside a RigidDoc — the fence is in the
 * type system, not a runtime check. The fence is RigidDoc-scoped: a flat {@link ComposedDoc}
 * keeps the full {@link Segment} surface.</p>
 */
public sealed interface RigidSegment extends Segment
        permits Listable, UnorderedListSegment, OrderedListSegment {
}
