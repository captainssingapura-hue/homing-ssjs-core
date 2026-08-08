package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.studio.base.Bootstrap;
import hue.captains.singapura.js.homing.studio.base.DefaultRuntimeParams;
import hue.captains.singapura.js.homing.studio.base.Umbrella;

/**
 * RFC 0044 — launches the Crate-Studio over homing-ssjs-core's own top-level
 * crates ({@link TopLevelCrates#ALL}). No classpath enumeration — the crate set
 * IS the module inventory (each crate's completeness guarded by its OrphanCheck).
 *
 * <p>Landing: {@code /}. Workspace:
 * {@code /app?app=genericWorkspace&ws_kind=conformance} — the crate Navigator +
 * Summary / Full Content / Conformance panes. Port defaults to {@code 8090}
 * ({@code -Dconformance.port=...}).</p>
 */
public final class ConformanceStudioServer {

    private ConformanceStudioServer() {}

    public static void main(String[] args) {
        int modules = TopLevelCrates.ALL.stream().mapToInt(c -> c.entries().size()).sum();
        System.out.println("[crate-studio] " + TopLevelCrates.ALL.size()
                + " top-level crates · " + modules + " owned modules");

        var umbrella = new Umbrella.Solo<>(ConformanceStudio.INSTANCE);
        int port = Integer.getInteger("conformance.port", 8090);
        new Bootstrap<>(new ConformanceStudioFixtures(umbrella, TopLevelCrates.ALL),
                new DefaultRuntimeParams(port)).start();
    }
}
