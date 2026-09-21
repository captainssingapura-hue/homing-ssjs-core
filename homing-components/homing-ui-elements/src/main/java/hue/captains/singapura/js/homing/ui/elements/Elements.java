package hue.captains.singapura.js.homing.ui.elements;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.ElementComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.HrefManager;

import java.util.List;

/**
 * The elements. {@code Button} is an element component made through
 * {@code ButtonBuilder}: the builder tells the caller its tag, the caller
 * mints the element on its own branch, sets the properties progressively
 * and builds; the button dresses the element and is adjusted live — a
 * colour word, complete as a semantic surface, and the extent that scales
 * every visual surface of it together. {@code Card} is a branch component — the caller makes a
 * sub-branch for it; the card mints its tree on it and offers {@code root}.
 * Nothing here holds state, fetches, or navigates on its own — a card's
 * link is set through the href manager, and that is the only thing beyond
 * {@code css} either asks of the base.
 */
public record Elements() implements DomModule<Elements> {

    /** An element component, made through its builder; adjusted live: colour, extent, label, setOn. */
    public record Button() implements ElementComponent<Elements> {
        @Override public String tag() { return "button"; }
        @Override public String summary() { return "A button in a colour word, at an extent and a size; made through its builder."; }
    }
    /** The builder: {@code new ButtonBuilder()} tells its tag; the properties set progressively; {@code build(el)}. */
    public record ButtonBuilder() implements Exportable._Constant<Elements> {}
    /** A branch component, made through its builder: {@code root} is what the caller appends, {@code body} where a caller puts more than text; size, title, dispose. */
    public record Card() implements BranchComponent<Elements> {
        @Override public String summary() { return "A card: a hard frame at the design's measure and aspect, with a head, a body and a foot."; }
    }
    /** The builder: {@code new CardBuilder()}; title, badge, text, link, size, onClick set progressively; {@code build(branch)} on a sub-branch of the caller's. */
    public record CardBuilder()   implements Exportable._Constant<Elements> {}

    public static final Elements INSTANCE = new Elements();

    @Override
    public ImportsFor<Elements> imports() {
        return ImportsFor.<Elements>builder()
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()), HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new ElementStyles.el_button(),
                        new ElementStyles.el_button_primary(),
                        new ElementStyles.el_button_secondary(),
                        new ElementStyles.el_button_danger(),
                        new ElementStyles.el_button_warning(),
                        new ElementStyles.el_button_success(),
                        new ElementStyles.el_button_plain(),
                        new ElementStyles.el_button_on(),
                        new ElementStyles.el_button_off(),
                        new ElementStyles.el_card(),
                        new ElementStyles.el_card_action(),
                        new ElementStyles.el_card_head(),
                        new ElementStyles.el_card_title(),
                        new ElementStyles.el_badge(),
                        new ElementStyles.el_card_body(),
                        new ElementStyles.el_card_text(),
                        new ElementStyles.el_card_foot(),
                        new ElementStyles.el_card_link()
                ), ElementStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<Elements> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Button(), new ButtonBuilder(), new Card(), new CardBuilder()));
    }
}
