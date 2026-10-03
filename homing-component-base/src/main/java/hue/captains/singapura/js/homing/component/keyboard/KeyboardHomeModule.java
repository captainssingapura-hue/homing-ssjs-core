package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code KeyboardHome}: the page's home - the root's default allocation, and
 * its anchor (RFC 0066 E3, keyboard). Where a yield or a leaver's hand-on goes
 * when it reaches the page; what the home gives up itself, given straight back
 * to it; and a native control letting go of the keys, as a yield from the
 * control - its member asked first. Statics on a steward, as
 * {@link KeyboardWalkModule}'s are; pure.
 */
public record KeyboardHomeModule() implements EsModule<KeyboardHomeModule> {

    /** The class of statics: {@code name}, {@code reached}, {@code letGo}. */
    public record KeyboardHome() implements Exportable._Constant<KeyboardHomeModule> {}

    public static final KeyboardHomeModule INSTANCE = new KeyboardHomeModule();

    @Override public ImportsFor<KeyboardHomeModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<KeyboardHomeModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new KeyboardHome())); }
}
