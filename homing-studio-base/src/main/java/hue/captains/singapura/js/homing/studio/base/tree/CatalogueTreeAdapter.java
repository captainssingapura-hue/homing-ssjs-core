package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.tree.Category;
import hue.captains.singapura.js.homing.tree.DimensionKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.Kind;
import hue.captains.singapura.js.homing.tree.LevelDepth;
import hue.captains.singapura.js.homing.tree.Summary;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.dims.DepthValue;
import hue.captains.singapura.js.homing.tree.dims.NameValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps the studio's catalogue tree (Catalogue + leaf Docs) onto the rigid
 * {@link hue.captains.singapura.js.homing.tree.TreeNode} substrate. The thin
 * String-to-typed-wrapping boundary lives here: native catalogue/doc Strings
 * are wrapped in typed {@link DimensionValue}s exactly once, at this seam.
 *
 * <p>Branch nodes (catalogues) carry {@code Kind = "catalogue"} and their
 * {@code Summary} inline, so a detail view can render a branch without a
 * server round-trip. Leaf nodes (docs) carry {@code Kind = doc.kind()}
 * ({@code "doc"}/{@code "composed"}/{@code "svg"}); identity is purely
 * positional (the child-index path), so a node carries no id.</p>
 *
 * <p>v1 handles {@code subCatalogues()} + {@link Entry.OfDoc} leaves —
 * the primary hierarchy. {@code OfStudio} portals and {@code OfIllustration}
 * leaves are skipped for now (noted as a follow-up).</p>
 *
 * <p>Functional Object: stateless, instance methods, one {@code INSTANCE}.</p>
 *
 * @since homing-tree-views v1
 */
public final class CatalogueTreeAdapter {

    public static final CatalogueTreeAdapter INSTANCE = new CatalogueTreeAdapter();

    private CatalogueTreeAdapter() {}

    /** Adapt a catalogue (and its subtree) rooted at depth 0 (L0). */
    public CatalogueTreeNode adapt(Catalogue<?> root) {
        if (root == null) throw new IllegalArgumentException("root");
        return catalogueNode(root, 0);
    }

    /** Adapt a catalogue at an explicit depth — supports re-rooting a
     *  subtree at a shifted level (the substrate's "start at any level"). */
    public CatalogueTreeNode adaptAtDepth(Catalogue<?> cat, int depth) {
        if (cat == null) throw new IllegalArgumentException("cat");
        return catalogueNode(cat, depth);
    }

    // ── Catalogue (branch) ────────────────────────────────────────────────

    private CatalogueTreeNode catalogueNode(Catalogue<?> cat, int depth) {
        var dims = baseDims(cat.name(), cat.summary(), cat.badge(), "catalogue", depth);

        var kids = new ArrayList<CatalogueTreeNode>();
        for (Catalogue<?> sub : cat.subCatalogues()) {
            kids.add(catalogueNode(sub, depth + 1));
        }
        for (Entry<?> entry : cat.leaves()) {
            CatalogueTreeNode leaf = leafNode(entry, depth + 1);
            if (leaf != null) kids.add(leaf);
        }

        return new CatalogueTreeNode(levelAtDepth(depth), dims, kids);
    }

    // ── Leaf (doc) ────────────────────────────────────────────────────────

    private CatalogueTreeNode leafNode(Entry<?> entry, int depth) {
        // v1: only OfDoc leaves participate in the nav tree.
        if (entry instanceof Entry.OfDoc<?, ?> od) {
            Doc doc = od.doc();
            var dims = baseDims(doc.title(), doc.summary(), doc.category(), doc.kind(), depth);
            return new CatalogueTreeNode(levelAtDepth(depth), dims, List.of());
        }
        // OfStudio / OfIllustration — deferred.
        return null;
    }

    // ── Dimensions ─────────────────────────────────────────────────────────

    private Map<DimensionKey, DimensionValue> baseDims(
            String label, String summary, String category, String kind, int depth) {
        var m = new LinkedHashMap<DimensionKey, DimensionValue>();
        m.put(DisplayLabel.INSTANCE, new NameValue(label == null ? "" : label));
        m.put(Summary.INSTANCE,      new NameValue(summary == null ? "" : summary));
        m.put(Category.INSTANCE,     new CategoryValue(category == null ? "" : category));
        m.put(Kind.INSTANCE,         new KindValue(kind == null ? "" : kind));
        m.put(LevelDepth.INSTANCE,   new DepthValue(depth));
        return m;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /** Depth → TreeLevel singleton, clamped at L8 (the substrate's floor). */
    private TreeLevel levelAtDepth(int depth) {
        return switch (Math.min(depth, 8)) {
            case 0 -> TreeLevel.L0.INSTANCE;
            case 1 -> TreeLevel.L1.INSTANCE;
            case 2 -> TreeLevel.L2.INSTANCE;
            case 3 -> TreeLevel.L3.INSTANCE;
            case 4 -> TreeLevel.L4.INSTANCE;
            case 5 -> TreeLevel.L5.INSTANCE;
            case 6 -> TreeLevel.L6.INSTANCE;
            case 7 -> TreeLevel.L7.INSTANCE;
            default -> TreeLevel.L8.INSTANCE;
        };
    }

}
