package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueAppHost;
import hue.captains.singapura.js.homing.studio.base.app.CataloguePath;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueRegistry;
import hue.captains.singapura.js.homing.studio.base.app.NavKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.NodeIdentity;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.dims.NameValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RFC 0053 — walks the normalized catalogue forest and asks, of every vertex,
 * whether the two derivations of "where am I" agree.
 *
 * <p>The point is not the rendering; it is that the two answers come from
 * different places:</p>
 *
 * <ul>
 *   <li><b>derived</b> — built by chaining each vertex's {@code segment} down from
 *       the root. Pure structure: what the normalized tree says the path is.</li>
 *   <li><b>authentic</b> — what the live {@link CatalogueRegistry} says, and what a
 *       URL actually resolves to today.</li>
 * </ul>
 *
 * <p>RFC 0051's re-key of that same index passed every suite while
 * {@code /goto?app=plan&id=…&phase=6} was already broken. A comparison over the
 * live catalogue is the only thing that catches that class of fault, so this
 * exists to be read as well as asserted.</p>
 *
 * <h2>Which index answered is itself the finding</h2>
 *
 * <p>The registry answers the two vertex kinds by unrelated means, and
 * {@link Via} records which one ran:</p>
 *
 * <ul>
 *   <li>{@link Via#IDENTITY} — a leaf's position is a lookup keyed on its
 *       {@link NavKey}. Genuinely independent of the segment chain, so agreement
 *       here is a real check.</li>
 *   <li>{@link Via#STRUCTURAL} — a catalogue has <b>no identity-keyed entry at
 *       all</b>. {@code pathOf(Catalogue)} re-walks the parent chain and joins
 *       {@code slug()}s, which is the same derivation this walker performs. So
 *       agreement there is close to tautological, and the honest reading of a
 *       structural row is "the registry cannot answer this by identity".</li>
 * </ul>
 *
 * <p>That split — leaves keyed by binding, catalogues keyed by class and
 * re-derived — is precisely what RFC 0053 Phase 1 collapses when {@code childIndex}
 * is re-keyed on identity. Until then the {@code byStructure} count is the size of
 * the gap, measured rather than estimated.</p>
 *
 * <p><b>The root is the one borrowed answer.</b> Its derived path is seeded from
 * the registry, because {@code pathOf} skips the root when chaining slugs. Seeding
 * keeps a single off-by-one from cascading into every descendant and drowning the
 * signal; every node below the root is derived independently.</p>
 *
 * <p>Functional Object: stateless, static entry point.</p>
 *
 * @since RFC 0053
 */
public final class CatalogueTreeParity {

    private CatalogueTreeParity() {}

    /** Whether the two derivations agree. */
    public enum Status {
        /** Both name the same path. */
        AGREE,
        /** Both answered, and they differ — the interesting failure. */
        DIFFER,
        /** Nothing places this vertex; it has no position at all. */
        UNPLACED
    }

    /** Which of the registry's two unrelated indexes produced the authentic path. */
    public enum Via {
        /** Keyed on the vertex's own identity — the answer RFC 0053 wants for every vertex. */
        IDENTITY,
        /** Re-derived by walking the parent chain, because no identity-keyed entry exists. */
        STRUCTURAL,
        /** Neither index answered. */
        NONE
    }

    /**
     * @param depth     nesting depth, for indentation only
     * @param indexPath the child-index path from the root — how the generic
     *                  {@code TreeRenderer} addresses this row. RFC 0040 made node
     *                  identity in that renderer purely positional, so interop
     *                  needs the ordinal path even though nothing else here does;
     *                  it is a rendering coordinate, never an identity
     * @param segment   the vertex's own segment
     * @param label     its display label, for reading
     * @param derived   the path built by chaining segments
     * @param authentic the path the live registry gives, or null
     * @param status    whether the two agree
     * @param via       which index answered
     * @param identity  the vertex's identity, so a disagreement can be traced
     */
    public record Row(int depth, List<Integer> indexPath, NodeName segment, String label,
                      CataloguePath derived, CataloguePath authentic,
                      Status status, Via via, NodeIdentity identity) {

        /** The URL this vertex actually resolves at today, or null when unplaced. */
        public String authenticUrl() {
            return authentic == null ? null : authentic.toUrl();
        }
    }

    /**
     * Every row, plus the counts that make a regression a number rather than a diff,
     * plus the normalized tree itself so a consumer can hand it to the generic
     * renderer and the rows to the same renderer's selection callbacks.
     */
    public record Report(NormalizedNode tree, List<Row> rows,
                         int agree, int differ, int unplaced,
                         int byIdentity, int byStructure) {

        public int total() { return rows.size(); }

        /** True when no vertex the registry places sits somewhere else. */
        public boolean clean() { return differ == 0; }
    }

    public static Report of(Catalogue<?> root, CatalogueRegistry registry) {
        NormalizedNode tree = CatalogueNormalizer.INSTANCE.normalize(root);

        // fqn -> catalogue, so a catalogue identity can still be resolved even
        // though the registry holds no identity-keyed entry for one.
        Map<String, Catalogue<?>> byName = new HashMap<>();
        for (Catalogue<?> c : registry.all()) byName.put(c.getClass().getName(), c);

        CataloguePath anchor = registry.pathOf(root);
        if (anchor == null) anchor = CataloguePath.of(List.of());

        var rows = new ArrayList<Row>();
        walk(tree, anchor, 0, List.of(), registry, byName, rows);

        int agree = 0, differ = 0, unplaced = 0, byIdentity = 0, byStructure = 0;
        for (Row r : rows) {
            switch (r.status()) {
                case AGREE    -> agree++;
                case DIFFER   -> differ++;
                case UNPLACED -> unplaced++;
            }
            switch (r.via()) {
                case IDENTITY   -> byIdentity++;
                case STRUCTURAL -> byStructure++;
                case NONE       -> { }
            }
        }
        return new Report(tree, List.copyOf(rows), agree, differ, unplaced, byIdentity, byStructure);
    }

    private static void walk(NormalizedNode node, CataloguePath derived, int depth,
                             List<Integer> indexPath,
                             CatalogueRegistry registry, Map<String, Catalogue<?>> byName,
                             List<Row> out) {
        CataloguePath authentic = null;
        Via via = Via.NONE;

        if (node.identity() instanceof NavKey key) {
            authentic = registry.pathOf(key);
            if (authentic != null) {
                via = Via.IDENTITY;
            } else {
                Catalogue<?> cat = catalogueFor(key, byName);
                if (cat != null) {
                    authentic = registry.pathOf(cat);
                    via = Via.STRUCTURAL;
                }
            }
        }

        Status status = (authentic == null)       ? Status.UNPLACED
                      : authentic.equals(derived) ? Status.AGREE
                                                  : Status.DIFFER;

        out.add(new Row(depth, indexPath, node.segment(), labelOf(node), derived, authentic,
                status, via, node.identity()));

        List<NormalizedNode> kids = node.children();
        for (int i = 0; i < kids.size(); i++) {
            NormalizedNode kid = kids.get(i);
            var childIndex = new ArrayList<>(indexPath);
            childIndex.add(i);
            walk(kid, derived.then(kid.segment()), depth + 1, List.copyOf(childIndex),
                    registry, byName, out);
        }
    }

    /** The catalogue a CatalogueAppHost binding names, if this identity is one. */
    private static Catalogue<?> catalogueFor(NavKey key, Map<String, Catalogue<?>> byName) {
        if (key.app() != CatalogueAppHost.class) return null;
        return (key.params() instanceof CatalogueAppHost.Params p) ? byName.get(p.id()) : null;
    }

    private static String labelOf(NormalizedNode node) {
        DimensionValue v = node.dimensions().get(DisplayLabel.INSTANCE);
        return (v instanceof NameValue name) ? name.text() : node.segment().value();
    }
}
