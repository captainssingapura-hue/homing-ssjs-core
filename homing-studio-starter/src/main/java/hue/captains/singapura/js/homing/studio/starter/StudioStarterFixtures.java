package hue.captains.singapura.js.homing.studio.starter;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.studio.base.DefaultFixtures;
import hue.captains.singapura.js.homing.studio.base.Fixtures;
import hue.captains.singapura.js.homing.studio.base.Studio;
import hue.captains.singapura.js.homing.studio.base.Umbrella;
import hue.captains.singapura.js.homing.studio.workspace.CatalogueForestGetAction;
import hue.captains.singapura.js.homing.studio.workspace.StudioWorkspaceSpec;
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
 * RFC 0040 — batteries-included {@link Fixtures} for downstream studios: it is
 * {@link DefaultFixtures} <i>plus</i> the Studio Workspace, pre-wired. A new
 * studio gets the catalogue Navigator, the tree feed, and leveled Open with no
 * per-studio code:
 *
 * <pre>{@code
 * new Bootstrap<>(
 *         new StudioStarterFixtures<>(new Umbrella.Solo<>(MyStudio.INSTANCE)),
 *         new DefaultRuntimeParams(8080)
 * ).start();
 * // Navigator at /app?app=genericWorkspace&ws_kind=studio — zero wiring.
 * }</pre>
 *
 * <h2>Why a starter module (not a studio-base default)</h2>
 *
 * <p>The workspace stack — {@code homing-workspace} / {@code -shell} /
 * {@code -studio-workspace} — is a <b>strict downstream of</b>
 * {@code homing-studio-base} ({@code WorkspaceShell} uses studio-base). So
 * studio-base cannot reference {@link GenericWorkspace} or
 * {@link StudioWorkspaceSpec} to default them — that would close a dependency
 * cycle. This starter sits <i>above</i> the whole stack and is the one place
 * the application-assembly defaults are composed.</p>
 *
 * <p>It is {@code DefaultFixtures} by composition (records are final, so it
 * delegates rather than extends), overriding exactly two seams:</p>
 * <ul>
 *   <li>{@link #harnessApps()} — appends {@link GenericWorkspace}, the V2
 *       shell's single composition AppModule.</li>
 *   <li>{@link #harnessGetActions()} — adds {@code GET /catalogue-tree}, the
 *       Navigator's tree feed, rooted at the primary studio's {@code home()}
 *       (the forest root; {@link CatalogueForestGetAction} descends
 *       {@code OfStudio} portals, so it serves solo and multi-studio alike).</li>
 * </ul>
 *
 * <p>The {@link StudioWorkspaceSpec} (kind {@code "studio"}) is registered in
 * the constructor, idempotently — replacing the old class-init "touch" smell.
 * The studio-base {@code Bootstrap} already defaults the path-keyed Open
 * endpoints ({@code /open-content}, {@code /open-refs}) and the
 * {@code SingleWidgetWorkspace} shell (both pure studio-base), so those need
 * no wiring here.</p>
 *
 * @param <S> the studio type at the umbrella's leaves
 * @since homing-studio-starter — RFC 0040 zero-config workspace
 */
public record StudioStarterFixtures<S extends Studio<?>>(Umbrella<S> umbrella)
        implements Fixtures<S>, ValueObject {

    public StudioStarterFixtures {
        Objects.requireNonNull(umbrella, "umbrella");
        // Register the Studio Workspace spec exactly once per JVM. Idempotent so
        // multiple studios / repeated boots (tests) don't trip the registry's
        // duplicate-kind guard.
        if (WorkspaceSpecRegistry.INSTANCE.get(StudioWorkspaceSpec.INSTANCE.kind()).isEmpty()) {
            WorkspaceSpecRegistry.INSTANCE.register(StudioWorkspaceSpec.INSTANCE);
        }
    }

    /** The DefaultFixtures we are, plus the workspace. */
    private DefaultFixtures<S> defaults() {
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
        actions.put("/catalogue-tree",
                new CatalogueForestGetAction(umbrella.studios().get(0).home()));
        return Map.copyOf(actions);
    }

    @Override
    public NodeChrome chromeFor(Umbrella<S> node) {
        return defaults().chromeFor(node);
    }
}
