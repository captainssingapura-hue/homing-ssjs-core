package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
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
public record ListMasterWidgetModule() implements Widget<Widget._None, ListMasterWidgetModule> {

    /** The class. */
    public record ListMasterWidget() implements Widget._Class<Widget._None, ListMasterWidgetModule>, NeedKeyboard {
        /** The rows walked, through the party: the view hands the list its keys while the focus is in it. */
        public static final List<KeyBinding> KEYS = List.of(
                KeyBinding.of(Key.ARROW_DOWN, "the row below chosen"), KeyBinding.of(Key.ARROW_UP, "the row above chosen"),
                KeyBinding.of(Key.HOME, "the first row chosen"), KeyBinding.of(Key.END, "the last row chosen"));
        @Override public List<KeyBinding> keys() { return KEYS; }
    }

    public static final ListMasterWidgetModule INSTANCE = new ListMasterWidgetModule();

    @Override public String title() { return "Preferences list"; }

    @Override
    public ImportsFor<ListMasterWidgetModule> imports() {
        return ImportsFor.<ListMasterWidgetModule>builder()
                .add(new ModuleImports<>(List.of(
                        new PreferencesStyles.pv_list(),
                        new PreferencesStyles.pv_list_row()
                ), PreferencesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ListMasterWidgetModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ListMasterWidget()));
    }
}
