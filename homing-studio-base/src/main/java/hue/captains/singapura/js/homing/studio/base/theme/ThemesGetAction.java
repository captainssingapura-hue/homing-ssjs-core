package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Emphasis;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.Layer;
import hue.captains.singapura.js.homing.design.Mode;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.Text;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /themes} — the registry on its two axes, for the picker: the
 * bases a page may wear, the colours each may be worn in, and for every
 * (base, colours) the slug that names the pair. The picker composes nothing;
 * it reads the slug off the entry the user chose.
 *
 * <pre>{@code
 * {
 *   "themes": [ { "slug": "neo-brutalism", "label": …, "group": …, "inspiration": …,
 *                 "swatches": { "surface": "#FFFFFF", "inverted": "#000000", "accent": "#FFE800",
 *                               "link": "#2B4CFF", "text": "#000000", "muted": "#4A4A4A", "edge": "#000000" },
 *                 "colours": [ { "palette": "neo-brutalism", "slug": "neo-brutalism", "own": true },
 *                              { "palette": "forest",        "slug": "neo-brutalism_forest" }, … ] }, … ],
 *   "palettes": [ { "slug": "forest", "label": "Forest", "inspiration": …, "swatches": { … } }, … ]
 * }
 * }</pre>
 *
 * <p>Swatches are read off the design's own words — the base surface, the
 * inverted surface, the primary surface, the link, body and muted inks, the
 * primary edge — and, for a registry not yet on designs, off its legacy
 * palette provision under the same seven names.</p>
 */
public class ThemesGetAction
        implements GetAction<RoutingContext, EmptyParam.NoQuery, EmptyParam.NoHeaders, DocContent> {

    /** The seven swatches, as (name, pair, property) — the design-side reading. */
    private record Swatch(String name, DesignClass<?> pair, String property, CssVar legacy) {}

    private static final List<Swatch> SWATCHES = List.of(
            new Swatch("surface",  DesignClass.of(Layer.Base.class,        Target.Color.Surface.class), "background-color", HomingVars.COLOR_SURFACE),
            new Swatch("inverted", DesignClass.of(Layer.Inverted.class,    Target.Color.Surface.class), "background-color", HomingVars.COLOR_SURFACE_INVERTED),
            new Swatch("accent",   DesignClass.of(Emphasis.Primary.class,  Target.Color.Surface.class), "background-color", HomingVars.COLOR_ACCENT),
            new Swatch("link",     DesignClass.of(Text.Link.class,         Target.Color.Ink.class),     Impl.Bindings.SOLE,  HomingVars.COLOR_TEXT_LINK),
            new Swatch("text",     DesignClass.of(Text.Body.class,         Target.Color.Ink.class),     Impl.Bindings.SOLE,  HomingVars.COLOR_TEXT_PRIMARY),
            new Swatch("muted",    DesignClass.of(Emphasis.Muted.class,    Target.Color.Ink.class),     Impl.Bindings.SOLE,  HomingVars.COLOR_TEXT_MUTED),
            new Swatch("edge",     DesignClass.of(Emphasis.Primary.class,  Target.Color.Edge.class),    "border-color",     HomingVars.COLOR_BORDER_EMPHASIS)
    );

    private final ThemeRegistry registry;

    public ThemesGetAction(ThemeRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
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
    public CompletableFuture<DocContent> execute(EmptyParam.NoQuery query, EmptyParam.NoHeaders headers) {
        return CompletableFuture.completedFuture(
                new DocContent(serialize(), "application/json; charset=utf-8"));
    }

    String serialize() {
        StringBuilder sb = new StringBuilder("{\"themes\":[");
        boolean first = true;
        for (Theme base : registry.bases()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('{').append(identity(base)).append(',')
              .append("\"swatches\":").append(swatches(base)).append(',')
              .append("\"colours\":[");
            boolean firstColour = true;
            for (Theme colours : registry.colours()) {
                Theme worn = registry.dressed(base, colours);
                if (!firstColour) sb.append(',');
                firstColour = false;
                sb.append("{\"palette\":").append(jstr(colours.slug()))
                  .append(",\"slug\":").append(jstr(worn.slug()))
                  .append(worn == base ? ",\"own\":true" : "")
                  .append('}');
            }
            sb.append("]}");
        }
        sb.append("],\"palettes\":[");
        first = true;
        for (Theme colours : registry.colours()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('{').append(identity(colours)).append(',')
              .append("\"swatches\":").append(swatches(colours)).append('}');
        }
        return sb.append("]}").toString();
    }

    private static String identity(Theme t) {
        return "\"slug\":" + jstr(t.slug()) + ",\"label\":" + jstr(t.label()) + ",\"group\":" + jstr(t.group())
             + ",\"inspiration\":" + jstr(t.inspiration());
    }

    /** The seven swatches of a theme, in daylight: off its words when it is a design, off its provision otherwise. */
    private String swatches(Theme t) {
        var out = new LinkedHashMap<String, String>();
        PaletteProvision<?, ?> legacy = t instanceof Design ? null : registry.paletteForSlug(t.slug(), GlobalColorPalette.INSTANCE);
        for (Swatch s : SWATCHES) {
            String v = t instanceof Design d ? word(d, s) : legacy != null ? legacy.values().getOrDefault(s.legacy(), "") : "";
            out.put(s.name(), v == null ? "" : v);
        }
        var sb = new StringBuilder("{");
        boolean first = true;
        for (var e : out.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(jstr(e.getKey())).append(':').append(jstr(e.getValue()));
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
