package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * RFC 0002-ext1 Phase 09 — serves per-theme CSS variable values as a separate,
 * cacheable CSS file at {@code GET /theme-vars?theme=Y}.
 *
 * <p>Body shape: one {@code :root { ... }} block listing every token
 * the theme's {@link PaletteProvision} of the global palette binds (RFC 0066). The browser caches it once per
 * theme; the value is referenced via {@code var(--foo)} in per-class CSS files
 * served by {@code /css-content}.</p>
 *
 * <p>Tolerant of missing themes: unknown slugs return 200 with empty body, so
 * deployments that haven't migrated to the new theme-bundle model don't break.
 * The legacy cascade still comes from {@code CssContentGetAction}'s {@code :root}
 * emission until the deployment retires it.</p>
 */
public class ThemeVarsGetAction
        implements GetAction<RoutingContext, ThemeVarsGetAction.Query, EmptyParam.NoHeaders, CssContent> {

    public record Query(String themeSlug) implements hue.captains.singapura.tao.http.action.Param._QueryString {}

    private final ThemeRegistry registry;
    private final Theme defaultTheme;

    public ThemeVarsGetAction(ThemeRegistry registry, Theme defaultTheme) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.defaultTheme = defaultTheme;
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("theme"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<CssContent> execute(Query query, EmptyParam.NoHeaders headers) {
        String slug = query.themeSlug() != null && !query.themeSlug().isBlank()
                ? query.themeSlug()
                : (defaultTheme != null ? defaultTheme.slug() : null);

        if (slug == null) {
            return CompletableFuture.completedFuture(new CssContent(""));
        }

        PaletteProvision<?, ?> palette = registry.paletteForSlug(slug);
        if (palette == null) {
            return CompletableFuture.completedFuture(new CssContent(""));
        }

        return CompletableFuture.completedFuture(new CssContent(palette.rootBlock()));
    }
}
