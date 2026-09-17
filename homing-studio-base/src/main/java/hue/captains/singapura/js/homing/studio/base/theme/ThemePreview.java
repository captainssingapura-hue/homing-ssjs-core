package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.LegacyAppMain;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;

import java.util.List;

/**
 * The theme preview page: {@code /app?app=theme-preview&theme=<slug>}. A
 * theme shown by wearing it — the studio's own elements rendered under the
 * theme the address names, exactly as the page template gives any page under
 * that theme.
 *
 * <p>The theme picker embeds this page in a frame and reloads the frame on
 * every selection, which is what makes the preview live for every kind of
 * theme part, not just the stylesheets. Headerless, so the frame never
 * contains a second picker.</p>
 */
@LegacyAppMain(reason = "Preview page; one-line body; same shape as ThemesIntro.")
public record ThemePreview() implements AppModule<AppModule._None, ThemePreview>, SelfContent {

    record appMain() implements AppModule._AppMain<AppModule._None, ThemePreview> {}

    public record link() implements AppLink<ThemePreview> {}

    public static final ThemePreview INSTANCE = new ThemePreview();

    @Override public String simpleName() { return "theme-preview"; }
    /** Page-kind label. {@code AppHtmlGetAction} appends the downstream brand. */
    @Override public String title()      { return "theme preview"; }

    @Override
    public ImportsFor<ThemePreview> imports() {
        return ImportsFor.<ThemePreview>builder()
                .add(new ModuleImports<>(List.of(new ThemePreviewRenderer.renderThemePreview()),
                        ThemePreviewRenderer.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ThemePreview> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new appMain()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver nameResolver) {
        return List.of(
                "function appMain(rootElement) {",
                "    rootElement.replaceChildren(renderThemePreview());",
                "}"
        );
    }
}
