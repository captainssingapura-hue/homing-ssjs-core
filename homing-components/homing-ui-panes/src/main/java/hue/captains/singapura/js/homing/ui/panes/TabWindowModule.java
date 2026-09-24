package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * {@code TabWindow}: the part of a tab row a bar can show, and where over the
 * row it is. It owns the <b>rail</b> — the element the chips sit in, and the
 * only thing that moves. The strip makes the rail and hands it over; the
 * arithmetic is {@link TabFitModule.TabFit}'s; this is where the two meet the
 * page.
 *
 * <p>The window rests on whole tabs and only on whole tabs. That is what the
 * squeeze buys: a width at which {@code per} tabs are the room exactly, so
 * moving the window is multiplication and nothing is ever clipped down the
 * middle. The rail's own {@code scrollLeft}, never {@code scrollIntoView} —
 * which scrolls whatever it must to obey, and a workspace may not move under a
 * widget because a tab was selected somewhere in it.</p>
 */
public record TabWindowModule() implements DomModule<TabWindowModule> {

    /** The class: {@code new TabWindow(rail, opts)}, then {@code row}, {@code fit}, {@code reveal}, {@code step}. */
    public record TabWindow() implements Exportable._Constant<TabWindowModule> {}

    public static final TabWindowModule INSTANCE = new TabWindowModule();

    @Override
    public ImportsFor<TabWindowModule> imports() {
        return ImportsFor.<TabWindowModule>builder()
                .add(new ModuleImports<>(List.of(new TabFitModule.TabFit()), TabFitModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<TabWindowModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new TabWindow()));
    }
}
