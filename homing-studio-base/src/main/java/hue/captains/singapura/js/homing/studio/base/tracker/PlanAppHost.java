package hue.captains.singapura.js.homing.studio.base.tracker;

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

    /** Build the canonical URL serving the given Plan's index page. */
    public static String urlFor(Class<? extends Plan> planClass) {
        return "/app?app=" + INSTANCE.simpleName() + "&id=" + planClass.getName();
    }

    /** Build the canonical URL serving a phase detail page. */
    public static String urlFor(Class<? extends Plan> planClass, String phaseId) {
        return urlFor(planClass) + "&phase=" + phaseId;
    }

    @Override public Class<Params> paramsType() { return Params.class; }

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
                "function appMain(rootElement) {",
                "    rootElement.replaceChildren(renderPlanHost({",
                "        planId: params.id,",
                "        phase:  params.phase",
                "    }));",
                "}"
        );
    }
}
