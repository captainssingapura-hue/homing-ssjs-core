package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.CssClassManager;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.server.PreferenceSteward;

import java.util.List;

/**
 * The theme picker's model half — everything that touches no DOM: fetching the
 * registry, shaping it into the tree payload, reading the active slug, and the
 * session flag that carries "the picker was open" across a theme switch.
 *
 * <p>Split out of {@link ThemePicker} when that module crossed the 250
 * effective-line limit. The line count was the prompt, not the reason: the seam
 * was already there, and it is the useful one — this half is testable without a
 * document and reusable by any other surface that wants to offer themes.</p>
 */
public record ThemePickerModel() implements DomModule<ThemePickerModel> {

    /** The active theme's slug, read from the current URL. */
    public record activeThemeSlug() implements Exportable._Constant<ThemePickerModel> {}

    /** GET /themes: the registry on its two axes, { themes, palettes }. */
    public record fetchRegistry() implements Exportable._Constant<ThemePickerModel> {}

    /** GET /themes, unwrapped to the bases alone. */
    public record fetchThemes() implements Exportable._Constant<ThemePickerModel> {}

    /** A base worn in some colours: its entry for a palette slug, or its own. */
    public record colourwayOf() implements Exportable._Constant<ThemePickerModel> {}

    /** The base and palette a worn slug names. */
    public record decompose() implements Exportable._Constant<ThemePickerModel> {}

    /** Find a theme record by slug, or null. */
    public record themeBySlug() implements Exportable._Constant<ThemePickerModel> {}

    /** Shape the registry into the payload TreeRenderer consumes. */
    public record themeTreeData() implements Exportable._Constant<ThemePickerModel> {}

    /** A tree selection's theme slug — the last segment of its name-path. */
    public record slugOfSelection() implements Exportable._Constant<ThemePickerModel> {}

    /** Navigate to the same page under another theme. */
    public record switchToTheme() implements Exportable._Constant<ThemePickerModel> {}

    /** Leave a note that the picker was open when we navigated away. */

    /** Read — and consume — that note. One-shot by design. */
    public record previewUrl() implements Exportable._Constant<ThemePickerModel> {}

    public static final ThemePickerModel INSTANCE = new ThemePickerModel();

    @Override
    public ImportsFor<ThemePickerModel> imports() {
        return ImportsFor.<ThemePickerModel>builder()
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(new CssClassManager.CssClassManagerInstance()),
                        CssClassManager.INSTANCE))
                .add(new ModuleImports<>(List.of(new PreferenceSteward.PreferenceStewardInstance(),
                                                 new PreferenceSteward.PreferenceViewInstance()),
                        PreferenceSteward.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ThemePickerModel> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new activeThemeSlug(), new fetchRegistry(), new fetchThemes(), new colourwayOf(), new decompose(), new themeBySlug(),
                new themeTreeData(), new slugOfSelection(), new switchToTheme(),
                new previewUrl()));
    }
}
