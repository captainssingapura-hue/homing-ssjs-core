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
import hue.captains.singapura.js.homing.studio.base.graph.DiagnosticsHub;
import hue.captains.singapura.js.homing.studio.base.graph.StudioGraphInspector;
import hue.captains.singapura.js.homing.studio.base.graph.StudioGraphMarkdownAction;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueAppHost;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueGetAction;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueRegistry;
import hue.captains.singapura.js.homing.studio.base.app.StudioBrand;
import hue.captains.singapura.js.homing.studio.base.theme.CssGroupImplRegistry;
import hue.captains.singapura.js.homing.studio.base.theme.ThemesGetAction;
import hue.captains.singapura.js.homing.studio.base.tracker.Plan;
import hue.captains.singapura.js.homing.studio.base.tracker.PlanGetAction;
import hue.captains.singapura.js.homing.studio.base.tracker.PlanRegistry;
import hue.captains.singapura.tao.http.action.ActionRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.PostAction;
import hue.captains.singapura.tao.http.vertx.VertxActionHost;
import hue.captains.singapura.tao.ontology.ValueObject;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        var host = new VertxActionHost(registry, params.port());
        host.start().onSuccess(server -> {
            int actualPort = server.actualPort();
            System.out.println("Studio listening on port " + actualPort);
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
        if (params.diagnosticsEnabled()) harnessApps.add(StudioGraphInspector.INSTANCE);
        // RFC 0016: when downstream has registered ContentTrees, the TreeAppHost
        // joins the app set so /app?app=tree&id=… resolves.
        if (!fixtures.trees().isEmpty()) {
            harnessApps.add(hue.captains.singapura.js.homing.studio.base.app.tree.TreeAppHost.INSTANCE);
        }
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
        var inner = new HomingActionRegistry(
                nameResolver, appResolver, params.resourceReader(),
                themeRegistry, appMeta);

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

        // RFC 0016 — harvest Docs wrapped by tree leaves. Trees register
        // their content as Docs (typically SvgDocs in the demo, but any
        // Doc subtype works). Without this, CatalogueRegistry-style
        // validation would reject the tree-wrapped Docs as "not in registry."
        allDocs.addAll(DocRegistry.harvestFromTrees(fixtures.trees()));

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
            // RFC 0014: when diagnostics is enabled the framework injects a
            // three-tier tile pyramid via the augmentation map — Diagnostics
            // tile on the home L0; per-studio parent tiles (or direct view
            // tiles in single-studio) on the DiagnosticsCatalogue page;
            // per-studio Object Graph + Type View on the &context=<studio>
            // variant of the same catalogue. See DiagnosticsHub.
            var diagnosticsAugmentations = params.diagnosticsEnabled()
                    ? new DiagnosticsHub(studios, brand.homeApp()).augmentations()
                    : java.util.Map.<hue.captains.singapura.js.homing.studio.base.app.CatalogueAugmentation.AugKey,
                                     hue.captains.singapura.js.homing.studio.base.app.CatalogueAugmentation>of();
            catalogueAction = new CatalogueGetAction(catalogueRegistry, diagnosticsAugmentations);
        } else {
            catalogueRegistry = null;
            catalogueAction   = null;
        }

        // --- Doc-refs action (RFC 0004-ext1 / RFC 0005-ext2 — carries breadcrumb chain).
        var docRefsAction = new DocRefsGetAction(docRegistry, catalogueRegistry);

        // --- Plan action (RFC 0005-ext1), only when plans registered.
        final PlanGetAction planAction;
        if (!plans.isEmpty()) {
            var planRegistry = new PlanRegistry(plans, docRegistry);
            planAction = new PlanGetAction(planRegistry, catalogueRegistry);
        } else {
            planAction = null;
        }

        // --- RFC 0014 diagnostic surface (gated by params.diagnosticsEnabled()).
        // Default off; enable via -Dhoming.diagnostics=true or a custom RuntimeParams subtype.
        final StudioGraphMarkdownAction graphMarkdownAction =
                params.diagnosticsEnabled() ? new StudioGraphMarkdownAction(this) : null;

        // --- RFC 0016 ContentTrees — only when downstream has registered any.
        final hue.captains.singapura.js.homing.studio.base.app.tree.TreeGetAction treeAction;
        if (!fixtures.trees().isEmpty()) {
            var treeRegistry = new hue.captains.singapura.js.homing.studio.base.app.tree.TreeRegistry(fixtures.trees());
            treeAction = new hue.captains.singapura.js.homing.studio.base.app.tree.TreeGetAction(treeRegistry, brand);
        } else {
            treeAction = null;
        }

        // --- Compose final ActionRegistry.
        final var harnessGetActions  = fixtures.harnessGetActions();
        final var harnessPostActions = fixtures.harnessPostActions();
        return new ActionRegistry<>() {
            @Override
            public Map<String, GetAction<RoutingContext, ?, ?, ?>> getActions() {
                Map<String, GetAction<RoutingContext, ?, ?, ?>> all = new HashMap<>(inner.getActions());
                all.put("/",            rootRedirect);
                all.put("/css-content", cssContentAction);
                all.put("/doc",         docAction);
                all.put("/doc-refs",    docRefsAction);
                all.put("/themes",      themesAction);
                all.put("/brand",       brandAction);
                if (catalogueAction != null) all.put("/catalogue", catalogueAction);
                if (planAction      != null) all.put("/plan",      planAction);
                if (graphMarkdownAction != null) all.put("/graph-md", graphMarkdownAction);
                if (treeAction != null)          all.put("/tree",     treeAction);
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
}
