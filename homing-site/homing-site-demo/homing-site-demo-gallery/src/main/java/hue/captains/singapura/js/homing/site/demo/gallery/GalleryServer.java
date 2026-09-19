package hue.captains.singapura.js.homing.site.demo.gallery;

/**
 * Serves {@link GallerySite} through its MPA: the framework's routes for
 * modules, sheets and themes, then the site's own.
 *
 * <pre>
 *   mvn -pl homing-site/homing-site-demo/homing-site-demo-gallery -am compile exec:java \
 *       -Dexec.mainClass=hue.captains.singapura.js.homing.site.demo.gallery.GalleryServer
 * </pre>
 * Listens on 8096 ({@code -Dgallery.port}).
 */
public final class GalleryServer {

    private static final int PORT = Integer.getInteger("gallery.port", 8096);

    private GalleryServer() {}

    public static void main(String[] args) {
        GallerySite.MPA.start(GallerySite.INSTANCE, PORT)
                .onFailure(err -> System.exit(1));
    }
}
