package hue.captains.singapura.js.homing.ui.splitgrid;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import java.util.List;

/**
 * The JS side of {@link SplitGridEvent}: {@code SplitGridEvents}, a class of
 * static factories, one per kind, each validating what the record's compact
 * constructor validates and returning a frozen plain object tagged by
 * {@code kind}. Headless: data in, data out.
 */
public record SplitGridEventsModule() implements EsModule<SplitGridEventsModule> {

    /** The factories by kind, and {@code KINDS}, the kinds in order. */
    public record SplitGridEvents() implements Exportable._Constant<SplitGridEventsModule> {}

    public static final SplitGridEventsModule INSTANCE = new SplitGridEventsModule();

    @Override public ImportsFor<SplitGridEventsModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<SplitGridEventsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new SplitGridEvents()));
    }
}
