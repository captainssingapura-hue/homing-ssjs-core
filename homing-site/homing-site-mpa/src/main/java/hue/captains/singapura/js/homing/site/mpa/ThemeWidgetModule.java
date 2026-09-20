package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.component.Widget;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.preferences.PreferenceField;
import hue.captains.singapura.js.homing.preferences.PreferencesStyles;

import java.util.List;

/**
 * The theme as a preference widget: the designs the site offers, then the
 * colours the chosen design is offered in, from {@code /themes}; a pick is
 * written through the field like any other setting, and the CSS manager
 * follows the store as it always has. Built on the generic field, so the
 * header, the note and the reset are the same as every other setting's.
 */
public record ThemeWidget() implements Widget<Widget._None, ThemeWidget> {

    public record construct() implements Widget._Construct<Widget._None, ThemeWidget> {}

    public static final ThemeWidget INSTANCE = new ThemeWidget();

    @Override public String title() { return "Theme"; }

    @Override
    public ImportsFor<ThemeWidget> imports() {
        return ImportsFor.<ThemeWidget>builder()
                .add(new ModuleImports<>(List.of(new PreferenceField.preferenceField()), PreferenceField.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PreferencesStyles.pv_kicker(),
                        new PreferencesStyles.pv_options(),
                        new PreferencesStyles.pv_option(),
                        new PreferencesStyles.pv_option_note()
                ), PreferencesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ThemeWidget> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new construct()));
    }
}
