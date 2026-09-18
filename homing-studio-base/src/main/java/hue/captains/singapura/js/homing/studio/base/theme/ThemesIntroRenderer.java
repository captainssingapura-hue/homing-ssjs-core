package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.server.PreferenceSteward;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;
import hue.captains.singapura.js.homing.studio.base.ui.StudioElements;

import java.util.List;

/**
 * Renderer for the {@link ThemesIntro} page: the registry on its two planes.
 * Fetches {@code /themes} and emits a stickily-headered page with the shared
 * picker and two listings — one row per design, with the colours it is
 * offered, and one row per palette, with the design it was crafted for and
 * the others it suits. A row is a link that wears the pair, {@code
 * ?theme=<slug>} through {@code href}; the rows the page wears are marked,
 * and the marks follow the CSS manager when the theme moves.
 */
public record ThemesIntroRenderer() implements DomModule<ThemesIntroRenderer> {

    public record renderThemesIntro() implements Exportable._Constant<ThemesIntroRenderer> {}

    public static final ThemesIntroRenderer INSTANCE = new ThemesIntroRenderer();

    @Override
    public ImportsFor<ThemesIntroRenderer> imports() {
        return ImportsFor.<ThemesIntroRenderer>builder()
                .add(new ModuleImports<>(List.of(new ThemePicker.mountThemePickerTree()),
                        ThemePicker.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new ThemePickerModel.fetchRegistry(),
                        new ThemePickerModel.colourwayOf(),
                        new ThemePickerModel.decompose(),
                        new ThemePickerModel.themeBySlug()
                ), ThemePickerModel.INSTANCE))
                .add(new ModuleImports<>(List.of(new domOpsParty()),
                        DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(new PreferenceSteward.PreferenceViewInstance()),
                        PreferenceSteward.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioElements.Header(),
                        new StudioElements.Listing(),
                        new StudioElements.ListItem()
                ), StudioElements.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_root(),
                        new StudioStyles.st_main(),
                        new StudioStyles.st_kicker(),
                        new StudioStyles.st_title(),
                        new StudioStyles.st_subtitle(),
                        new StudioStyles.st_loading(),
                        new StudioStyles.st_error(),
                        new StudioStyles.st_list_item_met()
                ), StudioStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new ThemePickerStyles.tp_current(),
                        new ThemePickerStyles.tp_swatch_dots(),
                        new ThemePickerStyles.tp_intro_dot(),
                        new ThemePickerStyles.tp_intro_desc(),
                        new ThemePickerStyles.tp_intro_offer()
                ), ThemePickerStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ThemesIntroRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderThemesIntro()));
    }
}
