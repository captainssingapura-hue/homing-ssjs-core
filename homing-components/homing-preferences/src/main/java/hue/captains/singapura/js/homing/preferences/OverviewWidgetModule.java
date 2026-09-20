package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.component.Widget;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.PreferenceSteward;
import hue.captains.singapura.js.homing.ui.elements.Elements;

import java.util.List;

/**
 * A group's page: its label and summary, the settings under it with the
 * value each has in force, and a reset for all of them at once. Params:
 * {@code label, summary?, settings: [{name, label}]} — the settings below
 * the group, flattened by the site when it declares the tree.
 */
public record OverviewWidget() implements Widget<Widget._None, OverviewWidget> {

    public record construct() implements Widget._Construct<Widget._None, OverviewWidget> {}

    public static final OverviewWidget INSTANCE = new OverviewWidget();

    @Override public String title() { return "Overview"; }

    @Override
    public ImportsFor<OverviewWidget> imports() {
        return ImportsFor.<OverviewWidget>builder()
                .add(new ModuleImports<>(List.of(
                        new PreferenceSteward.PreferenceStewardInstance(),
                        new PreferenceSteward.PreferenceViewInstance()
                ), PreferenceSteward.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.Button(), new Elements.setButtonOn()), Elements.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PreferencesStyles.pv_kicker(),
                        new PreferencesStyles.pv_title(),
                        new PreferencesStyles.pv_summary(),
                        new PreferencesStyles.pv_children(),
                        new PreferencesStyles.pv_child(),
                        new PreferencesStyles.pv_option_note(),
                        new PreferencesStyles.pv_actions()
                ), PreferencesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<OverviewWidget> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new construct()));
    }
}
