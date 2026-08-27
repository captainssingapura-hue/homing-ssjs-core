package hue.captains.singapura.js.homing.studio.base.graph;

import hue.captains.singapura.js.homing.studio.base.Studio;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueAugmentation;
import hue.captains.singapura.js.homing.studio.base.app.SyntheticEntry;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RFC 0014 — builds the framework's diagnostic tile injections for the
 * {@code CatalogueGetAction} augmentation map. Three tiers, all gated by
 * {@code params.diagnosticsEnabled()} (the caller — typically
 * {@code Bootstrap.compose()} — only instantiates this when the flag is on):
 *
 * <ol>
 *   <li><b>Home L0 page</b> — appends a single "Diagnostics" tile routing
 *       to {@link DiagnosticsCatalogue}. {@code replace=false} so the
 *       downstream studio's own tiles stay in place.</li>
 *
 *   <li><b>{@link DiagnosticsCatalogue} unscoped page</b>:
 *       <ul>
 *         <li><i>Multi-studio</i> ({@code studios.size() > 1}) — one parent
 *             tile per studio routing to a context-scoped variant of this
 *             same catalogue ({@code &context=<studio-FQN>}). {@code
 *             replace=true} so the catalogue's empty typed entries don't
 *             produce a blank above the synthetic tiles.</li>
 *         <li><i>Single-studio</i> — directly two view tiles (Object Graph,
 *             Type View) rooted at the single studio. No parent level.</li>
 *       </ul></li>
 *
 *   <li><b>{@link DiagnosticsCatalogue} per-studio context page</b>
 *       (multi-studio only) — for each studio, two view tiles (Object
 *       Graph, Type View) rooted at that studio. {@code replace=true}.</li>
 * </ol>
 *
 * <p>Pure data — no I/O, no mutable state. {@link #augmentations()} returns
 * the immutable map ready to hand to {@code CatalogueGetAction}.</p>
 */
public record DiagnosticsHub(
        List<? extends Studio<?>> studios,
        Class<? extends Catalogue<?>> homeAppClass
) implements StatelessFunctionalObject {

    public DiagnosticsHub {
        Objects.requireNonNull(studios,      "studios");
        Objects.requireNonNull(homeAppClass, "homeAppClass");
        studios = List.copyOf(studios);
    }

    /** Build the per-(class, context) augmentation map. */
    public Map<CatalogueAugmentation.AugKey, CatalogueAugmentation> augmentations() {
        var out = new LinkedHashMap<CatalogueAugmentation.AugKey, CatalogueAugmentation>();

        // (1) Home-L0 injection: one "Diagnostics" tile, appended after the
        //     studio's own tiles.
        out.put(
                CatalogueAugmentation.AugKey.of(homeAppClass),
                new CatalogueAugmentation(false, List.of(diagnosticsRootTile()))
        );

        if (studios.size() > 1) {
            // (2a) Multi-studio Diagnostics page — parent tiles only.
            var parents = new ArrayList<SyntheticEntry>(studios.size());
            for (Studio<?> studio : studios) parents.add(studioParentTile(studio));
            out.put(
                    CatalogueAugmentation.AugKey.of(DiagnosticsCatalogue.class),
                    new CatalogueAugmentation(true, parents)
            );

            // (3) Per-studio context page — Object Graph + Type View rooted
            //     at that studio. Keyed by (DiagnosticsCatalogue, studio-FQN).
            for (Studio<?> studio : studios) {
                out.put(
                        CatalogueAugmentation.AugKey.of(
                                DiagnosticsCatalogue.class,
                                studio.getClass().getName()),
                        new CatalogueAugmentation(true, viewTilesFor(studio))
                );
            }
        } else {
            // (2b) Single-studio Diagnostics page — directly the two view
            //      tiles rooted at the lone studio. (If studios is empty,
            //      something is very wrong upstream, but a graceful empty
            //      list keeps us out of NPE territory.)
            if (!studios.isEmpty()) {
                out.put(
                        CatalogueAugmentation.AugKey.of(DiagnosticsCatalogue.class),
                        new CatalogueAugmentation(true, viewTilesFor(studios.get(0)))
                );
            }
        }

        return Map.copyOf(out);
    }

    // -----------------------------------------------------------------------
    // Tile factories
    // -----------------------------------------------------------------------

    private static SyntheticEntry diagnosticsRootTile() {
        return new SyntheticEntry(
                "catalogue",
                "Diagnostics",
                "Self-introspection surfaces — live object graph and type view, "
                  + "scoped per studio in multi-studio compositions.",
                "DIAGNOSTICS",
                catalogueUrl(DiagnosticsCatalogue.class.getName())
        );
    }

    private static SyntheticEntry studioParentTile(Studio<?> studio) {
        String label = studio.home().name();
        return new SyntheticEntry(
                "catalogue",
                label,
                "Diagnostics scoped to " + label + " — object graph + type view rooted "
                  + "at this studio's reachable vertices.",
                "DIAGNOSTICS",
                hue.captains.singapura.js.homing.studio.base.app.CatalogueAppHost.urlFor(
                        DiagnosticsCatalogue.class.getName(), studio.getClass().getName())
        );
    }

    private static List<SyntheticEntry> viewTilesFor(Studio<?> studio) {
        String fqn = studio.getClass().getName();
        String studioName = studio.home().name();
        return List.of(
                new SyntheticEntry(
                        "app",
                        "Object Graph",
                        "Indented tree of every vertex reachable from " + studioName
                          + " — jOntology classification shown via emoji.",
                        "DIAGNOSTICS",
                        StudioGraphInspector.urlFor(fqn, StudioGraphView.TREE)
                ),
                new SyntheticEntry(
                        "app",
                        "Type View",
                        "One row per concrete vertex class reachable from " + studioName
                          + " — ❓-unmarked types surface first as a code-quality gauge.",
                        "DIAGNOSTICS",
                        StudioGraphInspector.urlFor(fqn, StudioGraphView.TYPES)
                )
        );
    }

    // -----------------------------------------------------------------------

    private static String catalogueUrl(String fqn) {
        return hue.captains.singapura.js.homing.studio.base.app.CatalogueAppHost.urlFor(fqn);
    }
}
