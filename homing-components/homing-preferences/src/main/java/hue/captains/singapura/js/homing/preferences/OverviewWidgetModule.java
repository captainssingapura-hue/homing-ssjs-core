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
public record OverviewWidgetModule() implements Widget<Widget._None, OverviewWidgetModule> {

    /** The class. */
    public record OverviewWidget() implements Widget._Class<Widget._None, OverviewWidgetModule> {}

    public static final OverviewWidgetModule INSTANCE = new OverviewWidgetModule();

    @Override public String title() { return "Overview"; }

    @Override
    public ImportsFor<OverviewWidgetModule> imports() {
        return ImportsFor.<OverviewWidgetModule>builder()
                .add(new ModuleImports<>(List.of(
                        new PreferenceSteward.PreferenceStewardInstance(),
                        new PreferenceSteward.PreferenceViewInstance()
                ), PreferenceSteward.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.Button()), Elements.INSTANCE))
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
    public ExportsOf<OverviewWidgetModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new OverviewWidget()));
    }
}
