package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueAppHost;
import hue.captains.singapura.js.homing.studio.base.app.CataloguePath;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueRegistry;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.NavKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.NodeIdentity;
import hue.captains.singapura.js.homing.tree.NodeIdentities;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.dims.NameValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    /**
     * The boot gate: the normalized forest and the live registry must agree, in
     * both directions, over whatever studio is starting.
     *
     * <p>Three separate claims, each failing with what it found:</p>
     * <ol>
     *   <li><b>Disjoint identities.</b> No two vertices carry one identity. This
     *       is RFC 0051 Law 1 restated, and it is the precondition that makes a
     *       union of subtree resolvers a well-defined function rather than an
     *       order-dependent one.</li>
     *   <li><b>Complete.</b> Every placement the registry holds appears as a
     *       vertex — the direction {@link #of} is blind to, since a skipped
     *       placement produces no row to disagree with.</li>
     *   <li><b>Agreeing.</b> No vertex resolves somewhere other than its segment
     *       chain says.</li>
     * </ol>
     *
     * <p>Run at boot beside {@code CataloguePathConformance}, and for the same
     * reason its comment gives: the derivations follow from the laws, and Phase 1
     * is exactly where "follows from" proved untrustworthy — twice, while
     * passing. It is a walk of a few hundred nodes, so it costs nothing to verify
     * rather than trust, for every studio including downstream ones, at the
     * moment a break is cheapest to find.</p>
     *
     * <p>Counts are deliberately <b>not</b> asserted. {@code byStructure} is a
     * measure of work remaining, not a law — it should shrink to zero as
     * catalogues gain identity-keyed entries, and a studio is not wrong for
     * having some today.</p>
     *
     * @throws IllegalStateException naming exactly which claim failed and where
     */
    public static void assertForestAgrees(CatalogueRegistry registry) {
        Report report = of(registry.root(), registry);

        var duplicates = NodeIdentities.duplicatesIn(report.tree());
        if (!duplicates.isEmpty()) {
            throw new IllegalStateException(
                    "RFC 0053 — one identity at more than one position, which Law 1 forbids "
                  + "and a resolver union cannot survive: " + duplicates);
        }

        List<String> missing = notInForest(registry, report);
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "RFC 0053 — the registry places these, and the normalized forest has no "
                  + "vertex for them: " + missing);
        }

        var disagreeing = new ArrayList<String>();
        for (Row row : report.rows()) {
            if (row.status() == Status.DIFFER) {
                disagreeing.add(row.label() + " derived=" + row.derived()
                        + " authentic=" + row.authentic());
            }
        }
        if (!disagreeing.isEmpty()) {
            throw new IllegalStateException(
                    "RFC 0053 — these vertices resolve somewhere other than their segment "
                  + "chain says: " + disagreeing);
        }
    }

    /**
     * The <b>other</b> direction: everything the registry places that the forest
     * does not contain.
     *
     * <p>{@link #of} proves that every vertex the forest holds sits where the
     * registry says. It cannot see what the forest OMITS — a normalizer that skips
     * a placement produces no row to disagree with, so a gap reads as silence
     * rather than as a finding. That is not hypothetical: the theme picker, an app
     * tile with no doc, was absent for exactly that reason and the forward check
     * had nothing to say about it.</p>
     *
     * <p>Illustrations are excluded deliberately rather than overlooked. RFC 0053
     * settles that they are decoration, claim no slug, and are not vertices.</p>
     *
     * @return one description per missing placement; empty when the forest is complete
     */
    public static List<String> notInForest(CatalogueRegistry registry, Report report) {
        var present = new HashSet<NodeIdentity>();
        for (Row row : report.rows()) present.add(row.identity());

        var missing = new ArrayList<String>();
        for (Catalogue<?> cat : registry.all()) {
            if (!present.contains(CatalogueNormalizer.identityOf(cat))) {
                missing.add("catalogue " + cat.name() + " (" + cat.getClass().getSimpleName() + ")");
            }
            for (Entry<?> entry : cat.leaves()) {
                if (entry instanceof Entry.OfLeaf<?, ?, ?> leaf) {
                    var key = new NavKey(leaf.nav().app().getClass(), leaf.nav().params());
                    if (!present.contains(key)) {
                        missing.add("leaf " + leaf.slug().value() + " in " + cat.name()
                                + (leaf.content() == null ? " (app tile, no doc)" : ""));
                    }
                }
            }
        }
        return List.copyOf(missing);
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
