package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code TabFit}: how many tabs a bar can show, how wide they must be, and
 * which of them the window is over. Arithmetic only — no DOM, no state; the
 * strip measures, asks, and applies.
 *
 * <p><b>The row is made to fit before it is made to scroll.</b> A bar with a
 * few tabs draws them at the width the design asked for. As they multiply they
 * are squeezed, all alike, until they reach a floor below which a tab is not a
 * tab any more — a word and a cross need room. Only then does the bar give up
 * and become a window over a row longer than itself.</p>
 *
 * <p>And the squeeze does one more thing, which is why it is worth doing well:
 * it chooses a width at which a <b>whole number</b> of tabs fills the bar
 * exactly. {@code per} tabs at {@code width} each are the room, to the pixel —
 * so a window is never half over a tab, nothing is ever clipped down the
 * middle, and moving the window is multiplication rather than a search. The
 * alternative — the natural width and a window wherever a scroll happens to
 * stop — shows a sliver at one end and a gap at the other, and asks a reader
 * to judge which of two half-tabs is the one they are on.</p>
 *
 * <p>The floor comes in already measured, because what it is made of belongs
 * to whoever holds the ruler. The narrowest a tab may be is an <b>aspect</b> —
 * how squat it is allowed to get against its own height — and that height is
 * the design's and does not move when the row is crowded, so the squeeze is a
 * width and only ever a width. A design with chunky tabs therefore keeps
 * chunky ones at their narrowest, with nothing said about either here.</p>
 */
public record TabFitModule() implements EsModule<TabFitModule> {

    /** The class of statics: {@code row(n, room, natural, least)}, {@code window(at, want, per, n)}, {@code step(at, by, per, n)}. */
    public record TabFit() implements Exportable._Constant<TabFitModule> {}

    public static final TabFitModule INSTANCE = new TabFitModule();

    @Override public ImportsFor<TabFitModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<TabFitModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new TabFit()));
    }
}
