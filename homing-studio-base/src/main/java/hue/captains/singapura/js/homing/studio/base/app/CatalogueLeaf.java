package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * RFC 0015 — sealed family of things that can appear as a leaf in a
 * {@code DocTree} (the umbrella term for {@link Catalogue} and the
 * upcoming {@code DynamicCatalogue}). Realises the leaf-kind closure axioms
 * from the DocTree ontology (T3, T4).
 *
 * <p>Two permits:</p>
 * <ul>
 *   <li>{@link Doc} — content-bearing leaf. The default presumption:
 *       most leaves are Docs. Realises DocTree T4's "content leaf"
 *       variant.</li>
 *   <li>{@link StudioProxy} — structural leaf. RFC 0011 cross-tree
 *       portal — names another tree's root rather than carrying content.
 *       Realises DocTree T4's "tree-root reference leaf" variant.</li>
 * </ul>
 *
 * <p>Future structural carve-outs (external links, search shortcuts,
 * mount points) extend the permits list explicitly via this ontology
 * entry — the framework refuses implicit non-Doc leaves.</p>
 *
 * <p><b>Phase 1 of RFC 0015.</b> This interface is introduced as a typed
 * marker; existing {@link Entry} variants continue to carry their
 * payloads in the {@code OfDoc}, {@code OfApp}, {@code OfPlan},
 * {@code OfStudio} sum. Phase 2-6 of the RFC collapses the Entry sum
 * into polymorphism over {@code CatalogueLeaf}; until then, this
 * interface is informational — the typed assertion that Doc and
 * StudioProxy are the two leaf kinds.</p>
 *
 * <p><b>Why not sealed?</b> The Java sealed-classes rule requires
 * permitted subtypes to live in the same package (in unnamed modules)
 * or the same named module. {@link Doc} lives in {@code studio.base};
 * {@link StudioProxy} lives in {@code studio.base.app}; the project
 * has no {@code module-info.java}. Until the project adopts JPMS
 * modules, the closure is documented and (later) enforced via a
 * conformance test rather than declared structurally. The ontological
 * statement that Doc and StudioProxy are the only two leaf kinds (per
 * DocTree T4) is unchanged.</p>
 *
 * @since RFC 0015 Phase 1
 */
public interface CatalogueLeaf extends StatelessFunctionalObject {

    /**
     * RFC 0051 Law 2 — this leaf's URL path segment, unique among its
     * siblings. Typed rather than {@code String} so the character set and
     * length cap are enforced at construction, not by convention.
     *
     * <p>The default derives from the implementing class, minus a trailing
     * {@code "Doc"}: {@code DemoIntroDoc} becomes {@code "demo-intro"}.
     * That is right for the common case — one class, one leaf — and wrong
     * for value-leaves where many instances share a class ({@code AppDoc},
     * {@code PlanDoc}, {@code StudioProxy}), which is why this is a default
     * to override rather than a fixed rule. Overriders owe only
     * sibling-uniqueness; the boot check enforces it.</p>
     */
    default NodeName slug() {
        return NodeName.ofType(getClass(), "Doc");
    }
}
