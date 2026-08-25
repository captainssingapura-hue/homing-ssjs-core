package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.server.AppHtmlGetAction;
import hue.captains.singapura.js.homing.server.AppQuery;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * RFC 0051 D4 — {@code /app?app=…&args} redirects to its path when the thing
 * it names has one; otherwise it renders exactly as before.
 *
 * <p>This is what makes parity hold BY CONSTRUCTION rather than by everyone
 * remembering. Only one form is ever displayed, so the address bar cannot
 * disagree with the breadcrumb — and no emitter had to be rewritten to get
 * there. Q3 settled that: emitters keep minting {@code (app, args)}, the
 * server owns positions, and redirection is a function rather than a third
 * way to address a thing.</p>
 *
 * <p>The case that pays for it is cross-references. A link inside doc content
 * is authored against a UUID, and a UUID has no idea where its doc sits —
 * converting every such emitter would mean teaching the whole content layer
 * about the tree. Inverting the address once, here, means a cross-reference
 * written years ago lands on the authentic path today.</p>
 *
 * <p>An {@code (app, args)} with no position renders flat and keeps its own
 * crumb. That is honest: a workspace shell or a picker genuinely is not in
 * the catalogue, and pretending otherwise would invent a position rather
 * than report one.</p>
 */
public final class FlatToPathRedirectGetAction
        implements GetAction<RoutingContext, AppQuery, EmptyParam.NoHeaders, HtmlPageContent> {

    private final CatalogueRegistry registry;
    private final AppHtmlGetAction inner;

    public FlatToPathRedirectGetAction(CatalogueRegistry registry, AppHtmlGetAction inner) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.inner    = Objects.requireNonNull(inner, "inner");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, AppQuery> queryStrMarshaller() {
        return inner.queryStrMarshaller();
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return inner.headerMarshaller();
    }

    @Override
    public CompletableFuture<HtmlPageContent> execute(AppQuery query, EmptyParam.NoHeaders headers) {
        CataloguePath path = (query.simpleName() == null) ? null
                : registry.pathForFlat(query.simpleName(), query.all());
        if (path == null) {
            return inner.execute(query, headers);
        }
        return CompletableFuture.completedFuture(new HtmlPageContent(redirectHtml(target(path, query))));
    }

    /**
     * The path, plus the query keys that refine rather than identify.
     *
     * <p>{@code ?phase=2} and {@code ?theme=} must survive the hop or the
     * redirect would quietly drop what the user asked for; the identity keys
     * must NOT, because the path already says which node this is and
     * repeating it invites the two to disagree.</p>
     */
    private String target(CataloguePath path, AppQuery query) {
        var identity = registry.flatIdentityKeysFor(query.simpleName());
        var carried = QueryString.params();
        for (var e : query.all().entrySet()) {
            if ("app".equals(e.getKey()) || identity.contains(e.getKey())) continue;
            for (String v : e.getValue()) QueryString.put(carried, e.getKey(), v);
        }
        return path.toUrl() + QueryString.encodeSuffix(carried);
    }

    /**
     * Meta-refresh plus {@code location.replace}, matching
     * {@code RootRedirectGetAction} — the host's pipeline is built around
     * typed bodies rather than status codes, so a 302 is not available here.
     *
     * <p>{@code replace} rather than {@code assign} on purpose: the flat form
     * should not become a back-button stop, or the user would bounce between
     * two addresses for one page.</p>
     */
    private static String redirectHtml(String target) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta http-equiv="refresh" content="0;url=%s">
                    <title>Redirecting…</title>
                </head>
                <body><script>window.location.replace(%s);</script></body>
                </html>
                """.formatted(htmlAttrEscape(target),
                              hue.captains.singapura.js.homing.core.StampedParams.jsString(target));
    }

    private static String htmlAttrEscape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
