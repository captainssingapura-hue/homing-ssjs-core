package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import java.util.List;

/**
 * {@code TabDrag}: the arithmetic of a chip in the hand — the bar as a row
 * of slots at one pitch, the chip placed by the press remembered as an
 * offset within it, the slot it is nearest, and how the others step aside —
 * with no element in sight. Headless, so the strip draws what it says and
 * the rules are tested without a browser.
 */
public record TabDragModule() implements EsModule<TabDragModule> {

    /** The class of statics: {@code pitch}, {@code clamp}, {@code dest}, {@code shift}. */
    public record TabDrag() implements Exportable._Constant<TabDragModule> {}

    public static final TabDragModule INSTANCE = new TabDragModule();

    @Override public ImportsFor<TabDragModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<TabDragModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new TabDrag()));
    }
}
