package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The JS side of {@link PaneEvent}: {@code PaneEvents}, one frozen object of
 * factories, one per kind, each validating what the record's compact
 * constructor validates and returning a frozen plain object tagged by
 * {@code kind} with the record's components as fields. Headless: data in,
 * data out.
 */
public record PaneEventsModule() implements EsModule<PaneEventsModule> {

    /** The factories keyed by kind, and {@code KINDS}, the kinds in order. */
    public record PaneEvents() implements Exportable._Constant<PaneEventsModule> {}

    public static final PaneEventsModule INSTANCE = new PaneEventsModule();

    @Override public ImportsFor<PaneEventsModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<PaneEventsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PaneEvents()));
    }
}
