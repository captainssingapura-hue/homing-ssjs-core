package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
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
 * the steward: {@code new PreferenceField(branch, params, opts)} reads the value,
 * writes a pick, forgets on reset, and follows the store so a change made
 * anywhere is drawn here.
 */
public record PreferenceFieldModule() implements DomModule<PreferenceFieldModule> {

    /** The class. */
    public record PreferenceField() implements BranchComponent<PreferenceFieldModule> {
        @Override public String summary() { return "What every setting's widget has in common: the label, the summary, the control's slot."; }
    }

    public static final PreferenceFieldModule INSTANCE = new PreferenceFieldModule();

    @Override
    public ImportsFor<PreferenceFieldModule> imports() {
        return ImportsFor.<PreferenceFieldModule>builder()
                .add(new ModuleImports<>(List.of(
                        new PreferenceSteward.PreferenceStewardInstance(),
                        new PreferenceSteward.PreferenceViewInstance()
                ), PreferenceSteward.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.ButtonBuilder()), Elements.INSTANCE))
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
    public ExportsOf<PreferenceFieldModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PreferenceField()));
    }
}
