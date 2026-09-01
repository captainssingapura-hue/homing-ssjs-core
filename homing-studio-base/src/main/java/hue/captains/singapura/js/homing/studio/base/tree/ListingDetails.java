package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.studio.base.app.Navigable;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * What the vertices of a <b>listing</b> resolve to — everything the display layer
 * needs to draw a catalogue as cards or as a tree, and nothing else (RFC 0053).
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
 * <h2>Open on the kind, and open on the answer</h2>
 *
 * <p>An earlier draft of this class claimed the answer shape <i>closes</i> —
 * that every node of every tree kind has a label, a summary, a badge, an icon
 * and a kind. That was generalised from one family and is false. Measured across
 * the producers: the three catalogue-shaped ones ({@code CatalogueNormalizer},
 * {@code CatalogueTreeAdapter}, {@code CrateTreeGetAction}) supply all five;
 * the four doc normalizers supply a label and nothing else.</p>
 *
 * <p>The mismatch runs deeper than empty fields. {@link OfLeaf} requires a
 * {@code Navigable}, and the doc packages contain none — a doc section is not
 * opened by app-plus-params, it is addressed by name-path inside a viewer. And
 * the leaf/branch split is itself catalogue semantics: a doc section with
 * subsections has prose <i>and</i> children, so neither case fits it. Docs are
 * not waiting for this type; {@code DocTreeV2} already pairs a structure tree
 * with content providers, answering in a shape of its own.</p>
 *
 * <p>So {@code NodeResolver}'s type parameter is load-bearing exactly as the
 * identity appendix had it: <b>the identity kind stays open, and each tree
 * family brings its own answer</b> — {@code ListingDetails} here, a content
 * provider on the doc side. What this type closes is one family, which is why
 * it is named for a listing rather than for nodes in general.</p>
 *
 * <p>Within that family it still earns its keep: it types what was previously
 * {@code Object}, with no wildcard on the access path, because the answer is
 * uniform across a union of catalogue subtrees even though their identities are
 * not.</p>
 *
 * <p>The listing's job ends here — at what to draw and where it goes. A doc's
 * body is the viewer's business, at the viewer's own address.</p>
 *
 * @since RFC 0053
 */
public sealed interface ListingDetails extends ValueObject {

    /** How this vertex looks. Present on every case; it is the shared half. */
    Illustration illustration();

    /**
     * A destination: how it looks, plus the binding that opens it.
     *
     * @param illustration the display projection
     * @param nav          the app and typed params this leaf is opened by
     */
    record OfLeaf(Illustration illustration, Navigable<?, ?> nav) implements ListingDetails {
        public OfLeaf {
            Objects.requireNonNull(illustration, "ListingDetails.OfLeaf.illustration");
            Objects.requireNonNull(nav,          "ListingDetails.OfLeaf.nav");
        }
    }

    /**
     * A container: how it looks, and nothing more. Its address is its path, so it
     * needs no binding to be reached — which is why RFC 0051's authentic path
     * makes this the smaller of the two cases rather than the poorer one.
     */
    record OfBranch(Illustration illustration) implements ListingDetails {
        public OfBranch {
            Objects.requireNonNull(illustration, "ListingDetails.OfBranch.illustration");
        }
    }
}
