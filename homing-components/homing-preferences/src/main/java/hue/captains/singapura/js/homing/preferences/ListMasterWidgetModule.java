package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.component.Widget;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The master a site gets unless it brings a tree view: the rigid tree
 * flattened into a listbox, depth as indentation, every group open. A
 * preferences tree is ten to twenty nodes, and a flattened tree reads
 * fine at that size. Offers the view the surface a tree widget offers —
 * {@code onSelect(fn)}, {@code select(path)} — so a real tree view is one
 * provider away. Params: {@code tree, labels}, as the registry stamps them.
 */
public record ListMasterWidget() implements Widget<Widget._None, ListMasterWidget> {

    public record construct() implements Widget._Construct<Widget._None, ListMasterWidget> {}

    public static final ListMasterWidget INSTANCE = new ListMasterWidget();

    @Override public String title() { return "Preferences list"; }

    @Override
    public ImportsFor<ListMasterWidget> imports() {
        return ImportsFor.<ListMasterWidget>builder()
                .add(new ModuleImports<>(List.of(
                        new PreferencesStyles.pv_list(),
                        new PreferencesStyles.pv_list_row()
                ), PreferencesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ListMasterWidget> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new construct()));
    }
}
