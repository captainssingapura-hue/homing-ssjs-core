package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Box.Control;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary;
import static hue.captains.singapura.js.homing.design.Interaction.Focus;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Interaction.Selectable;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Structure.Divider;
import static hue.captains.singapura.js.homing.design.Target.Affordance;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Motion;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Caption;
import static hue.captains.singapura.js.homing.design.Text.Heading;
import static hue.captains.singapura.js.homing.design.Text.Kicker;
import static hue.captains.singapura.js.homing.design.Text.Lede;

/**
 * The view's two panes and the widgets' fields. Words worn, layout in the
 * bodies; the master's rows are the tree view's own and wear nothing here.
 */
public record PreferencesStyles() implements CssGroup<PreferencesStyles> {

    public static final PreferencesStyles INSTANCE = new PreferencesStyles();

    /** Master beside detail; the master as wide as its names, the detail the rest. */
    public record pv_root() implements CssClass<PreferencesStyles> {
        @Override public String body() { return """
            display: grid;
            grid-template-columns: minmax(200px, 280px) minmax(0, 1fr);
            gap: 0;
            min-height: 360px;
            align-items: stretch;
            """;
        }
    }

    /** The master pane: divided from the detail. */
    public record pv_master() implements CssClass<PreferencesStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Divider.class, Color.Edge.class), of(Divider.class, Shape.Rule.class)); }
        @Override public String body() { return """
            border-width: 0 1px 0 0;
            padding: 8px 8px 8px 0;
            min-width: 0;
            overflow: auto;
            """;
        }
    }

    /** The detail pane: the chosen node's widget. */
    public record pv_detail() implements CssClass<PreferencesStyles> {
        @Override public String body() { return """
            padding: 4px 0 8px 24px;
            min-width: 0;
            overflow: auto;
            display: flex;
            flex-direction: column;
            """;
        }
    }

    // ── A widget's furniture ──────────────────────────────────────────────────

    public record pv_kicker() implements CssClass<PreferencesStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Kicker.class, Type.Treatment.class), of(Kicker.class, Color.Ink.class)); }
        @Override public String body() { return "margin: 0 0 4px;"; }
    }

    public record pv_title() implements CssClass<PreferencesStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Heading.class, Type.Face.class), of(Heading.class, Type.Scale.class), of(Heading.class, Type.Weight.class), of(Heading.class, Color.Ink.class)); }
        @Override public String body() { return "margin: 0 0 6px;"; }
    }

    public record pv_summary() implements CssClass<PreferencesStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Lede.class, Color.Ink.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return "margin: 0 0 18px;"; }
    }

    /** A line about the value: what it is, where it came from. */
    public record pv_note() implements CssClass<PreferencesStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return "margin: 12px 0 0;"; }
    }

    /** The row of what a widget lets you do below its control. */
    public record pv_actions() implements CssClass<PreferencesStyles> {
        @Override public String body() { return """
            display: flex;
            gap: 10px;
            margin-top: 16px;
            """;
        }
    }

    // ── Choice ────────────────────────────────────────────────────────────────

    public record pv_options() implements CssClass<PreferencesStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 2px;
            margin: 0;
            padding: 0;
            list-style: none;
            max-width: 480px;
            """;
        }
    }

    /** An option: selectable, marked by aria-selected. */
    public record pv_option() implements CssClass<PreferencesStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Selectable.class, Color.Surface.class), of(Selectable.class, Color.Ink.class), of(Selectable.class, Color.Edge.class), of(Selectable.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Selectable.class, Motion.Ease.class), of(Selectable.class, Affordance.Cursor.class), of(Body.class, Type.Face.class), of(Focus.class, Shape.Rule.class), of(Focus.class, Color.Edge.class)); }
        @Override public String body() { return """
            font: inherit;
            display: flex;
            align-items: baseline;
            gap: 10px;
            padding: 7px 10px;
            text-align: left;
            width: 100%;
            box-sizing: border-box;
            """;
        }
    }

    public record pv_option_note() implements CssClass<PreferencesStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
            margin-left: auto;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            min-width: 0;
            """;
        }
    }

    // ── Toggle ────────────────────────────────────────────────────────────────

    /** The switch: a control whose track is raised and whose knob is primary when on. */
    public record pv_switch() implements CssClass<PreferencesStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Control.class, Shape.Rule.class), of(Interactive.class, Affordance.Cursor.class), of(Interactive.class, Motion.Ease.class), of(Focus.class, Shape.Rule.class), of(Focus.class, Color.Edge.class)); }
        @Override public String body() { return """
            font: inherit;
            display: inline-flex;
            align-items: center;
            gap: 12px;
            padding: 6px 10px;
            border-radius: 999px;
            """;
        }
    }

    public record pv_switch_track() implements CssClass<PreferencesStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            width: 36px;
            height: 20px;
            border-radius: 999px;
            position: relative;
            opacity: .35;
            """;
        }
    }

    /** The knob: primary when on, muted when off; it slides. */
    public record pv_switch_knob() implements CssClass<PreferencesStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class), of(Interactive.class, Motion.Ease.class)); }
        @Override public String body() { return """
            position: absolute;
            top: 2px;
            left: 2px;
            width: 16px;
            height: 16px;
            border-radius: 50%;
            transition-property: transform, opacity;
            opacity: .45;
            [aria-checked="true"] & { transform: translateX(16px); opacity: 1; }
            """;
        }
    }

    // ── Scale ─────────────────────────────────────────────────────────────────

    public record pv_range() implements CssClass<PreferencesStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class), of(Interactive.class, Affordance.Cursor.class), of(Focus.class, Shape.Rule.class), of(Focus.class, Color.Edge.class)); }
        @Override public String body() { return """
            width: 100%;
            max-width: 420px;
            background: transparent;
            accent-color: currentColor;
            """;
        }
    }

    public record pv_readout() implements CssClass<PreferencesStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Heading.class, Type.Face.class), of(Heading.class, Type.Scale.class), of(Heading.class, Color.Ink.class)); }
        @Override public String body() { return "margin: 8px 0 0;"; }
    }

    // ── Overview ──────────────────────────────────────────────────────────────

    public record pv_children() implements CssClass<PreferencesStyles> {
        @Override public String body() { return """
            margin: 0;
            padding: 0;
            list-style: none;
            display: flex;
            flex-direction: column;
            gap: 6px;
            """;
        }
    }

    public record pv_child() implements CssClass<PreferencesStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Divider.class, Color.Edge.class), of(Divider.class, Shape.Rule.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
            border-width: 0 0 1px 0;
            padding: 6px 0;
            display: flex;
            gap: 12px;
            align-items: baseline;
            """;
        }
    }

    @Override
    public List<CssClass<PreferencesStyles>> cssClasses() {
        return List.of(new pv_root(), new pv_master(), new pv_detail(),
                       new pv_kicker(), new pv_title(), new pv_summary(), new pv_note(), new pv_actions(),
                       new pv_options(), new pv_option(), new pv_option_note(),
                       new pv_switch(), new pv_switch_track(), new pv_switch_knob(),
                       new pv_range(), new pv_readout(),
                       new pv_children(), new pv_child());
    }
}
