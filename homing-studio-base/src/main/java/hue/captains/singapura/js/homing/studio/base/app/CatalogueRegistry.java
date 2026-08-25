package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;
import hue.captains.singapura.js.homing.core.AppModule;
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
 *
 * <p>RFC 0051 adds four more, which together make "the path of a being" a
 * total, deterministic function — the precondition for URL/breadcrumb
 * parity:</p>
 *
 * <ol>
 *   <li><b>Law 1, at most one position</b> — no {@code (app, args)} and no
 *       doc sits in two catalogues. Differing tile framing does not make a
 *       second being.</li>
 *   <li><b>Law 2, distinct sibling segments</b> — a parent's sub-catalogues
 *       and addressable leaves share one {@link NodeName} namespace.</li>
 *   <li><b>Law 3, positioned implies resolvable</b> — every registered
 *       non-root catalogue is claimed by a parent, so the downward walk
 *       reaches everything the upward walk describes.</li>
 *   <li><b>Law 4, one root</b> — exactly one un-hosted L0, and it is the
 *       brand's home.</li>
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
        var navHomeMap  = new HashMap<NavKey, Catalogue<?>>();
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

            // ---- RFC 0051 Law 2: siblings have distinct slugs ----
            // A path segment picks exactly one child, so sub-catalogues and
            // addressable leaves share ONE namespace under a parent — a
            // sub-catalogue named the same as a leaf is just as ambiguous as
            // two leaves named alike. Illustrations sit out: they are
            // decoration, never a path segment, so they cannot be ambiguous.
            var slugOwner = new HashMap<NodeName, Object>();
            for (Catalogue<?> child : subs) {
                claimSlug(slugOwner, child.slug(), child, parent);
            }
            for (Entry<?> e : parent.leaves() == null ? List.<Entry<?>>of() : parent.leaves()) {
                CatalogueLeaf leaf = switch (e) {
                    case Entry.OfDoc<?, ?>(Doc d)              -> d;
                    case Entry.OfStudio<?, ?>(StudioProxy<?> p) -> p;
                    case Entry.OfIllustration<?> ignored        -> null;
                    case null                                   -> null;
                };
                if (leaf != null) {
                    claimSlug(slugOwner, leaf.slug(), leaf, parent);
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
                        // RFC 0051 Law 1 — AT MOST ONE POSITION. A being may be
                        // reached from anywhere, but it SITS in exactly one place:
                        // that is what makes its tree path well-defined, and so
                        // deterministic rather than an artifact of scan order.
                        // This was putIfAbsent, which silently kept whichever
                        // placement the boot scan reached first. The rule already
                        // holds one level up — StudioProxyManager rejects a source
                        // L0 hosted twice, and DocRegistry rejects a UUID used by
                        // two Docs — so leaves were the level that never got it.
                        //
                        // Re-registering the SAME position is fine: a value-Doc
                        // (e.g. PlanDoc(MyPlan.INSTANCE)) can be harvested more
                        // than once from one catalogue. Two DIFFERENT homes is the
                        // violation.
                        Catalogue<?> priorHome = docHomeMap.put(id, parent);
                        if (priorHome != null && priorHome != parent) {
                            throw new IllegalStateException(
                                    "Doc " + d.getClass().getName() + " (uuid=" + id + ")"
                                  + " is positioned in two catalogues: "
                                  + priorHome.getClass().getName() + " and "
                                  + parent.getClass().getName()
                                  + ". A navigable has at most ONE position (RFC 0051 - the path axiom);"
                                  + " keep the canonical entry and drop the echo.");
                        }
                        // RFC 0051 Law 1, app half. AppDoc.uuid() seeds from the
                        // WHOLE Navigable — name and summary included — so two
                        // tiles for the same (app, args) with different display
                        // framings get different UUIDs and slide past the check
                        // above. That is exactly the "multiple framings of one
                        // navigable" the axiom rules out: identity is (app, args),
                        // and the framing is only how one position is dressed.
                        // So key this check on (app class, params) alone.
                        if (d instanceof AppDoc<?, ?> ad) {
                            var key = new NavKey(ad.nav().app().getClass(), ad.nav().params());
                            Catalogue<?> priorNavHome = navHomeMap.put(key, parent);
                            if (priorNavHome != null && priorNavHome != parent) {
                                throw new IllegalStateException(
                                        "App " + key.app().getName() + " with params " + key.params()
                                      + " is positioned in two catalogues: "
                                      + priorNavHome.getClass().getName() + " and "
                                      + parent.getClass().getName()
                                      + ". A navigable is identified by (app, args) and has at most ONE"
                                      + " position (RFC 0051 - the path axiom). A differing tile name or"
                                      + " summary does not make it a second being; keep the canonical entry.");
                            }
                        }
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

        requirePositionedAreResolvable();
        requireSingleUnhostedRoot();
    }

    /**
     * RFC 0051 Law 3 — POSITIONED IMPLIES RESOLVABLE: the upward chain and the
     * downward tree describe the same tree.
     *
     * <p>Breadcrumbs walk UP through {@code parent()}, and the CRTP level types
     * already guarantee that walk ends at an L0 — checking for it would be
     * vacuous. The forward path resolver walks DOWN through
     * {@code subCatalogues()}, and nothing guaranteed the two agree. The
     * existing parent-match check verifies one direction: a child that a parent
     * lists must name that parent back. The converse was never checked, so a
     * registered catalogue that its declared parent does not list is invisible
     * going down while still producing a confident breadcrumb going up — a URL
     * the crumb trail shows and the resolver cannot walk.</p>
     *
     * <p>A hosted L0 is claimed by its {@link StudioProxy} rather than by a
     * {@code subCatalogues()} entry, which is the same fact in the umbrella's
     * vocabulary; the root is claimed by being the root.</p>
     */
    private void requirePositionedAreResolvable() {
        var claimed = new HashMap<Class<?>, Catalogue<?>>();
        for (Catalogue<?> parent : byClass.values()) {
            for (Catalogue<?> child : parent.subCatalogues()) {
                claimed.put(child.getClass(), parent);
            }
        }
        for (Catalogue<?> c : byClass.values()) {
            // L0s are out of scope here: the level types forbid an L0 from
            // appearing in any subCatalogues(), so "claimed by a parent" is not
            // a question that can be asked of one. Roots are Law 4's business.
            if (c instanceof L0_Catalogue<?>) continue;
            if (claimed.containsKey(c.getClass())) continue;
            Catalogue<?> declared = declaredParentOf(c);
            throw new IllegalStateException(
                    "Catalogue " + c.getClass().getName() + " is registered and names "
                  + (declared == null ? "no parent" : declared.getClass().getName() + " as its parent")
                  + ", but no parent lists it in subCatalogues()."
                  + " Its breadcrumbs would resolve while the path to it could not be walked"
                  + " (RFC 0051 - Law 3, positioned implies resolvable).");
        }
    }

    /**
     * RFC 0051 Law 4 — ONE ROOT. Every path needs a place to start, and an
     * L0 that no umbrella hosts is by definition a place someone can start.
     * Two such roots means a path prefix is ambiguous; none means the tree
     * has no entrance at all.
     *
     * <p>The brand's home-app is the root. This checks that the structure
     * agrees with the brand rather than merely that the brand names something
     * registered — an umbrella deployment that forgets one proxy leaves a
     * second reachable root, and the URL scheme would have no way to say
     * which studio a bare path belongs to.</p>
     */
    private void requireSingleUnhostedRoot() {
        var unhosted = new ArrayList<Catalogue<?>>();
        for (Catalogue<?> c : byClass.values()) {
            if (c instanceof L0_Catalogue<?> && !proxyManager.isHosted(asL0Class(c))) {
                unhosted.add(c);
            }
        }
        if (unhosted.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one un-hosted L0 catalogue (the root every path"
                  + " starts from), found " + unhosted.size() + ": "
                  + unhosted.stream().map(c -> c.getClass().getName()).toList()
                  + " (RFC 0051 - Law 4). An L0 no umbrella hosts is a second entrance;"
                  + " wrap it in a StudioProxy under the umbrella, or drop it.");
        }
        Catalogue<?> root = unhosted.get(0);
        if (root.getClass() != brand.homeApp()) {
            throw new IllegalStateException(
                    "The un-hosted root is " + root.getClass().getName()
                  + " but StudioBrand.homeApp names " + brand.homeApp().getName()
                  + ". The brand's home and the structural root must be the same"
                  + " catalogue (RFC 0051 - Law 4), or a path and the home button"
                  + " would disagree about where the tree begins.");
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends L0_Catalogue<?>> asL0Class(Catalogue<?> c) {
        return (Class<? extends L0_Catalogue<?>>) c.getClass();
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

    /**
     * RFC 0051 Law 2 — record one sibling's claim on a slug, or fail.
     *
     * <p>The claimant compared is the child VALUE, not its description: several
     * distinct {@code ComposedDoc}s under one catalogue share a class and would
     * describe identically, so comparing descriptions would wave through exactly
     * the collisions this law exists to catch. Records give value equality, so
     * the same leaf listed twice still passes while two different leaves that
     * happen to derive the same segment do not.</p>
     */
    private static void claimSlug(Map<NodeName, Object> owner,
                                  NodeName slug,
                                  Object claimant,
                                  Catalogue<?> parent) {
        if (slug == null) {
            throw new IllegalStateException(
                    describe(claimant) + " under " + parent.getClass().getName()
                  + " has a null slug()");
        }
        Object prior = owner.put(slug, claimant);
        if (prior != null && !prior.equals(claimant)) {
            throw new IllegalStateException(
                    "Two children of " + parent.getClass().getName()
                  + " claim the path segment '" + slug + "': "
                  + describe(prior) + " and " + describe(claimant)
                  + ". A segment must pick exactly one child (RFC 0051 - Law 2);"
                  + " override slug() on one of them.");
        }
    }

    /** A child's name for error messages — its class, plus a display label for
     *  value-children where the class alone would not say which one. */
    private static String describe(Object child) {
        if (child == null) return "null";
        String cls = child.getClass().getName();
        if (child instanceof AppDoc<?, ?> ad)  return cls + "(" + ad.nav().name() + ")";
        if (child instanceof StudioProxy<?> p) return cls + "(" + p.name() + ")";
        if (child instanceof Doc d)            return cls + "(" + d.title() + ")";
        return cls;
    }

    /**
     * RFC 0051 — the identity of a navigable, and nothing else. An app class
     * plus its typed params record; the params record's own {@code equals}
     * carries the args comparison, which is why {@code AppModule._Param}
     * implementations are records.
     *
     * <p>Deliberately excludes the tile's display {@code name}/{@code summary}:
     * those are framing, and the path axiom says one {@code (app, args)} has
     * at most one position regardless of how that position is dressed.</p>
     */
    private record NavKey(Class<?> app, AppModule._Param params) {}
}
