package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocRegistry;
import hue.captains.singapura.js.homing.studio.base.tracker.Plan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Boot-time registry of {@link Catalogue}s with breadcrumb derivation via
 * typed parent() calls (RFC 0005-ext2) + studio-proxy cross-tree augmentation
 * (RFC 0011).
 *
 * <p>Boot-time validations:</p>
 *
 * <ol>
 *   <li><b>Parent-match</b> — each sub-catalogue child's {@code parent()}
 *       returns the containing catalogue's INSTANCE.</li>
 *   <li><b>Closure completeness</b> — every sub-catalogue and every
 *       {@code Entry.OfStudio} source is in the registered list.</li>
 *   <li><b>Doc reachability</b> — every {@code Entry.OfDoc} references a
 *       doc in the supplied {@link DocRegistry}.</li>
 *   <li><b>Brand home-app is L0</b> — {@code brand.homeApp()} is an
 *       L0 catalogue in the list.</li>
 *   <li><b>One hosting per source L0</b> — built by
 *       {@link StudioProxyManager#scan}.</li>
 * </ol>
 */
public final class CatalogueRegistry {

    private final StudioBrand brand;
    private final Map<Class<? extends Catalogue<?>>, Catalogue<?>> byClass;
    /** Reverse index: doc UUID → first catalogue containing the doc. */
    private final Map<UUID, Catalogue<?>> docHome;
    /** Reverse index: plan class → first catalogue containing the plan. */
    private final Map<Class<? extends Plan>, Catalogue<?>> planHome;
    /** RFC 0011 — typed reverse-ref for source-L0-hosted-by-umbrella relationships. */
    private final StudioProxyManager proxyManager;

    public CatalogueRegistry(StudioBrand brand,
                             DocRegistry docRegistry,
                             Collection<? extends Catalogue<?>> catalogues) {
        this(brand, docRegistry, catalogues, null, Map.of());
    }

    /** RFC 0011 — accepts an explicit {@link StudioProxyManager}; when null,
     *  the manager is auto-scanned from the registered catalogues' OfStudio leaves. */
    public CatalogueRegistry(StudioBrand brand,
                             DocRegistry docRegistry,
                             Collection<? extends Catalogue<?>> catalogues,
                             StudioProxyManager proxyManager) {
        this(brand, docRegistry, catalogues, proxyManager, Map.of());
    }

    /**
     * RFC 0016 — accepts an {@code extraDocHomes} map that augments the
     * doc-home reverse index after the catalogue-leaf scan. Used by
     * {@code Bootstrap} to register tree-leaf docs against the catalogue
     * that hosts their tree (the catalogue containing the {@code TreeAppHost}
     * navigable leaf for that tree). Without this, tree-leaf docs would
     * not appear in {@link #breadcrumbsForDoc} and their {@code /doc-refs}
     * response would carry an empty breadcrumb chain.
     *
     * <p>Catalogue-leaf docs still take precedence — entries explicitly
     * declared as catalogue leaves win over any conflicting entry in
     * {@code extraDocHomes}.</p>
     */
    @SuppressWarnings("unchecked")
    public CatalogueRegistry(StudioBrand brand,
                             DocRegistry docRegistry,
                             Collection<? extends Catalogue<?>> catalogues,
                             StudioProxyManager proxyManager,
                             Map<UUID, Catalogue<?>> extraDocHomes) {
        this.brand = Objects.requireNonNull(brand, "brand");
        Objects.requireNonNull(docRegistry, "docRegistry");
        Objects.requireNonNull(catalogues,  "catalogues");

        // Build the class → catalogue lookup.
        var byClass = new LinkedHashMap<Class<? extends Catalogue<?>>, Catalogue<?>>();
        for (Catalogue<?> c : catalogues) {
            requireValid(c);
            Class<? extends Catalogue<?>> cls =
                    (Class<? extends Catalogue<?>>) c.getClass();
            Catalogue<?> prev = byClass.put(cls, c);
            if (prev != null && prev != c) {
                throw new IllegalStateException(
                        "Catalogue class registered twice with different instances: " + cls.getName());
            }
        }

        // Brand's home-app must reference a registered L0 catalogue.
        if (!byClass.containsKey(brand.homeApp())) {
            throw new IllegalStateException(
                    "StudioBrand.homeApp references " + brand.homeApp().getName()
                  + " which is not in the registered catalogue list");
        }
        Catalogue<?> homeCatalogue = byClass.get(brand.homeApp());
        if (!(homeCatalogue instanceof L0_Catalogue<?>)) {
            throw new IllegalStateException(
                    "StudioBrand.homeApp " + brand.homeApp().getName()
                  + " must be an L0_Catalogue (the studio's root). Got "
                  + homeCatalogue.getClass().getName() + " which is at level "
                  + levelOf(homeCatalogue));
        }

        // Validate sub-catalogues + leaves and build reverse indices.
        var docHomeMap  = new HashMap<UUID, Catalogue<?>>();
        var planHomeMap = new HashMap<Class<? extends Plan>, Catalogue<?>>();
        for (Catalogue<?> parent : byClass.values()) {
            // ---- Sub-catalogue children ----
            List<? extends Catalogue<?>> subs = parent.subCatalogues();
            if (subs == null) {
                throw new IllegalStateException(
                        "Catalogue " + parent.getClass().getName() + " has null subCatalogues()");
            }
            if (parent instanceof L8_Catalogue<?, ?> && !subs.isEmpty()) {
                throw new IllegalStateException(
                        "Catalogue " + parent.getClass().getName()
                      + " is an L8_Catalogue (terminal level) but declares "
                      + subs.size() + " sub-catalogue(s). L8 catalogues cannot nest further.");
            }
            for (Catalogue<?> child : subs) {
                if (child == null) {
                    throw new IllegalStateException(
                            "Catalogue " + parent.getClass().getName()
                          + " has a null entry in subCatalogues()");
                }
                if (!byClass.containsKey(child.getClass())) {
                    throw new IllegalStateException(
                            "Catalogue " + parent.getClass().getName()
                          + " references sub-catalogue " + child.getClass().getName()
                          + " which is not in the registered catalogue list");
                }
                Catalogue<?> declaredParent = declaredParentOf(child);
                if (declaredParent != parent) {
                    throw new IllegalStateException(
                            "Catalogue " + parent.getClass().getName()
                          + " contains " + child.getClass().getName()
                          + " in subCatalogues(), but the latter's parent() returns "
                          + (declaredParent == null ? "null" : declaredParent.getClass().getName())
                          + " — must return the containing catalogue's INSTANCE.");
                }
            }

            // ---- Leaves ----
            List<? extends Entry<?>> leaves = parent.leaves();
            if (leaves == null) {
                throw new IllegalStateException(
                        "Catalogue " + parent.getClass().getName() + " has null leaves()");
            }
            for (Entry<?> e : leaves) {
                if (e == null) {
                    throw new IllegalStateException(
                            "Catalogue " + parent.getClass().getName()
                          + " has a null Entry in leaves()");
                }
                switch (e) {
                    case Entry.OfDoc<?, ?>(Doc d) -> {
                        if (d == null) {
                            throw new IllegalStateException(
                                    "Catalogue " + parent.getClass().getName()
                                  + " has Entry.OfDoc with null doc");
                        }
                        UUID id = d.uuid();
                        if (id == null || docRegistry.resolve(id) == null) {
                            throw new IllegalStateException(
                                    "Catalogue " + parent.getClass().getName()
                                  + " references Doc " + d.getClass().getName()
                                  + " (uuid=" + id + ") which is not in the DocRegistry");
                        }
                        docHomeMap.putIfAbsent(id, parent);
                        // RFC 0015 Phase 6: when the doc is a PlanDoc, register the
                        // wrapped Plan's class in planHomeMap so the existing
                        // breadcrumbsForPlan(class) API continues to work for
                        // Plans surfaced via the unified Doc family.
                        if (d instanceof hue.captains.singapura.js.homing.studio.base.tracker.PlanDoc pd) {
                            planHomeMap.putIfAbsent(pd.plan().getClass(), parent);
                        }
                    }
                    // RFC 0015 Phase 6: OfApp / OfPlan cases removed. Plans
                    // and Navigables now flow through OfDoc(PlanDoc/AppDoc);
                    // their validation falls through the OfDoc branch above.
                    // planHomeMap registration moved into the OfDoc branch
                    // (when doc is a PlanDoc, register the wrapped Plan's
                    // class as having this catalogue as home).
                    case Entry.OfIllustration<?>(CatalogueIllustration illustration) -> {
                        // No registry-side validation — illustrations are
                        // decoration, not addressable content. Boot accepts
                        // any non-null illustration body (validated in the
                        // record's compact constructor).
                    }
                    case Entry.OfStudio<?, ?>(StudioProxy<?> proxy) -> {
                        if (proxy == null) {
                            throw new IllegalStateException(
                                    "Catalogue " + parent.getClass().getName()
                                  + " has Entry.OfStudio with null StudioProxy");
                        }
                        L0_Catalogue<?> source = proxy.source();
                        if (!byClass.containsKey(source.getClass())) {
                            throw new IllegalStateException(
                                    "Catalogue " + parent.getClass().getName()
                                  + " has a StudioProxy wrapping " + source.getClass().getName()
                                  + " which is not in the registered catalogue list."
                                  + " RFC 0011: the wrapped source L0 must be registered.");
                        }
                    }
                }
            }
        }

        // RFC 0016 — fold in extra docHome entries (e.g. tree-leaf docs
        // registered under their tree's host catalogue). Catalogue-leaf
        // entries already in docHomeMap take precedence.
        if (extraDocHomes != null) {
            for (var entry : extraDocHomes.entrySet()) {
                docHomeMap.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        this.byClass  = Map.copyOf(byClass);
        this.docHome  = Map.copyOf(docHomeMap);
        this.planHome = Map.copyOf(planHomeMap);
        this.proxyManager = (proxyManager != null) ? proxyManager
                                                   : StudioProxyManager.scan(byClass.values());
    }

    private static void requireValid(Catalogue<?> c) {
        Objects.requireNonNull(c, "catalogue must not be null");
        if (c.name() == null || c.name().isBlank()) {
            throw new IllegalStateException(
                    "Catalogue " + c.getClass().getName() + " has null/blank name()");
        }
    }

    /** Declared parent via sealed-exhaustive switch. Null for L0 (no parent). */
    private static Catalogue<?> declaredParentOf(Catalogue<?> c) {
        return switch (c) {
            case L0_Catalogue<?> l0    -> null;
            case L1_Catalogue<?, ?> l1 -> l1.parent();
            case L2_Catalogue<?, ?> l2 -> l2.parent();
            case L3_Catalogue<?, ?> l3 -> l3.parent();
            case L4_Catalogue<?, ?> l4 -> l4.parent();
            case L5_Catalogue<?, ?> l5 -> l5.parent();
            case L6_Catalogue<?, ?> l6 -> l6.parent();
            case L7_Catalogue<?, ?> l7 -> l7.parent();
            case L8_Catalogue<?, ?> l8 -> l8.parent();
        };
    }

    /** The numeric level (0..8) of a catalogue. */
    public static int levelOf(Catalogue<?> c) {
        return switch (c) {
            case L0_Catalogue<?> l0    -> 0;
            case L1_Catalogue<?, ?> l1 -> 1;
            case L2_Catalogue<?, ?> l2 -> 2;
            case L3_Catalogue<?, ?> l3 -> 3;
            case L4_Catalogue<?, ?> l4 -> 4;
            case L5_Catalogue<?, ?> l5 -> 5;
            case L6_Catalogue<?, ?> l6 -> 6;
            case L7_Catalogue<?, ?> l7 -> 7;
            case L8_Catalogue<?, ?> l8 -> 8;
        };
    }

    // -----------------------------------------------------------------------
    // Lookups
    // -----------------------------------------------------------------------

    public StudioBrand brand() { return brand; }

    public StudioProxyManager proxyManager() { return proxyManager; }

    public Catalogue<?> resolve(Class<? extends Catalogue<?>> cls) {
        return byClass.get(cls);
    }

    public Catalogue<?> parentOf(Class<? extends Catalogue<?>> cls) {
        Catalogue<?> at = byClass.get(cls);
        return at == null ? null : declaredParentOf(at);
    }

    public List<Catalogue<?>> breadcrumbs(Class<? extends Catalogue<?>> cls) {
        Catalogue<?> at = byClass.get(cls);
        if (at == null) return List.of();
        return breadcrumbs(at);
    }

    public List<Catalogue<?>> breadcrumbs(Catalogue<?> at) {
        List<Catalogue<?>> chain = new ArrayList<>();
        Catalogue<?> cursor = at;
        while (cursor != null) {
            chain.add(cursor);
            cursor = declaredParentOf(cursor);
        }
        Collections.reverse(chain);
        return augmentForProxy(chain);
    }

    /**
     * RFC 0011: if this chain's root is hosted by an umbrella catalogue, prepend
     * the umbrella's chain so the breadcrumb spans both trees.
     *
     * <p>chain[0] is always the L0 root (typed-walk via parent() invariant).
     * If isHosted(rootClass), prepend umbrella-chain (umbrella-root → … → host)
     * to the full source chain (source L0 → … → leaf). The source L0 is kept —
     * it's a meaningful navigation rung between the host tile and the source
     * sub-tree (e.g. {@code Homing Studios / Core / Homing / Building Blocks}).</p>
     */
    @SuppressWarnings("unchecked")
    private List<Catalogue<?>> augmentForProxy(List<Catalogue<?>> chain) {
        if (chain.isEmpty()) return List.copyOf(chain);
        Catalogue<?> root = chain.get(0);
        if (!(root instanceof L0_Catalogue<?>)) return List.copyOf(chain);
        Class<? extends L0_Catalogue<?>> rootClass =
                (Class<? extends L0_Catalogue<?>>) root.getClass();
        if (!proxyManager.isHosted(rootClass)) return List.copyOf(chain);
        Catalogue<?> host = proxyManager.hostFor(rootClass);
        // Walk the host's own typed chain (umbrella-root → … → host), then
        // append the full source chain (source L0 → … → leaf). The source L0
        // sits between the host tile and its descendants as a navigation rung.
        List<Catalogue<?>> umbrella = new ArrayList<>();
        Catalogue<?> cursor = host;
        while (cursor != null) {
            umbrella.add(cursor);
            cursor = declaredParentOf(cursor);
        }
        Collections.reverse(umbrella);
        List<Catalogue<?>> out = new ArrayList<>(umbrella);
        out.addAll(chain);
        return List.copyOf(out);
    }

    public List<Catalogue<?>> breadcrumbsForDoc(UUID docId) {
        Catalogue<?> home = docHome.get(docId);
        return home == null ? List.of() : breadcrumbs(home);
    }

    public List<Catalogue<?>> breadcrumbsForPlan(Class<? extends Plan> cls) {
        Catalogue<?> home = planHome.get(cls);
        return home == null ? List.of() : breadcrumbs(home);
    }

    public Collection<Catalogue<?>> all() {
        return Collections.unmodifiableCollection(byClass.values());
    }

    public int size() {
        return byClass.size();
    }
}
