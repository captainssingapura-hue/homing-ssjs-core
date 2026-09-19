package hue.captains.singapura.js.homing.site;

import hue.captains.singapura.tao.http.action.ActionRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.PostAction;
import hue.captains.singapura.tao.http.config.HostConfig;
import hue.captains.singapura.tao.http.vertx.VertxActionHost;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;
import java.util.Objects;

/**
 * RFC 0066 Episode 3 — serves a {@link Site}: its router behind the one
 * catch-all {@link SiteGetAction}, on a port.
 *
 * <p>{@link #registry} is the composition and {@link #start} the hosting, kept
 * apart so a site can be mounted into a larger registry — beside the
 * server's {@code /module} and {@code /css-content}, say — without going
 * through this host's {@code main}-shaped start. The studio's Bootstrap is
 * that larger composition for one particular site; this is the floor it
 * will stand on.</p>
 */
public final class SiteHost {

    private SiteHost() {}

    /** The site as an action registry: one GET route, {@link SiteGetAction#ROUTE}. */
    public static ActionRegistry<RoutingContext> registry(Site site) {
        Objects.requireNonNull(site, "SiteHost.site");
        var action = new SiteGetAction(site.router());
        return new ActionRegistry<>() {
            @Override public Map<String, GetAction<RoutingContext, ?, ?, ?>> getActions() {
                return Map.of(SiteGetAction.ROUTE, action);
            }
            @Override public Map<String, PostAction<RoutingContext, ?, ?, ?>> postActions() {
                return Map.of();
            }
        };
    }

    /**
     * Hosts the site over plain HTTP on {@code port}, logging the address it
     * came up on. The future is the server, for a caller that wants to stop
     * it or to know that it is up; a {@code main} may ignore it.
     */
    public static Future<HttpServer> start(Site site, int port) {
        var host = new VertxActionHost(registry(site), HostConfig.http(port));
        return host.start()
                .onSuccess(server -> System.out.println(
                        site.name() + " listening on http://localhost:" + server.actualPort() + "/"))
                .onFailure(err -> System.err.println(
                        site.name() + " failed to start: " + err.getMessage()));
    }
}
