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
 * The Themes page: the registry on its two planes — every design, with the
 * colours it is offered, and every palette, with the design it was crafted
 * for and the others it suits — plus the shared picker to wear a pair.
 * Reachable from any studio that wires it into its catalogues; URL:
 * {@code /app?app=themes}.
 *
 * <p>Stateless — the AppModule emits a tiny JS shim that hands the page to
 * {@link ThemesIntroRenderer}, which reads {@link ThemesGetAction}
 * ({@code GET /themes}). The picker in the sticky header, the picker on the
 * page and the rows' links all end in the same switch, so the pair the user
 * wears is sticky across navigation.</p>
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
