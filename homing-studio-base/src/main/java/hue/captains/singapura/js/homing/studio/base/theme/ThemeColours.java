package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The theme picker's colour control: a pill floating over the preview that
 * names the colours the base is shown in, and the menu it opens — one option
 * per palette the registry offers for that base, its own marked as the
 * default, a borrowed one marked with the design it was crafted for. Colour
 * is an orthogonal plane of a design, so this is orthogonal to the picker's
 * tree — the tree picks the base, this picks the colours, and the slug the
 * page will wear is read off the registry's entry for the pair.
 */
public record ThemeColours() implements DomModule<ThemeColours> {

    /** Build the control into the preview wrap; returns { update, isOpen, handleKeydown, close }. */
    public record mountColourMenu() implements Exportable._Constant<ThemeColours> {}

    public static final ThemeColours INSTANCE = new ThemeColours();

    @Override
    public ImportsFor<ThemeColours> imports() {
        return ImportsFor.<ThemeColours>builder()
                .add(new ModuleImports<>(List.of(new ThemePickerModel.colourwayOf()), ThemePickerModel.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new ThemePickerStyles.tp_colours(),
                        new ThemePickerStyles.tp_colours_label(),
                        new ThemePickerStyles.tp_colours_scrim(),
                        new ThemePickerStyles.tp_colours_menu(),
                        new ThemePickerStyles.tp_colours_item(),
                        new ThemePickerStyles.tp_swatch_dots(),
                        new ThemePickerStyles.tp_swatch_dot(),
                        new ThemePickerStyles.tp_swatch_own(),
                        new ThemePickerStyles.tp_swatch_for()
                ), ThemePickerStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ThemeColours> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new mountColourMenu()));
    }
}
