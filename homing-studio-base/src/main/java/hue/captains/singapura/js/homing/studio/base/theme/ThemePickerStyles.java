package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Wearable;
import hue.captains.singapura.js.homing.studio.base.ui.MasterDetailStyles;
import hue.captains.singapura.js.homing.studio.base.ui.SystemDialogStyles;

import java.util.List;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Box.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Pairing.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;


/**
 * The theme picker: the header button, the picker dialog's body (a
 * master/detail split over the dialog's body), and the preview pane.
 * Structure only; every element wears what it means.
 */
public record ThemePickerStyles() implements CssGroup<ThemePickerStyles> {
    public static final ThemePickerStyles INSTANCE = new ThemePickerStyles();

    /** The header button: an outlined control on the inverted band. */
    public record tp_btn() implements CssClass<ThemePickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(OnInverted.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(OnInverted.class, Color.Ink.class), of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
            font: inherit;
            display: inline-flex;
            align-items: center;
            gap: 4px;
            background: transparent;
            padding: 2px 8px;
            margin-left: auto;
            """; }
    }
    public record tp_btn_label() implements CssClass<ThemePickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(OnInvertedMuted.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }
    public record tp_body() implements CssClass<ThemePickerStyles> {
        @Override public List<CssClass<?>> dependsOn() {
            return List.of(new MasterDetailStyles.md_split(), new SystemDialogStyles.sd_body());
        }
        @Override public String body() { return """
            /* A column: MasterDetail's split takes the space, the footer sits
               under it. The split is flex:1 1 auto, so it yields to the
               footer's fixed height rather than pushing it out of view. */
            display: flex;
            flex-direction: column;
            height: 100%;
            min-height: 0;
            """; }
    }
    public record tp_preview_name() implements CssClass<ThemePickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Heading.class, Type.Weight.class), of(Label.class, Type.Scale.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
            display: flex;
            align-items: baseline;
            gap: 8px;
            margin-bottom: 8px;
            """; }
    }
    public record tp_current() implements CssClass<ThemePickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Kicker.class, Type.Treatment.class), of(Primary.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }
    public record tp_preview_note() implements CssClass<ThemePickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Caption.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            margin-bottom: 16px;
            """; }
    }
    public record tp_preview_pane() implements CssClass<ThemePickerStyles> {
        @Override public List<CssClass<?>> dependsOn() { return List.of(new MasterDetailStyles.md_body()); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            height: 100%;
            min-height: 0;
            box-sizing: border-box;
            padding-right: 16px;
            """; }
    }
    public record tp_preview_wrap() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            position: relative;
            display: flex;
            flex-direction: column;
            flex: 1 1 0;
            min-height: 0;
            """; }
    }
    public record tp_preview_frame() implements CssClass<ThemePickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class)); }
        @Override public String body() { return """
            display: block;
            box-sizing: border-box;
            width: 100%;
            flex: 1 1 0;
            min-height: 420px;
            """; }
    }
    /** The loading veil over the preview: hidden until the component shows it — visibility is the component's. */
    public record tp_preview_loading() implements CssClass<ThemePickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Muted.class, Color.Ink.class), of(Caption.class, Type.Scale.class), of(Control.class, Shape.Corner.class)); }
        @Override public String body() { return """
            position: absolute;
            inset: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            pointer-events: none;
            opacity: 0;
            transition: opacity 120ms ease;
            """; }
    }
    public record tp_preview_loading_on() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            opacity: 0.92;
            """; }
    }
    public record tp_inline() implements CssClass<ThemePickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class)); }
        @Override public String body() { return """
            max-width: 760px;
            overflow: hidden;
            """; }
    }
    public record tp_inline_head() implements CssClass<ThemePickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Hairline.class, Color.Edge.class), of(Hairline.class, Shape.Rule.class), of(Label.class, Type.Weight.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
            padding: 12px 14px;
            """; }
    }

    /** The colour control in the preview pane: a kicker and a row of swatch buttons. */
    public record tp_colours() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 8px;
            margin-bottom: 12px;
            """; }
    }
    public record tp_colours_label() implements CssClass<ThemePickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Kicker.class, Type.Treatment.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            margin-right: 4px;
            """; }
    }
    /** One palette: a control edged in the interactive edge, which the design colours in when it is checked. */
    public record tp_swatch() implements CssClass<ThemePickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Interactive.class, Color.Edge.class), of(Control.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class), of(Interactive.class, Affordance.Cursor.class), of(Interactive.class, Motion.Ease.class)); }
        @Override public String body() { return """
            font: inherit;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 3px 8px 3px 4px;
            """; }
    }
    public record tp_swatch_dots() implements CssClass<ThemePickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Hairline.class, Color.Edge.class), of(Hairline.class, Shape.Rule.class), of(Inline.class, Shape.Corner.class)); }
        @Override public String body() { return """
            display: inline-flex;
            overflow: hidden;
            """; }
    }
    /** A dot of the palette: its colour is DATA, set per dot by the module (RFC 0044). */
    public record tp_swatch_dot() implements CssClass<ThemePickerStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--tp-dot")); }
        @Override public String body() { return """
            width: 10px;
            height: 16px;
            background-color: var(--tp-dot);
            """; }
    }
    public record tp_swatch_own() implements CssClass<ThemePickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Kicker.class, Type.Scale.class)); }
        @Override public String body() { return ""; }
    }

    @Override
    public List<CssClass<ThemePickerStyles>> cssClasses() {
        return List.of(
                new tp_btn(), new tp_btn_label(),
                new tp_body(), new tp_inline(), new tp_inline_head(),
                new tp_preview_name(), new tp_current(), new tp_preview_note(),
                new tp_preview_pane(), new tp_preview_frame(), new tp_preview_wrap(),
                new tp_preview_loading(), new tp_preview_loading_on(),
                new tp_colours(), new tp_colours_label(), new tp_swatch(), new tp_swatch_dots(), new tp_swatch_dot(), new tp_swatch_own()
        );
    }
}
