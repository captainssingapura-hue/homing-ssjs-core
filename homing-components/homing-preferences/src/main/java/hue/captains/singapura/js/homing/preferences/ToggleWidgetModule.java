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
public record ToggleWidgetModule() implements Widget<Widget._None, ToggleWidgetModule> {

    /** The class. */
    public record ToggleWidget() implements Widget._Class<Widget._None, ToggleWidgetModule> {}

    public static final ToggleWidgetModule INSTANCE = new ToggleWidgetModule();

    @Override public String title() { return "Toggle"; }

    @Override
    public ImportsFor<ToggleWidgetModule> imports() {
        return ImportsFor.<ToggleWidgetModule>builder()
                .add(new ModuleImports<>(List.of(new PreferenceFieldModule.PreferenceField()), PreferenceFieldModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PreferencesStyles.pv_switch(),
                        new PreferencesStyles.pv_switch_track(),
                        new PreferencesStyles.pv_switch_knob()
                ), PreferencesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ToggleWidgetModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ToggleWidget()));
    }
}
