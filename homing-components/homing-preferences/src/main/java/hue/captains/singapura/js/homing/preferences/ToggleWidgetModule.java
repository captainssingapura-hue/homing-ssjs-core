package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.component.Widget;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * A preference that is on or off: a switch, {@code "true"} or {@code "false"}
 * in the store. Params: {@code name, label, summary?, default?, on?, off?} —
 * the last two the words shown beside the switch in each state.
 */
public record ToggleWidget() implements Widget<Widget._None, ToggleWidget> {

    public record construct() implements Widget._Construct<Widget._None, ToggleWidget> {}

    public static final ToggleWidget INSTANCE = new ToggleWidget();

    @Override public String title() { return "Toggle"; }

    @Override
    public ImportsFor<ToggleWidget> imports() {
        return ImportsFor.<ToggleWidget>builder()
                .add(new ModuleImports<>(List.of(new PreferenceField.preferenceField()), PreferenceField.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PreferencesStyles.pv_switch(),
                        new PreferencesStyles.pv_switch_track(),
                        new PreferencesStyles.pv_switch_knob()
                ), PreferencesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ToggleWidget> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new construct()));
    }
}
