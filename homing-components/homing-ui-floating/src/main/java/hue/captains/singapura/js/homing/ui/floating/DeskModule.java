package hue.captains.singapura.js.homing.ui.floating;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import java.util.List;

/**
 * {@code Desk}: the floor the panes float on. It owns the stack — z-order,
 * the active one, the host they float in — opens a pane holding a widget by
 * the base's contract, raises the one pressed or focused, closes on the
 * cross or Escape, and reports every mutation as one {@code FloatEvents}
 * object on one sink. The workspace's substrate.
 */
public record DeskModule() implements DomModule<DeskModule> {

    /** The class: {@code new Desk(branch, {host, onEvent?, minW?, minH?})}; {@code open(spec)}, {@code raise(id)}, {@code close(id)}, {@code dispose()}. */
    public record Desk() implements Exportable._Constant<DeskModule> {}

    public static final DeskModule INSTANCE = new DeskModule();

    @Override
    public ImportsFor<DeskModule> imports() {
        return ImportsFor.<DeskModule>builder()
                .add(new ModuleImports<>(List.of(new FloatingPaneModule.FloatingPane()), FloatingPaneModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FloatEventsModule.FloatEvents()), FloatEventsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FloatingStyles.fp_desk()), FloatingStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DeskModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Desk()));
    }
}
