package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.PreferenceSteward;
import hue.captains.singapura.js.homing.ui.elements.Elements;

import java.util.List;

/**
 * What every setting's widget has in common: the header (kicker, title,
 * summary), the body the control goes in, the note under it saying where
 * the value came from, and the way back to the site's default. Built on
 * the steward: {@code preferenceField(branch, params)} reads the value,
 * writes a pick, forgets on reset, and follows the store so a change made
 * anywhere is drawn here.
 */
public record PreferenceField() implements DomModule<PreferenceField> {

    public record preferenceField() implements Exportable._Constant<PreferenceField> {}

    public static final PreferenceField INSTANCE = new PreferenceField();

    @Override
    public ImportsFor<PreferenceField> imports() {
        return ImportsFor.<PreferenceField>builder()
                .add(new ModuleImports<>(List.of(
                        new PreferenceSteward.PreferenceStewardInstance(),
                        new PreferenceSteward.PreferenceViewInstance()
                ), PreferenceSteward.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.Button(), new Elements.setButtonOn()), Elements.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PreferencesStyles.pv_kicker(),
                        new PreferencesStyles.pv_title(),
                        new PreferencesStyles.pv_summary(),
                        new PreferencesStyles.pv_note(),
                        new PreferencesStyles.pv_actions()
                ), PreferencesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PreferenceField> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new preferenceField()));
    }
}
