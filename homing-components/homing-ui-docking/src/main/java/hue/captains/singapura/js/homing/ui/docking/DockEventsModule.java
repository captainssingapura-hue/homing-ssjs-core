package hue.captains.singapura.js.homing.ui.docking;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import java.util.List;

/**
 * The JS side of {@link DockEvent}: {@code DockEvents}, a class of static
 * factories, one per kind, each validating what the record's compact
 * constructor validates and returning a frozen plain object tagged by
 * {@code kind}. Headless: data in, data out.
 */
public record DockEventsModule() implements EsModule<DockEventsModule> {

    /** The factories by kind, and {@code KINDS}, the kinds in order. */
    public record DockEvents() implements Exportable._Constant<DockEventsModule> {}

    public static final DockEventsModule INSTANCE = new DockEventsModule();

    @Override public ImportsFor<DockEventsModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<DockEventsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new DockEvents()));
    }
}
