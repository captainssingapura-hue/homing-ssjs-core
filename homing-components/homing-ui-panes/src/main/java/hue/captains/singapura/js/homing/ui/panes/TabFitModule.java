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
 * <p>The floor is a <b>proportion</b> of the design's own tab rather than a
 * number of pixels, so a design with chunky tabs keeps chunky ones at their
 * narrowest and one with fine tabs keeps fine ones. A caller with a reason may
 * put a pixel floor under it as well; the larger of the two wins.</p>
 */
public record TabFitModule() implements EsModule<TabFitModule> {

    /** The class of statics: {@code row(n, room, natural, floor)}, {@code window(at, want, per, n)}, {@code step(at, by, per, n)}. */
    public record TabFit() implements Exportable._Constant<TabFitModule> {}

    public static final TabFitModule INSTANCE = new TabFitModule();

    @Override public ImportsFor<TabFitModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<TabFitModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new TabFit()));
    }
}
