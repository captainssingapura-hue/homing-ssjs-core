package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.rules.ModuleEnumerator;
import hue.captains.singapura.js.homing.conformance.rules.ModuleRegistry;
import hue.captains.singapura.js.homing.studio.base.Bootstrap;
import hue.captains.singapura.js.homing.studio.base.DefaultRuntimeParams;
import hue.captains.singapura.js.homing.studio.base.Umbrella;

/**
 * RFC 0044 — launches the conformance studio over homing-ssjs-core's own served
 * modules. Enumerates the homing JS modules on the classpath and starts the
 * dedicated conformance workspace via {@link ConformanceStudioFixtures}.
 *
 * <p>Landing: {@code /} (the Conformance landing catalogue). Workspace:
 * {@code /app?app=genericWorkspace&ws_kind=conformance} — the module Navigator
 * + Summary / Full Content / Conformance panes. Port defaults to {@code 8090}
 * ({@code -Dconformance.port=...}).</p>
 */
public final class ConformanceStudioServer {

    private ConformanceStudioServer() {}

    public static void main(String[] args) {
        ModuleRegistry registry = ModuleEnumerator.HOMING.fromClasspath();
        System.out.println("[conformance-studio] enumerated " + registry.size()
                + " modules across " + registry.byPackage().size() + " packages");

        var umbrella = new Umbrella.Solo<>(ConformanceStudio.INSTANCE);
        int port = Integer.getInteger("conformance.port", 8090);
        new Bootstrap<>(new ConformanceStudioFixtures(umbrella, registry),
                new DefaultRuntimeParams(port)).start();
    }
}
