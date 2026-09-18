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
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;


/**
 * The workspace switcher's detail pane: a heading, a list of workspaces, a
 * rename row and a maintenance row. Structure only; the list is a recessed
 * field whose edge answers focus, the buttons are edged controls, the danger
 * button wears the danger ink and edge, an unavailable button is inert.
 */
public record WorkspaceSwitcherStyles() implements CssGroup<WorkspaceSwitcherStyles> {
    public static final WorkspaceSwitcherStyles INSTANCE = new WorkspaceSwitcherStyles();

    public record ws_detail() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 8px;
            height: 100%;
            min-height: 0;
            """; }
    }
    public record ws_head() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Weight.class), of(Heading.class, Color.Ink.class), of(Label.class, Type.Scale.class)); }
        @Override public String body() { return ""; }
    }
    public record ws_sub() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }
    /** The list is a recessed field; its edge answers focus (the raised edge's focus slot). */
    public record ws_list() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Recessed.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class)); }
        @Override public String body() { return """
            flex: 1 1 auto;
            min-height: 0;
            overflow-y: auto;
            padding: 4px 0;
            outline: none;
            """; }
    }
    /** Kept for the switcher's JS, which applies it on focus; the design's focus slot on the edge does the painting. */
    public record ws_list_focus() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String pseudoState() { return ":focus"; }
        @Override public String body() { return ""; }
    }
    public record ws_note() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }
    public record ws_row() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 8px;
            flex-wrap: wrap;
            """; }
    }
    public record ws_input() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Recessed.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
            flex: 1 1 160px;
            min-width: 0;
            font: inherit;
            padding: 4px 8px;
            """; }
    }
    public record ws_btn() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Caption.class, Type.Scale.class), of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
            font: inherit;
            padding: 4px 12px;
            """; }
    }
    /** The destructive action: applied beside ws_btn; the danger ink and edge win by order. */
    public record ws_btn_danger() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Danger.class, Color.Ink.class), of(Danger.class, Color.Edge.class)); }
        @Override public String body() { return ""; }
    }
    public record ws_btn_off() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Inert.class, Effect.Opacity.class), of(Inert.class, Affordance.Cursor.class)); }
        @Override public String body() { return ""; }
    }
    public record ws_maint() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Cap.class, Color.Edge.class), of(Cap.class, Shape.Rule.class)); }
        @Override public String body() { return """
            padding-top: 8px;
            """; }
    }

    @Override
    public List<CssClass<WorkspaceSwitcherStyles>> cssClasses() {
        return List.of(
                new ws_detail(), new ws_head(), new ws_sub(),
                new ws_list(), new ws_list_focus(), new ws_note(),
                new ws_row(), new ws_input(),
                new ws_btn(), new ws_btn_danger(), new ws_btn_off(),
                new ws_maint()
        );
    }
}
