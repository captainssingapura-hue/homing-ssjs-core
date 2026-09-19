package hue.captains.singapura.js.homing.ui.elements;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Box.Control;
import static hue.captains.singapura.js.homing.design.Box.Inline;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary;
import static hue.captains.singapura.js.homing.design.Emphasis.Secondary;
import static hue.captains.singapura.js.homing.design.Interaction.Focus;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Pairing.OnPrimary;
import static hue.captains.singapura.js.homing.design.Structure.Bar;
import static hue.captains.singapura.js.homing.design.Target.Affordance;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Effect;
import static hue.captains.singapura.js.homing.design.Target.Motion;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Caption;
import static hue.captains.singapura.js.homing.design.Text.Heading;
import static hue.captains.singapura.js.homing.design.Text.Kicker;
import static hue.captains.singapura.js.homing.design.Text.Link;

/**
 * The elements' classes. Every colour, face, edge, corner and motion is a
 * design word worn; the bodies hold layout and nothing that could be a
 * value. A button is a control that is primary or plain and shows a focus
 * ring; a card is a raised box with a heading, a line and a link.
 */
public record ElementStyles() implements CssGroup<ElementStyles> {

    public static final ElementStyles INSTANCE = new ElementStyles();

    // ── Button ────────────────────────────────────────────────────────────────

    /** What every button is: a control, interactive, with a focus ring. */
    public record el_button() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Control.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Interactive.class, Affordance.Cursor.class), of(Interactive.class, Motion.Ease.class), of(Focus.class, Shape.Rule.class), of(Focus.class, Color.Edge.class), of(Heading.class, Type.Face.class)); }
        @Override public String body() { return """
            font: inherit;
            padding: 8px 18px;
            min-width: 56px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            """;
        }
    }

    /** The primary button: the accent surface, ink for it. */
    public record el_button_primary() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class), of(OnPrimary.class, Color.Ink.class), of(Primary.class, Color.Edge.class)); }
        @Override public String body() { return ""; }
    }

    /** The plain button: raised, in body ink. */
    public record el_button_plain() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class)); }
        @Override public String body() { return ""; }
    }

    // ── Card ──────────────────────────────────────────────────────────────────

    /** The card: a raised box with a bar for an edge. */
    public record el_card() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Raised.class, Effect.Filter.class), of(Raised.class, Shape.Corner.class), of(Bar.class, Color.Edge.class), of(Bar.class, Shape.Rule.class)); }
        @Override public String body() { return """
            padding: 18px 20px;
            display: flex;
            flex-direction: column;
            gap: 6px;
            """;
        }
    }

    public record el_card_title() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Heading.class, Type.Face.class), of(Heading.class, Type.Scale.class), of(Heading.class, Type.Weight.class), of(Heading.class, Color.Ink.class)); }
        @Override public String body() { return """
            margin: 0;
            display: flex;
            align-items: baseline;
            gap: 8px;
            """;
        }
    }

    /** A short upper-case tag beside a title: a chip on the secondary surface, as the studio's badges are. */
    public record el_badge() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Secondary.class, Color.Surface.class), of(Heading.class, Color.Ink.class), of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Kicker.class, Type.Treatment.class), of(Inline.class, Shape.Corner.class)); }
        @Override public String body() { return """
            margin-left: auto;
            padding: 2px 8px;
            """;
        }
    }

    public record el_card_text() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Caption.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "margin: 0;"; }
    }

    /** The card's link, last, pushed to the bottom of the box. */
    public record el_card_link() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Link.class, Color.Ink.class), of(Link.class, Type.Decoration.class), of(Link.class, Motion.Ease.class), of(Focus.class, Shape.Rule.class), of(Focus.class, Color.Edge.class)); }
        @Override public String body() { return "margin-top: auto;"; }
    }

    @Override
    public List<CssClass<ElementStyles>> cssClasses() {
        return List.of(new el_button(), new el_button_primary(), new el_button_plain(),
                       new el_card(), new el_card_title(), new el_badge(), new el_card_text(), new el_card_link());
    }
}
