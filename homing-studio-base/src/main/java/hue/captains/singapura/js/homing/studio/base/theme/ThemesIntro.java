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
 * A single intro page that lists every registered {@link hue.captains.singapura.js.homing.core.Theme}
 * with its key palette swatches and a one-click activator. Reachable from any
 * studio that wires it into its catalogues; URL: {@code /app?app=themes}.
 *
 * <p>Stateless — the AppModule emits a tiny JS shim that fetches
 * {@link ThemesGetAction} ({@code GET /themes}) and hands the JSON to
 * {@link ThemesIntroRenderer}. The page-level theme picker (in the sticky
 * header) and the page's own activator links both flow through {@code href},
 * so the user's chosen theme is sticky across navigation.</p>
 */
@LegacyAppMain(reason = "Theme picker page; tiny body; opportunistic migration.")
public record ThemesIntro() implements AppModule<AppModule._None, ThemesIntro>, SelfContent {

    record appMain() implements AppModule._AppMain<AppModule._None, ThemesIntro> {}

    public record link() implements AppLink<ThemesIntro> {}

    public static final ThemesIntro INSTANCE = new ThemesIntro();

    @Override public String simpleName() { return "themes"; }
    /** Page-kind label. {@code AppHtmlGetAction} appends the downstream brand. */
    @Override public String title()      { return "themes"; }

    @Override
    public ImportsFor<ThemesIntro> imports() {
        return ImportsFor.<ThemesIntro>builder()
                .add(new ModuleImports<>(List.of(new ThemesIntroRenderer.renderThemesIntro()),
                        ThemesIntroRenderer.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ThemesIntro> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new appMain()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver nameResolver) {
        return List.of(
                "function appMain(rootElement) {",
                "    rootElement.replaceChildren(renderThemesIntro());",
                "}"
        );
    }
}
