package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.QueryString;
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
    /** RFC 0051 — per-catalogue slug → child (Catalogue, Doc or StudioProxy). */
    private final Map<Class<?>, Map<NodeName, Object>> childIndex;
    /** RFC 0051 D4 — catalogue FQN → instance, for redirecting flat catalogue URLs. */
    private final Map<String, Catalogue<?>> byClassName;
    /** RFC 0051 D4 — which query keys identify a node, per app. */
    private final Map<String, java.util.Set<String>> flatIdentityKeys;
    /** RFC 0051 D4 — canonical flat address → path. */
    private final Map<String, CataloguePath> flatToPath;
    /** RFC 0051 Phase 5 — canonical flat address to the doc it names, so the
     *  chrome can be resolved server-side instead of fetched. */
    private final Map<String, Doc> flatToDoc;

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
     * not appear in the doc-home index, and the trail stamped into their
     * page would stop at the tree's host instead of reaching the leaf.
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
        var childIndexMap = new HashMap<Class<?>, Map<NodeName, Object>>();
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
            var slugOwner = new LinkedHashMap<NodeName, Object>();
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
            // RFC 0051 Phase 2 — the scan that proves segments unique is the
            // one that builds the downward index, so the map the resolver
            // walks is by construction the map the law checked. Building it
            // separately would let the two drift.
            childIndexMap.put(parent.getClass(), Map.copyOf(slugOwner));

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
        this.childIndex = Map.copyOf(childIndexMap);
        this.proxyManager = (proxyManager != null) ? proxyManager
                                                   : StudioProxyManager.scan(byClass.values());

        requirePositionedAreResolvable();
        requireSingleUnhostedRoot();

        // RFC 0051 D4 — invert each positioned doc's own flat URL, so a raw
        // (app, args) can be redirected to its path.
        var byName = new HashMap<String, Catalogue<?>>();
        for (Catalogue<?> c : byClass.values()) byName.put(c.getClass().getName(), c);
        this.byClassName = Map.copyOf(byName);

        var keysByApp = new HashMap<String, java.util.Set<String>>();
        var flat      = new HashMap<String, CataloguePath>();
        var flatDocs  = new HashMap<String, Doc>();
        for (UUID id : docHome.keySet()) {
            Doc d = docRegistry.resolve(id);
            if (d == null) continue;
            String url = d.url();
            if (url == null) continue;
            Map<String, List<String>> args = QueryString.parse(url);
            String app = QueryString.first(args, "app");
            if (app == null) continue;
            // The doc's own URL defines which keys identify it. "app" is the
            // dispatch, not an argument.
            var identity = new java.util.TreeSet<>(args.keySet());
            identity.remove("app");
            keysByApp.computeIfAbsent(app, k -> new java.util.TreeSet<>()).addAll(identity);
            CataloguePath path = pathOf(d);
            if (path != null) {
                flat.put(flatKey(app, args, identity), path);
                flatDocs.put(flatKey(app, args, identity), d);
            }
        }
        this.flatIdentityKeys = Map.copyOf(keysByApp);
        this.flatToPath       = Map.copyOf(flat);
        this.flatToDoc        = Map.copyOf(flatDocs);
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

    // ---------------------------------------------------------------------
    // RFC 0051 Phase 2 — the path/node bijection.
    //
    // The registry already held byClass (FQN → catalogue) and docHome (UUID →
    // catalogue). Neither is a position: both answer "which node" without
    // saying where the node sits, so neither can produce a URL or read one.
    // The two methods below are inverses, and the laws are what make them so —
    // Law 2 gives the downward step exactly one candidate per segment, Law 1
    // gives the upward walk exactly one answer, and Laws 3 and 4 make the two
    // walks meet at the same root.
    // ---------------------------------------------------------------------

    /** The root every path starts from — the single un-hosted L0 (Law 4). */
    public Catalogue<?> root() {
        return byClass.get(brand.homeApp());
    }

    /**
     * RFC 0051 D4 — the path a flat {@code (app, args)} address belongs to,
     * or null when it names nothing positioned.
     *
     * <p>This is what lets a raw URL self-correct without every emitter having
     * to learn about paths. Q3 settled that emitters keep minting
     * {@code (app, args)} and the server owns positions — cross-references in
     * doc content are the case that makes it worth it: they are authored
     * against a UUID, and a UUID has no idea where its doc sits.</p>
     *
     * <p>The index is built from each positioned doc's OWN {@code url()}, so
     * the flat form it is keyed by is the same string that doc would emit.
     * Nothing here re-derives how a doc kind is addressed; it reads what the
     * doc says and inverts it.</p>
     *
     * <p>Only the identity keys participate. A request carrying
     * {@code ?phase=2} still matches the plan it names, because {@code phase}
     * was never part of any doc's canonical URL — which is also why the
     * redirect can carry it across.</p>
     */
    public CataloguePath pathForFlat(String app, Map<String, List<String>> args) {
        if (app == null) return null;
        // A catalogue names its node directly by class, not through a doc.
        if (CatalogueAppHost.INSTANCE.simpleName().equals(app)) {
            List<String> ids = args == null ? null : args.get("id");
            if (ids == null || ids.isEmpty()) return null;
            Catalogue<?> node = byClassName.get(ids.get(0));
            return node == null ? null : pathOf(node);
        }
        java.util.Set<String> keys = flatIdentityKeys.get(app);
        if (keys == null) return null;
        return flatToPath.get(flatKey(app, args, keys));
    }

    /**
     * RFC 0051 Phase 5 — the page chrome for a flat {@code (app, args)}: the
     * title and the breadcrumb trail, resolved server-side.
     *
     * <p>This is the same answer {@code /doc-refs} and {@code /app-refs} give,
     * arrived at without a round trip. The chrome fetching its own crumb after
     * render is what produces the pop-in: the page paints, then the trail
     * appears. The server already resolved the node to answer the request, so
     * the fetch was re-deriving something it had.</p>
     *
     * <p>Returns null when the address names nothing positioned — an
     * unpositioned app has no trail to state, and inventing one would be
     * worse than leaving the chrome to say only where it is.</p>
     */
    public List<Crumb> chromeCrumbsForFlat(String app, Map<String, List<String>> args) {
        Doc doc = flatDoc(app, args);
        Catalogue<?> node = (doc == null) ? catalogueForFlat(app, args) : docHome.get(doc.uuid());
        if (node == null) return null;
        var out = new ArrayList<Crumb>();
        for (Catalogue<?> c : breadcrumbs(node)) {
            CataloguePath p = pathOf(c);
            out.add(new Crumb(crumbTextOf(c), p == null ? "" : p.toUrl()));
        }
        if (doc != null) {
            out.add(new Crumb(doc.title(), ""));
        } else if (!out.isEmpty()) {
            // A catalogue page's last crumb IS the page. Blank its href for the
            // same reason a doc's is blank, and the same reason
            // CatalogueGetAction blanks it: you are already there, and a
            // self-link in a trail is a dead control.
            Crumb self = out.remove(out.size() - 1);
            out.add(new Crumb(self.text(), ""));
        }
        return out;
    }

    /**
     * The doc a flat {@code (app, args)} address names, or null.
     *
     * <p>Public so a caller that knows something this registry does not — a
     * tree-leaf's position INSIDE its content tree, say — can key that
     * knowledge by the same address.</p>
     */
    public Doc docForFlat(String app, Map<String, List<String>> args) {
        return flatDoc(app, args);
    }

    /** The doc a flat address names, if any. */
    private Doc flatDoc(String app, Map<String, List<String>> args) {
        var keys = (app == null) ? null : flatIdentityKeys.get(app);
        return keys == null ? null : flatToDoc.get(flatKey(app, args, keys));
    }

    /** The catalogue a flat address names, for the catalogue app. */
    private Catalogue<?> catalogueForFlat(String app, Map<String, List<String>> args) {
        if (!CatalogueAppHost.INSTANCE.simpleName().equals(app)) return null;
        String id = QueryString.first(args, "id");
        return id == null ? null : byClassName.get(id);
    }

    /** RFC 0009 — crumb text is the icon glyph plus the name, when there is one. */
    static String crumbTextOf(Catalogue<?> c) {
        String icon = c.icon();
        return (icon == null || icon.isEmpty()) ? c.name() : icon + " " + c.name();
    }

    /** Which query keys identify a node for this app — the rest are refinements
     *  a redirect should carry across rather than absorb. */
    public java.util.Set<String> flatIdentityKeysFor(String app) {
        var keys = (app == null) ? null : flatIdentityKeys.get(app);
        if (keys != null) return keys;
        // A catalogue is addressed by class rather than through a doc, so it
        // has no entry in the doc-derived index.
        return CatalogueAppHost.INSTANCE.simpleName().equals(app)
                ? java.util.Set.of("id") : java.util.Set.of();
    }

    /** The lookup key: the app plus only the args that identify a node. */
    private static String flatKey(String app, Map<String, List<String>> args,
                                  java.util.Collection<String> identityKeys) {
        var sb = new StringBuilder(app);
        for (String k : new java.util.TreeSet<>(identityKeys)) {
            String v = (args == null) ? null : QueryString.first(args, k);
            sb.append('|').append(k).append('=').append(v == null ? "" : v);
        }
        return sb.toString();
    }

    /**
     * Walk a path down the tree.
     *
     * <p>An empty path is the root itself, so {@code /cat} is the studio's
     * front door rather than a miss.</p>
     */
    public PathResolution resolve(CataloguePath path) {
        Objects.requireNonNull(path, "path");
        Catalogue<?> at = root();
        for (int i = 0; i < path.depth(); i++) {
            NodeName segment = path.segments().get(i);
            Object child = childIndex.getOrDefault(at.getClass(), Map.of()).get(segment);
            if (child == null) {
                return new PathResolution.Miss(path, i, PathResolution.Reason.NO_SUCH_CHILD);
            }
            switch (child) {
                // A proxy stands for the studio it wraps, so descending through
                // an umbrella tile continues into the source tree — the same
                // rung the breadcrumb shows (RFC 0011).
                case StudioProxy<?> proxy -> at = proxy.source();
                case Catalogue<?> c       -> at = c;
                case Doc d -> {
                    if (i < path.depth() - 1) {
                        return new PathResolution.Miss(path, i + 1, PathResolution.Reason.PAST_A_LEAF);
                    }
                    return new PathResolution.ToLeaf(path, at, d);
                }
                default -> throw new IllegalStateException(
                        "Unexpected child kind in the path index: " + child.getClass().getName());
            }
        }
        return new PathResolution.ToCatalogue(path, at);
    }

    /** Convenience: resolve straight from a URL. A URL that is not a catalogue
     *  path at all is a miss at segment 0, not an exception. */
    public PathResolution resolveUrl(String url) {
        CataloguePath path = CataloguePath.parse(url);
        return path == null
                ? new PathResolution.Miss(new CataloguePath(List.of()), 0,
                                          PathResolution.Reason.NO_SUCH_CHILD)
                : resolve(path);
    }

    /**
     * The path of a catalogue node — the inverse of {@link #resolve}.
     *
     * <p>Derived from {@link #breadcrumbs(Catalogue)}, so the URL and the crumb
     * trail are not merely consistent: they are the same walk rendered two
     * ways. That is the parity RFC 0051 is named for, and deriving the path
     * from anywhere else is precisely how the two would drift apart.</p>
     *
     * <p>The leading rung is dropped — it is the root, which the {@code cat}
     * prefix already stands for.</p>
     */
    public CataloguePath pathOf(Catalogue<?> at) {
        Objects.requireNonNull(at, "at");
        List<Catalogue<?>> chain = breadcrumbs(at);
        var segments = new ArrayList<NodeName>();
        for (int i = 1; i < chain.size(); i++) segments.add(chain.get(i).slug());
        return new CataloguePath(segments);
    }

    /**
     * The path of a positioned doc: its home catalogue's path plus its own
     * segment. Null when the doc has no position — Law 1 allows at most one,
     * it does not require any, and docs harvested from content trees have
     * none.
     */
    public CataloguePath pathOf(Doc doc) {
        Objects.requireNonNull(doc, "doc");
        Catalogue<?> home = docHome.get(doc.uuid());
        return home == null ? null : pathOf(home).then(doc.slug());
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
