package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.component.Widget;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * A preference that is a number in a range: a slider with a readout.
 * Params: {@code name, label, summary?, default?, min, max, step?, unit?}.
 */
public record ScaleWidget() implements Widget<Widget._None, ScaleWidget> {

    public record construct() implements Widget._Construct<Widget._None, ScaleWidget> {}

    public static final ScaleWidget INSTANCE = new ScaleWidget();

    @Override public String title() { return "Scale"; }

    @Override
    public ImportsFor<ScaleWidget> imports() {
        return ImportsFor.<ScaleWidget>builder()
                .add(new ModuleImports<>(List.of(new PreferenceField.preferenceField()), PreferenceField.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PreferencesStyles.pv_range(),
                        new PreferencesStyles.pv_readout()
                ), PreferencesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ScaleWidget> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new construct()));
    }
}
