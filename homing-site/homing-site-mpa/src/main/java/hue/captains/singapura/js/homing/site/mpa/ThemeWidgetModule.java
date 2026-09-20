package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.component.Widget;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.preferences.PreferenceFieldModule;
import hue.captains.singapura.js.homing.preferences.PreferencesStyles;

import java.util.List;

/**
 * The theme as a preference widget: the designs the site offers, then the
 * colours the chosen design is offered in, from {@code /themes}; a pick is
 * written through the field like any other setting, and the CSS manager
 * follows the store as it always has. Built on the generic field, so the
 * header, the note and the reset are the same as every other setting's.
 */
public record ThemeWidgetModule() implements Widget<Widget._None, ThemeWidgetModule> {

    /** The class. */
    public record ThemeWidget() implements Widget._Class<Widget._None, ThemeWidgetModule> {}

    public static final ThemeWidgetModule INSTANCE = new ThemeWidgetModule();

    @Override public String title() { return "Theme"; }

    @Override
    public ImportsFor<ThemeWidgetModule> imports() {
        return ImportsFor.<ThemeWidgetModule>builder()
                .add(new ModuleImports<>(List.of(new PreferenceFieldModule.PreferenceField()), PreferenceFieldModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PreferencesStyles.pv_kicker(),
                        new PreferencesStyles.pv_options(),
                        new PreferencesStyles.pv_option(),
                        new PreferencesStyles.pv_option_note()
                ), PreferencesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ThemeWidgetModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ThemeWidget()));
    }
}
