package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.Component;
import hue.captains.singapura.js.homing.core.CssBlock;
import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.Layer;
import hue.captains.singapura.js.homing.core.Layers;
import hue.captains.singapura.js.homing.core.PaletteClass;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.util.CssClassName;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * GET /css-content?class=&lt;CssGroup canonical name&gt;[&amp;theme=&lt;slug&gt;]
 *
 * <p>Renders CSS programmatically from a typed {@link CssGroupImpl} resolved
 * via the registry passed at construction. Body shape:</p>
 * <ol>
 *   <li>{@code :root { … }} from {@link CssGroupImpl#cssVariables()}</li>
 *   <li>{@link CssGroupImpl#globalRules()} verbatim — pseudo-classes, descendant
 *       selectors, media queries, html/body resets</li>
 *   <li>One {@code .kebab-name { body }} per declared {@link CssClass} in the
 *       group, body sourced by reflection from the impl's matching method
 *       (record {@code st_root} → method {@code st_root()} → selector
 *       {@code .st-root})</li>
 * </ol>
 *
 * <p>Hard cut: unknown {@code class} or {@code theme} returns 404. No
 * file-based fallback (RFC 0002 §3.6).</p>
 */
public class CssContentGetAction
        implements GetAction<RoutingContext, ModuleQuery, EmptyParam.NoHeaders, CssContent> {

    private final List<CssGroupImpl<?, ?>> impls;
    private final Theme defaultTheme;

    /**
     * @param impls every registered {@link CssGroupImpl}; must contain at least
     *              one entry per {@code (group, theme)} pair the deployment serves
     * @param defaultTheme the theme to use when a request omits {@code ?theme=}.
     *                     May be {@code null}, in which case unparameterized
     *                     requests return 404
     */
    public CssContentGetAction(List<CssGroupImpl<?, ?>> impls, Theme defaultTheme) {
        this.impls = Objects.requireNonNull(impls, "impls");
        this.defaultTheme = defaultTheme;
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, ModuleQuery> queryStrMarshaller() {
        return ctx -> new ModuleQuery(
                ctx.request().getParam("class"),
                ctx.request().getParam("theme"),
                ctx.request().getParam("locale")
        );
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<CssContent> execute(ModuleQuery query, EmptyParam.NoHeaders headers) {
        if (query.className() == null || query.className().isBlank()) {
            return CompletableFuture.failedFuture(ResourceNotFound.missingClass());
        }
        try {
            Class<?> clazz = Class.forName(query.className());
            Object instance = resolveInstance(clazz);
            if (!(instance instanceof CssGroup<?> group)) {
                return CompletableFuture.failedFuture(ResourceNotFound.wrongType(query.className(), "CssGroup"));
            }

            String themeSlug = query.theme() != null && !query.theme().isBlank()
                    ? query.theme()
                    : (defaultTheme != null ? defaultTheme.slug() : null);
            if (themeSlug == null) {
                return CompletableFuture.failedFuture(ResourceNotFound.forClass(
                        query.className(),
                        new IllegalStateException(
                                "No theme specified and no default theme configured")));
            }
            // RFC 0002-ext1 Phase 10/11: groups whose classes all have non-null
            // `body()` no longer need a registered CssGroupImpl. The renderer
            // handles `impl == null` by rendering purely from inline bodies.
            // Theme cascade comes from the theme-bundle endpoints
            // (/theme-vars, /theme-globals), not from the per-group response.
            CssGroupImpl<?, ?> impl = findImpl(group, themeSlug);
            return CompletableFuture.completedFuture(new CssContent(renderCss(impl, group)));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(ResourceNotFound.forClass(query.className(), e));
        }
    }

    // ---- helpers --------------------------------------------------------

    private static Object resolveInstance(Class<?> clazz) throws Exception {
        try {
            return clazz.getField("INSTANCE").get(null);
        } catch (NoSuchFieldException e) {
            return clazz.getDeclaredConstructor().newInstance();
        }
    }

    /** First impl whose group() class equals {@code group}'s class AND theme().slug() matches. */
    private CssGroupImpl<?, ?> findImpl(CssGroup<?> group, String themeSlug) {
        for (var impl : impls) {
            if (impl.group().getClass().equals(group.getClass())
                    && impl.theme().slug().equals(themeSlug)) {
                return impl;
            }
        }
        return null;
    }

    private static String renderCss(CssGroupImpl<?, ?> impl, CssGroup<?> group) {
        StringBuilder sb = new StringBuilder();

        // Defect 0003 — cascade-layer declaration goes first so the browser
        // honours the ladder regardless of bundle load order.
        sb.append(Layers.declaration()).append("\n\n");

        // 1. :root custom properties stay unlayered — CSS vars don't
        // participate in cascade conflicts; layering them would only
        // complicate `var(--…)` resolution.
        if (impl != null) {
            Map<String, String> primitives = impl.cssVariables();
            Map<String, String> semantic   = impl.semanticTokens();
            if (!primitives.isEmpty() || !semantic.isEmpty()) {
                sb.append(":root {\n");
                primitives.forEach((k, v) -> sb.append("    ").append(k).append(": ").append(v).append(";\n"));
                semantic  .forEach((k, v) -> sb.append("    ").append(k).append(": ").append(v).append(";\n"));
                sb.append("}\n\n");
            }
            // Legacy globalRules — wrap in @layer component as the safe
            // default. Themes targeting other tiers should migrate to
            // ThemeGlobals.chunks().
            String global = impl.globalRules();
            if (global != null && !global.isEmpty()) {
                sb.append("@layer component {\n");
                sb.append(global.indent(4));
                if (!global.endsWith("\n")) sb.append('\n');
                sb.append("}\n\n");
            }
        }

        // 2. Per-class rules — group by the cascade tier each CssClass opts
        // into via InLayer<L>. Classes without an InLayer marker fall into
        // @layer component (the implicit default — see Layers.ofImplementor).
        Map<Class<? extends Layer>, List<String>> byLayer = new LinkedHashMap<>();
        for (Class<? extends Layer> layer : Layers.ASCENDING) {
            byLayer.put(layer, new ArrayList<>());
        }

        for (CssClass<?> cssClass : group.cssClasses()) {
            // RFC 0066 — a palette is a PROVIDED class: no rule of its own, its
            // body is the theme's :root binding, emitted unlayered like the
            // impl's :root block above (custom properties do not cascade-conflict).
            // No provision under this theme is the completeness failure the
            // build gate reports; here it renders as a comment so the sheet
            // still arrives and the page says what is missing.
            if (cssClass instanceof PaletteClass<?> palette) {
                if (impl instanceof PaletteProvision<?, ?> provision) {
                    sb.append(provision.rootBlock()).append('\n');
                } else {
                    sb.append("/* render error: no PaletteProvision for ")
                      .append(palette.getClass().getSimpleName()).append(" under this theme */\n\n");
                }
                continue;
            }
            Class<? extends Layer> layer = Layers.ofImplementor(cssClass);
            List<String> bucket = byLayer.get(layer);
            try {
                String baseKebab = CssClassName.toCssName(cssClass.getClass());
                String selector = "." + baseKebab;
                String state = cssClass.pseudoState();
                if (state != null && !state.isEmpty()) selector += state;

                String body;
                String inline = cssClass.body();
                if (inline != null) {
                    body = inline;
                } else if (impl != null) {
                    Method m = impl.getClass().getMethod(cssClass.getClass().getSimpleName());
                    body = ((CssBlock<?>) m.invoke(impl)).body();
                } else {
                    bucket.add("/* render error: no body() and no registered impl for "
                            + cssClass.getClass().getSimpleName() + " */");
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
            } catch (NoSuchMethodException e) {
                bucket.add("/* render error: no method " + cssClass.getClass().getSimpleName()
                        + "() on " + (impl == null ? "(null impl)" : impl.getClass().getSimpleName()) + " */");
            } catch (Exception e) {
                bucket.add("/* render error: " + cssClass.getClass().getSimpleName()
                        + " — " + e.getMessage() + " */");
            }
        }

        // 3. Emit each non-empty layer in ASCENDING order, wrapped in
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
