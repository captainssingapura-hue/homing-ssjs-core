package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code KeyboardWalk}: the walk over the focus party tree — Tab and
 * Shift+Tab moving the steward's candidate in pre-order, Enter confirming it,
 * Escape calling it off — and where a step lands: over the members a
 * container says it is not showing, over a member that has left the walk
 * ({@code inWalk()} false) and everything under it, and home to the holder to
 * end. Headless statics on a steward, so the rules are read and tested
 * without a browser.
 */
public record KeyboardWalkModule() implements EsModule<KeyboardWalkModule> {

    /** The class of statics: {@code keyDown}, {@code move}, {@code step}, {@code offerable}. */
    public record KeyboardWalk() implements Exportable._Constant<KeyboardWalkModule> {}

    public static final KeyboardWalkModule INSTANCE = new KeyboardWalkModule();

    @Override public ImportsFor<KeyboardWalkModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<KeyboardWalkModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new KeyboardWalk()));
    }
}
