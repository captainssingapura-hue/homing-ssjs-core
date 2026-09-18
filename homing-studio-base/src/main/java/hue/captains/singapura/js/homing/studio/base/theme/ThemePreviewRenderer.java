package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;
import hue.captains.singapura.js.homing.studio.base.ui.StudioElements;

import java.util.List;

/**
 * A theme, shown by wearing it. One page carrying the elements the studio's
 * pages are made of, under whatever theme the address names — the picker
 * embeds it in a frame and reloads the frame per selection.
 *
 * <p>Renders no header: the header carries the picker, and a picker inside
 * the picker's own preview is a hall of mirrors.</p>
 */
public record ThemePreviewRenderer() implements DomModule<ThemePreviewRenderer> {

    public record renderThemePreview() implements Exportable._Constant<ThemePreviewRenderer> {}

    public static final ThemePreviewRenderer INSTANCE = new ThemePreviewRenderer();

    @Override
    public ImportsFor<ThemePreviewRenderer> imports() {
        return ImportsFor.<ThemePreviewRenderer>builder()
                .add(new ModuleImports<>(List.of(new domOpsParty()),
                        DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new ThemePickerModel.activeThemeSlug(),
                        new ThemePickerModel.fetchRegistry(),
                        new ThemePickerModel.decompose()
                ), ThemePickerModel.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioElements.Card(),
                        new StudioElements.Section(),
                        new StudioElements.Listing(),
                        new StudioElements.ListItem(),
                        new StudioElements.StatusBadge(),
                        new StudioElements.OverallProgress(),
                        new StudioElements.TodoList(),
                        new StudioElements.MetricsTable(),
                        new StudioElements.Panel(),
                        new StudioElements.Footer(),
                        new StudioElements.NavLink()
                ), StudioElements.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_root(),
                        new StudioStyles.st_main(),
                        new StudioStyles.st_kicker(),
                        new StudioStyles.st_title(),
                        new StudioStyles.st_subtitle(),
                        new StudioStyles.st_badge_rfc(),
                        new StudioStyles.st_badge_brand()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ThemePreviewRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderThemePreview()));
    }
}
