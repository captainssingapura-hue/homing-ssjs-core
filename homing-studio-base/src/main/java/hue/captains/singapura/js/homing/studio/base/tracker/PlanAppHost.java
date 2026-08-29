package hue.captains.singapura.js.homing.studio.base.tracker;

import hue.captains.singapura.js.homing.core.AppUrl;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.LegacyAppMain;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.studio.base.app.DocReader;

import java.util.List;

/**
 * Single shared {@link AppModule} that serves any registered {@link Plan} (RFC 0005-ext1).
 *
 * <p>URL contract:</p>
 * <pre>
 *   /app?app=plan&id=&lt;class-fqn&gt;                    ← index page
 *   /app?app=plan&id=&lt;class-fqn&gt;&phase=&lt;phase-id&gt;  ← phase detail page
 * </pre>
 *
 * <p>One AppHost serves both views — {@code phase} param presence determines view kind.
 * The renderer fetches the full plan payload from {@link PlanGetAction} and chooses
 * which view to emit.</p>
 *
 * @since RFC 0005-ext1
 */
@LegacyAppMain(reason = "Multi-view tracker (phases / decisions / acceptance). PlanHostRenderer is 344 lines — Modest File Size split candidate; pairs with Component migration.")
public record PlanAppHost() implements AppModule<PlanAppHost.Params, PlanAppHost>, SelfContent {

    record appMain() implements AppModule._AppMain<PlanAppHost.Params, PlanAppHost> {}

    public record link() implements AppLink<PlanAppHost> {}

    /** Query parameters — class FQN of the Plan + optional phase id for the step view. */
    public record Params(String id, String phase) implements AppModule._Param {}

    public static final PlanAppHost INSTANCE = new PlanAppHost();

    /**
     * Build the canonical URL serving the given Plan's index page.
     *
     * <p>RFC 0051 (D8) — minted through {@link AppUrl} and this app's own
     * {@link #CODEC}, so the address and the parse that reads it back are one
     * statement. It used to concatenate, which is why {@code from(to(p)) == p}
     * held only by the accident that class names need no escaping.</p>
     */
    public static String urlFor(Class<? extends Plan> planClass) {
        return urlFor(planClass, null);
    }

    /** Build the canonical URL serving a phase detail page. */
    public static String urlFor(Class<? extends Plan> planClass, String phaseId) {
        return AppUrl.flat(INSTANCE.simpleName(), CODEC,
                new Params(planClass.getName(), phaseId));
    }

    /** RFC 0051 - plan id required, phase optional (a plan opens at its
     *  overview when no phase is named). */
    public static final ParamCodec<Params> CODEC = new ParamCodec<>() {

        @Override public Decoded<Params> from(java.util.Map<String, java.util.List<String>> query) {
            String id = QueryString.first(query, "id");
            if (id == null || id.isBlank()) return Decoded.missing("id");
            return Decoded.ok(new Params(id, QueryString.first(query, "phase")));
        }

        @Override public java.util.Map<String, java.util.List<String>> to(Params params) {
            var out = QueryString.params();
            QueryString.put(out, "id", params.id());
            QueryString.put(out, "phase", params.phase());
            return out;
        }
    };

    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public ParamCodec<Params> paramCodec() { return CODEC; }

    @Override public String simpleName() { return "plan"; }

    /** Page-kind label. {@code AppHtmlGetAction} appends the downstream brand;
     *  the renderer refines it to {@code "<plan-name> · <brand>"} on load. */
    @Override public String title() { return "plan"; }

    @Override
    public ImportsFor<PlanAppHost> imports() {
        return ImportsFor.<PlanAppHost>builder()
                .add(new ModuleImports<>(List.of(new DocReader.link()),                       DocReader.INSTANCE))
                .add(new ModuleImports<>(List.of(new PlanHostRenderer.renderPlanHost()),      PlanHostRenderer.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PlanAppHost> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new appMain()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver nameResolver) {
        return List.of(
                // RFC 0051 - params from the server; a /cat path has no query.
                // RFC 0051 — chrome is handed in, never built. The stamp ends
                // at the plan, which is where this page sits whatever ?phase=
                // says; moving between phases is this app's own navigation.
                "function appMain(rootElement, params, chrome) {",
                "    rootElement.replaceChildren(renderPlanHost({",
                "        planId: params.id,",
                "        phase:  params.phase,",
                "        crumbs: chrome && chrome.crumbs",
                "    }));",
                "}"
        );
    }
}
