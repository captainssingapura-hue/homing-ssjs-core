package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * {@code TabHand}, the hand on a strip's chip: the press, the drag in the
 * row, the drag afloat, the crossing between them by the two-thirds rule,
 * and the letting go. The strip arms each chip with it and keeps the state
 * and the classes; the hand reads rectangles, asks {@code TabDrag} for the
 * arithmetic, writes the chip's position as custom properties and tells the
 * strip what happened. It mints nothing of its own.
 */
public record TabHandModule() implements DomModule<TabHandModule> {

    /** The class: {@code new TabHand(strip)}, then {@code arm(chip, closeBtn)} per chip. */
    public record TabHand() implements Exportable._Constant<TabHandModule> {}

    public static final TabHandModule INSTANCE = new TabHandModule();

    @Override
    public ImportsFor<TabHandModule> imports() {
        return ImportsFor.<TabHandModule>builder()
                .add(new ModuleImports<>(List.of(new TabDragModule.TabDrag()), TabDragModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<TabHandModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new TabHand()));
    }
}
