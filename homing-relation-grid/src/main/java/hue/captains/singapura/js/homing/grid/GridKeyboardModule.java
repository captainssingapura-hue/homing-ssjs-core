package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 — {@code GridKeyboard}: the shallow keyboard, keydown → selection
 * intent and nothing else (the RFC 0049 ShallowKeyboard pattern one level
 * down). Excel-shaped bindings: arrows move (edge-confined), Shift+arrows
 * extend, Tab/Shift+Tab step, Home/End jump the row, Ctrl+A selects the view.
 * Deep-mode keys arrive with Phase 5's edit controller, which owns the
 * keyboard while editing.
 */
public record GridKeyboardModule() implements DomModule<GridKeyboardModule> {

    /** The single export — the {@code GridKeyboard} JS class. */
    public record GridKeyboard() implements Exportable._Constant<GridKeyboardModule> {}

    public static final GridKeyboardModule INSTANCE = new GridKeyboardModule();

    @Override
    public ImportsFor<GridKeyboardModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridKeyboardModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GridKeyboard()));
    }
}
