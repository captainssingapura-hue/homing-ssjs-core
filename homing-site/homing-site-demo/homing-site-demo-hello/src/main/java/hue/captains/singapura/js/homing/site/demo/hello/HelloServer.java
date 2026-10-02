package hue.captains.singapura.js.homing.site.demo.hello;

import hue.captains.singapura.js.homing.site.SiteHost;

/**
 * Serves {@link HelloSite}. This file is the whole deployment: a site and a
 * port. No Bootstrap, no Fixtures, no crate, no theme.
 *
 * <pre>
 *   mvn -pl homing-site/homing-site-demo/homing-site-demo-hello -am compile exec:java \
 *       -Dexec.mainClass=hue.captains.singapura.js.homing.site.demo.hello.HelloServer
 * </pre>
 * Listens on 8094 ({@code -Dhello.port}).
 */
public final class HelloServer {

    private static final int PORT = Integer.getInteger("hello.port", 8094);

    private HelloServer() {}

    public static void main(String[] args) {
        SiteHost.start(HelloSite.INSTANCE, PORT)
                .onFailure(err -> System.exit(1));
    }
}
