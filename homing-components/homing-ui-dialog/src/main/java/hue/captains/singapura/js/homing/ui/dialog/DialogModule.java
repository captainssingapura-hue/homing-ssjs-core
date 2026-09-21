package hue.captains.singapura.js.homing.ui.dialog;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.elements.Elements;
import hue.captains.singapura.js.homing.ui.floating.FloatingPaneModule;

import java.util.List;

/**
 * {@code Dialog}. {@code new Dialog(branch, opts)} opens a floating pane on
 * the sub-branch the caller made for it — the pane's head, body and grip;
 * the caller's content fills the body; an action foot of the elements'
 * buttons — in a layer over the viewport, holds the screen through
 * {@link ModalityModule Modality} when modal, and is the instance that
 * closes it. Every close path dissolves the branch, so the layer, the scrim,
 * the pane and whatever the content built go together. A branch component.
 *
 * <p>The contract is the studio's {@code SystemDialog}'s, kept on purpose:
 * {@code content(branch, bodyEl) → {onKeydown?, focusEl?}}, {@code actions}
 * with an {@code id}, a {@code label}, {@code primary} and {@code
 * onClick(dialog)}, and an instance with {@code close}, {@code actionEl}
 * and {@code setAction}. A caller of the one is a caller of the other.</p>
 */
public record DialogModule() implements DomModule<DialogModule> {

    /** The class. */
    public record Dialog() implements BranchComponent<DialogModule>, NeedKeyboard {
        @Override public String summary() { return "A floating pane that owns the screen until dismissed, or, non-modal, one that does not."; }
        /** Claimed on open, given back on close; the content is asked first, then these. */
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ESCAPE, "the dialog closed, unless the content took it"), KeyBinding.of(Key.ENTER, "the primary action, outside a form control, unless the content took it"));
        }
    }

    public static final DialogModule INSTANCE = new DialogModule();

    @Override
    public ImportsFor<DialogModule> imports() {
        return ImportsFor.<DialogModule>builder()
                .add(new ModuleImports<>(List.of(new ModalityModule.Modality()), ModalityModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FloatingPaneModule.FloatingPane()), FloatingPaneModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.ButtonBuilder()), Elements.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new DialogStyles.dl_scrim(),
                        new DialogStyles.dl_layer(),
                        new DialogStyles.dl_float(),
                        new DialogStyles.dl_actions()
                ), DialogStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DialogModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Dialog()));
    }
}
