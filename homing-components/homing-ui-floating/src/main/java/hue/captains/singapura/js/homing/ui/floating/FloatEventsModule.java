package hue.captains.singapura.js.homing.ui.floating;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import java.util.List;

/**
 * The JS side of {@link FloatEvent}: {@code FloatEvents}, a class of static
 * factories, one per kind, each validating what the record's compact
 * constructor validates and returning a frozen plain object tagged by
 * {@code kind} with the record's components as fields. Headless: data in,
 * data out.
 */
public record FloatEventsModule() implements EsModule<FloatEventsModule> {

    /** The factories by kind, and {@code KINDS}, the kinds in order. */
    public record FloatEvents() implements Exportable._Constant<FloatEventsModule> {}

    public static final FloatEventsModule INSTANCE = new FloatEventsModule();

    @Override public ImportsFor<FloatEventsModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<FloatEventsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new FloatEvents()));
    }
}
