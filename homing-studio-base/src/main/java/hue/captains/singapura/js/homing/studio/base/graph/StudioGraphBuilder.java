package hue.captains.singapura.js.homing.studio.base.graph;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.studio.base.Bootstrap;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocReference;
import hue.captains.singapura.js.homing.studio.base.Fixtures;
import hue.captains.singapura.js.homing.studio.base.Reference;
import hue.captains.singapura.js.homing.studio.base.Studio;
import hue.captains.singapura.js.homing.studio.base.Umbrella;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.tracker.Dependency;
import hue.captains.singapura.js.homing.studio.base.tracker.Phase;
import hue.captains.singapura.js.homing.studio.base.tracker.Plan;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.HashMap;
import java.util.Map;

/**
 * RFC 0014 Phase 1 — builds a {@link StudioGraph} by walking a composed
 * {@link Bootstrap}'s typed structure. Stateless functional object;
 * reach via {@link #INSTANCE}.
 *
 * <p>The walk visits every typed surface the framework's primitives expose:
 * Bootstrap → params + fixtures → umbrella + harness apps → studios →
 * catalogues + apps + plans + brand → sub-catalogues + entries →
 * (docs / apps / plans / proxies) → references + phases + decisions +
 * acceptance + objectives + tasks + metrics + dependencies.</p>
 *
 * <p>Two edge kinds emitted (per {@link StudioGraph.Kind}):</p>
 * <ul>
 *   <li><b>CONTAINS</b> — structural parent-child (Bootstrap ⊃ Fixtures ⊃
 *       Umbrella ⊃ Studio ⊃ Catalogue ⊃ ... etc.).</li>
 *   <li><b>REFERENCES</b> — cross-references (Doc → Doc via
 *       {@link DocReference}; Phase → Phase via {@link Dependency}'s
 *       {@code phaseId} resolved within the same plan).</li>
 * </ul>
 *
 * <p>External references ({@code ExternalReference} with a URI target)
 * are skipped in Phase 1 — they don't point at an in-graph vertex. The
 * cross-tree {@code StudioProxyManager} reverse-refs are also deferred
 * to Phase 2 alongside richer reverse-citation queries.</p>
 *
 * @since RFC 0014
 */
public record StudioGraphBuilder() implements StatelessFunctionalObject {

    public static final StudioGraphBuilder INSTANCE = new StudioGraphBuilder();

    /** Build the typed graph from a composed Bootstrap. */
    public StudioGraph build(Bootstrap<?, ?> bootstrap) {
        var g = new StudioGraph.Mutable();
        g.vertex(bootstrap);
        g.contains(bootstrap, bootstrap.params());
        g.contains(bootstrap, bootstrap.fixtures());
        visitFixtures(g, bootstrap.fixtures());
        return g.build();
    }

    // -------------------------------------------------------------------------
    // Recursive visit helpers — each adds its own outgoing edges
    // -------------------------------------------------------------------------

    private void visitFixtures(StudioGraph.Mutable g, Fixtures<?> fixtures) {
        var umbrella = fixtures.umbrella();
        g.contains(fixtures, umbrella);
        visitUmbrella(g, umbrella);

        for (AppModule<?, ?> app : fixtures.harnessApps()) {
            g.contains(fixtures, app);
            // AppModules are leaves in Phase 1 — no further walk.
        }
        // ThemeRegistry, defaultTheme, brand deferred per RFC 0014 D11.
    }

    private void visitUmbrella(StudioGraph.Mutable g, Umbrella<?> umbrella) {
        switch (umbrella) {
            case Umbrella.Solo<?> solo -> {
                var studio = solo.studio();
                g.contains(solo, studio);
                visitStudio(g, studio);
            }
            case Umbrella.Group<?> group -> {
                for (Umbrella<?> child : group.children()) {
                    g.contains(group, child);
                    visitUmbrella(g, child);
                }
            }
        }
    }

    private void visitStudio(StudioGraph.Mutable g, Studio<?> studio) {
        // Home catalogue + every catalogue in the studio's closure.
        Catalogue<?> home = studio.home();
        g.contains(studio, home);
        for (Catalogue<?> cat : studio.catalogues()) {
            g.contains(studio, cat);
            visitCatalogue(g, cat);
        }
        // Apps: studio-intrinsic AppModules (e.g. DocBrowser for HomingStudio).
        for (AppModule<?, ?> app : studio.apps()) {
            g.contains(studio, app);
        }
        // Plans: each plan + its internal structure.
        for (Plan plan : studio.plans()) {
            g.contains(studio, plan);
            visitPlan(g, plan);
        }
        // Brand (if any).
        if (studio.standaloneBrand() != null) {
            g.contains(studio, studio.standaloneBrand());
        }
        // Themes deferred per RFC 0014 D11.
    }

