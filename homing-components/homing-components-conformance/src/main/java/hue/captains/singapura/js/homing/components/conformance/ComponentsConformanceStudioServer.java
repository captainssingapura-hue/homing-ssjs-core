package hue.captains.singapura.js.homing.components.conformance;

import hue.captains.singapura.js.homing.conformance.studio.ConformanceStudio;
import hue.captains.singapura.js.homing.conformance.studio.ConformanceStudioFixtures;
import hue.captains.singapura.js.homing.studio.base.Bootstrap;
import hue.captains.singapura.js.homing.studio.base.DefaultRuntimeParams;
import hue.captains.singapura.js.homing.studio.base.Umbrella;

/**
 * The conformance studio over the components' crates: the framework's
 * studio, verbatim, given {@link ComponentsConformance#TOP_LEVEL} and the
 * report {@link ComponentsConformanceExport} wrote at build time.
 *
 * <p>Landing: {@code /}. Workspace: {@code /app?app=genericWorkspace&ws_kind=conformance}.
 * Port {@code 8097} by default ({@code -Dconformance.port}), beside the
 * framework's 8090 and the demo's 8091.</p>
 */
public final class ComponentsConformanceStudioServer {

    private ComponentsConformanceStudioServer() {}

    public static void main(String[] args) {
        int modules = ComponentsConformance.TOP_LEVEL.stream().mapToInt(c -> c.entries().size()).sum();
        System.out.println("[components-crate-studio] " + ComponentsConformance.TOP_LEVEL.size()
                + " top-level crate(s) · " + modules + " served modules");
        var umbrella = new Umbrella.Solo<>(ConformanceStudio.INSTANCE);
        int port = Integer.getInteger("conformance.port", 8097);
        new Bootstrap<>(new ConformanceStudioFixtures(umbrella, ComponentsConformance.TOP_LEVEL),
                new DefaultRuntimeParams(port)).start();
    }
}
