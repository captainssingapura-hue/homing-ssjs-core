package hue.captains.singapura.js.homing.ui.dialog;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.elements.Elements;

import java.util.List;

/**
 * The dialog. {@code openDialog(opts)} builds a frame on a child of the
 * caller's branch — a title bar, the body the caller's content fills, an
 * action row of the elements' buttons — puts it over the page, holds the
 * screen through {@link Modality} when modal, and returns a handle that
 * closes it. Every close path dissolves the branch, so the frame, the scrim
 * and whatever the content built go together.
 *
 * <p>The contract is the studio's {@code SystemDialog}'s, kept on purpose:
 * {@code content(branch, bodyEl) → {onKeydown?, focusEl?}}, {@code actions}
 * with an {@code id}, a {@code label}, {@code primary} and {@code
 * onClick(handle)}, and a handle with {@code close}, {@code actionEl} and
 * {@code setAction}. A caller of the one is a caller of the other.</p>
 */
public record Dialog() implements DomModule<Dialog> {

    public record openDialog() implements Exportable._Constant<Dialog> {}

    public static final Dialog INSTANCE = new Dialog();

    @Override
    public ImportsFor<Dialog> imports() {
        return ImportsFor.<Dialog>builder()
                .add(new ModuleImports<>(List.of(new Modality.holdModality()), Modality.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.Button(), new Elements.setButtonOn()), Elements.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new DialogStyles.dl_scrim(),
                        new DialogStyles.dl_frame(),
                        new DialogStyles.dl_glow(),
                        new DialogStyles.dl_title(),
                        new DialogStyles.dl_title_label(),
                        new DialogStyles.dl_close(),
                        new DialogStyles.dl_body(),
                        new DialogStyles.dl_actions()
                ), DialogStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<Dialog> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new openDialog()));
    }
}
