package hue.captains.singapura.js.homing.studio.base.ui.layout;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * MultiTabPaneModule — second of the three independent workspace
 * primitives. Layers on top of {@link SplitPaneModule} to add tabs with
 * a <b>conserved budget</b>: the total tab capacity across the entire
 * workspace is fixed (default 16), and each pane's capacity is derived
 * from its depth in the split tree as {@code budget / 2^depth}.
 *
 * <h2>The budget rule</h2>
 *
 * <p>A workspace's total tab capacity is fixed and equals {@code budget}.
 * Splitting a pane halves its capacity for each child; merging two leaf
 * siblings doubles the surviving pane's capacity. The budget is therefore
 * <b>conserved across any layout configuration</b> — the workspace cannot
 * grow its tab count by reshuffling panes.</p>
 *
 * <p>For the default {@code budget = 16}:</p>
 *
 * <table>
 *   <caption>Depth → per-pane capacity</caption>
 *   <tr><th>Depth</th><th>Capacity / pane</th><th>Max panes</th></tr>
 *   <tr><td>0 (single root)</td><td>16</td><td>1</td></tr>
 *   <tr><td>1</td><td>8</td><td>2</td></tr>
 *   <tr><td>2 (2x2 starter)</td><td>4</td><td>4</td></tr>
 *   <tr><td>3</td><td>2</td><td>8</td></tr>
 *   <tr><td>4 (deepest)</td><td>1</td><td>16</td></tr>
 * </table>
 *
 * <p>Splitting is forbidden only when capacity would drop below 1 (so the
 * deepest pane stays at capacity 1, not 0.5). Existing tabs stay in the
 * originating child even when that puts it over its new capacity — the
 * capacity pill turns red to surface this, but the split is allowed.
 * {@code addTab} is still gated by capacity (no growth via reshuffle), so
 * the budget invariant survives where it matters. Design intent: every
 * pane is always splittable as long as the workspace hasn't hit its
 * 16-pane ceiling.</p>
 *
 * <p>The 16-pane cap is principled: above this, a single workspace's
 * visual density and performance both degrade. Use cases genuinely
 * needing more widgets working together are served by RFC 0025's future
 * inter-connected multi-window feature, not by relaxing this constraint.</p>
 *
 * <h2>Operations</h2>
 *
 * <ul>
 *   <li>{@code addTab(slotId, tab)} — append a tab to a pane; throws if at capacity.</li>
 *   <li>{@code removeTab(slotId, tabId)} — close a tab; if pane empties, it stays empty.</li>
 *   <li>{@code switchTab(slotId, tabId)} — make a tab active.</li>
 *   <li>{@code split(slotId, orientation)} — split a leaf pane; gated by budget rule.</li>
 *   <li>{@code merge(slotId)} — merge a leaf pane with its leaf sibling; tabs concatenate.</li>
 *   <li>{@code canSplit(slotId)} / {@code canMerge(slotId)} — predicate forms for UI gating.</li>
 *   <li>{@code capacityOf(slotId)} — depth-derived capacity number.</li>
 * </ul>
 *
 * <p>The two operations are mathematical inverses; together with
 * {@code addTab}/{@code removeTab} they're the complete API. There is no
 * separate "close pane" — the only way to reduce pane count is
 * {@code merge}, and the halving math is its own rescue (a merged pane
 * always has room for the union of its children's tabs).</p>
 *
 * <p>Like SplitPane, MultiTabPane uses raw DOM internally (no
 * DomOpsParty) — the primitive is portable beyond the workspace shell.
 * Lifecycle owned by an explicit {@code destroy()}.</p>
 */
public record MultiTabPaneModule() implements DomModule<MultiTabPaneModule> {

    /** The single export — the {@code MultiTabPane} JS class. */
    public record MultiTabPane() implements Exportable._Constant<MultiTabPaneModule> {}

    public static final MultiTabPaneModule INSTANCE = new MultiTabPaneModule();

    @Override
    public ImportsFor<MultiTabPaneModule> imports() {
        return ImportsFor.<MultiTabPaneModule>builder()
                .add(new ModuleImports<>(
                        List.of(new SplitPaneModule.SplitPane()),
                        SplitPaneModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new MultiTabPaneDragModule.TabDragController()),
                        MultiTabPaneDragModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<MultiTabPaneModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new MultiTabPane()));
    }
}
