package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Box.Container;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Feedback.Danger;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Size;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Caption;
import static hue.captains.singapura.js.homing.design.Text.Code;
import static hue.captains.singapura.js.homing.design.Text.Heading;
import static hue.captains.singapura.js.homing.design.Text.Kicker;
import static hue.captains.singapura.js.homing.design.Text.Label;

/**
 * The components workbench's styles: the navigator's box, and the card the
 * summary pane draws for the selected node. Every look is a design word;
 * the bodies hold layout only.
 */
public record ComponentsStyles() implements CssGroup<ComponentsStyles> {

    public static final ComponentsStyles INSTANCE = new ComponentsStyles();

    /** The navigator: fills its pane, scrolls. */
    public record cw_tree() implements CssClass<ComponentsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
            height: 100%;
            overflow: auto;
            padding: 8px 4px;
            box-sizing: border-box;
            """;
        }
    }

    /** A line of status under the tree while it loads, or when it cannot. */
    public record cw_status() implements CssClass<ComponentsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return "padding: 6px 8px;"; }
    }

    /** The status when the feed failed. */
    public record cw_status_failed() implements CssClass<ComponentsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Danger.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    /** The summary pane: fills, scrolls, air around the card. */
    public record cw_pane() implements CssClass<ComponentsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
            height: 100%;
            overflow: auto;
            box-sizing: border-box;
            padding: 16px;
            """;
        }
    }

    /** The card for the selected node: a raised container. */
    public record cw_card() implements CssClass<ComponentsStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Raised.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Shadow.class),
                           of(Container.class, Shape.Corner.class), of(Container.class, Shape.Rule.class), of(Container.Card.class, Size.Inset.class));
        }
        @Override public String body() { return """
            max-width: 640px;
            display: flex;
            flex-direction: column;
            gap: 6px;
            """;
        }
    }

    /** The kind over the title: composition, vehicle, catalogue, or the component's shape. */
    public record cw_kicker() implements CssClass<ComponentsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Treatment.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    /** The node's name. */
    public record cw_title() implements CssClass<ComponentsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Heading.class, Type.Scale.class), of(Heading.class, Type.Weight.class)); }
        @Override public String body() { return "margin: 0;"; }
    }

    /** One line on what it is. */
    public record cw_summary() implements CssClass<ComponentsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Scale.class)); }
        @Override public String body() { return "margin: 2px 0 6px;"; }
    }

    /** A fact about the node: a key and a value on one line. */
    public record cw_fact() implements CssClass<ComponentsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
            display: flex;
            gap: 10px;
            align-items: baseline;
            """;
        }
    }

    /** The key of a fact, muted, at a fixed width so the values align. */
    public record cw_key() implements CssClass<ComponentsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            flex: none;
            inline-size: 72px;
            """;
        }
    }

    /** A value that is a name in code: a module, a path, a tag. */
    public record cw_code() implements CssClass<ComponentsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Code.class, Type.Face.class)); }
        @Override public String body() { return "overflow-wrap: anywhere;"; }
    }

    /** The pane before anything is selected. */
    public record cw_empty() implements CssClass<ComponentsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return ""; }
    }

    @Override
    public List<CssClass<ComponentsStyles>> cssClasses() {
        return List.of(new cw_tree(), new cw_status(), new cw_status_failed(), new cw_pane(), new cw_card(), new cw_kicker(), new cw_title(),
                       new cw_summary(), new cw_fact(), new cw_key(), new cw_code(), new cw_empty());
    }
}
