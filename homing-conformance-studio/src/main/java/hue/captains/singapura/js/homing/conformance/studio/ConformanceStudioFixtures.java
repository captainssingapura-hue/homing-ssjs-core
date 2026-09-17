package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.studio.base.DefaultFixtures;
import hue.captains.singapura.js.homing.studio.base.Fixtures;
import hue.captains.singapura.js.homing.studio.base.Umbrella;
import hue.captains.singapura.js.homing.workspace.shell.GenericWorkspace;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceGroupApp;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceGroupRegistry;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceGroups;
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
 * plus the workspace shell and the crate-tree feed, parameterized by the
 * studio's <b>top-level (owned) crates</b>. A downstream instantiates this over
 * its OWN crates (and its own studio identity) to get a Crate-Studio for its
 * modules with no bespoke wiring.
 *
 * <p>Composition over the {@code DefaultFixtures} seams (records are final):</p>
 * <ul>
 *   <li>registers {@link ConformanceWorkspaceSpec} and its {@link ConformanceWorkspaceGroup} (RFC 0058)
 *       once per JVM, idempotently;</li>
 *   <li>{@link #harnessApps()} appends {@link GenericWorkspace} (the shell app);</li>
 *   <li>{@link #harnessGetActions()} adds {@code GET /crate-tree}, the
 *       Navigator's feed over the top-level crates' modules.</li>
 * </ul>
 * Brand, themes, and the catalogue harness come from the defaults (brand from
 * the studio's {@code standaloneBrand()} via {@link #umbrella()}).
 *
 * @param umbrella the studio identity (landing + brand)
 * @param topLevel the owned crates the studio browses
 */
public record ConformanceStudioFixtures(Umbrella<ConformanceStudio> umbrella, List<Crate> topLevel)
        implements Fixtures<ConformanceStudio>, ValueObject {

    public ConformanceStudioFixtures {
        Objects.requireNonNull(umbrella, "umbrella");
        topLevel = List.copyOf(Objects.requireNonNull(topLevel, "topLevel"));
        // RFC 0058 — the spec and the group holding it; the landing places the group.
        ConformanceWorkspaceGroup.register();
        WorkspaceGroups.assertPlacedOnce(ConformanceLandingCatalogue.INSTANCE, WorkspaceGroupRegistry.INSTANCE);
    }

    private DefaultFixtures<ConformanceStudio> defaults() {
        return new DefaultFixtures<>(umbrella);
    }

    @Override
    public List<AppModule<?, ?>> harnessApps() {
        var apps = new ArrayList<>(defaults().harnessApps());
        apps.add(GenericWorkspace.INSTANCE);
        apps.add(WorkspaceGroupApp.INSTANCE);   // RFC 0058 — the authentic-path app the landing places
        return List.copyOf(apps);
    }

    @Override
    public Map<String, GetAction<RoutingContext, ?, ?, ?>> harnessGetActions() {
        var actions = new LinkedHashMap<>(defaults().harnessGetActions());
        actions.put("/crate-tree", new CrateTreeGetAction(topLevel));
        actions.put("/crate-graph", new CrateGraphGetAction(topLevel));
        actions.put("/crate-conformance", new CrateConformanceGetAction(topLevel));
        actions.put("/conformance-report", new ConformanceReportGetAction());
        return Map.copyOf(actions);
    }

    @Override
    public NodeChrome chromeFor(Umbrella<ConformanceStudio> node) {
        return defaults().chromeFor(node);
    }

    /**
     * RFC 0044 — the crate roots this studio serves: its own top-level crates
     * <b>plus</b> the framework serving stack it always mounts ({@link
     * TopLevelCrates#ALL}). A module outside their closure is in no registered
     * crate, so {@code /module} refuses to serve it.
     */
    @Override
    public java.util.List<Crate> crates() {
        var roots = new ArrayList<Crate>(topLevel);
        roots.addAll(TopLevelCrates.ALL);
        return roots;
    }
}
