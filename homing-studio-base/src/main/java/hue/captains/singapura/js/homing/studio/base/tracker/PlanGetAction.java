package hue.captains.singapura.js.homing.studio.base.tracker;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueAppHost;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueRegistry;
import hue.captains.singapura.js.homing.studio.base.app.StudioBrand;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /plan?id=<class-fqn>} — serves a {@link Plan}'s fully-resolved data
 * as JSON for {@code PlanAppHost}'s renderer to consume.
 *
 * <p>Per RFC 0005-ext1 §2.6, the response is a single coherent payload covering
 * both index and phase-detail views. Renderer chooses what to display based on
 * the {@code phase} URL param. Server pre-resolves all URLs (brand, breadcrumbs,
 * doc-reader URL for executionDoc, etc.) — renderer constructs nothing.</p>
 *
 * @since RFC 0005-ext1
 */
public class PlanGetAction
        implements GetAction<RoutingContext, PlanGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String id) implements Param._QueryString {}

    private final PlanRegistry planRegistry;
    private final CatalogueRegistry catalogueRegistry;   // optional; may be null
    private final hue.captains.singapura.js.homing.studio.base.DocRegistry docRegistry; // optional

    public PlanGetAction(PlanRegistry planRegistry, CatalogueRegistry catalogueRegistry) {
        this(planRegistry, catalogueRegistry, null);
    }

    /**
     * RFC 0051 — with a {@link hue.captains.singapura.js.homing.studio.base.DocRegistry},
     * the plan's companion-doc links can be emitted as the target's own
     * {@code (app, args)} instead of the renderer guessing {@code doc-reader}
     * from a bare UUID.
     */
    public PlanGetAction(PlanRegistry planRegistry, CatalogueRegistry catalogueRegistry,
                         hue.captains.singapura.js.homing.studio.base.DocRegistry docRegistry) {
        this.planRegistry      = Objects.requireNonNull(planRegistry, "planRegistry");
        this.catalogueRegistry = catalogueRegistry;   // may be null when no catalogues registered
        this.docRegistry       = docRegistry;         // may be null; links fall back to the UUID form
    }

    /** Resolve a companion-doc UUID to the target's own address, or null. */
    private String docUrl(String uuid) {
        if (uuid == null || uuid.isBlank() || docRegistry == null) return null;
        try {
            var d = docRegistry.resolve(java.util.UUID.fromString(uuid.trim()));
            return d == null ? null : d.url();
        } catch (IllegalArgumentException notAUuid) {
            return null;
        }
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("id"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        String fqn = query.id();
        if (fqn == null || fqn.isBlank()) {
            return CompletableFuture.failedFuture(notFound("id", "Required query parameter 'id' was not provided"));
        }
        Class<?> raw;
        try {
            raw = Class.forName(fqn);
        } catch (ClassNotFoundException e) {
            return CompletableFuture.failedFuture(notFound(fqn, "Class not found"));
        }
        if (!Plan.class.isAssignableFrom(raw)) {
            return CompletableFuture.failedFuture(notFound(fqn, "Class is not a Plan"));
        }
        @SuppressWarnings("unchecked")
        Class<? extends Plan> cls = (Class<? extends Plan>) raw;
        Plan plan = planRegistry.resolve(cls);
        if (plan == null) {
            return CompletableFuture.failedFuture(notFound(fqn, "Plan not registered"));
        }
        try {
            String body = serialize(plan);
            return CompletableFuture.completedFuture(new DocContent(body, "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(fqn,
                    "Failed to serialise plan: " + e.getMessage()));
        }
    }

    String serialize(Plan p) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"name\":")     .append(jstr(p.name())).append(',');
        sb.append("\"summary\":")  .append(jstr(p.summary())).append(',');
        sb.append("\"kicker\":")   .append(jstr(p.kicker())).append(',');
        sb.append("\"subtitle\":") .append(jstr(p.subtitle())).append(',');
        sb.append("\"totalProgress\":").append(p.totalProgress()).append(',');
        sb.append("\"openDecisions\":").append(p.openDecisions()).append(',');
        sb.append("\"acceptanceMet\":").append(p.acceptanceMet()).append(',');
        sb.append("\"executionDoc\":").append(jstr(p.executionDoc())).append(',');
        sb.append("\"dossierDoc\":") .append(jstr(p.dossierDoc())).append(',');
        // RFC 0051 - the companion docs' own (app, args), so the footer links
        // do not guess a viewer from a UUID.
        sb.append("\"executionUrl\":").append(jstr(docUrl(p.executionDoc()))).append(',');
        sb.append("\"dossierUrl\":")  .append(jstr(docUrl(p.dossierDoc()))).append(',');
        // RFC 0051 — the plan's own address, so the renderer builds its phase
        // links from a path rather than re-deriving /app?app=plan&id=. Null
        // when the plan has no position; the renderer keeps its flat fallback.
        sb.append("\"selfUrl\":")    .append(jstr(selfUrl(p))).append(',');

        // Brand + breadcrumbs (when CatalogueRegistry is available).
        if (catalogueRegistry != null) {
            StudioBrand brand = catalogueRegistry.brand();
            String logoSvg = (brand.logo() != null) ? brand.logo().resolve().orElse("") : "";
            sb.append("\"brand\":{")
              .append("\"label\":")  .append(jstr(brand.label())).append(',')
              .append("\"logo\":")   .append(jstr(logoSvg)).append(',')
              .append("\"homeUrl\":").append(jstr(homeUrl(brand)))
              .append("},");
            // RFC 0005-ext2: typed catalogue chain from root → containing
            // catalogue (typically Journeys). Renderer appends the plan name
            // as the final non-link crumb. RFC 0009: prefix each crumb's
            // text with the catalogue's icon glyph when non-empty.
            sb.append("\"breadcrumbs\":[");
            List<Catalogue<?>> chain = catalogueRegistry.breadcrumbsForPlan(p.getClass());
            boolean firstCrumb = true;
            for (Catalogue<?> c : chain) {
                if (!firstCrumb) sb.append(',');
                firstCrumb = false;
                String icon = c.icon();
                String text = (icon == null || icon.isEmpty()) ? c.name() : icon + " " + c.name();
                @SuppressWarnings("unchecked")
                Class<? extends Catalogue<?>> cClass = (Class<? extends Catalogue<?>>) c.getClass();
                // RFC 0051 — same as /doc-refs: a plan page builds its own
                // crumbs, so converting CatalogueGetAction did not reach them.
                var path = catalogueRegistry.pathOf(c);
                sb.append('{')
                  .append("\"text\":").append(jstr(text)).append(',')
                  .append("\"href\":").append(jstr(path != null ? path.toUrl()
                                                                : CatalogueAppHost.urlFor(cClass)))
                  .append('}');
            }
            sb.append("],");
        } else {
            sb.append("\"brand\":null,");
            sb.append("\"breadcrumbs\":[],");
        }

        // Objectives — optional 4th pillar; rendered at top of index view when non-empty.
        sb.append("\"objectives\":[");
        boolean firstO = true;
        for (Objective o : p.objectives()) {
            if (!firstO) sb.append(',');
            firstO = false;
            sb.append('{')
              .append("\"label\":")      .append(jstr(o.label())).append(',')
              .append("\"description\":").append(jstr(o.description()))
              .append('}');
        }
        sb.append("],");

        // Phases.
        sb.append("\"phases\":[");
        boolean firstPhase = true;
        for (Phase phase : p.phases()) {
            if (!firstPhase) sb.append(',');
            firstPhase = false;
            serializePhase(sb, phase);
        }
        sb.append("],");

        // Decisions.
        sb.append("\"decisions\":[");
        boolean firstD = true;
        for (Decision d : p.decisions()) {
            if (!firstD) sb.append(',');
            firstD = false;
            serializeDecision(sb, d);
        }
        sb.append("],");

        // Acceptance.
        sb.append("\"acceptance\":[");
        boolean firstA = true;
        for (Acceptance a : p.acceptance()) {
            if (!firstA) sb.append(',');
            firstA = false;
            sb.append('{')
              .append("\"label\":")      .append(jstr(a.label())).append(',')
              .append("\"description\":").append(jstr(a.description())).append(',')
              .append("\"met\":")        .append(a.met())
              .append('}');
        }
        sb.append("]}");
        return sb.toString();
    }

    private static void serializePhase(StringBuilder sb, Phase phase) {
        sb.append('{');
        sb.append("\"id\":")           .append(jstr(phase.id())).append(',');
        sb.append("\"label\":")        .append(jstr(phase.label())).append(',');
        sb.append("\"summary\":")      .append(jstr(phase.summary())).append(',');
        sb.append("\"description\":")  .append(jstr(phase.description())).append(',');
        sb.append("\"status\":")       .append(jstr(phase.status() == null ? "" : phase.status().name())).append(',');
        sb.append("\"verification\":") .append(jstr(phase.verification())).append(',');
        sb.append("\"rollback\":")     .append(jstr(phase.rollback())).append(',');
        sb.append("\"effort\":")       .append(jstr(phase.effort())).append(',');
        sb.append("\"notes\":")        .append(jstr(phase.notes())).append(',');
        sb.append("\"progressPercent\":").append(phase.progressPercent()).append(',');

        sb.append("\"tasks\":[");
        boolean f = true;
        for (Task t : phase.tasks()) {
            if (!f) sb.append(',');
            f = false;
            sb.append('{')
              .append("\"description\":").append(jstr(t.description())).append(',')
              .append("\"done\":")       .append(t.done())
              .append('}');
        }
        sb.append("],");

        sb.append("\"dependsOn\":[");
        boolean fd = true;
        for (Dependency d : phase.dependsOn()) {
            if (!fd) sb.append(',');
            fd = false;
            sb.append('{')
              .append("\"phaseId\":").append(jstr(d.phaseId())).append(',')
              .append("\"reason\":") .append(jstr(d.reason()))
              .append('}');
        }
        sb.append("],");

        sb.append("\"metrics\":[");
        boolean fm = true;
        for (Metric m : phase.metrics()) {
            if (!fm) sb.append(',');
            fm = false;
            sb.append('{')
              .append("\"label\":") .append(jstr(m.label())).append(',')
              .append("\"before\":").append(jstr(m.before())).append(',')
              .append("\"after\":") .append(jstr(m.after())).append(',')
              .append("\"delta\":") .append(jstr(m.delta()))
              .append('}');
        }
        sb.append("]}");
    }

    private static void serializeDecision(StringBuilder sb, Decision d) {
        sb.append('{');
        sb.append("\"id\":")            .append(jstr(d.id())).append(',');
        sb.append("\"question\":")      .append(jstr(d.question())).append(',');
        sb.append("\"recommendation\":").append(jstr(d.recommendation())).append(',');
        sb.append("\"chosenValue\":")   .append(jstr(d.chosenValue())).append(',');
        sb.append("\"status\":")        .append(jstr(d.status() == null ? "" : d.status().name())).append(',');
        sb.append("\"rationale\":")     .append(jstr(d.rationale())).append(',');
        sb.append("\"notes\":")         .append(jstr(d.notes()));
        sb.append('}');
    }

    /** RFC 0051 - the plan's own path, via its PlanDoc position. */
    private String selfUrl(Plan p) {
        if (catalogueRegistry == null) return null;
        var path = catalogueRegistry.pathOf(new PlanDoc(p));
        return path == null ? null : path.toUrl();
    }

    /** RFC 0051 - the brand link is the tree root, whose path is "/cat". */
    private String homeUrl(hue.captains.singapura.js.homing.studio.base.app.StudioBrand brand) {
        if (catalogueRegistry == null) return "/app?app=catalogue&id=" + brand.homeApp().getName();
        var root = catalogueRegistry.root();
        return root == null ? "/app?app=catalogue&id=" + brand.homeApp().getName()
                            : catalogueRegistry.pathOf(root).toUrl();
    }

    private static String jstr(String v) {
        if (v == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason)
        );
    }
}
