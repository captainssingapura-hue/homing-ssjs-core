package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.rules.ModuleRegistry;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.studio.base.DefaultFixtures;
import hue.captains.singapura.js.homing.studio.base.Fixtures;
import hue.captains.singapura.js.homing.studio.base.Umbrella;
import hue.captains.singapura.js.homing.workspace.shell.GenericWorkspace;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpecRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.ontology.ValueObject;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RFC 0044 — the reusable conformance-studio fixtures: {@link DefaultFixtures}
 * plus the workspace shell and the module-tree feed, parameterized by a
 * {@link ModuleRegistry}. A downstream instantiates this over its OWN registry
 * (and its own studio identity) to get a conformance workspace for its modules
 * with no bespoke wiring.
 *
 * <p>Composition over the {@code DefaultFixtures} seams (records are final):</p>
 * <ul>
 *   <li>registers {@link ConformanceWorkspaceSpec} (kind {@code "conformance"})
 *       once per JVM, idempotently;</li>
 *   <li>{@link #harnessApps()} appends {@link GenericWorkspace} (the shell app);</li>
 *   <li>{@link #harnessGetActions()} adds {@code GET /module-tree}, the
 *       Navigator's feed, rooted at the supplied registry.</li>
 * </ul>
 * Brand, themes, and the catalogue harness come from the defaults (brand from
 * the studio's {@code standaloneBrand()} via {@link #umbrella()}).
 *
 * @param umbrella the studio identity (landing + brand)
 * @param registry the modules the workspace browses
 */
public record ConformanceStudioFixtures(Umbrella<ConformanceStudio> umbrella, ModuleRegistry registry)
        implements Fixtures<ConformanceStudio>, ValueObject {

    public ConformanceStudioFixtures {
        Objects.requireNonNull(umbrella, "umbrella");
        Objects.requireNonNull(registry, "registry");
        if (WorkspaceSpecRegistry.INSTANCE.get(ConformanceWorkspaceSpec.INSTANCE.kind()).isEmpty()) {
            WorkspaceSpecRegistry.INSTANCE.register(ConformanceWorkspaceSpec.INSTANCE);
        }
    }

    private DefaultFixtures<ConformanceStudio> defaults() {
        return new DefaultFixtures<>(umbrella);
    }

    @Override
    public List<AppModule<?, ?>> harnessApps() {
        var apps = new ArrayList<>(defaults().harnessApps());
        apps.add(GenericWorkspace.INSTANCE);
        return List.copyOf(apps);
    }

    @Override
    public Map<String, GetAction<RoutingContext, ?, ?, ?>> harnessGetActions() {
        var actions = new LinkedHashMap<>(defaults().harnessGetActions());
        actions.put("/module-tree", new ModuleTreeGetAction(registry));
        return Map.copyOf(actions);
    }

    @Override
    public NodeChrome chromeFor(Umbrella<ConformanceStudio> node) {
        return defaults().chromeFor(node);
    }
}
