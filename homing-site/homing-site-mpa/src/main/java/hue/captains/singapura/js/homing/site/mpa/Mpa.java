package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.site.Placed;
import hue.captains.singapura.js.homing.site.Site;
import hue.captains.singapura.tao.http.action.ActionRegistry;
import hue.captains.singapura.tao.http.config.HostConfig;
import hue.captains.singapura.tao.http.vertx.VertxActionHost;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

/**
 * What a multi-page app is to a site: its one declaration of brand, themes and
 * crates, from which a JS app becomes a page and the framework's routes are
 * mounted beside the site's own.
 *
 * <p>The DEFINITION, and all of it that core holds. An implementation decides
 * what a page looks like around the app — its chrome, and whatever the chrome
 * offers, such as preferences — and that is why the standard one,
 * {@code StandardMpa}, lives with the components it is built of
 * ({@code homing-site-mpa-standard}, in the ui-components repo): core defines,
 * and depends on no component.</p>
 *
 * <p>An MPA knows nothing of how the site routes: a page made here is a
 * {@link Placed} navigable, and whatever router hands it out may tell it where
 * it is.</p>
 */
public interface Mpa {

    /** The brand on the page's bar. */
    Brand brand();

    /** The themes the site offers; the first listed is its default. */
    ThemeRegistry themes();

    /** The registry's default — first listed — when it lists any. */
    default Optional<String> defaultTheme() {
        return themes().themes().isEmpty() ? Optional.empty() : Optional.of(themes().themes().get(0).slug());
    }

    /** The address a served module is imported from, without the theme. */
    String moduleUrl(EsModule<?> module);

    /** {@code app} bound to {@code params}, as a page of this MPA. */
    <P extends AppModule._Param, M extends AppModule<P, M>> Placed page(M app, P params);

    /** A paramless app as a page of this MPA. */
    default <M extends AppModule<AppModule._None, M>> Placed page(M app) {
        return page(app, AppModule._None.INSTANCE);
    }

    /**
     * The framework's routes, then the site's catch-all. Insertion order is the
     * order the host mounts them, and the catch-all must come last or it would
     * answer for the framework's routes too.
     */
    ActionRegistry<RoutingContext> registry(Site site);

    /** Hosts the site with this MPA over plain HTTP on {@code port}. */
    default Future<HttpServer> start(Site site, int port) {
        var host = new VertxActionHost(registry(site), HostConfig.http(port));
        return host.start()
                .onSuccess(server -> System.out.println(
                        site.name() + " listening on http://localhost:" + server.actualPort() + "/"))
                .onFailure(err -> System.err.println(
                        site.name() + " failed to start: " + err.getMessage()));
    }
}
