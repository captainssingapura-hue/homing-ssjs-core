package hue.captains.singapura.js.homing.ui.split;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The JS side of {@link SplitEvent}: {@code SplitEvents}, one frozen object
 * of factories, one per kind, each validating what the record's compact
 * constructor validates and returning a frozen plain object tagged by
 * {@code kind}. Headless: data in, data out.
 */
public record SplitEventsModule() implements EsModule<SplitEventsModule> {

    /** The factories keyed by kind, and {@code KINDS}, the kinds in order. */
    public record SplitEvents() implements Exportable._Constant<SplitEventsModule> {}

    public static final SplitEventsModule INSTANCE = new SplitEventsModule();

    @Override public ImportsFor<SplitEventsModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<SplitEventsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new SplitEvents()));
    }
}
