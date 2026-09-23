package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code KeyboardShortcuts}: the keys a page claims before anyone — tried by
 * the steward ahead of the native world and ahead of the holder, because a
 * command that summons something has to work while the hand is in a control.
 * One rule keeps it honest: a chord, or a function key, never a key someone
 * could be typing. Headless statics over the caller's list.
 */
public record KeyboardShortcutsModule() implements EsModule<KeyboardShortcutsModule> {

    /** The class of statics: {@code add}, {@code took}, {@code claimable}. */
    public record KeyboardShortcuts() implements Exportable._Constant<KeyboardShortcutsModule> {}

    public static final KeyboardShortcutsModule INSTANCE = new KeyboardShortcutsModule();

    @Override public ImportsFor<KeyboardShortcutsModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<KeyboardShortcutsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new KeyboardShortcuts()));
    }
}
