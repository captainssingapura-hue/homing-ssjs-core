package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.component.Widget;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * A preference that is one of a list: options with a value, a label and a
 * note, drawn as a listbox; the chosen one is written through the steward.
 * Params: {@code name, label, summary?, default?, options: [{value, label, note?}]}.
 */
public record ChoiceWidget() implements Widget<Widget._None, ChoiceWidget> {

    public record construct() implements Widget._Construct<Widget._None, ChoiceWidget> {}

    public static final ChoiceWidget INSTANCE = new ChoiceWidget();

    @Override public String title() { return "Choice"; }

    @Override
    public ImportsFor<ChoiceWidget> imports() {
        return ImportsFor.<ChoiceWidget>builder()
                .add(new ModuleImports<>(List.of(new PreferenceField.preferenceField()), PreferenceField.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PreferencesStyles.pv_options(),
                        new PreferencesStyles.pv_option(),
                        new PreferencesStyles.pv_option_note()
                ), PreferencesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ChoiceWidget> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new construct()));
    }
}
