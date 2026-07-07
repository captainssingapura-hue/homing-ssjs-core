package hue.captains.singapura.js.homing.color.studio;

import hue.captains.singapura.js.homing.studio.base.Bootstrap;
import hue.captains.singapura.js.homing.studio.base.DefaultRuntimeParams;

/**
 * Runnable entry point for the standalone Color Studio.
 *
 * <p>Run with:</p>
 * <pre>{@code
 * mvn -f homing-color-studio/pom.xml \
 *     org.codehaus.mojo:exec-maven-plugin:3.1.0:java \
 *     -Dexec.mainClass="hue.captains.singapura.js.homing.color.studio.ColorStudioServer"
 * }</pre>
 *
 * <p>Then open the colour workspace:</p>
 * <pre>{@code http://localhost:8080/app?app=genericWorkspace&ws_kind=color}</pre>
 *
 * <p>Port defaults to 8080; override with {@code -Dcolor.port=<n>}.</p>
 */
public final class ColorStudioServer {

    private ColorStudioServer() {}

    public static void main(String[] args) {
        int port = Integer.getInteger("color.port", 8080);
        new Bootstrap<>(
                new ColorStudioFixtures(new hue.captains.singapura.js.homing.studio.base.Umbrella.Solo<>(ColorStudio.INSTANCE)),
                new DefaultRuntimeParams(port)
        ).start();
    }
}
