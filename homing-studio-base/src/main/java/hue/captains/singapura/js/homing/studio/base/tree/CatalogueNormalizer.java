package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueAppHost;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.NavKey;
import hue.captains.singapura.js.homing.tree.NodeIdentity;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.RigidTrees;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.TreeNormalizer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RFC 0040 — the catalogue's {@link TreeNormalizer}: maps a {@link Catalogue}
 * tree onto a {@link NormalizedNode} tree on the rigid-tree substrate. The
 * normalized counterpart of {@link CatalogueTreeAdapter}; built alongside it so
 * the original adapter stays untouched until the tree view is proven and the
 * migration flips over.
 *
 * <p>Context-free: {@link #normalize(Catalogue)} always builds a standalone
 * tree rooted at {@code L0}. The level is intrinsic to position (no hand-
 * threaded {@code int depth}); a node's depth lives in {@link
 * NormalizedNode#level()} alone — there is no {@code LevelDepth} dimension.</p>
 *
 * <p>The new capability over {@code CatalogueTreeAdapter}: {@code OfStudio}
 * portals (RFC 0011) are <b>grafted</b>. A portal's proxied source L0 is
 * normalized standalone, then re-levelled under the host via {@link
 * RigidTrees#graftUnder} — the studio "synthetic forest". {@code
 * OfIllustration} leaves remain deferred.</p>
 *
 * @since homing-studio-base — RFC 0040 normalize pipeline
 */
public final class CatalogueNormalizer implements TreeNormalizer<Catalogue<?>> {

    public static final CatalogueNormalizer INSTANCE = new CatalogueNormalizer();

    private CatalogueNormalizer() {}

    @Override
    public NormalizedNode normalize(Catalogue<?> root) {
        return toCatalogueTree(root).structure();
    }

    /**
     * The full transform: the pure structure tree plus the details its vertices
     * resolve to, built in ONE walk (RFC 0053).
     *
     * <p>Structure and answers cannot drift because nothing derives them
     * separately — the mistake that gave a catalogue four derivations. The details
     * map is keyed by identity, so a grafted subtree's answers merge in without
     * translation; that is the resolver union happening at build time, with
     * disjointness as its precondition.</p>
     */
    public CatalogueTree toCatalogueTree(Catalogue<?> root) {
        if (root == null) throw new IllegalArgumentException("root");
        var details = new LinkedHashMap<NodeIdentity, ListingDetails>();
        NormalizedNode structure = catalogueNode(root, TreeLevel.L0.INSTANCE, details);
        return new CatalogueTree(structure, details);
    }

    // ── Catalogue (branch) ────────────────────────────────────────────────

    private NormalizedNode catalogueNode(Catalogue<?> cat, TreeLevel level,
                                         Map<NodeIdentity, ListingDetails> details) {
        var kids = new ArrayList<NormalizedNode>();

        NavKey identity = CatalogueAppHost.identityFor(cat);
        // A branch is an illustration and nothing more: its address is its path,
        // so it needs no binding to be reached. Note the icon, which dimensions
        // never carried — the catalogue had one and the tile could not show it.
        details.put(identity, new ListingDetails.OfBranch(new Illustration(
                cat.name(), cat.summary(), cat.badge(), cat.icon(), "catalogue")));

        // Children sit one level below. At the L18 cap there is no room — a
        // catalogue that deep simply renders without its descendants.
        TreeLevel childLevel = level.below().orElse(null);
        if (childLevel != null) {
            for (Catalogue<?> sub : cat.subCatalogues()) {
                kids.add(catalogueNode(sub, childLevel, details));
            }
            for (Entry<?> entry : cat.leaves()) {
                NormalizedNode leaf = leafNode(entry, level, details);
                if (leaf != null) kids.add(leaf);
            }
        }
        return new NormalizedNode(level, cat.slug(), identity, Map.of(), kids);
    }


    // ── Leaf (doc / portal) ───────────────────────────────────────────────

    private NormalizedNode leafNode(Entry<?> entry, TreeLevel hostLevel,
                                    Map<NodeIdentity, ListingDetails> details) {
        TreeLevel childLevel = hostLevel.below().orElse(null);
        if (childLevel == null) return null;   // host already at the cap

        if (entry instanceof Entry.OfLeaf<?, ?, ?> od) {
            // A leaf with no doc is still a position. RFC 0051 Phase 6 made a
            // Navigable the binding in its own right, so an app tile's place in
            // the tree is as real as a document's — it was only ever missing here
            // because this branch tested for content rather than for a placement.
            // The studio's theme picker is the one such leaf, and it was the sole
            // vertex the forest lacked against the registry's own inventory.
            Doc doc = od.content();
            // The doc branch keeps reading the doc, unchanged, so this stays a
            // strictly additive change: exactly one new vertex, no existing one
            // redescribed. (Entry defaults kind/category from content anyway, but
            // a placement may override them, and that is a separate question.)
            // The placement names the segment and the binding names the identity —
            // Phase 6 moved the slug off the doc and onto the placement precisely
            // so the vertex, not the payload, owns both.
            var identity = new NavKey(od.nav().app().getClass(), od.nav().params());
            // A leaf is a destination, so it answers as an illustrated navigable:
            // how it looks, plus the binding that opens it.
            details.put(identity, new ListingDetails.OfLeaf(
                    (doc != null)
                            ? new Illustration(doc.title(), doc.summary(), od.category(), "", doc.kind())
                            : new Illustration(od.nav().name(), od.nav().summary(), od.category(), "", od.kind()),
                    od.nav()));
            return NormalizedNode.leaf(childLevel, od.slug(), identity, Map.of());
        }

        if (entry instanceof Entry.OfStudio<?, ?> os) {
            // GRAFT (RFC 0040): normalize the proxied source studio as a
            // standalone L0 tree, then re-level it so its root lands one level
            // below this host — a pure recursive shift, no host context inside
            // normalize. This is the studio synthetic forest.
            //
            // Segment and identity ride through the shift untouched (RFC 0053), so
            // the grafted vertices keep the SOURCE studio's identities. That is what
            // makes the resolver union well-defined, and what makes mounting one
            // source twice collide rather than silently duplicate.
            CatalogueTree standalone = toCatalogueTree(os.proxy().source());
            // THE UNION, at build time. The source's answers merge in unchanged
            // because they are keyed by identity, which a graft does not touch —
            // no rebasing, no translation. putAll is safe for the same reason
            // first-match is: the domains are disjoint, and the boot gate asserts
            // it, so nothing here can be overwriting anything.
            details.putAll(standalone.details());
            return RigidTrees.graftUnder(standalone.structure(), hostLevel);
        }

        return null;   // OfIllustration — deferred
    }

}
