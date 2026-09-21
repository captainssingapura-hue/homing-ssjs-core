package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Box.Container;
import static hue.captains.singapura.js.homing.design.Box.Control;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Interaction.Inert;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Interaction.Selectable;
import static hue.captains.singapura.js.homing.design.Layer.Overlay;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Structure.Divider;
import static hue.captains.singapura.js.homing.design.Target.Affordance;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Effect;
import static hue.captains.singapura.js.homing.design.Target.Motion;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Size;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Caption;
import static hue.captains.singapura.js.homing.design.Text.Label;

/**
 * The context menus' styles: the layer they show in, the frame that is
 * {@code Container.Menu}, the rows that are {@code Control.Option} and
 * {@code Selectable}, the marks, the hint, the separator. The frame's place
 * is the geometry's, carried in runtime variables the steward sets. Every
 * look is a design word; the bodies hold layout only.
 */
public record MenuStyles() implements CssGroup<MenuStyles> {

    public static final MenuStyles INSTANCE = new MenuStyles();

    /** The layer: fixed over the page, in the document only while a menu is open; the hand passes through it except on a frame. */
    public record cm_layer() implements CssClass<MenuStyles> {
        @Override public String body() { return """
            position: fixed;
            inset: 0;
            z-index: 10021;
            pointer-events: none;
            """;
        }
    }

    /**
     * A frame: {@code Container.Menu} to the design — its corner, rule and
     * ring the container's, its inset, gap and least width its own —
     * raised, under the overlay shadow, at {@code --cm-x/--cm-y} in the
     * layer. Focusable, so the keys have somewhere to go; no ring drawn for
     * that, the cursor is the ring.
     */
    public record cm_frame() implements CssClass<MenuStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--cm-x"), new CssVar("--cm-y")); }
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Container.Menu.class, Shape.Corner.class), of(Container.Menu.class, Shape.Rule.class),
                           of(Container.Menu.class, Size.Inset.class), of(Container.Menu.class, Size.Gap.class), of(Container.Menu.class, Size.Extent.class),
                           of(Raised.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Overlay.class, Shape.Shadow.class),
                           of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class));
        }
        @Override public String body() { return """
            position: absolute;
            left: var(--cm-x, 0px);
            top: var(--cm-y, 0px);
            display: flex;
            flex-direction: column;
            box-sizing: border-box;
            pointer-events: auto;
            outline: none;
            user-select: none;
            """;
        }
    }

    /**
     * A row: {@code Control.Option} to the design — its inset, its gap
     * between mark, label and hint, its corner — and {@code Selectable} for
     * its colour: the design's rest, hover, and the cursor as highlighted.
     */
    public record cm_item() implements CssClass<MenuStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Control.Option.class, Shape.Corner.class), of(Control.Option.class, Size.Inset.class), of(Control.Option.class, Size.Gap.class),
                           of(Selectable.class, Color.Surface.class), of(Selectable.class, Color.Ink.class), of(Selectable.class, Motion.Ease.class),
                           of(Interactive.class, Affordance.Cursor.class), of(Label.class, Type.Scale.class));
        }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            white-space: nowrap;
            outline: none;
            """;
        }
    }

    /** The label of a row: takes the room the marks and the hint leave. */
    public record cm_item_label() implements CssClass<MenuStyles> {
        @Override public String body() { return "flex: 1 1 auto;"; }
    }

    /** The mark before the label: the check of a checked row, else blank of the same width so labels align. */
    public record cm_item_check() implements CssClass<MenuStyles> {
        @Override public String body() { return """
            flex: none;
            inline-size: 1.1em;
            text-align: center;
            """;
        }
    }

    /** The hint after the label, in a muted caption. */
    public record cm_item_hint() implements CssClass<MenuStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return "flex: none;"; }
    }

    /** The mark after a submenu row: the arrow to its items. */
    public record cm_item_arrow() implements CssClass<MenuStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            flex: none;
            inline-size: 1.1em;
            text-align: end;
            """;
        }
    }

    /** A row that cannot be picked now: inert. */
    public record cm_item_disabled() implements CssClass<MenuStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Inert.class, Effect.Opacity.class), of(Inert.class, Affordance.Cursor.class)); }
        @Override public String body() { return ""; }
    }

    /** A row hidden for the bound object: not there. Last in the sheet, so it wins the display. */
    public record cm_item_hidden() implements CssClass<MenuStyles> {
        @Override public String body() { return "display: none;"; }
    }

    /** A separator between rows: the design's divider. */
    public record cm_separator() implements CssClass<MenuStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Divider.class, Color.Edge.class), of(Divider.class, Shape.Rule.class)); }
        @Override public String body() { return """
            flex: none;
            margin-block: 4px;
            """;
        }
    }

    @Override
    public List<CssClass<MenuStyles>> cssClasses() {
        return List.of(new cm_layer(), new cm_frame(), new cm_item(), new cm_item_label(), new cm_item_check(), new cm_item_hint(),
                       new cm_item_arrow(), new cm_item_disabled(), new cm_separator(), new cm_item_hidden());
    }
}
