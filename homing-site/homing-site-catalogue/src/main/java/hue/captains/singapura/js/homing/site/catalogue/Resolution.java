package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.site.Path;

/**
 * What a {@link CatalogueTree} found at a path: a vertex, a leaf, or the
 * segment it could not follow. The site contract's router collapses this to
 * a navigable or empty; this richer form is for a site that wants to say why
 * a miss missed, or to know the parent a leaf was found under.
 */
public sealed interface Resolution {

    /** The path led to a vertex: its listing is the page. */
    record AtCatalogue(Path path, Catalogue<?> catalogue) implements Resolution {}

    /** The path led to a leaf, found in {@code parent}. */
    record AtLeaf(Path path, Catalogue<?> parent, Leaf<?> leaf) implements Resolution {}

    /** The walk stopped at segment {@code failedAt} (0-based), for {@code reason}. */
    record Miss(Path path, int failedAt, Reason reason) implements Resolution {
        /** The segment that failed, or {@code (end)} when the path ran out. */
        public String at() {
            return failedAt < path.depth() ? path.segments().get(failedAt) : "(end)";
        }
    }

    enum Reason {
        /** The vertex has no child by that slug. */
        NO_SUCH_CHILD,
        /** A leaf was reached with segments still to go; a leaf has no children. */
        PAST_A_LEAF
    }

    Path path();

    default boolean isHit() { return !(this instanceof Miss); }
}
