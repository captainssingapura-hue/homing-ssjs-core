package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.StampedParams;
import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.site.Html;
import hue.captains.singapura.js.homing.site.Placed;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.site.Trail;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A JS app as a page under the chrome: the {@link Placed} navigable an
 * {@code AppModule} bound to its params becomes.
 *
 * <p>The page is the scaffold the studio's flat route writes, redrawn for a
 * site: one document that imports the chrome module, mounts the bar with
 * the brand and the trail it was told, then imports the app and hands it the
 * slot and its params. The app is any {@code AppModule}, mounted where it
 * always is; it extends nothing to be a page here.</p>
 *
 * <p>The chrome data names the brand, the trail and the site's preferences
 * registry by its served address; the bar imports the registry only when its
 * button is pressed.</p>
 *
 * <p>Params: the binding's win, the query fills in the rest, and the app's
 * own codec decides what the page receives — so the page gets the app's
 * params and nothing else that happened to be in the address. The theme on
 * the module URLs is only the registry's default; the client resolves the
 * one it wears through the steward.</p>
 *
 * @param <P> the app's params type
 * @param <M> the app
 */
public record AppPage<P extends AppModule._Param, M extends AppModule<P, M>>(
        M app, P params, StandardMpa mpa) implements Placed {

    public AppPage {
        Objects.requireNonNull(app, "AppPage.app");
        Objects.requireNonNull(params, "AppPage.params (AppModule._None.INSTANCE for a paramless app)");
        Objects.requireNonNull(mpa, "AppPage.mpa");
    }

    @Override
    public HtmlPageContent html(Trail trail, Query query) {
        String title  = trail.isEmpty() ? app.title() : trail.last().text();
        String stamped = stampParams(query);
        String crumbs  = stampCrumbs(trail);
        String brand   = "Object.freeze({label:" + StampedParams.jsString(mpa.brand().label())
                       + ",home:" + StampedParams.jsString(mpa.brand().home()) + "})";
        String theme   = mpa.defaultTheme().map(StampedParams::jsString).orElse("null");
        String prefs   = "Object.freeze({module:" + StampedParams.jsString(mpa.moduleUrl(mpa.preferences())) + "})";

        return new HtmlPageContent("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>%s</title>
                    <link rel="icon" href="data:,">
                </head>
                <body>
                    <div id="app"></div>
                    <script type="module">
                        // The registry's default rides on the module URLs; the theme the page
                        // wears is the client's to resolve (address, then store, then this).
                        const theme = %s;
                        const themed = u => theme ? u + "&theme=" + encodeURIComponent(theme) : u;
                        const chrome = Object.freeze({ brand: %s, crumbs: %s, preferences: %s });
                        %s
                        const { mountChrome } = await import(themed(%s));
                        const main = mountChrome(document.getElementById("app"), chrome);
                        const { appMain } = await import(themed(%s));
                        appMain(main%s);
                    </script>
                </body>
                </html>
                """.formatted(
                        Html.escape(title + " · " + mpa.brand().label()),
                        theme, brand, crumbs, prefs,
                        stamped == null ? "" : "const params = " + stamped + ";",
                        StampedParams.jsString(mpa.moduleUrl(MpaChrome.INSTANCE)),
                        StampedParams.jsString(mpa.moduleUrl(app)),
                        stamped == null ? "" : ", params"));
    }

    /** The app's params as a JS object, or null when the app declares no codec. */
    private String stampParams(Query query) {
        ParamCodec<P> codec = app.paramCodec();
        if (codec == ParamCodec.None.INSTANCE) return null;
        Map<String, List<String>> merged = QueryString.params();
        query.all().forEach((k, vs) -> vs.forEach(v -> QueryString.put(merged, k, v)));
        codec.to(params).forEach((k, vs) -> { merged.remove(k); vs.forEach(v -> QueryString.put(merged, k, v)); });
        P effective = codec.from(merged) instanceof ParamCodec.Decoded.Ok<P>(P p) ? p : params;
        return StampedParams.jsObject(codec.to(effective));
    }

    private static String stampCrumbs(Trail trail) {
        var sb = new StringBuilder("Object.freeze([");
        boolean first = true;
        for (Trail.Crumb c : trail.crumbs()) {
            if (!first) sb.append(',');
            first = false;
            sb.append("Object.freeze({text:").append(StampedParams.jsString(c.text()))
              .append(",to:").append(StampedParams.jsString(c.href())).append("})");
        }
        return sb.append("])").toString();
    }
}
