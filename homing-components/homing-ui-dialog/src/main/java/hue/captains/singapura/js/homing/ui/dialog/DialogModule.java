package hue.captains.singapura.js.homing.ui.dialog;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.elements.Elements;

import java.util.List;

/**
 * {@code Dialog}. {@code new Dialog(branch, opts)} builds a frame on the
 * sub-branch the caller made for it — a title bar, the body the caller's
 * content fills, an action row of the elements' buttons — puts it over the
 * page, holds the screen through {@link ModalityModule Modality} when modal,
 * and is the instance that closes it. Every close path dissolves the branch,
 * so the frame, the scrim and whatever the content built go together. A
 * branch component.
 *
 * <p>The contract is the studio's {@code SystemDialog}'s, kept on purpose:
 * {@code content(branch, bodyEl) → {onKeydown?, focusEl?}}, {@code actions}
 * with an {@code id}, a {@code label}, {@code primary} and {@code
 * onClick(dialog)}, and an instance with {@code close}, {@code actionEl}
 * and {@code setAction}. A caller of the one is a caller of the other.</p>
 */
public record DialogModule() implements DomModule<DialogModule> {

    /** The class. */
    public record Dialog() implements Exportable._Constant<DialogModule> {}

    public static final DialogModule INSTANCE = new DialogModule();

    @Override
    public ImportsFor<DialogModule> imports() {
        return ImportsFor.<DialogModule>builder()
                .add(new ModuleImports<>(List.of(new ModalityModule.Modality()), ModalityModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.ButtonBuilder()), Elements.INSTANCE))
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
    public ExportsOf<DialogModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Dialog()));
    }
}
