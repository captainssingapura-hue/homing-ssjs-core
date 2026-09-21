package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.ServingContextModule;
import hue.captains.singapura.js.homing.preferences.PreferencesViewModule;
import hue.captains.singapura.js.homing.ui.dialog.DialogModule;

import java.util.List;

/**
 * The preferences button on the bar. Pressed, it opens the dialog with the
 * preferences view inside: the site's tree on the left, the chosen node's
 * widget on the right. The site's registry is named in the page's chrome
 * data and imported on the first open — its module, and every widget's,
 * arrive only when they are needed.
 *
 * <p>The dialog is modal, Escape closes it, and Done is a plain action so
 * that Enter stays the tree's. Nothing is written here: every widget
 * writes through the steward, and the page follows the store as it always
 * has, so a theme picked in the dialog is worn behind it as it is picked.</p>
 */
public record PreferencesButtonModule() implements DomModule<PreferencesButtonModule> {

    /** The class. */
    public record PreferencesButton() implements BranchComponent<PreferencesButtonModule> {
        @Override public String summary() { return "The button on the bar, and the dialog it opens."; }
    }

    public static final PreferencesButtonModule INSTANCE = new PreferencesButtonModule();

    @Override
    public ImportsFor<PreferencesButtonModule> imports() {
        return ImportsFor.<PreferencesButtonModule>builder()
                .add(new ModuleImports<>(List.of(new DialogModule.Dialog()), DialogModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PreferencesViewModule.PreferencesView()), PreferencesViewModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ServingContextModule.withServingContext()), ServingContextModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new MpaStyles.mpa_prefs(),
                        new MpaStyles.mpa_prefs_btn(),
                        new MpaStyles.mpa_prefs_btn_label()
                ), MpaStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PreferencesButtonModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PreferencesButton()));
    }
}
