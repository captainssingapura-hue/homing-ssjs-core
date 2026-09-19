package hue.captains.singapura.js.homing.ui.dialog;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * Modality, headless: what it takes for one element to own the screen.
 * {@code holdModality(frame, opts)} makes everything else inert, captures
 * the keyboard on the document, and returns the release that undoes both
 * and gives the focus back. It mints nothing and wears nothing — the scrim
 * is the dialog's, and so is every look.
 *
 * <p>Kept as a module of its own inside the dialog's, because a second
 * component will want exactly this — a popover, a menu — and then it moves
 * into the component base as a file, not a rewrite.</p>
 */
public record Modality() implements EsModule<Modality> {

    public record holdModality() implements Exportable._Constant<Modality> {}

    public static final Modality INSTANCE = new Modality();

    @Override public ImportsFor<Modality> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<Modality> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new holdModality()));
    }
}
