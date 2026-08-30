package hue.captains.singapura.js.homing.studio.base.app.tree;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * A <b>catalogue authored as data</b> rather than as classes: a hierarchy of
 * named nodes whose leaves bear typed content, playing the same role a
 * {@code Catalogue} plays and rendering through the same UI surface.
 *
 * <p><b>Renamed from {@code ContentTree} on 2026-08-30 (RFC 0053).</b> The old
 * name described the shape, which distinguished nothing — a {@code Catalogue} is
 * a tree too — and invited the reading that this is a different KIND of thing
 * whose interior might be app-internal state. It is not. A {@link TreeLeaf} holds
 * a {@code Doc}, carries a sibling-unique segment, and overrides display fields
 * "when the same Doc appears in multiple contexts with different framings" —
 * the same record, with the same rationale, as an {@code Entry.OfLeaf}. The only
 * real difference is the authoring route.</p>
 *
 * <p>Which is what the name now says, and it settles a question the old one kept
 * open: whether a doc's addressability should depend on whether its container
 * was written in Java or in data.</p>
 *
 * <p>Realises the DocTree ontology (T1–T10) for data-authored hierarchies. Each
 * one has its own identity (URL slug); every node is addressable by
 * {@code (id, path)}; the root is itself a {@link TreeBranch} — no separate
 * "root" wrapper, the metadata IS the root.</p>
 *
 * <p>Phase 1 of RFC 0016: <b>declared</b> only, boot-registered via
 * {@code Fixtures.trees()}. Provider-sourced hierarchies ({@code TreeProvider})
 * and cross-hierarchy references via root reference leaves are deferred. Note
 * that "dynamic" here names the AUTHORING — structure is values, not classes —
 * and not the sourcing, which remains declared until {@code TreeProvider} lands.</p>
 *
 * @param id   URL-safe identity slug (e.g. "animals"); unique across the
 *             registered set
 * @param root the root branch; carries the display name + structure
 *
 * @since RFC 0016 (as ContentTree); renamed RFC 0053
 */
public record DynamicCatalogue(String id, TreeBranch root) implements ValueObject {
    public DynamicCatalogue {
        Objects.requireNonNull(id,   "DynamicCatalogue.id");
        Objects.requireNonNull(root, "DynamicCatalogue.root");
        if (id.isBlank()) {
            throw new IllegalArgumentException("DynamicCatalogue.id must not be blank");
        }
        if (!id.matches("[a-z0-9-]+")) {
            throw new IllegalArgumentException(
                    "DynamicCatalogue.id must be URL-safe slug ([a-z0-9-]+); got: " + id);
        }
    }

    /** Convenience — derive the slug id from the root's name. */
    public DynamicCatalogue(TreeBranch root) {
        this(TreeBranch.slugify(root.name()), root);
    }
}
