package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/** The keyboard party's vocabulary as data — the JS class of statics mirroring {@link KeyboardEvent}. */
public record KeyboardEventsModule() implements EsModule<KeyboardEventsModule> {

    /** The class of static factories: {@code Granted}, {@code Taken}, {@code Released}; {@code KINDS}. */
    public record KeyboardEvents() implements Exportable._Constant<KeyboardEventsModule> {}

    public static final KeyboardEventsModule INSTANCE = new KeyboardEventsModule();

    @Override public ImportsFor<KeyboardEventsModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<KeyboardEventsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new KeyboardEvents()));
    }
}
