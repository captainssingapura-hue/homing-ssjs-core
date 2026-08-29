package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.AppUrl;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * GET {@code /} — emits a tiny HTML page that immediately redirects to a target URL.
 * Used as the home-page entry-point so users typing the bare host (e.g.
 * {@code http://localhost:8081/}) land on the studio's main app instead of seeing a 404.
 *
 * <p>Implemented as an HTML response (meta-refresh + JS replace) rather than
 * a 302 because the host's {@code GetActionHandler} pipeline is built around
 * {@code TypedContent} bodies, not status-code redirects. The visible effect
 * is identical: the browser hops straight to the target URL without leaving
 * a flash of content.</p>
 */
public class RootRedirectGetAction
        implements GetAction<RoutingContext, RootRedirectGetAction.Query, EmptyParam.NoHeaders, HtmlPageContent> {

    /**
     * Captures the incoming request's session-style query keys so the
     * redirect target can carry them forward. Without this, navigating from
     * any themed page to {@code /} loses the user's theme/locale on the hop
     * to the configured target — and the matching {@code href.set} session-key
     * propagation in the browser-side {@code HrefManager} can't help here
     * because the redirect HTML is rendered before any user JS runs.
     */
    public record Query(String theme, String locale) implements Param._QueryString {}

    private final String targetUrl;

    /**
     * Backward-compat: build a redirect to the named app's flat address.
     * Use {@link #toUrl(String)} for a redirect to an arbitrary URL (e.g. one carrying
     * query params like {@code /app?app=catalogue&id=<fqn>} per RFC 0005).
     *
     * <p>RFC 0051 (D8) — minted through {@link AppUrl} rather than concatenated,
     * so this is not a seventh spelling of the flat address.</p>
     */
    public RootRedirectGetAction(String rootSimpleName) {
        this.targetUrl = AppUrl.flat(rootSimpleName, java.util.Map.of());
    }

    /** Build a redirect to an arbitrary URL. */
    public static RootRedirectGetAction toUrl(String targetUrl) {
        return new RootRedirectGetAction(targetUrl, true);
    }

    private RootRedirectGetAction(String url, boolean isFullUrl) {
        this.targetUrl = url;
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(
                ctx.request().getParam("theme"),
                ctx.request().getParam("locale"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<HtmlPageContent> execute(Query query, EmptyParam.NoHeaders headers) {
        // Forward the user's theme/locale onto the redirect target so the
        // chosen theme survives the hop. Companion to HrefManager's session-
        // key propagation — that handles the click on the way IN, this
        // handles the redirect on the way OUT to the home app.
        String target = targetUrl;
        target = appendIfMissing(target, "theme",  query.theme());
        target = appendIfMissing(target, "locale", query.locale());

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta http-equiv="refresh" content="0;url=%s">
                    <title>Redirecting…</title>
                </head>
                <body>
                    <script>window.location.replace(%s);</script>
                </body>
                </html>
                """.formatted(htmlAttrEscape(target), "\"" + jsStringEscape(target) + "\"");
        return CompletableFuture.completedFuture(new HtmlPageContent(html));
    }

    private static String appendIfMissing(String url, String key, String value) {
        if (value == null || value.isEmpty()) return url;
        // Caller already set the key on the target — caller wins.
        if (url.contains("?" + key + "=") || url.contains("&" + key + "=")) return url;
        char sep = url.indexOf('?') >= 0 ? '&' : '?';
        return url + sep + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String htmlAttrEscape(String s) {
        return s.replace("&", "&amp;").replace("\"", "&quot;");
    }

    private static String jsStringEscape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
