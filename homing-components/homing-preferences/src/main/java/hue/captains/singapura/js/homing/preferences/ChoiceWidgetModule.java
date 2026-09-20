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
public record ChoiceWidgetModule() implements Widget<Widget._None, ChoiceWidgetModule> {

    /** The class. */
    public record ChoiceWidget() implements Widget._Class<Widget._None, ChoiceWidgetModule> {}

    public static final ChoiceWidgetModule INSTANCE = new ChoiceWidgetModule();

    @Override public String title() { return "Choice"; }

    @Override
    public ImportsFor<ChoiceWidgetModule> imports() {
        return ImportsFor.<ChoiceWidgetModule>builder()
                .add(new ModuleImports<>(List.of(new PreferenceFieldModule.PreferenceField()), PreferenceFieldModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PreferencesStyles.pv_options(),
                        new PreferencesStyles.pv_option(),
                        new PreferencesStyles.pv_option_note()
                ), PreferencesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ChoiceWidgetModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ChoiceWidget()));
    }
}
