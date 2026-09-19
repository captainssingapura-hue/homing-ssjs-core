package hue.captains.singapura.js.homing.ui.elements;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.HrefManager;

import java.util.List;

/**
 * The elements: builders a page calls with the branch that will own what
 * they make. {@code Button(branch, name, props)} and {@code Card(branch,
 * name, props)} each return one node; the caller places it. Nothing here
 * holds state, fetches, or navigates on its own — a card's link is set
 * through the href manager, and that is the only thing beyond {@code css}
 * either builder asks of the base.
 */
public record Elements() implements DomModule<Elements> {

    public record Button()        implements Exportable._Constant<Elements> {}
    public record setButtonOn()   implements Exportable._Constant<Elements> {}
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
        return new ExportsOf<>(INSTANCE, List.of(new Button(), new setButtonOn(), new Card()));
    }
}
