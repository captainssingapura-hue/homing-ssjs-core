package hue.captains.singapura.js.homing.studio.base.composed;

import java.util.List;

/**
 * A bullet list ({@code <ul>}) of <b>homogeneous</b> items — every item the same
 * concrete {@link Listable} kind (e.g. all {@link ParagraphSegment}, or all
 * {@link ImageSegment} for a gallery).
 *
 * <p>Items are {@link Listable}, so a list <b>cannot contain a list</b> (nesting
 * is the tree's job — a deliberate tradeoff, enforced at compile time).
 * Homogeneity (all items the same class) is enforced at construction.</p>
 *
 * @param items the list items, in order; at least one, all the same concrete kind
 */
public record UnorderedListSegment(List<Listable> items) implements RigidSegment {

    public UnorderedListSegment {
        items = ListItems.homogeneous(items, "UnorderedListSegment");
    }
}
