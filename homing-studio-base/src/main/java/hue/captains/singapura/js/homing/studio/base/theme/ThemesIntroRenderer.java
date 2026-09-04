package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;
import hue.captains.singapura.js.homing.studio.base.ui.StudioElements;

import java.util.List;

/**
 * Renderer for the {@link ThemesIntro} page. Fetches {@code /themes}, then
 * emits a stickily-headered page with one Listing of theme rows — each row
 * showing the theme's name, slug, palette swatches, and an "Activate" link
 * that flips {@code ?theme=<slug>} via {@code href.set} (which propagates
 * through the page URL on click).
 */
public record ThemesIntroRenderer() implements DomModule<ThemesIntroRenderer> {

    public record renderThemesIntro() implements Exportable._Constant<ThemesIntroRenderer> {}

    public static final ThemesIntroRenderer INSTANCE = new ThemesIntroRenderer();

    @Override
    public ImportsFor<ThemesIntroRenderer> imports() {
        return ImportsFor.<ThemesIntroRenderer>builder()
                .add(new ModuleImports<>(List.of(new ThemePicker.mountThemePickerTree()),
                        ThemePicker.INSTANCE))
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
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
                        new StudioStyles.st_error()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ThemesIntroRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderThemesIntro()));
    }
}
