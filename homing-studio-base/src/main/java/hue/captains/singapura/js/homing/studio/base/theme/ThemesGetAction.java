package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeVariables;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /themes} — emits a JSON catalogue of every registered {@link Theme}
 * with its key palette swatches, consumed by the {@code ThemesIntro} page.
 *
 * <p>The selected swatch keys cover the page's preview surface — page bg,
 * header band, accent, link, primary text, muted text, and the emphasis
 * border. Every theme guarantees these via the semantic vocabulary in
 * {@link StudioVars}.</p>
 *
 * <p>Response shape:</p>
 * <pre>{@code
 * {
 *   "themes": [
 *     {
 *       "slug":  "default",
 *       "label": "Default",
 *       "palette": {
 *         "surface":          "#FAFBFC",
 *         "surface-inverted": "#1B1F3A",
 *         "accent":           "#D4A04C",
 *         "text-link":        "#2545B0",
 *         "text-primary":     "#1F2447",
 *         "text-muted":       "#6B6F8E",
 *         "border-emphasis":  "#D4A04C"
 *       }
 *     },
 *     ...
 *   ]
 * }
 * }</pre>
 */
public class ThemesGetAction
        implements GetAction<RoutingContext, EmptyParam.NoQuery, EmptyParam.NoHeaders, DocContent> {

    /** Subset of StudioVars used in the page swatches. Order = render order. */
    private static final List<CssVar> PALETTE_KEYS = List.of(
            StudioVars.COLOR_SURFACE,
            StudioVars.COLOR_SURFACE_INVERTED,
            StudioVars.COLOR_ACCENT,
            StudioVars.COLOR_TEXT_LINK,
            StudioVars.COLOR_TEXT_PRIMARY,
            StudioVars.COLOR_TEXT_MUTED,
            StudioVars.COLOR_BORDER_EMPHASIS
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
        for (Theme theme : registry.themes()) {
            if (!first) sb.append(',');
            first = false;
            ThemeVariables<?> vars = findVars(theme);
            Map<CssVar, String> values = vars != null ? vars.values() : Map.of();

            sb.append("{\"slug\":") .append(jstr(theme.slug())) .append(',')
              .append("\"label\":").append(jstr(theme.label())).append(',')
              .append("\"group\":").append(jstr(theme.group())).append(',')
              .append("\"palette\":{");
            boolean firstKey = true;
            for (CssVar k : PALETTE_KEYS) {
                if (!firstKey) sb.append(',');
                firstKey = false;
                sb.append(jstr(stripVarPrefix(k.name())))
                  .append(':')
                  .append(jstr(values.getOrDefault(k, "")));
            }
            sb.append("}}");
        }
        sb.append("]}");
        return sb.toString();
    }

    /** Find the {@link ThemeVariables} entry whose theme matches the given Theme by slug. */
    private ThemeVariables<?> findVars(Theme theme) {
        for (var v : registry.variables()) {
            if (v.theme().slug().equals(theme.slug())) return v;
        }
        return null;
    }

    /** Strip the leading "--" so the JSON keys read as friendly names ("surface" not "--color-surface"). */
    private static String stripVarPrefix(String name) {
        String n = name.startsWith("--") ? name.substring(2) : name;
        return n.startsWith("color-") ? n.substring("color-".length()) : n;
    }

    private static String jstr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
