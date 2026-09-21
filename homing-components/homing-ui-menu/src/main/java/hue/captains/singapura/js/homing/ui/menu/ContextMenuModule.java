package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.icons.IconModule;

import java.util.List;

/**
 * {@code ContextMenu}, one kind's instance: the frame, its rows and the
 * submenu frames of its submenu rows, built once on the branch given and
 * kept; bound to an object while shown, to none after; the cursor by the
 * geometry; a submenu beside its row. The steward makes one per kind at the
 * kind's first open. A primitive, owning the DOM on its branch.
 */
public record ContextMenuModule() implements DomModule<ContextMenuModule> {

    /** The class. */
    public record ContextMenu() implements Exportable._Constant<ContextMenuModule> {}

    public static final ContextMenuModule INSTANCE = new ContextMenuModule();

    @Override
    public ImportsFor<ContextMenuModule> imports() {
        return ImportsFor.<ContextMenuModule>builder()
                .add(new ModuleImports<>(List.of(
                        new MenuStyles.cm_frame(),
                        new MenuStyles.cm_frame_static(),
                        new MenuStyles.cm_item(),
                        new MenuStyles.cm_item_label(),
                        new MenuStyles.cm_item_hint(),
                        new MenuStyles.cm_item_disclose(),
                        new MenuStyles.cm_item_disabled(),
                        new MenuStyles.cm_item_hidden(),
                        new MenuStyles.cm_separator()
                ), MenuStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new MenuGeometryModule.MenuGeometry()), MenuGeometryModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new MenuTreeModule.MenuTree()), MenuTreeModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new IconModule.Icon()), IconModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ContextMenuModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ContextMenu()));
    }
}
