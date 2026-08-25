package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.studio.base.Doc;

/**
 * RFC 0051 — what a {@link CataloguePath} resolved to.
 *
 * <p>A sum rather than a nullable node, because the three outcomes are things
 * a handler must answer differently: render a catalogue page, open a leaf, or
 * produce a 404. Returning null or an empty Optional for the third would make
 * "not found" indistinguishable from "found nothing to say about it", and
 * would throw away where the walk stopped — which is the only part of a miss
 * worth logging.</p>
 */
public sealed interface PathResolution {

    /** The path names a catalogue node. */
    record ToCatalogue(CataloguePath path, Catalogue<?> catalogue) implements PathResolution {}

    /** The path names a leaf, held by {@code parent}. */
    record ToLeaf(CataloguePath path, Catalogue<?> parent, Doc doc) implements PathResolution {}

    /**
     * The path names nothing.
     *
     * @param path      the path as given
     * @param failedAt  index of the segment that could not be followed, or
     *                  {@code path.depth()} when the path ran past a leaf
     * @param reason    which way it failed
     */
    record Miss(CataloguePath path, int failedAt, Reason reason) implements PathResolution {

        /** Where the segment that failed is, as text — for messages and logs. */
        public String at() {
            return failedAt < path.depth() ? path.segments().get(failedAt).value() : "(end)";
        }
    }

    /** Why a path did not resolve. */
    enum Reason {
        /** No child of the node reached so far carries that segment. */
        NO_SUCH_CHILD,
        /**
         * A leaf matched, but segments remained. Distinguished from
         * NO_SUCH_CHILD because it means the caller held a real address and
         * appended to it — usually a stale deep link rather than a typo, and
         * worth redirecting to the leaf rather than 404ing.
         */
        PAST_A_LEAF
    }

    /** True when this resolution found something. */
    default boolean isHit() { return !(this instanceof Miss); }
}
