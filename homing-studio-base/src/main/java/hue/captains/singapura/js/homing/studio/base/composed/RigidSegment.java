package hue.captains.singapura.js.homing.studio.base.composed;

/**
 * The subset of {@link Segment}s a {@code RigidDoc} node may hold — every inline
 * content kind <b>except</b> the two the rigid-tree model excludes by design:
 *
 * <ul>
 *   <li>{@link ComposedSegment} — doc-in-doc recursion. Redundant in a RigidDoc,
 *       which already nests through its own {@code DocNode} children (structure),
 *       so a grafted sub-doc buys nothing; the inline-summary use it was reached
 *       for is served by {@link SimpleListSegment}.</li>
 *   <li>{@link DocumentaryWidget} — embedded interactivity. Not worth the extra
 *       complexity inside a document; interactive widgets are hosted by the
 *       workspace instead.</li>
 * </ul>
 *
 * <p>Because {@code DocNode.content()} is typed {@code List<RigidSegment>}, those
 * two <b>will not compile</b> inside a RigidDoc — the fence is in the type system,
 * not a runtime check. The fence is RigidDoc-scoped: a flat {@link ComposedDoc}
 * keeps the full {@link Segment} surface (both excluded kinds remain valid there).</p>
 */
public sealed interface RigidSegment extends Segment
        permits Listable, UnorderedListSegment, OrderedListSegment {
}
