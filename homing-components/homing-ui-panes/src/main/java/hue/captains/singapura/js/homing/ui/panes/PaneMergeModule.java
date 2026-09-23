package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code PaneMerge}: whether one dock can take another's tabs, answered
 * before a single tab moves. A merge is many moves and there is no half of
 * one — a spent budget or a name already taken part way through would leave
 * the tabs scattered over two docks, one of which is about to go — so the
 * whole of it is planned from the two lists of names and the receiving
 * pane's budget, and the caller either does all of it or refuses and says
 * why. Headless, and pure: it imports nothing and touches nothing; the
 * moving is the caller's, one {@code detachTab}/{@code attachTab} a name, in
 * the order the plan gives.
 */
public record PaneMergeModule() implements EsModule<PaneMergeModule> {

    /** The class of statics: {@code plan(from, to, budget)}. */
    public record PaneMerge() implements Exportable._Constant<PaneMergeModule> {}

    public static final PaneMergeModule INSTANCE = new PaneMergeModule();

    @Override public ImportsFor<PaneMergeModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<PaneMergeModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PaneMerge()));
    }
}
