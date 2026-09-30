package hue.captains.singapura.js.homing.studio.base.composed;

import java.util.List;
import java.util.Objects;

/** Shared validation for {@link UnorderedListSegment} / {@link OrderedListSegment}. */
final class ListItems {

    private ListItems() {}

    /**
     * Defensive-copy {@code items}, and enforce: non-null, non-empty, and every
     * item the <b>same concrete class</b> (homogeneity). Returns the copied list.
     */
    static List<Listable> homogeneous(List<Listable> items, String owner) {
        Objects.requireNonNull(items, owner + ".items");
        List<Listable> copy = List.copyOf(items);
        if (copy.isEmpty()) {
            throw new IllegalArgumentException(owner + ".items must have at least one item");
        }
        Class<?> first = copy.get(0).getClass();
        for (Listable it : copy) {
            if (it.getClass() != first) {
                throw new IllegalArgumentException(
                        owner + " items must all be the same kind; got "
                        + first.getSimpleName() + " and " + it.getClass().getSimpleName());
            }
        }
        return copy;
    }
}
