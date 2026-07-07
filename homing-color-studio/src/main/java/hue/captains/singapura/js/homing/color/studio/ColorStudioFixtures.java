package hue.captains.singapura.js.homing.color.studio;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.studio.base.Fixtures;
import hue.captains.singapura.js.homing.studio.base.Umbrella;
import hue.captains.singapura.js.homing.studio.starter.StudioStarterFixtures;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpecRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.ontology.ValueObject;
import io.vertx.ext.web.RoutingContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Batteries-included {@link Fixtures} for the standalone Color Studio: it is
 * {@link StudioStarterFixtures} (so the V2 shell, the catalogue tree feed, and
 * leveled Open all come for free) <i>plus</i> two Color-Studio-specific
 * contributions:
 *
 * <ul>
 *   <li>registers {@link ColorWorkspaceSpec} (kind {@code "color"}) into the
 *       {@link WorkspaceSpecRegistry} in the constructor, idempotently — so
 *       {@code ?app=genericWorkspace&ws_kind=color} resolves;</li>
 *   <li>adds {@code GET /color-tree} ({@link ColorTreeGetAction}), the feed the
 *       Color Tree + Colours widgets fetch.</li>
 * </ul>
 *
 * <p>Composes (rather than extends) {@code StudioStarterFixtures} — records are
 * final — delegating every other seam to it.</p>
 */
public record ColorStudioFixtures(Umbrella<ColorStudio> umbrella)
        implements Fixtures<ColorStudio>, ValueObject {

    public ColorStudioFixtures {
        Objects.requireNonNull(umbrella, "umbrella");
        // Register the colour workspace exactly once per JVM (idempotent).
        if (WorkspaceSpecRegistry.INSTANCE.get(ColorWorkspaceSpec.INSTANCE.kind()).isEmpty()) {
            WorkspaceSpecRegistry.INSTANCE.register(ColorWorkspaceSpec.INSTANCE);
        }
    }

    /** The StudioStarterFixtures we delegate to for everything framework-standard. */
    private StudioStarterFixtures<ColorStudio> base() {
        return new StudioStarterFixtures<>(umbrella);
    }

    @Override
    public List<AppModule<?, ?>> harnessApps() {
        return base().harnessApps();
    }

    @Override
    public Map<String, GetAction<RoutingContext, ?, ?, ?>> harnessGetActions() {
        var actions = new LinkedHashMap<>(base().harnessGetActions());
        actions.put("/color-tree", new ColorTreeGetAction());
        actions.put("/color-swatches", new ColorSwatchesGetAction());
        return Map.copyOf(actions);
    }

    @Override
    public NodeChrome chromeFor(Umbrella<ColorStudio> node) {
        return base().chromeFor(node);
    }
}
