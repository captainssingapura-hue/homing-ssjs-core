package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.Component;
import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.Layer;
import hue.captains.singapura.js.homing.core.Layers;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.util.CssClassName;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * GET /css-content?class=&lt;CssGroup or DerivedCss canonical name&gt;[&amp;theme=|style=|palette=&lt;slug&gt;]
 *
 * <p>A provided group renders from its declared bodies, each class
 * {@code selector { body }} in its {@code @layer}: the same in every theme, so a
 * theme on the request is ignored and none is needed. A derived sheet is its
 * renderer's, under the slug asked for by the name of what it varies with - the
 * theme, the style or the palette, the others ignored - else its own fallback,
 * else, for one varying with the theme, the default theme (RFC 0064, D25; RFC
 * 0067, appendix Two Classes per Leaf).</p>
 *
 * <p>Hard cut: unknown {@code class} or {@code theme} returns 404. No
 * file-based fallback (RFC 0002 §3.6).</p>
 */
public class CssContentGetAction
        implements GetAction<RoutingContext, CssQuery, EmptyParam.NoHeaders, CssContent> {

    private final Theme defaultTheme;
    private final ServedModules served;
    private final List<CssRenderer> renderers;

    /**
     * @param defaultTheme the theme to use when a request omits {@code ?theme=}.
     *                     May be {@code null}, in which case unparameterized
     *                     requests return 404
     */
    public CssContentGetAction(Theme defaultTheme) {
        this(defaultTheme, ServedModules.NONE, List.of());
    }

    /**
     * @param served    the deployment's modules by canonical name — a group is resolved by lookup, never by reflection
     * @param renderers the design side's renderers, asked first; a group none claims renders from its bodies
     */
    public CssContentGetAction(Theme defaultTheme, ServedModules served, List<CssRenderer> renderers) {
        this.defaultTheme = defaultTheme;
        this.served = served == null ? ServedModules.NONE : served;
        this.renderers = List.copyOf(renderers);
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, CssQuery> queryStrMarshaller() {
        return ctx -> new CssQuery(
                ctx.request().getParam("class"),
                ctx.request().getParam(DerivedCss.VariesWith.THEME.param()),
                ctx.request().getParam(DerivedCss.VariesWith.STYLE.param()),
                ctx.request().getParam(DerivedCss.VariesWith.PALETTE.param())
        );
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<CssContent> execute(CssQuery query, EmptyParam.NoHeaders headers) {
        if (query.className() == null || query.className().isBlank()) {
            return CompletableFuture.failedFuture(ResourceNotFound.missingClass());
        }
        try {
            var found = served.find(query.className());
            if (found.isEmpty()) {
                return CompletableFuture.failedFuture(ResourceNotFound.forClass(query.className(),
                        new IllegalStateException("group '" + query.className() + "' is not declared in any registered crate")));
            }
            if (found.get() instanceof CssGroup<?> group) return CompletableFuture.completedFuture(new CssContent(renderCss(group)));
            if (!(found.get() instanceof DerivedCss<?> sheet)) {
                return CompletableFuture.failedFuture(ResourceNotFound.wrongType(query.className(), "CssGroup or DerivedCss"));
            }

            DerivedCss.VariesWith varies = sheet.variesWith();
            String slug = query.slugFor(varies);
            if (slug == null) slug = sheet.fallback().orElse(null);
            if (slug == null && varies == DerivedCss.VariesWith.THEME && defaultTheme != null) slug = defaultTheme.slug();
            if (slug == null) {
                return CompletableFuture.failedFuture(ResourceNotFound.forClass(
                        query.className(),
                        new IllegalStateException("No " + varies.param() + " specified, and the sheet names no fallback"
                                + (varies == DerivedCss.VariesWith.THEME ? " and no default theme is configured" : ""))));
            }
            for (CssRenderer r : renderers) {
                var css = r.render(sheet, slug);
                if (css.isPresent()) return CompletableFuture.completedFuture(new CssContent(css.get()));
            }
            return CompletableFuture.completedFuture(new CssContent("/* no renderer derives " + query.className() + " */\n"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(ResourceNotFound.forClass(query.className(), e));
        }
    }

    // ---- helpers --------------------------------------------------------


    private static String renderCss(CssGroup<?> group) {
        StringBuilder sb = new StringBuilder();

        // Defect 0003 — cascade-layer declaration goes first so the browser
        // honours the ladder regardless of bundle load order.
        sb.append(Layers.declaration()).append("\n\n");

        // Per-class rules — grouped by the cascade tier each CssClass opts into
        // via InLayer<L>. Classes without an InLayer marker fall into
        // @layer component (the implicit default — see Layers.ofImplementor).
        Map<Class<? extends Layer>, List<String>> byLayer = new LinkedHashMap<>();
        for (Class<? extends Layer> layer : Layers.ASCENDING) {
            byLayer.put(layer, new ArrayList<>());
        }

        for (CssClass<?> cssClass : group.cssClasses()) {
            Class<? extends Layer> layer = Layers.ofImplementor(cssClass);
            List<String> bucket = byLayer.get(layer);
            String baseKebab = CssClassName.toCssName(cssClass.getClass());
            String selector = cssClass.selector();
            if (selector == null) {
                selector = "." + baseKebab;
                String state = cssClass.pseudoState();
                if (state != null && !state.isEmpty()) selector += state;
            }

            // The declared body. A class with none is a declaration error,
            // rendered as a comment so the sheet still arrives and says so.
            String body = cssClass.body();
            if (body == null) {
                bucket.add("/* render error: no body() for " + cssClass.getClass().getSimpleName() + " */");
                continue;
            }

            StringBuilder rule = new StringBuilder();
            rule.append(selector).append(" {\n");
            if (!body.isEmpty()) rule.append(body.indent(4));
            rule.append("}\n");

            for (String variant : cssClass.variants()) {
                rule.append(".").append(variant).append("-").append(baseKebab)
                        .append(":").append(variant).append(" {\n");
                if (!body.isEmpty()) rule.append(body.indent(4));
                rule.append("}\n");
            }
            bucket.add(rule.toString());
        }

        // Emit each non-empty layer in ASCENDING order, wrapped in
        // `@layer X { … }`. The declaration at the top fixes cascade order
        // regardless of how these blocks interleave with other bundles.
        for (Class<? extends Layer> layer : Layers.ASCENDING) {
            List<String> rules = byLayer.get(layer);
            if (rules.isEmpty()) continue;
            sb.append("@layer ").append(Layers.CSS_NAME.get(layer)).append(" {\n");
            for (String rule : rules) {
                sb.append(rule.indent(4));
            }
            sb.append("}\n\n");
        }

        return sb.toString();
    }

}
