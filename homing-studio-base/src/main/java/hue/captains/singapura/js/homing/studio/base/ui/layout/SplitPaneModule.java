package hue.captains.singapura.js.homing.studio.base.ui.layout;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * SplitPaneModule — first of the three independent <i>workspace
 * primitives</i> (the others being {@code MultiTabPaneModule} and
 * {@code ModalModule}). Exports a single JS class, {@link SplitPane},
 * implementing a recursive 2-dimensional split-pane layout manager.
 *
 * <p>The SplitPane class knows nothing about tabs, widgets, or modals —
 * only about <i>panes</i>, <i>splits</i>, and <i>dividers</i>. Each leaf
 * pane is identified by a stable {@code slotId}; the consumer supplies a
 * {@code renderSlot(slotId, paneElement)} callback that fills the leaf's
 * DOM container with whatever it pleases.</p>
 *
 * <p>The runtime shape of the layout tree is documented on the JS sidecar
 * (and mirrored by the SplitPane building-block guide). We deliberately do
 * <i>not</i> model the Pane tree as Java types yet — when a Java consumer
 * (e.g. the future workspace shell) needs to declare a static layout in
 * code, we will add a typed {@code Pane} sealed hierarchy alongside this
 * module. For now, callers compose layout trees as plain JS object
 * literals at runtime.</p>
 *
 * <p>Doctrine: this primitive uses raw {@code document.*} DOM internally
 * (no DomOpsParty). Reason — SplitPane must be reusable <i>outside</i> the
 * workspace shell (e.g. as the host for a slideshow ComposedDoc, or for a
 * before/after comparison viewer). The full lifecycle is owned by an
 * explicit {@code destroy()} method that removes listeners and DOM.</p>
 *
 * <p>Public JS API (the constructor returns a controller with):</p>
 * <ul>
 *   <li>{@code setLayout(newTree)} — replace the layout tree and re-render.</li>
 *   <li>{@code getLayout()} — return the current layout tree (with current ratios).</li>
 *   <li>{@code relayout()} — re-apply sizes (e.g. after container resize).</li>
 *   <li>{@code split(slotId, orientation, newSlotId, side?)} — split a leaf
 *       in two. The leaf becomes a split node containing [original, new] (or
 *       [new, original] when {@code side === "before"}); the caller's
 *       {@code renderSlot} is invoked for the new {@code newSlotId} on
 *       re-render. The residing app drives UX — the demo puts ⇆/⇅ buttons
 *       on each leaf; a {@code MultiTabPane} will put them on its tab strip.</li>
 *   <li>{@code destroy()} — remove all listeners and clear the container.</li>
 * </ul>
 */
public record SplitPaneModule() implements DomModule<SplitPaneModule> {

    /**
     * The single export — the {@code SplitPane} JS class. Lives as a nested
     * record so {@code Exportable._Constant}'s {@code getSimpleName()}-based
     * naming convention emits the unprefixed {@code SplitPane} symbol on
     * the wire.
     */
    public record SplitPane() implements Exportable._Constant<SplitPaneModule> {}

    public static final SplitPaneModule INSTANCE = new SplitPaneModule();

    @Override
    public ImportsFor<SplitPaneModule> imports() {
        // SplitPaneModule is intentionally dependency-free at the framework
        // level. It injects its own scoped <style> tag on first construction
        // so it remains drop-in usable without a CSS registry.
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<SplitPaneModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new SplitPane()));
    }
}
