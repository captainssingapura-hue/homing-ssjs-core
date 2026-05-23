package hue.captains.singapura.js.homing.studio.base.ui.layout;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * MultiTabPaneDragModule — per-instance drag controller for {@link
 * MultiTabPaneModule}. Owns the mousedown → mousemove → mouseup flow
 * for tab chips: ghost element, drop indicator, capacity-gated drop,
 * detach-to-Modal, and re-dock from Modal.
 *
 * <p>Split out from {@link MultiTabPaneModule} for Modest File Size
 * compliance — the drag flow is dense ({@code ~250} effective lines) and
 * benefits from sitting in its own file. The MultiTabPane class
 * instantiates a {@code TabDragController} in its constructor and calls
 * {@code beginDrag(event, slotId, tabId)} from chip mousedown handlers.</p>
 *
 * <p>Per-instance state — module-level singletons (which the original
 * js-demo's MultiTabPane used for {@code _drag} and {@code _instances})
 * are gone. Each MultiTabPane owns one TabDragController; both share
 * lifetime.</p>
 */
public record MultiTabPaneDragModule() implements DomModule<MultiTabPaneDragModule> {

    /** The single export — the {@code TabDragController} JS class. */
    public record TabDragController() implements Exportable._Constant<MultiTabPaneDragModule> {}

    public static final MultiTabPaneDragModule INSTANCE = new MultiTabPaneDragModule();

    @Override
    public ImportsFor<MultiTabPaneDragModule> imports() {
        return ImportsFor.<MultiTabPaneDragModule>builder()
                .add(new ModuleImports<>(
                        List.of(new ModalModule.Modal()),
                        ModalModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<MultiTabPaneDragModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new TabDragController()));
    }
}
