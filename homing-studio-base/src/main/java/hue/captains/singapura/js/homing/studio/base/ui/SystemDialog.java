package hue.captains.singapura.js.homing.studio.base.ui;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * A system dialog — one that owns the screen until dismissed, or in non-modal
 * mode one that does not. RFC 0057, Phase 1.
 *
 * <p>Everything the theme picker had to do by hand to be a proper dialog, lifted
 * out so the next dialog cannot be built without it: a scrim that dims by filter,
 * {@code inert} on everything behind, capture-phase keyboard ownership released
 * on every close path, focus moved in on open and restored on close, Escape to
 * close and Enter as the primary action, an action row, a size derived from the
 * viewport, and an accent glow. Declaring a dialog gets all of it.</p>
 *
 * <p><b>This is not {@link hue.captains.singapura.js.homing.studio.base.ui.layout.ModalModule
 * Modal}, and does not compose it.</b> Modal is the draggable, resizable panel
 * that carries a widget out of a MultiTabPane, and it stays exactly that — it is
 * deliberately not modal, because a detached widget must leave the page usable.
 * RFC 0057 left open whether the contract should compose Modal or stand alone.
 * It stands alone: Modal is entirely conformance-baselined at 210 of 250
 * effective lines, mints raw DOM, and injects its CSS unlayered so that no typed
 * sheet can restyle it. A dialog on its own branch and its own sheet has none of
 * those constraints, and the picker's glow becomes an ordinary border and
 * box-shadow instead of an outline-and-filter workaround.</p>
 *
 * <p><b>Opt-in modality.</b> {@code modal: false} gives the same frame, actions
 * and keyboard model with no scrim, no inert, and keys scoped to the dialog
 * itself — which is what non-modal means.</p>
 */
public record SystemDialog() implements DomModule<SystemDialog> {

    /** Opens a dialog and returns its handle: {@code { el, bodyEl, branch, close(), setAction(id, state) }}. */
    public record openSystemDialog() implements Exportable._Constant<SystemDialog> {}

    public static final SystemDialog INSTANCE = new SystemDialog();

    @Override
    public ImportsFor<SystemDialog> imports() {
        return ImportsFor.<SystemDialog>builder()
                .add(new ModuleImports<>(List.of(
                        new SystemDialogStyles.sd_scrim(),
                        new SystemDialogStyles.sd_frame(),
                        new SystemDialogStyles.sd_glow(),
                        new SystemDialogStyles.sd_title(),
                        new SystemDialogStyles.sd_title_label(),
                        new SystemDialogStyles.sd_close(),
                        new SystemDialogStyles.sd_body(),
                        new SystemDialogStyles.sd_actions(),
                        new SystemDialogStyles.sd_action(),
                        new SystemDialogStyles.sd_action_primary(),
                        new SystemDialogStyles.sd_action_off()
                ), SystemDialogStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<SystemDialog> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new openSystemDialog()));
    }
}
