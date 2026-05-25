package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.conformance.NoDomDestructionConformanceTest;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.studio.base.ui.layout.ModalModule;
import hue.captains.singapura.js.homing.studio.base.ui.layout.MultiTabPaneDragModule;
import hue.captains.singapura.js.homing.studio.base.ui.layout.MultiTabPaneModule;
import hue.captains.singapura.js.homing.studio.base.ui.layout.SplitPaneModule;

import java.util.List;
import java.util.Set;

/**
 * Enforces the "no DOM-destruction" doctrine across studio-base's
 * workspace-substrate primitives — SplitPane, MultiTabPane,
 * MultiTabPaneDrag, Modal. See {@link NoDomDestructionConformanceTest}
 * for the rationale; see the "Workspace Is the Substrate" doctrine for
 * the principle this operationalises.
 *
 * <p>Adding a new layout / chrome primitive to studio-base?
 * Append its module instance to {@link #domModules()}. The scan picks up
 * the new module's JS automatically.</p>
 */
class StudioBaseNoDomDestructionConformanceTest extends NoDomDestructionConformanceTest {

    @Override
    protected List<DomModule<?>> domModules() {
        return List.of(
                SplitPaneModule.INSTANCE,
                MultiTabPaneModule.INSTANCE,
                MultiTabPaneDragModule.INSTANCE,
                ModalModule.INSTANCE
        );
    }

    @Override
    protected Set<Class<? extends DomModule<?>>> allowList() {
        return Set.of(
                // ─── ModalModule — explicit exemption.
                //
                // Modal.setContent(el) is a wholesale-body-swap API: it
                // replaces the modal's body content in one call. The
                // implementation uses `this._body.innerHTML = ""` to clear
                // the previous body before appending the new content.
                //
                // Why this is acceptable here (and not in SplitPane /
                // MultiTabPane):
                //
                //   1. The Modal primitive does not own the inner DOM's
                //      state. setContent's contract is "I'm taking your
                //      previous body and replacing it" — callers who care
                //      about preserving the previous body's UI state are
                //      responsible for moving its content out BEFORE
                //      calling setContent.
                //
                //   2. The workspace's drag-to-modal flow (the canonical
                //      consumer that hosts branch-owned widget DOM in a
                //      modal) never uses setContent. It passes `content`
                //      at construction time, then destroys the modal
                //      entirely after moving `tab._contentEl` back out
                //      via single-move appendChild (see
                //      MultiTabPaneDragModule._armModalRedock). So no
                //      widget DOM ever sees the wipe.
                //
                //   3. A redesign that removed the wipe (e.g. "callers
                //      must take ownership of the previous body before
                //      calling setContent") would shift the burden to
                //      every consumer; the current design keeps Modal's
                //      API simple and pushes state-preservation
                //      responsibility to the one consumer that needs it.
                //
                // If this exemption ever stops applying — e.g. if a
                // consumer starts using setContent to swap widget DOM
                // in a modal body — the fix is at the consumer (move
                // the previous body's persistent content out first),
                // not by removing this allowlist entry.
                ModalModule.class
        );
    }
}
