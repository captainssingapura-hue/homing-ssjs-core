package hue.captains.singapura.js.homing.ui.docking;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.floating.DeskModule;
import java.util.List;

/**
 * {@code Docking}: the protocol between a desk and its docks. It owns the
 * desk as a layer over the docks' host; a dock is a multi-tab pane given to
 * it. A chip pulled off a dock's strip is detached and floats under the same
 * hand; a floating pane dragged over a dock is offered to it, and dropped
 * there becomes its tab where the mark said. Every dock and undock is one
 * {@code DockEvents} object on the sink, beside the desk's and the panes'
 * own. The workspace's seed. A branch component.
 */
public record DockingModule() implements DomModule<DockingModule> {

    /** The class: {@code new Docking(branch, {host, onEvent?, minW?, minH?})}; {@code addDock(pane)}, {@code undock(dock, tab, e)}, {@code dock(paneId, dock, index?)}. */
    public record Docking() implements Exportable._Constant<DockingModule> {}

    public static final DockingModule INSTANCE = new DockingModule();

    @Override
    public ImportsFor<DockingModule> imports() {
        return ImportsFor.<DockingModule>builder()
                .add(new ModuleImports<>(List.of(new DeskModule.Desk()), DeskModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DockEventsModule.DockEvents()), DockEventsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DockingModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Docking()));
    }
}
