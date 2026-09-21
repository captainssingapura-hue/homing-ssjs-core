package hue.captains.singapura.js.homing.ui.floating;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
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
    public record Desk() implements BranchComponent<DeskModule>, NeedKeyboard {
        @Override public String summary() { return "The layer floating panes live on: a stack, the frontmost active."; }
        /** The desk holds the keys for its panes: the active pane's widget's first, whatever they are, then Escape. */
        @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ESCAPE, "the active pane closed, when it can be closed; after its widget")); }
    }

    public static final DeskModule INSTANCE = new DeskModule();

    @Override
    public ImportsFor<DeskModule> imports() {
        return ImportsFor.<DeskModule>builder()
                .add(new ModuleImports<>(List.of(new FloatingPaneModule.FloatingPane()), FloatingPaneModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FloatEventsModule.FloatEvents()), FloatEventsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FloatingStyles.fp_desk(), new FloatingStyles.fp_desk_layer()), FloatingStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DeskModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Desk()));
    }
}
