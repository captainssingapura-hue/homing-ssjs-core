package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * A slot that hosts widgets the way the workspace does: any number
 * constructed and kept, one of them in the DOM.
 *
 * <pre>
 *   createWidgetSlot({ branch, host }) → slot
 *   slot.show(key, construct, params, ask)   constructs on first show, then shows what it kept
 *   slot.tell(key, message)                  the host's way in, when the widget has a tell
 *   slot.hide()                              takes the shown widget out of the DOM, keeps it
 *   slot.current()                           the key shown, or null
 *   slot.has(key), slot.keys()
 *   slot.dispose(key), slot.disposeAll()     dispose() if present, then the branch dissolves
 * </pre>
 *
 * <p>Headless: the slot mints nothing and wears nothing. It hands each
 * widget a branch of its own under the slot's and the host's {@code ask},
 * attaches and detaches the root the widget returned, tells {@code
 * setActive} on the way in and out, passes a message to a widget's {@code
 * tell}, and dissolves on disposal. That is the whole of hosting a widget, and it
 * is the same for a dialog pane, a single-widget workspace and a tab.</p>
 */
public record WidgetSlot() implements EsModule<WidgetSlot> {

    public record createWidgetSlot() implements Exportable._Constant<WidgetSlot> {}

    public static final WidgetSlot INSTANCE = new WidgetSlot();

    @Override public ImportsFor<WidgetSlot> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WidgetSlot> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createWidgetSlot()));
    }
}
