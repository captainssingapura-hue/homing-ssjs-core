package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import hue.captains.singapura.tao.http.action.TypedContent;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * GET {@code /themes} — what the site offers, for the preferences menu.
 *
 * <pre>
 * { "default": slug,
 *   "themes":   [ { slug, label, group, inspiration,
 *                   colours: [ { palette, slug, own?, fits? } ] } ],
 *   "palettes": [ { slug, label, group, inspiration } ] }
 * </pre>
 *
 * <p>A theme is a style worn in a palette (RFC 0067): the rows are the styles, each listed with every
 * palette the registry knows, whether it fits, and the slug each pairing makes.</p>
 */
public final class ThemesGetAction
        implements GetAction<RoutingContext, EmptyParam.NoQuery, EmptyParam.NoHeaders, ThemesGetAction.Json> {

    public static final String ROUTE = "/themes";

    public record Json(String body) implements TypedContent {
        @Override public String contentType() { return "application/json; charset=utf-8"; }
    }


    private final ThemeRegistry registry;

    public ThemesGetAction(ThemeRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "ThemesGetAction.registry");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, EmptyParam.NoQuery> queryStrMarshaller() {
        return ctx -> new EmptyParam.NoQuery();
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<Json> execute(EmptyParam.NoQuery query, EmptyParam.NoHeaders headers) {
        return CompletableFuture.completedFuture(new Json(serialize()));
    }

    /** The payload, for a caller that wants it without the wire. */
    public String serialize() {
        var sb = new StringBuilder("{\"default\":");
        sb.append(registry.themes().isEmpty() ? "null" : jstr(registry.themes().get(0).slug()));
        sb.append(",\"themes\":[");
        boolean first = true;
        for (Theme base : registry.bases()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('{').append(identity(base)).append(",\"colours\":[");
            boolean firstColour = true;
            for (Theme colours : registry.colours()) {
                Theme worn = registry.dressed(base, colours);
                if (!firstColour) sb.append(',');
                firstColour = false;
                sb.append("{\"palette\":").append(jstr(colours.slug()))
                  .append(",\"slug\":").append(jstr(worn.slug()))
                  .append(worn == base ? ",\"own\":true" : "")
                  .append(registry.fits(base, colours) ? ",\"fits\":true" : "")
                  .append('}');
            }
            sb.append("]}");
        }
        sb.append("],\"palettes\":[");
        first = true;
        for (Theme colours : registry.colours()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('{').append(identity(colours));
            sb.append('}');
        }
        return sb.append("]}").toString();
    }

    private static String identity(Theme t) {
        return "\"slug\":" + jstr(t.slug()) + ",\"label\":" + jstr(t.label())
             + ",\"group\":" + jstr(t.group()) + ",\"inspiration\":" + jstr(t.inspiration());
    }

    private static String jstr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
