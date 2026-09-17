package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SimpleAppResolver;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.CompletableFuture;

/**
 * GET {@code /app?app=<simpleName>}    — the RFC 0001 contract; resolves via {@link SimpleAppResolver}.
 * <br>
 * GET {@code /app?class=<canonical>}   — legacy fallback; uses reflection.
 *
 * <p>If a {@link SimpleAppResolver} is configured, {@code ?app=} is supported.
 * If not, only {@code ?class=} works (legacy mode for servers built before Step 07).</p>
 *
 * <p>Proxy-app names are explicitly rejected: a proxy is a URL builder, not a
 * navigable target. Requests for {@code ?app=&lt;proxy-name&gt;} return 404.</p>
 */
public class AppHtmlGetAction
        implements GetAction<RoutingContext, AppQuery, EmptyParam.NoHeaders, HtmlPageContent> {

    private final ModuleNameResolver nameResolver;
    private final SimpleAppResolver appResolver;   // may be null in legacy-only mode
    private final ThemeRegistry themeRegistry;     // RFC 0002-ext1 — for the theme picker widget
    private final AppMeta meta;                    // downstream-supplied brand label



    public AppHtmlGetAction(ModuleNameResolver nameResolver) {
        this(nameResolver, null, ThemeRegistry.EMPTY, AppMeta.DEFAULT);
    }

    public AppHtmlGetAction(ModuleNameResolver nameResolver, SimpleAppResolver appResolver) {
        this(nameResolver, appResolver, ThemeRegistry.EMPTY, AppMeta.DEFAULT);
    }

    public AppHtmlGetAction(ModuleNameResolver nameResolver, SimpleAppResolver appResolver, ThemeRegistry themeRegistry) {
        this(nameResolver, appResolver, themeRegistry, AppMeta.DEFAULT);
    }

    /**
     * RFC 0051 — no ChromeResolver. This action used to take one, a function
     * from {@code (app, args)} to a trail, so a flat render could show the
     * breadcrumb of a position its own URL did not state. That was the last
     * second derivation in the system: everywhere else the crumb IS the path,
     * read off the address, and here alone it was looked up.
     *
     * <p>The path route resolves the node anyway and now passes the crumbs it
     * already holds. The flat route passes none, which D4 makes honest — /app
     * renders rather than redirects, so a raw address is a permalink, and a
     * permalink states no position.</p>
     */
    public AppHtmlGetAction(ModuleNameResolver nameResolver, SimpleAppResolver appResolver,
                             ThemeRegistry themeRegistry, AppMeta meta) {
        this.nameResolver = nameResolver;
        this.appResolver = appResolver;
        this.themeRegistry = themeRegistry != null ? themeRegistry : ThemeRegistry.EMPTY;
        this.meta = meta != null ? meta : AppMeta.DEFAULT;
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, AppQuery> queryStrMarshaller() {
        return ctx -> new AppQuery(
                ctx.request().getParam("app"),
                ctx.request().getParam("class"),
                ctx.request().getParam("theme"),
                ctx.request().getParam("locale"),
                // RFC 0051 — the app's own params travel as the raw map; the
                // resolved app's codec is what turns them into a typed value.
                hue.captains.singapura.js.homing.core.QueryString.parse(ctx.request().query())
        );
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<HtmlPageContent> execute(AppQuery query, EmptyParam.NoHeaders headers) {
        if (!query.hasSimpleName() && !query.hasClassName()) {
            return CompletableFuture.failedFuture(ResourceNotFound.missingClass());
        }

        AppModule<?, ?> app;
        try {
            if (query.hasSimpleName()) {
                if (appResolver == null) {
                    return CompletableFuture.failedFuture(notFound(query.simpleName(),
                            "?app= dispatch requires a SimpleAppResolver — server was constructed without one"));
                }
                if (appResolver.resolveProxy(query.simpleName()) != null) {
                    return CompletableFuture.failedFuture(notFound(query.simpleName(),
                            "Proxy apps are not routable; they are URL builders for typed external links"));
                }
                app = appResolver.resolveApp(query.simpleName());
                if (app == null) {
                    return CompletableFuture.failedFuture(notFound(query.simpleName(),
                            "No app registered with this simple name"));
                }
            } else {
                // Legacy ?class= path — kept for backwards compatibility during Step 11 migration.
                Class<?> clazz = Class.forName(query.className());
                Object instance;
                try {
                    var instanceField = clazz.getField("INSTANCE");
                    instance = instanceField.get(null);
                } catch (NoSuchFieldException e) {
                    instance = clazz.getDeclaredConstructor().newInstance();
                }
                if (!(instance instanceof AppModule<?, ?> a)) {
                    return CompletableFuture.failedFuture(
                            ResourceNotFound.wrongType(query.className(), "AppModule"));
                }
                app = a;
            }
        } catch (Exception e) {
            String resource = query.hasSimpleName() ? query.simpleName() : query.className();
            return CompletableFuture.failedFuture(ResourceNotFound.forClass(resource, e));
        }

        return dispatch(app, query);
    }

    /**
     * RFC 0051 Phase 6 — bridge from the wire form to the typed one. Exists to
     * name the app's params type {@code P}, which {@code execute} cannot: it
     * holds an {@code AppModule<?, ?>}, and decoding needs the codec typed.
     */
    private <P extends hue.captains.singapura.js.homing.core.AppModule._Param>
            CompletableFuture<HtmlPageContent> dispatch(
                    hue.captains.singapura.js.homing.core.AppModule<P, ?> app, AppQuery query) {

        var decoded = app.paramCodec().from(query.all());
        P params = (decoded instanceof hue.captains.singapura.js.homing.core.ParamCodec.Decoded.Ok<P>(P p))
                ? p : null;
        // RFC 0051 — no crumbs. A flat address states no position, so this
        // route shows none: D4 kept /app as a render route rather than a
        // redirect, which makes a raw (app, args) a PERMALINK rather than a
        // page in the tree. Looking a trail up here would be the last second
        // derivation in the system — a claim about where you are that the
        // address does not make. /goto is what answers that question.
        return executeTyped(app, params, query.theme(), query.locale(), query.all(), null);
    }

    /**
     * RFC 0051 Phase 6 — the TYPED entry point, for callers inside Java that
     * already hold an app and its params.
     *
     * <p>A codec is a boundary translator, and between two Java callers there
     * is no boundary. {@code /app} needs one because its app name and params
     * genuinely arrive as strings; {@code /cat} does not, because it resolved a
     * node whose binding was typed all along. Before this existed, the path
     * route flattened its node to {@code (String, Map)} so this action could
     * look the app back up by name and decode the map it had just encoded —
     * and {@code stampParams} then RE-encoded the result, a Map to P to Map
     * round trip inside one method.</p>
     *
     * <p>{@code execute(AppQuery)} stays as the wire adapter and is the right
     * shape for the wire. This is the right shape for everything else.</p>
     *
     * @param params    the app's typed params; null when it declares no codec,
     *                  which is exactly when nothing is stamped
     * @param allQuery  the full query the page was reached with — the crumb
     *                  lookup still consults an index keyed on the wire form,
     *                  so it is passed through unchanged until that index is
     *                  re-keyed on the typed pair
     */
    public <P extends hue.captains.singapura.js.homing.core.AppModule._Param>
            CompletableFuture<HtmlPageContent> executeTyped(
                    hue.captains.singapura.js.homing.core.AppModule<P, ?> app,
                    P params, String theme, String locale,
                    java.util.Map<String, java.util.List<String>> allQuery,
                    java.util.List<java.util.Map.Entry<String, String>> crumbs) {

        AppQuery query = new AppQuery(app.simpleName(), null, theme, locale,
                allQuery == null ? java.util.Map.of() : allQuery);
        // RFC 0064 — the server no longer takes the page's ?theme=. The context
        // is the client's to resolve (the steward: address as override, stored
        // preference, registry default) and to put on the resources that vary
        // by it. What the page template propagates is only the registry's
        // default, so every downstream that still expects a concrete slug —
        // the theme bundle, module URLs, the three template parts — sees one.
        // The address's ?theme= is read by the client, not here.
        String effectiveTheme = themeRegistry.themes().isEmpty()
                ? null
                : themeRegistry.themes().get(0).slug();

        // RFC 0051 — stamp the app's params into the page, so the client does
        // not re-parse a URL the server has already interpreted. Only apps
        // that declare a codec are stamped; the rest keep today's behaviour
        // exactly, including their generated client-side params const, so the
        // migration is per-app rather than a flag day.
        String stampedParams = stampParams(app, params);

        // RFC 0051 Phase 5 — the breadcrumb, resolved here rather than fetched
        // by the chrome after paint. The server walked the tree to answer this
        // request; asking the browser to ask again is what makes the trail
        // pop in a beat late.
        String stampedCrumbs = stampCrumbs(crumbs);

        String baseModuleUrl = nameResolver.resolve(app).basePath();
        String themeJs  = effectiveTheme != null ? "\"" + effectiveTheme + "\"" : "null";

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>%s</title>
                    <!-- Inline SVG favicon — silences the browser's automatic
                         /favicon.ico request without a static asset round-trip.
                         Amber H matches the studio brand's accent. Upgraded to
                         the studio's brand logo (when registered) by the small
                         /brand-fetch script below. -->
                    <link rel="icon" id="__homing_favicon__" type="image/svg+xml" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 64 64'><text x='50%%' y='55%%' text-anchor='middle' dominant-baseline='central' font-family='Georgia,serif' font-weight='700' font-size='44' fill='%%23F4B942'>H</text></svg>">
                    <script>
                        // Upgrade the favicon to the studio's brand logo when
                        // /brand is registered AND carries a non-empty `logo`.
                        // Degrades silently — studios without StudioBrand wiring,
                        // or with no logo, keep the framework-default amber H.
                        fetch("/brand").then(function (r) { return r.ok ? r.json() : null; })
                            .then(function (brand) {
                                if (!brand || !brand.logo) return;
                                var link = document.getElementById("__homing_favicon__");
                                if (!link) return;
                                // btoa needs binary; unescape(encodeURIComponent(...))
                                // is the canonical UTF-8-safe encoding bridge.
                                link.href = "data:image/svg+xml;base64,"
                                          + btoa(unescape(encodeURIComponent(brand.logo)));
                            })
                            .catch(function () { /* fetch failed — keep the default */ });
                    </script>
                </head>
                <body>
                    <div id="app"></div>
                    <script type="module">
                        // RFC 0064: the page is served under the registry's default theme
                        // and no locale. Neither is the address's to say here — the client
                        // resolves both through the preference steward (an ?override for
                        // this page, else the stored pick, else the default) and puts them
                        // on the resources that vary by them. What rides on the module URL
                        // is only the default the server propagated. Browser
                        // prefers-color-scheme is intentionally ignored — themes are
                        // explicit, not auto-derived.
                        const theme = %s;
                        // color-scheme is NOT set from the theme slug. It used to be
                        // — `style.colorScheme = theme` — which assigned "carbon" or
                        // "forest" to a property that accepts only
                        // normal | light | dark | light dark. Every theme was therefore
                        // setting an invalid value, the browser fell back to light, and
                        // the inline style outranked the `:root { color-scheme: … }` each
                        // theme declares in its own Globals CSS. Harmless while every
                        // theme was light-primary; visible the moment one was not, as a
                        // bright scrollbar down a near-black page.
                        //
                        // The theme owns this. Nothing to do here.
                        let moduleUrl = "%s";
                        if (theme) moduleUrl += "&theme=" + encodeURIComponent(theme);
                        %s
                        %s
                        const { appMain } = await import(moduleUrl);
                        appMain(document.getElementById("app")%s);
                    </script>
                </body>
                </html>
                """.formatted(htmlEscape(app.title() + " · " + meta.label()),
                              themeJs, baseModuleUrl,
                              stampedParams == null ? "" : "const params = " + stampedParams + ";",
                              stampedCrumbs == null ? "" : "const chrome = Object.freeze({crumbs: "
                                                          + stampedCrumbs + "});",
                              // A third argument, so an app that wants only
                              // params is unaffected and one that wants the
                              // chrome can take it. Passed whenever either is
                              // present, since JS ignores extra arguments but
                              // cannot skip a middle one.
                              (stampedParams == null && stampedCrumbs == null) ? ""
                                      : (stampedParams == null ? ", undefined" : ", params")
                                        + (stampedCrumbs == null ? "" : ", chrome"));

        return CompletableFuture.completedFuture(new HtmlPageContent(html));
    }

    private static String htmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason)
        );
    }

    /**
     * RFC 0051 — the app's params as a stamped JS value, or null when this app
     * has no codec and should keep today's client-side parsing.
     *
     * <p>Params go through the codec in both directions rather than being
     * copied from the query: {@code from} drops keys the app does not claim
     * (the action's own theme and locale among them) and {@code to} writes
     * back only what the record holds, so the page receives the app's params
     * and nothing else that happened to be in the URL.</p>
     *
     * <p>A decode failure stamps nothing rather than failing the request.
     * Turning malformed params into a 400 is the right end state and what the
     * Decoded ADT exists for, but it changes the response for every app at
     * once; that belongs with the route work, not with adding a stamp.</p>
     */
    private static <P extends hue.captains.singapura.js.homing.core.AppModule._Param>
            String stampParams(hue.captains.singapura.js.homing.core.AppModule<P, ?> app, P params) {
        var codec = app.paramCodec();
        if (codec == hue.captains.singapura.js.homing.core.ParamCodec.None.INSTANCE) return null;
        if (params == null) return null;   // malformed on the way in; stamp nothing (D5 deferral)
        return hue.captains.singapura.js.homing.core.StampedParams.jsObject(codec.to(params));
    }

    /**
     * The page's breadcrumb as a frozen JS array, or null when there is none
     * to state.
     *
     * <p>Escaped through the same {@link StampedParams#jsString} the params
     * use: crumb text is doc titles and catalogue names, which are authored
     * content rather than user input, but a page that escapes one source and
     * not another is a page whose safety depends on knowing which is which.
     * One rule for everything entering a script.</p>
     */
    private String stampCrumbs(java.util.List<java.util.Map.Entry<String, String>> crumbs) {
        if (crumbs == null || crumbs.isEmpty()) return null;
        var sb = new StringBuilder("Object.freeze([");
        boolean first = true;
        for (var c : crumbs) {
            if (!first) sb.append(',');
            first = false;
            sb.append("Object.freeze({text:")
              .append(hue.captains.singapura.js.homing.core.StampedParams.jsString(c.getKey()))
              .append(",href:")
              .append(hue.captains.singapura.js.homing.core.StampedParams.jsString(c.getValue()))
              .append("})");
        }
        return sb.append("])").toString();
    }
}
