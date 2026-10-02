package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code WidgetSlot}: a slot that hosts widgets the way the workspace does —
 * any number constructed and kept, one of them in the DOM.
 *
 * <pre>
 *   new WidgetSlot({ branch, host })
 *   slot.show(key, Widget, params)           constructs `new Widget(branch, params)` on first show, then shows what it kept
 *   slot.widget(key)                         the kept widget, for a holder that operates it
 *   slot.hide()                              takes the shown widget out of the DOM, keeps it
 *   slot.current()                           the key shown, or null
 *   slot.has(key), slot.keys()
 *   slot.dispose(key), slot.disposeAll()     dispose() if present, then the branch dissolves
 * </pre>
 *
 * <p>Headless: the slot mints nothing and wears nothing. It hands each
 * widget a branch of its own under the slot's, attaches and detaches the
 * root the widget holds, tells {@code setActive} on the way in and out,
 * keeps the widget for the holder, and dissolves on disposal. That is the
 * whole of hosting a widget, and it is the same for a dialog pane, a
 * single-widget workspace and a tab.</p>
 */
public record WidgetSlotModule() implements EsModule<WidgetSlotModule> {

    /** The class. */
    public record WidgetSlot() implements Exportable._Constant<WidgetSlotModule> {}

    public static final WidgetSlotModule INSTANCE = new WidgetSlotModule();

    @Override public ImportsFor<WidgetSlotModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WidgetSlotModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WidgetSlot()));
    }
}