    private void visitCatalogue(StudioGraph.Mutable g, Catalogue<?> catalogue) {
        if (!g.markWalked(catalogue)) return;
        g.vertex(catalogue);
        // Sub-catalogues (CONTAINS).
        for (Catalogue<?> sub : catalogue.subCatalogues()) {
            g.contains(catalogue, sub);
            visitCatalogue(g, sub);
        }
        // Leaf entries (CONTAINS each entry, then recurse into the entry's target).
        for (Entry<?> entry : catalogue.leaves()) {
            g.contains(catalogue, entry);
            visitEntry(g, entry);
        }
    }

    private void visitEntry(StudioGraph.Mutable g, Entry<?> entry) {
        switch (entry) {
            // RFC 0051 Phase 6 — a bound leaf's edges are the ones the OfDoc
            // branch below has to RECOVER by unwrapping: the leaf holds its
            // navigable and its app directly, and its content when it has any.
            case Entry.OfLeaf<?, ?, ?> leaf -> {
                g.contains(entry, leaf.nav());
                g.contains(leaf.nav(), leaf.nav().app());
                if (leaf.content() != null) {
                    g.contains(entry, leaf.content());
                    visitDoc(g, leaf.content());
                }
            }
            case Entry.OfDoc<?, ?> ofDoc -> {
                Doc doc = ofDoc.doc();
                g.contains(entry, doc);
                // RFC 0015 Phase 6: when the doc is a synthetic Doc subtype
                // (PlanDoc, AppDoc), walk into the wrapped Plan / Navigable to
                // preserve the structural edges the old OfApp / OfPlan switch
                // produced. Prose Docs and ProxyDocs fall through to visitDoc.
                if (doc instanceof hue.captains.singapura.js.homing.studio.base.tracker.PlanDoc pd) {
                    g.contains(doc, pd.plan());
                    visitPlan(g, pd.plan());
                } else if (doc instanceof hue.captains.singapura.js.homing.studio.base.app.AppDoc<?, ?> ad) {
                    g.contains(doc, ad.nav());
                    g.contains(ad.nav(), ad.nav().app());
                } else {
                    visitDoc(g, doc);
                }
            }
            case Entry.OfStudio<?, ?> ofStudio -> {
                // StudioProxy points cross-tree at the source studio's L0.
                // For Phase 1, treat the proxy itself as the target of CONTAINS;
                // cross-tree REFERENCES walk is deferred to Phase 2.
                g.contains(entry, ofStudio.proxy());
            }
            case Entry.OfIllustration<?> ofIllustration -> {
                // Illustration is a decoration leaf; not addressable, not citable.
                // Surface it in the graph as a CONTAINS edge so the typed graph
                // is complete, but no further walk — it carries no references.
                g.contains(entry, ofIllustration.illustration());
            }
        }
    }

    private void visitDoc(StudioGraph.Mutable g, Doc doc) {
        if (!g.markWalked(doc)) return;
        g.vertex(doc);
        // References: each Reference is a cross-ref edge. DocReference targets are
        // in-graph Docs; ExternalReferences point outside the graph (skipped here).
        for (Reference ref : doc.references()) {
            if (ref instanceof DocReference docRef) {
                g.references(doc, docRef.target(), docRef.name());
            }
            // ExternalReference: not added as an edge (target is a URI, not a vertex).
        }
    }

    private void visitPlan(StudioGraph.Mutable g, Plan plan) {
        if (!g.markWalked(plan)) return;
        g.vertex(plan);

        // Build a phaseId → Phase lookup for resolving Dependency.phaseId references.
        Map<String, Phase> phaseById = new HashMap<>();
        for (Phase phase : plan.phases()) {
            phaseById.put(phase.id(), phase);
        }

        for (Phase phase : plan.phases()) {
            g.contains(plan, phase);
            phase.tasks().forEach(t -> g.contains(phase, t));
            phase.metrics().forEach(m -> g.contains(phase, m));
            // Phase REFERENCES Phase via dependsOn — typed cross-ref.
            for (Dependency dep : phase.dependsOn()) {
                Phase target = phaseById.get(dep.phaseId());
                if (target != null) {
                    g.references(phase, target, dep.reason());
                }
            }
        }
        plan.decisions().forEach(d -> g.contains(plan, d));
        plan.acceptance().forEach(a -> g.contains(plan, a));
        plan.objectives().forEach(o -> g.contains(plan, o));
    }
}
