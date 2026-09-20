package hue.captains.singapura.js.homing.ui.elements;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.HrefManager;

import java.util.List;

/**
 * The elements: two classes, one of each shape. {@code Button} is an
 * element component — the caller mints a {@code Button.TAG} element on its
 * own branch and hands it in; the button dresses it and offers
 * {@code setOn}. {@code Card} is a branch component — the caller makes a
 * sub-branch for it; the card mints its tree on it and offers {@code root}.
 * Nothing here holds state, fetches, or navigates on its own — a card's
 * link is set through the href manager, and that is the only thing beyond
 * {@code css} either asks of the base.
 */
public record Elements() implements DomModule<Elements> {

    /** An element component: {@code new Button(el, props)}, the element minted by the caller with {@code Button.TAG}. */
    public record Button()        implements Exportable._Constant<Elements> {}
    /** A branch component: {@code new Card(branch, props)}, on a sub-branch of its own; {@code root} is what the caller appends. */
    public record Card()          implements Exportable._Constant<Elements> {}

    public static final Elements INSTANCE = new Elements();

    @Override
    public ImportsFor<Elements> imports() {
        return ImportsFor.<Elements>builder()
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()), HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new ElementStyles.el_button(),
                        new ElementStyles.el_button_primary(),
                        new ElementStyles.el_button_plain(),
                        new ElementStyles.el_button_off(),
                        new ElementStyles.el_card(),
                        new ElementStyles.el_card_title(),
                        new ElementStyles.el_badge(),
                        new ElementStyles.el_card_text(),
                        new ElementStyles.el_card_link()
                ), ElementStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<Elements> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Button(), new Card()));
    }
}
