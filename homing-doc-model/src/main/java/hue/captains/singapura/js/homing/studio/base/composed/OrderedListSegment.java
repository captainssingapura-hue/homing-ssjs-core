package hue.captains.singapura.js.homing.studio.base.composed;

import java.util.List;

/**
 * A numbered list ({@code <ol>}) of <b>homogeneous</b> items — every item the
 * same concrete {@link Listable} kind. Same rules as {@link UnorderedListSegment}:
 * items are {@link Listable} (so a list cannot contain a list — nesting is the
 * tree's job), and homogeneity is enforced at construction.
 *
 * @param items the list items, in order; at least one, all the same concrete kind
 */
public record OrderedListSegment(List<Listable> items) implements RigidSegment {

    public OrderedListSegment {
        items = ListItems.homogeneous(items, "OrderedListSegment");
    }
}
