package hue.captains.singapura.js.homing.component.keyboard;

import java.util.List;

/**
 * A component that takes keys says which, on its declaration: the export
 * record of a {@code UiComponent} implements this beside its shape and lists
 * its bindings, each key with its meaning.
 *
 * <pre>{@code
 * public record Slider() implements BranchComponent<SliderModule>, NeedKeyboard {
 *     @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ARROW_UP, "a step up"), ...); }
 * }
 * }</pre>
 *
 * <p>That is a declaration, not a registration: at runtime the party knows
 * only members. A container takes its keys through the party, as a member
 * of the page's {@code KeyboardSteward} claiming by the convention; a small
 * component takes them natively, by a {@code keydown} listener on its own
 * element — both declare, so the page's map is whole. {@link KeyboardRegistry}
 * derives a page's keyboard map from the components its crate closure
 * catalogues, checks page shortcuts against each other, and refuses any
 * module but the steward that listens on the document or the window or in
 * the capture phase.</p>
 */
public interface NeedKeyboard {

    /** The keys this component takes, each with its meaning; never empty. */
    List<KeyBinding> keys();
}
