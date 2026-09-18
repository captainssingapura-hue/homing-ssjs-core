package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Box.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Feedback.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Pairing.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;


/**
 * The CSS graph workbench: a head, a bar of controls, and the waves of nodes.
 * Structure only; a node is a raised card, a badge is an inline mark, a
 * prior badge is on the primary surface, a sheet's state is feedback.
 */
public record CssGraphStyles() implements CssGroup<CssGraphStyles> {
    public static final CssGraphStyles INSTANCE = new CssGraphStyles();

    public record cg_root() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Caption.class, Type.Scale.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            height: 100%;
            min-height: 0;
            """; }
    }
    public record cg_head() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Hairline.class, Color.Edge.class), of(Hairline.class, Shape.Rule.class)); }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            flex: 0 0 auto;
            """; }
    }
    public record cg_title() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Heading.class, Type.Weight.class)); }
        @Override public String body() { return ""; }
    }
    public record cg_worn() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            margin-left: auto;
            """; }
    }
    public record cg_btn() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Kicker.class, Type.Scale.class), of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
            font: inherit;
            padding: 2px 8px;
            """; }
    }
    public record cg_bar() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Hairline.class, Color.Edge.class), of(Hairline.class, Shape.Rule.class)); }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            flex: 0 0 auto;
            """; }
    }
    public record cg_select() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Kicker.class, Type.Scale.class)); }
        @Override public String body() { return """
            font: inherit;
            padding: 2px 4px;
            """; }
    }
    public record cg_note() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            """; }
    }
    public record cg_note_err() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Danger.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }
    public record cg_waves() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            display: flex;
            gap: 12px;
            padding: 12px;
            overflow: auto;
            flex: 1 1 auto;
            align-items: flex-start;
            """; }
    }
    public record cg_wave() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 8px;
            min-width: 180px;
            """; }
    }
    public record cg_wave_head() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Kicker.class, Type.Treatment.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }
    public record cg_node() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class)); }
        @Override public String body() { return """
            padding: 8px;
            display: flex;
            flex-direction: column;
            gap: 2px;
            """; }
    }
    public record cg_node_id() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Weight.class)); }
        @Override public String body() { return """
            word-break: break-all;
            """; }
    }
    public record cg_badge() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Treatment.class), of(Inline.class, Shape.Corner.class), of(Recessed.class, Color.Surface.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            display: inline-block;
            padding: 0 4px;
            margin-right: 4px;
            """; }
    }
    /** The prior's badge: applied beside cg_badge; the primary surface and ink win by order. */
    public record cg_badge_prior() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class), of(OnPrimary.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }
    /** An unknown node's badge: no surface, a dashed inline edge. */
    public record cg_badge_unknown() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Edge.class), of(Inline.class, Shape.Rule.class)); }
        @Override public String body() { return """
            background: transparent;
            """; }
    }
    public record cg_deps() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Kicker.class, Type.Scale.class)); }
        @Override public String body() { return ""; }
    }
    public record cg_sheets() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class)); }
        @Override public String body() { return ""; }
    }
    public record cg_sheet() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Link.class, Motion.Ease.class)); }
        @Override public String body() { return """
            display: inline-block;
            margin-right: 8px;
            """; }
    }
    public record cg_sheet_pending() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Inert.class, Effect.Opacity.class), of(Caption.class, Type.Treatment.class)); }
        @Override public String body() { return ""; }
    }
    public record cg_sheet_landed() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }
    public record cg_sheet_applied() implements CssClass<CssGraphStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Success.class, Color.Ink.class), of(Label.class, Type.Weight.class)); }
        @Override public String body() { return ""; }
    }

    @Override
    public List<CssClass<CssGraphStyles>> cssClasses() {
        return List.of(
                new cg_root(), new cg_head(), new cg_title(), new cg_worn(), new cg_btn(),
                new cg_bar(), new cg_select(), new cg_note(), new cg_note_err(),
                new cg_waves(), new cg_wave(), new cg_wave_head(),
                new cg_node(), new cg_node_id(), new cg_badge(), new cg_badge_prior(), new cg_badge_unknown(),
                new cg_deps(), new cg_sheets(), new cg_sheet(), new cg_sheet_pending(), new cg_sheet_landed(), new cg_sheet_applied()
        );
    }
}
