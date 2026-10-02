package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Emphasis;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.Layer;
import hue.captains.singapura.js.homing.design.Mode;
import hue.captains.singapura.js.homing.design.Palette;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.Text;
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
 *   "themes":   [ { slug, label, group, inspiration, swatches,
 *                   colours: [ { palette, slug, own?, fits? } ] } ],
 *   "palettes": [ { slug, label, group, inspiration, anchor?, swatches } ] }
 * </pre>
 *
 * <p>A theme is a design worn in colours (RFC 0066 E2): the bases are the
 * designs, each listed with every palette the registry knows and whether
 * it fits, and the slug each pairing makes. The swatches are four of the
 * design's own bound colours, read from its light-mode rest values — data
 * for the dots beside a row, nothing the client would otherwise know.</p>
 */
public final class ThemesGetAction
        implements GetAction<RoutingContext, EmptyParam.NoQuery, EmptyParam.NoHeaders, ThemesGetAction.Json> {

    public static final String ROUTE = "/themes";

    public record Json(String body) implements TypedContent {
        @Override public String contentType() { return "application/json; charset=utf-8"; }
    }

    private record Swatch(String name, DesignClass<?> pair, String property) {}

    private static final List<Swatch> SWATCHES = List.of(
            new Swatch("surface",  DesignClass.of(Layer.Base.class,       Target.Color.Surface.class), "background-color"),
            new Swatch("inverted", DesignClass.of(Layer.Inverted.class,   Target.Color.Surface.class), "background-color"),
            new Swatch("accent",   DesignClass.of(Emphasis.Primary.class, Target.Color.Surface.class), "background-color"),
            new Swatch("text",     DesignClass.of(Text.Body.class,        Target.Color.Ink.class),     Impl.Bindings.SOLE));

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
            sb.append('{').append(identity(base)).append(",\"swatches\":").append(swatches(base)).append(",\"colours\":[");
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
            if (colours instanceof Palette p) sb.append(",\"anchor\":").append(jstr(p.anchor().slug()));
            sb.append(",\"swatches\":").append(swatches(colours)).append('}');
        }
        return sb.append("]}").toString();
    }

    private static String identity(Theme t) {
        return "\"slug\":" + jstr(t.slug()) + ",\"label\":" + jstr(t.label())
             + ",\"group\":" + jstr(t.group()) + ",\"inspiration\":" + jstr(t.inspiration());
    }

    private static String swatches(Theme t) {
        var sb = new StringBuilder("{");
        boolean first = true;
        for (Swatch s : SWATCHES) {
            if (!first) sb.append(',');
            first = false;
            sb.append(jstr(s.name())).append(':').append(jstr(t instanceof Design d ? word(d, s) : ""));
        }
        return sb.append('}').toString();
    }

    private static String word(Design d, Swatch s) {
        if (!(d.impl(s.pair()) instanceof Impl.Bindings b)) return "";
        var rest = b.values().getOrDefault(Mode.LIGHT, Map.of()).getOrDefault(State.REST, Map.of());
        String v = rest.get(s.property());
        return v != null ? v : rest.getOrDefault(Impl.Bindings.SOLE, "");
    }

    private static String jstr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
