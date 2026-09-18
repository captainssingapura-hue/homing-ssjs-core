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
 * <p>Renders CSS from the group's declared bodies, with the theme's word from the
 * {@link CssGroupImpl} resolved
 * via the registry passed at construction. Body shape:</p>
 * <ol>
 *   <li>a {@link PaletteClass}: the theme's {@link PaletteProvision} as {@code :root} blocks, unlayered</li>
 *   <li>every other class: {@code selector { declared body; theme override }} in its
 *       {@code @layer} — the override appended inside the rule (RFC 0066)</li>
 * </ol>
 *
 * <p>Hard cut: unknown {@code class} or {@code theme} returns 404. No
 * file-based fallback (RFC 0002 §3.6).</p>
 */
public class CssContentGetAction
        implements GetAction<RoutingContext, ModuleQuery, EmptyParam.NoHeaders, CssContent> {

    private final List<CssGroupImpl<?, ?>> impls;
    private final Theme defaultTheme;
    private final ServedModules served;
    private final List<CssRenderer> renderers;

    /**
     * @param impls every registered {@link CssGroupImpl}; must contain at least
     *              one entry per {@code (group, theme)} pair the deployment serves
     * @param defaultTheme the theme to use when a request omits {@code ?theme=}.
     *                     May be {@code null}, in which case unparameterized
     *                     requests return 404
     */
    public CssContentGetAction(List<CssGroupImpl<?, ?>> impls, Theme defaultTheme) {
        this(impls, defaultTheme, ServedModules.NONE, List.of());
    }

    /**
     * @param served    the deployment's modules by canonical name — a group is resolved by lookup, never by reflection
     * @param renderers the design side's renderers, asked first; a group none claims renders from its bodies
     */
    public CssContentGetAction(List<CssGroupImpl<?, ?>> impls, Theme defaultTheme, ServedModules served, List<CssRenderer> renderers) {
        this.impls = Objects.requireNonNull(impls, "impls");
        this.defaultTheme = defaultTheme;
        this.served = served == null ? ServedModules.NONE : served;
        this.renderers = List.copyOf(renderers);
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
            var found = served.find(query.className());
            if (found.isEmpty()) {
                return CompletableFuture.failedFuture(ResourceNotFound.forClass(query.className(),
                        new IllegalStateException("group '" + query.className() + "' is not declared in any registered crate")));
            }
            if (!(found.get() instanceof CssGroup<?> group)) {
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
            // Theme cascade comes from the palette group (RFC 0066, the prior)
            // and /theme-globals, not from the per-group response.
            for (CssRenderer r : renderers) {
                var css = r.render(group, themeSlug);
                if (css.isPresent()) return CompletableFuture.completedFuture(new CssContent(css.get()));
            }
            CssGroupImpl<?, ?> impl = findImpl(group, themeSlug);
            return CompletableFuture.completedFuture(new CssContent(renderCss(impl, group)));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(ResourceNotFound.forClass(query.className(), e));
        }
    }

    // ---- helpers --------------------------------------------------------


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

        // Per-class rules — grouped by the cascade tier each CssClass opts into
        // via InLayer<L>. Classes without an InLayer marker fall into
        // @layer component (the implicit default — see Layers.ofImplementor).
        Map<Class<? extends Layer>, List<String>> byLayer = new LinkedHashMap<>();
        for (Class<? extends Layer> layer : Layers.ASCENDING) {
            byLayer.put(layer, new ArrayList<>());
        }

        for (CssClass<?> cssClass : group.cssClasses()) {
            // RFC 0066 — a palette is a PROVIDED class: no rule of its own, its
            // body is the theme's :root binding, emitted unlayered (custom
            // properties do not cascade-conflict). No provision under this
            // theme is the completeness failure the build gate reports; here it
            // renders as a comment so the sheet still arrives and says what is
            // missing.
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
            String baseKebab = CssClassName.toCssName(cssClass.getClass());
            String selector = cssClass.selector();
            if (selector == null) {
                selector = "." + baseKebab;
                String state = cssClass.pseudoState();
                if (state != null && !state.isEmpty()) selector += state;
            }

            // RFC 0066 — the declared body, then the theme's override for this
            // class appended INSIDE the rule: the theme says only what differs
            // and wins by source order, same layer, same specificity. A class
            // with neither is a declaration error, rendered as a comment.
            String declared = cssClass.body();
            String override = overrideFor(impl, cssClass);
            if (declared == null && override == null) {
                bucket.add("/* render error: no body() and no override for "
                        + cssClass.getClass().getSimpleName() + " on "
                        + (impl == null ? "(null impl)" : impl.getClass().getSimpleName()) + " */");
                continue;
            }
            String body = (declared == null ? "" : declared)
                        + (override == null ? "" : (declared == null || declared.isEmpty() ? "" : "\n") + override);

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

    /**
     * The theme's block for a class, if its impl has one: a public no-arg
     * method named after the record, returning a {@link CssBlock}. Absent
     * method — the common case — is no override. A method that exists but
     * fails is a declaration error and renders as a comment in the body.
     */
    private static String overrideFor(CssGroupImpl<?, ?> impl, CssClass<?> cssClass) {
        if (impl == null) return null;
        Method m;
        try {
            m = impl.getClass().getMethod(cssClass.getClass().getSimpleName());
        } catch (NoSuchMethodException e) {
            return null;
        }
        try {
            Object block = m.invoke(impl);
            return block instanceof CssBlock<?> b ? b.body() : String.valueOf(block);
        } catch (Exception e) {
            return "/* render error: " + cssClass.getClass().getSimpleName() + " — " + e.getMessage() + " */";
        }
    }
}
