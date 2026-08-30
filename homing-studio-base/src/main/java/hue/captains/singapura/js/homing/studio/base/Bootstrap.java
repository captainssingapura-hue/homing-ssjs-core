package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.SimpleAppResolver;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.server.AppMeta;
import hue.captains.singapura.js.homing.server.CssContentGetAction;
import hue.captains.singapura.js.homing.server.HomingActionRegistry;
import hue.captains.singapura.js.homing.server.QueryParamResolver;
import hue.captains.singapura.js.homing.server.RootRedirectGetAction;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.studio.base.app.BrandGetAction;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.graph.StudioGraph;
import hue.captains.singapura.js.homing.studio.base.graph.StudioGraphBuilder;
import hue.captains.singapura.js.homing.studio.base.graph.DiagnosticsCatalogue;
import hue.captains.singapura.js.homing.studio.base.graph.StudioGraphInspector;
import hue.captains.singapura.js.homing.studio.base.graph.StudioGraphMarkdownAction;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueAppHost;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueGetAction;
import hue.captains.singapura.js.homing.studio.base.app.CataloguePathGetAction;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueRegistry;
import hue.captains.singapura.js.homing.studio.base.app.GotoNavigableGetAction;
import hue.captains.singapura.js.homing.studio.base.app.StudioBrand;
import hue.captains.singapura.js.homing.studio.base.theme.CssGroupImplRegistry;
import hue.captains.singapura.js.homing.studio.base.theme.ThemesGetAction;
import hue.captains.singapura.js.homing.studio.base.app.DocTreeViewer;
import hue.captains.singapura.js.homing.studio.base.composed.DocTreeContentGetAction;
import hue.captains.singapura.js.homing.studio.base.composed.DocTreeGetAction;
import hue.captains.singapura.js.homing.studio.base.widget.SingleWidgetWorkspace;
import hue.captains.singapura.js.homing.studio.base.tracker.Plan;
import hue.captains.singapura.js.homing.studio.base.tracker.PlanGetAction;
import hue.captains.singapura.js.homing.studio.base.tracker.PlanRegistry;
import hue.captains.singapura.tao.http.action.ActionRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.PostAction;
import hue.captains.singapura.tao.http.config.HostConfig;
import hue.captains.singapura.tao.http.vertx.VertxActionHost;
import hue.captains.singapura.tao.ontology.ValueObject;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * RFC 0012 — the typed studio bootstrap. Construct with a {@link Fixtures}
 * and a {@link RuntimeParams}; call {@link #start()}. No static methods, no
 * INSTANCE field, no parameter explosion — the record IS the functional
 * object, satisfying the Functional Objects doctrine by construction.
 *
 * <pre>{@code
 * // Standalone studio.
 * new Bootstrap<>(
 *         new DefaultFixtures<>(new Umbrella.Solo<>(HomingStudio.INSTANCE)),
 *         new DefaultRuntimeParams(8080)
 * ).start();
 *
 * // Multi-studio composition.
 * Umbrella<Studio<?>> tree = new Umbrella.Group<>("Homing Demo", "Three studios, one server.",
 *         List.of(new Umbrella.Solo<>(MultiStudio.INSTANCE),
 *                 new Umbrella.Solo<>(DemoBaseStudio.INSTANCE),
 *                 new Umbrella.Solo<>(SkillsStudio.INSTANCE),
 *                 new Umbrella.Solo<>(HomingStudio.INSTANCE)));
 * new Bootstrap<>(new DefaultFixtures<>(tree), new DefaultRuntimeParams(8082)).start();
 * }</pre>
 *
 * @param <S> the studio type at the umbrella's leaves
 * @param <F> the {@link Fixtures} subtype harnessing {@code S}
 */
public record Bootstrap<S extends Studio<?>, F extends Fixtures<S>>(
        F fixtures,
        RuntimeParams params) implements ValueObject {

    public Bootstrap {
        Objects.requireNonNull(fixtures, "fixtures");
        Objects.requireNonNull(params,   "params");
    }

    /**
     * Compose the studio set into a typed action registry and boot the
     * Vert.x host. Blocks only briefly during registry construction; the
     * Vert.x server starts asynchronously.
     */
    public void start() {
        var registry = compose();
        // The on/off HTTPS switch: TLS present -> https, absent -> plain http.
        var config = params.tls()
                .map(tls -> HostConfig.https(params.port(), tls))
                .orElseGet(() -> HostConfig.http(params.port()));
        var host = new VertxActionHost(registry, config);
        host.start().onSuccess(server -> {
            int actualPort = server.actualPort();
            String scheme = params.tls().isPresent() ? "https" : "http";
            System.out.println("Studio listening on " + scheme + " port " + actualPort);
            for (var studio : fixtures.umbrella().studios()) {
                System.out.println("  · " + studio.getClass().getSimpleName()
                        + " (home: " + studio.home().getClass().getSimpleName() + ")");
            }
        }).onFailure(err -> {
            System.err.println("Failed to start: " + err.getMessage());
            System.exit(1);
        });
    }

    /**
     * Build the typed in-memory object graph from this Bootstrap's composed
     * studio set (RFC 0014). Eager construction; the returned graph is
     * immutable and queryable via Stream-based primitives.
     *
     * <p>Internally delegates to {@link StudioGraphBuilder#INSTANCE}'s
     * functional-object walk over the typed primitives reachable from this
     * Bootstrap — Fixtures → Umbrella → Studios → Catalogues → Entries →
     * Docs / AppModules / Plans, plus typed cross-references (DocReferences,
     * Phase dependencies).</p>
     */
    public StudioGraph graph() {
        return StudioGraphBuilder.INSTANCE.build(this);
    }

    /**
     * Compose the studio set into an {@link ActionRegistry} without starting
     * Vert.x — useful for tests, or for downstream that wants its own host.
     */
    public ActionRegistry<RoutingContext> compose() {
        var studios = fixtures.umbrella().studios();
        if (studios.isEmpty()) {
            throw new IllegalArgumentException("Bootstrap.compose: umbrella has no studios");
        }

        // --- Union apps: each studio's intrinsic apps + harness apps + (when
        // diagnostics is enabled) the StudioGraphInspector. Dedup by class.
        var harnessApps = new ArrayList<>(fixtures.harnessApps());
        // RFC 0040 — the leveled-Open shell is a framework default: every
        // studio gets ?app=singleWidgetWorkspace (+ the /open-content,
        // /open-refs endpoints below) with zero downstream wiring, so opening
        // a Navigator leaf stays in the leveled world (path URL, no uuid).
        harnessApps.add(SingleWidgetWorkspace.INSTANCE);
        // RFC 0039 — the rigid-tree ComposedDoc viewer (parallel to the legacy
        // ComposedViewer): ?app=doc-tree-viewer&id=<uuid> serves the two-part
        // doc-tree payload via /doc-tree (registered below).
        harnessApps.add(DocTreeViewer.INSTANCE);
        // RFC 0053 - the catalogue as a TREE, drawn from the normalized forest
        // rather than the routing index: ?app=catalogue-tree, backed by
        // /catalogue-parity below. Read-only and off the resolution path.
        harnessApps.add(hue.captains.singapura.js.homing.studio.base.tree.CatalogueTreeView.INSTANCE);
        if (params.diagnosticsEnabled()) harnessApps.add(StudioGraphInspector.INSTANCE);
        var apps = unionAppsByClass(studios, harnessApps);
        if (apps.isEmpty()) {
            throw new IllegalArgumentException("Bootstrap.compose: at least one AppModule required");
        }

        // --- Union catalogues: each studio's catalogues(). Dedup by class.
        // RFC 0014: when diagnostics is enabled, the framework's own
        // DiagnosticsCatalogue (an L0) joins the union so its tiles surface
        // alongside the downstream studio's root catalogue(s) — multi-L0
        // navigation is supported, so this doesn't displace anything.
        var catalogues = new ArrayList<Catalogue<?>>(unionCataloguesByClass(studios));
        if (params.diagnosticsEnabled()) catalogues.add(DiagnosticsCatalogue.INSTANCE);

        // --- Union plans: each studio's plans() PLUS every Plan reachable as
        // a catalogue-leaf (Entry.OfDoc(PlanDoc(plan))). Defect 0005 resolution:
        // catalogue-leaf is the canonical registration site; Studio.plans() is
        // an additional explicit channel for URL-only plans (no catalogue tile).
        // Dedup by class so either registration path — or both — works.
        var plans = unionPlansByClass(studios, catalogues);

        // --- Brand: from fixtures (default is first studio's standaloneBrand).
        StudioBrand brand = fixtures.brand();
        if (!catalogues.isEmpty() && brand == null) {
            throw new IllegalArgumentException(
                    "Bootstrap.compose: a non-empty catalogues list requires a non-null StudioBrand "
                            + "(supply via Studio.standaloneBrand() or Fixtures.brand())");
        }

        // --- Theme registry + default theme + resource reader come from fixtures + params.
        ThemeRegistry themeRegistry = fixtures.themeRegistry();
        Theme defaultTheme = fixtures.defaultTheme();

        // --- Resolvers + registries.
        var nameResolver = new QueryParamResolver();
        var appResolver  = new SimpleAppResolver(apps);
        var rootApp      = apps.get(0); // legacy "first app" fallback when no catalogues

        var appMeta = (brand != null && brand.label() != null && !brand.label().isBlank())
                ? new AppMeta(brand.label())
                : AppMeta.DEFAULT;
        // RFC 0051 Phase 5 — the page stamps its own breadcrumb, which needs the
        // catalogue tree. The tree is built further down (it needs the doc
        // registry, which needs the app closure), so the resolver is late-bound
        // through a holder rather than passed by value. Set once during boot,
        // before anything serves; the alternative was reordering a construction
        // sequence whose order is itself a dependency chain.
        var treeHolder = new java.util.concurrent.atomic.AtomicReference<CatalogueRegistry>();
        // RFC 0040 forest root, hoisted: the chrome resolver below needs it to
        // stamp a leveled page, and it depends only on the studio list.
        final Catalogue<?> openRoot = studios.get(0).home();

        // RFC 0051 — the ChromeResolver is gone. This was a lambda from
        // (app, args) to a trail, handed to AppHtmlGetAction so a FLAT render
        // could show the breadcrumb of a position its own URL did not state.
        // That was the last second derivation in the system: everywhere else
        // the crumb IS the path, read off the address; here alone it was looked
        // up. D4 makes a raw (app, args) a permalink rather than a page, and a
        // permalink states no position — so it shows none, and /goto is what
        // answers "where does this live". The path route resolves the node
        // anyway and passes the crumbs it already holds.
        //
        // Sacrificed with it, deliberately and recorded in RFC 0053: the
        // leveled-Open stamp and the enriched tree-leaf trails. Both existed
        // to give a flat address a trail, and both are tree-shaped — a
        // DynamicCatalogue cannot hold a position, so its pages were flat and needed
        // rescuing. RFC 0053 gives those nodes real paths, at which point they
        // need no rescue.
        var inner = new HomingActionRegistry(
                nameResolver, appResolver, params.resourceReader(),
                themeRegistry, appMeta, fixtures.servableModuleClasses());

        // --- Doc registry — walk DocProviders from apps AND catalogues (RFC 0004 + RFC 0005).
        var docProviders = new ArrayList<DocProvider>();
        for (AppModule<?, ?> app : appResolver.apps()) {
            if (app instanceof DocProvider p) docProviders.add(p);
        }
        for (Catalogue<?> c : catalogues) {
            if (c instanceof DocProvider p) docProviders.add(p);
        }
        var allDocs = new ArrayList<Doc>();
        for (var p : docProviders) allDocs.addAll(p.docs());

        // RFC 0015 Phase 3b — harvest synthetic Docs (PlanDoc, AppDoc, future
        // ProxyDoc) from catalogue leaves. After the Entry factory rewire,
        // Entry.of(host, plan) creates OfDoc(PlanDoc(plan)); these synthetic
        // Docs don't flow through any DocProvider, so the harvest is the only
        // path into DocRegistry. Record value-equality lets duplicate
        // appearances across catalogues collapse safely.
        allDocs.addAll(DocRegistry.harvestSyntheticFromLeaves(catalogues));

        var docRegistry = new DocRegistry(allDocs);

        // --- Standard studio actions.
        var cssContentAction = new CssContentGetAction(CssGroupImplRegistry.ALL, defaultTheme);
        var docAction        = new DocGetAction(docRegistry);
        var themesAction     = new ThemesGetAction(themeRegistry);
        var brandAction      = new BrandGetAction(brand, !catalogues.isEmpty());

        // --- Root redirect: brand home catalogue (catalogues present) or first app.
        final RootRedirectGetAction rootRedirect = (!catalogues.isEmpty() && brand != null)
                ? RootRedirectGetAction.toUrl(CatalogueAppHost.urlFor(brand.homeApp()))
                : new RootRedirectGetAction(rootApp.simpleName());

        // --- Catalogue registry + action (RFC 0005), only when catalogues registered.
        final CatalogueGetAction catalogueAction;
        final CatalogueRegistry catalogueRegistry;
        if (!catalogues.isEmpty()) {
            catalogueRegistry = new CatalogueRegistry(brand, docRegistry, catalogues);
            // RFC 0051 Phase 2 — every position must survive the round trip
            // through its own URL. This follows from the boot laws, but Phase 1
            // is exactly where "follows from" proved untrustworthy twice: one
            // law was vacuous under the type system and another compared the
            // wrong thing, both while passing. The check is a few hundred map
            // lookups over the whole tree, so it costs nothing to stop
            // trusting the derivation and simply verify it — for every studio,
            // downstream ones included, at the moment a break is cheapest to
            // find.
            hue.captains.singapura.js.homing.studio.base.app.CataloguePathConformance
                    .assertPathBijection(catalogueRegistry);
                    // RFC 0053 - and the normalized forest must agree with it, both ways:
                    // distinct identities, nothing placed but absent, nothing resolving
                    // elsewhere. Same argument as above, one structure further out.
                    hue.captains.singapura.js.homing.studio.base.tree.CatalogueTreeParity
                            .assertForestAgrees(catalogueRegistry);
            // RFC 0051 Phase 5 — the tree exists now; the chrome resolver can see it.
            treeHolder.set(catalogueRegistry);
            // RFC 0053 / D8: the tile-injection mechanism is retired. It existed
            // only to push SyntheticEntry rows into the `entries` payload, and
            // that payload is gone - the listing draws the tree. DiagnosticsCatalogue
            // itself is still registered when the flag is on - though that flag has
            // been unbootable since RFC 0051 Law 4 landed: DiagnosticsCatalogue is a
            // SECOND un-hosted L0 and the registry rejects it. So nothing that
            // worked is lost here. What the mechanism took with it is the per-studio
            // &context= projection, which rendered ONE identity as two different
            // listings and could never have been made lawful.
            catalogueAction = new CatalogueGetAction(catalogueRegistry);
        } else {
            catalogueRegistry = null;
            catalogueAction   = null;
        }

        // --- Doc-refs action (RFC 0004-ext1 / RFC 0005-ext2).
        //
        // RFC 0051 — the enriched tree-leaf trails that used to be computed
        // here are gone with the ChromeResolver that consumed them. They gave
        // a tree leaf's FLAT page a breadcrumb by splicing the catalogue chain
        // onto the tree-internal one — a trail assembled for an address that
        // states no position. RFC 0053 gives those leaves real paths, at which
        // point the trail is the path and nothing needs splicing.

        var docRefsAction = new DocRefsGetAction(docRegistry);

        // --- App-refs action (RFC 0025 L2.2 — breadcrumb chain for AppModule
        // launches via Navigable entries, where the URL has no ?id=<uuid>).
        // Resolves the AppDoc by AppModule simpleName, serialises the chain
        // through the same shape as /doc-refs so the StandardMPA chrome can
        // use a uniform code path.

        // --- RFC 0040 — leveled Open endpoints. Rooted at the primary studio's
        // home() (the forest root — the synthetic launcher in a multi-studio
        // umbrella, per the "primary studio = first" convention). The
        // SingleWidgetWorkspace shell fetches doc bytes from /open-content and
        // its breadcrumb from /open-refs by the same child-index path it was
        // opened by — no uuid, URL and breadcrumb share one source of truth.
        final OpenContentGetAction openContentAction = new OpenContentGetAction(openRoot);
        final OpenRefsGetAction openRefsAction = new OpenRefsGetAction(openRoot);

        // --- Plan action (RFC 0005-ext1), only when plans registered.
        final PlanGetAction planAction;
        if (!plans.isEmpty()) {
            var planRegistry = new PlanRegistry(plans, docRegistry);
            planAction = new PlanGetAction(planRegistry, catalogueRegistry, docRegistry);
        } else {
            planAction = null;
        }

        // --- RFC 0014 diagnostic surface (gated by params.diagnosticsEnabled()).
        // Default off; enable via -Dhoming.diagnostics=true or a custom RuntimeParams subtype.
        final StudioGraphMarkdownAction graphMarkdownAction =
                params.diagnosticsEnabled() ? new StudioGraphMarkdownAction(this) : null;

        // --- Compose final ActionRegistry.
        // RFC 0051 — only meaningful when there are catalogues to address.
        final CataloguePathGetAction pathAction = (catalogueRegistry == null) ? null
                : new CataloguePathGetAction(catalogueRegistry, inner.appAction());

        final var harnessGetActions  = fixtures.harnessGetActions();
        final var harnessPostActions = fixtures.harnessPostActions();
        return new ActionRegistry<>() {
            @Override
            public Map<String, GetAction<RoutingContext, ?, ?, ?>> getActions() {
                Map<String, GetAction<RoutingContext, ?, ?, ?>> all = new HashMap<>(inner.getActions());
                all.put("/",            rootRedirect);
                // RFC 0051 — the authentic address. Mounted on both the
                // wildcard and the bare root, because a Vert.x /cat/* route
                // does not match /cat itself, and /cat is the studio's front
                // door (the empty path resolves to the root catalogue).
                if (pathAction != null) {
                    all.put(CataloguePathGetAction.ROUTE,      pathAction);
                    all.put(CataloguePathGetAction.ROOT_ROUTE, pathAction);
                }
                // RFC 0051 — /app is left alone deliberately. It EXECUTES an
                // app in direct-access mode and does nothing else. An earlier
                // pass had it redirect a positioned (app, args) to its path so
                // old links self-corrected, which worked but made one route
                // sometimes-render and sometimes-redirect: a caller could no
                // longer tell from the URL which it would get, and a caller who
                // genuinely wanted the flat render could not have it.
                // "Go to this navigable" is now its own action (/goto), so the
                // render route can go back to being only a render route.
                all.put("/css-content", cssContentAction);
                all.put("/doc",         docAction);
                // RFC 0051 - "go to this navigable", for the whole (app, args)
                // space. Separate from /app on purpose: /app renders, and its
                // self-correcting redirect is a property of the render route,
                // not something a managed reference should lean on.
                if (catalogueRegistry != null) {
                    all.put(GotoNavigableGetAction.ROUTE, new GotoNavigableGetAction(catalogueRegistry));
                }
                all.put("/doc-tree",    new DocTreeGetAction(docRegistry, openRoot));
                // The raw-bytes endpoint for resource-backed inline segments
                // (svg / table / image) embedded in a rigid-tree doc. Sibling of
                // /doc-tree: the tree JSON hands each such segment a
                // /doc-tree-content?id=&path=&seg= URL, and this action re-normalizes
                // the root and returns the embedded doc's own bytes. Without it,
                // embedded SVGs/images/tables in a RigidDoc/ComposedDoc 404.
                all.put("/doc-tree-content", new DocTreeContentGetAction(docRegistry));
                all.put("/doc-refs",    docRefsAction);
                all.put("/open-content", openContentAction);
                all.put("/open-refs",    openRefsAction);
                all.put("/themes",      themesAction);
                all.put("/brand",       brandAction);
                if (catalogueAction != null) all.put("/catalogue", catalogueAction);
                if (planAction      != null) all.put("/plan",      planAction);
                if (graphMarkdownAction != null) all.put("/graph-md", graphMarkdownAction);
                if (catalogueRegistry != null) all.put("/catalogue-parity",
                        new hue.captains.singapura.js.homing.studio.base.tree.CatalogueTreeParityGetAction(catalogueRegistry));
                all.putAll(harnessGetActions);
                return Map.copyOf(all);
            }

            @Override
            public Map<String, PostAction<RoutingContext, ?, ?, ?>> postActions() {
                Map<String, PostAction<RoutingContext, ?, ?, ?>> all = new HashMap<>(inner.postActions());
                all.putAll(harnessPostActions);
                return Map.copyOf(all);
            }
        };
    }

    // ----- composition helpers (instance methods so jOntology's Immutable enforcer accepts the record) -----

    private List<AppModule<?, ?>> unionAppsByClass(
            List<? extends Studio<?>> studios,
            List<AppModule<?, ?>> harnessApps) {
        // Harness apps first — they're the framework's spine, studios layer on top.
        var byClass = new LinkedHashMap<Class<?>, AppModule<?, ?>>();
        for (var app : harnessApps) putAppDedup(byClass, app);
        for (var studio : studios) {
            for (var app : studio.apps()) putAppDedup(byClass, app);
        }
        return List.copyOf(byClass.values());
    }

    private void putAppDedup(Map<Class<?>, AppModule<?, ?>> byClass, AppModule<?, ?> app) {
        var existing = byClass.putIfAbsent(app.getClass(), app);
        if (existing != null && existing != app) {
            throw new IllegalStateException(
                    "Bootstrap.compose: two instances of AppModule class "
                            + app.getClass().getName()
                            + " supplied — same class must mean same instance");
        }
    }

    private List<Catalogue<?>> unionCataloguesByClass(List<? extends Studio<?>> studios) {
        var byClass = new LinkedHashMap<Class<?>, Catalogue<?>>();
        for (var studio : studios) {
            for (Catalogue<?> c : studio.catalogues()) {
                byClass.putIfAbsent(c.getClass(), c);
            }
        }
        return List.copyOf(byClass.values());
    }

    /**
     * Union of Plans from every studio's {@code Studio.plans()} (URL-only
     * channel) and every {@code Entry.OfDoc(PlanDoc(plan))} leaf reachable
     * across the catalogue closure (the catalogue-leaf channel). Either path
     * — or both — registers a plan. Dedup is by class. Defect 0005 resolution.
     */
    private List<Plan> unionPlansByClass(List<? extends Studio<?>> studios,
                                         List<Catalogue<?>> catalogues) {
        var byClass = new LinkedHashMap<Class<?>, Plan>();
        for (var studio : studios) {
            for (Plan p : studio.plans()) {
                byClass.putIfAbsent(p.getClass(), p);
            }
        }
        for (Plan p : PlanRegistry.harvestFromLeaves(catalogues)) {
            byClass.putIfAbsent(p.getClass(), p);
        }
        return List.copyOf(byClass.values());
    }

    /**
     * The leveled child-index path a request carries, or null when it is not a
     * leveled request. RFC 0051 Phase 6 - reads the same lN parameters
     * OpenRefsGetAction reads, so the stamp and the endpoint cannot disagree
     * about what a path means.
     */
    private List<Integer> levelPathOf(Map<String, List<String>> args) {
        if (args == null || args.get("l0") == null) return null;
        var out = new ArrayList<Integer>();
        for (int n = 0; n < 32; n++) {
            List<String> v = args.get("l" + n);
            if (v == null || v.isEmpty()) break;
            try {
                out.add(Integer.parseInt(v.get(0)));
            } catch (NumberFormatException e) {
                break;
            }
        }
        return out.isEmpty() ? null : out;
    }
}
