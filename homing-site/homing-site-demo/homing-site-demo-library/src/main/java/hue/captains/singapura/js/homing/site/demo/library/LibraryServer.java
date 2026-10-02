package hue.captains.singapura.js.homing.site.demo.library;

import hue.captains.singapura.js.homing.site.SiteHost;

/**
 * Serves {@link LibrarySite}. A site and a port; the tree was checked when
 * the site class loaded, so a broken catalogue fails here, before the port
 * is taken.
 *
 * <pre>
 *   mvn -pl homing-site/homing-site-demo/homing-site-demo-library -am compile exec:java \
 *       -Dexec.mainClass=hue.captains.singapura.js.homing.site.demo.library.LibraryServer
 * </pre>
 * Listens on 8095 ({@code -Dlibrary.port}).
 */
public final class LibraryServer {

    private static final int PORT = Integer.getInteger("library.port", 8095);

    private LibraryServer() {}

    public static void main(String[] args) {
        SiteHost.start(LibrarySite.INSTANCE, PORT)
                .onFailure(err -> System.exit(1));
    }
}
