package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.tree.Category;
import hue.captains.singapura.js.homing.tree.DimensionKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.Kind;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.RigidTrees;
import hue.captains.singapura.js.homing.tree.Summary;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.TreeNormalizer;
import hue.captains.singapura.js.homing.tree.dims.NameValue;

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
        if (root == null) throw new IllegalArgumentException("root");
        return catalogueNode(root, TreeLevel.L0.INSTANCE);
    }

    // ── Catalogue (branch) ────────────────────────────────────────────────

    private NormalizedNode catalogueNode(Catalogue<?> cat, TreeLevel level) {
        var dims = baseDims(cat.name(), cat.summary(), cat.badge(), "catalogue");
        var kids = new ArrayList<NormalizedNode>();

        // Children sit one level below. At the L18 cap there is no room — a
        // catalogue that deep simply renders without its descendants.
        TreeLevel childLevel = level.below().orElse(null);
        if (childLevel != null) {
            for (Catalogue<?> sub : cat.subCatalogues()) {
                kids.add(catalogueNode(sub, childLevel));
            }
            for (Entry<?> entry : cat.leaves()) {
                NormalizedNode leaf = leafNode(entry, level);
                if (leaf != null) kids.add(leaf);
            }
        }
        return new NormalizedNode(level, dims, kids);
    }

    // ── Leaf (doc / portal) ───────────────────────────────────────────────

    private NormalizedNode leafNode(Entry<?> entry, TreeLevel hostLevel) {
        TreeLevel childLevel = hostLevel.below().orElse(null);
        if (childLevel == null) return null;   // host already at the cap

        if (entry instanceof Entry.OfDoc<?, ?> od) {
            Doc doc = od.doc();
            var dims = baseDims(doc.title(), doc.summary(), doc.category(), doc.kind());
            return NormalizedNode.leaf(childLevel, dims);
        }

        if (entry instanceof Entry.OfStudio<?, ?> os) {
            // GRAFT (RFC 0040): normalize the proxied source studio as a
            // standalone L0 tree, then re-level it so its root lands one level
            // below this host — a pure recursive shift, no host context inside
            // normalize. This is the studio synthetic forest.
            NormalizedNode standalone = normalize(os.proxy().source());
            return RigidTrees.graftUnder(standalone, hostLevel);
        }

        return null;   // OfIllustration — deferred
    }

    // ── Dimensions ─────────────────────────────────────────────────────────

    private Map<DimensionKey, DimensionValue> baseDims(
            String label, String summary, String category, String kind) {
        // No LevelDepth dimension — depth lives in level() alone (RFC 0040).
        var m = new LinkedHashMap<DimensionKey, DimensionValue>();
        m.put(DisplayLabel.INSTANCE, new NameValue(label == null ? "" : label));
        m.put(Summary.INSTANCE,      new NameValue(summary == null ? "" : summary));
        m.put(Category.INSTANCE,     new CategoryValue(category == null ? "" : category));
        m.put(Kind.INSTANCE,         new KindValue(kind == null ? "" : kind));
        return m;
    }
}
