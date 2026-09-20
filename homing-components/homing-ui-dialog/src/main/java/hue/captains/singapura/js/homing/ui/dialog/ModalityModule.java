package hue.captains.singapura.js.homing.ui.dialog;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code Modality}, headless: what it takes for one element to own the
 * screen. {@code new Modality(frame, opts)} makes everything else inert and
 * captures the keyboard on the document; {@code release()} undoes both and
 * gives the focus back. It mints nothing and wears nothing — the scrim is
 * the dialog's, and so is every look. An element component of a kind: the
 * frame comes in by the constructor, minted by whoever holds the screen.
 *
 * <p>Kept as a module of its own inside the dialog's, because a second
 * component will want exactly this — a popover, a menu — and then it moves
 * into the component base as a file, not a rewrite.</p>
 */
public record ModalityModule() implements EsModule<ModalityModule> {

    /** The class. */
    public record Modality() implements Exportable._Constant<ModalityModule> {}

    public static final ModalityModule INSTANCE = new ModalityModule();

    @Override public ImportsFor<ModalityModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<ModalityModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Modality()));
    }
}
