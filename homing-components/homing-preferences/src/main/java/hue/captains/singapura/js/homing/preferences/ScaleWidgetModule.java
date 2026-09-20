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
public record ScaleWidgetModule() implements Widget<Widget._None, ScaleWidgetModule> {

    /** The class. */
    public record ScaleWidget() implements Widget._Class<Widget._None, ScaleWidgetModule> {}

    public static final ScaleWidgetModule INSTANCE = new ScaleWidgetModule();

    @Override public String title() { return "Scale"; }

    @Override
    public ImportsFor<ScaleWidgetModule> imports() {
        return ImportsFor.<ScaleWidgetModule>builder()
                .add(new ModuleImports<>(List.of(new PreferenceFieldModule.PreferenceField()), PreferenceFieldModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PreferencesStyles.pv_range(),
                        new PreferencesStyles.pv_readout()
                ), PreferencesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ScaleWidgetModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ScaleWidget()));
    }
}
