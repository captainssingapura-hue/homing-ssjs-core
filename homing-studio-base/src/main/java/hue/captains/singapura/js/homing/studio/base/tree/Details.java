package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.studio.base.app.Navigable;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * What a {@code NodeResolver} answers with — everything the display layer needs
 * about a vertex, and nothing else (RFC 0053).
 *
 * <p>Two cases, because there are two kinds of thing to draw:</p>
 *
 * <ul>
 *   <li>{@link OfLeaf} — an <b>illustrated navigable</b>. A leaf is a destination,
 *       and it carries the binding that opens it alongside how it looks.</li>
 *   <li>{@link OfBranch} — an <b>illustration</b>. A branch is a container; its
 *       card leads to another listing, and its address is its path.</li>
 * </ul>
 *
 * <h2>Open on the kind, closed on the answer</h2>
 *
 * <p>This looks like a reversal of the identity appendix, which argued a
 * resolver's result must be untyped. It is a refinement, and the hinge is that
 * <b>display is bounded even when content is not</b>:</p>
 *
 * <ul>
 *   <li>the <b>identity kind</b> stays open — the substrate cannot enumerate its
 *       consumers, and {@code CrateTreeGetAction} building nodes from another
 *       module proves it;</li>
 *   <li>the <b>answer shape</b> closes — every node of every tree kind has a
 *       label, a summary, a badge, an icon and a kind.</li>
 * </ul>
 *
 * <p>Closing it costs a downstream module nothing: it still brings its own
 * identity kind and merely describes itself in the one vocabulary every tree
 * already speaks. And it types what was previously {@code Object}, without
 * putting a wildcard on the access path, because the answer is uniform across a
 * union even though the identities are not.</p>
 *
 * <p>The tree layer's job ends here — at what to draw and where it goes. A doc's
 * body is the viewer's business, at the viewer's own address.</p>
 *
 * @since RFC 0053
 */
public sealed interface Details extends ValueObject {

    /** How this vertex looks. Present on every case; it is the shared half. */
    Illustration illustration();

    /**
     * A destination: how it looks, plus the binding that opens it.
     *
     * @param illustration the display projection
     * @param nav          the app and typed params this leaf is opened by
     */
    record OfLeaf(Illustration illustration, Navigable<?, ?> nav) implements Details {
        public OfLeaf {
            Objects.requireNonNull(illustration, "Details.OfLeaf.illustration");
            Objects.requireNonNull(nav,          "Details.OfLeaf.nav");
        }
    }

    /**
     * A container: how it looks, and nothing more. Its address is its path, so it
     * needs no binding to be reached — which is why RFC 0051's authentic path
     * makes this the smaller of the two cases rather than the poorer one.
     */
    record OfBranch(Illustration illustration) implements Details {
        public OfBranch {
            Objects.requireNonNull(illustration, "Details.OfBranch.illustration");
        }
    }
}
