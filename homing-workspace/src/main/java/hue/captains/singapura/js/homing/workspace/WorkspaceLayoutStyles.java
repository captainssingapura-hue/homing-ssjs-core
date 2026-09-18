package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Box.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;


/**
 * The workspace frame: a ribbon over the content, a footer under it, and the
 * fullscreen and locked states. Structure only; each element wears what it
 * means — the frame is the base layer edged like a raised thing, the ribbon
 * and footer are raised bands with a line between them and the content, the
 * buttons are ghost controls that show a surface only when hovered.
 */
public record WorkspaceLayoutStyles() implements CssGroup<WorkspaceLayoutStyles> {
    public static final WorkspaceLayoutStyles INSTANCE = new WorkspaceLayoutStyles();

    public record wl_root() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> dependsOn() { return List.of(new StudioStyles.st_main()); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class)); }
        @Override public String body() { return """
                position: relative;
                display: flex;
                flex-direction: column;
                width: 100%;
                height: 100%;
                overflow: hidden;
                box-sizing: border-box;
                """; }
    }
    public record wl_ribbon() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Hairline.class, Color.Edge.class), of(Hairline.class, Shape.Rule.class), of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 8px;
                padding: 4px 10px;
                flex-shrink: 0;
                """; }
    }
    public record wl_ribbon_title() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Weight.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }
    public record wl_ribbon_items() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 6px;
                margin-left: 16px;
                flex: 1;
                """; }
    }
    /** A ghost control: the interactive surface and edge, which are nothing at rest and appear on hover — the state is the design's slot, not a class applied beside. */
    public record wl_ribbon_button() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Label.class, Type.Scale.class), of(Control.class, Shape.Corner.class), of(Interactive.class, Color.Surface.class), of(Interactive.class, Color.Edge.class), of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 26px;
                height: 22px;
                border: 1px solid transparent;
                background: transparent;
                line-height: 1;
                """; }
    }
    public record wl_ribbon_separator() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Tertiary.class, Color.Surface.class)); }
        @Override public String body() { return """
                width: 1px;
                height: 16px;
                """; }
    }
    public record wl_ribbon_label() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return ""; }
    }
    public record wl_ribbon_fs() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Label.class, Type.Scale.class), of(Control.class, Shape.Corner.class), of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
                margin-left: auto;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 26px;
                height: 22px;
                border: 1px solid transparent;
                background: transparent;
                line-height: 1;
                """; }
    }
    public record wl_content() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                flex: 1;
                min-height: 0;
                position: relative;
                overflow: hidden;
                """; }
    }
    public record wl_footer() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Cap.class, Color.Edge.class), of(Cap.class, Shape.Rule.class), of(Muted.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Kicker.class, Type.Scale.class)); }
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 8px;
                padding: 3px 10px;
                flex-shrink: 0;
                """; }
    }
    public record wl_footer_separator() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Tertiary.class, Color.Surface.class)); }
        @Override public String body() { return """
                width: 1px;
                height: 12px;
                """; }
    }
    public record wl_footer_button() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class), of(Control.class, Shape.Corner.class), of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 22px;
                height: 18px;
                border: 1px solid transparent;
                background: transparent;
                line-height: 1;
                """; }
    }
    public record wl_workspace_active() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return ""; /* marker — rules apply via body selector below */ }
    }
    public record wl_body_locked() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> dependsOn() { return List.of(new StudioStyles.st_root()); }
        @Override public String body() { return """
                height: 100vh !important;
                overflow: hidden !important;
                """; }
    }
    public record wl_fullscreen_active() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return ""; /* marker class — rules live in companion records */ }
    }
    /** The frame gone fullscreen: flush with the viewport, so it wears the base layer's absence of edge and corner. */
    public record wl_root_fullscreen() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Shape.Rule.class), of(Base.class, Shape.Corner.class)); }
        @Override public String body() { return """
                position: fixed;
                inset: 0;
                z-index: 9000;
                height: 100vh;
                """; }
    }
    public record wl_chrome_hidden() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                display: none !important;
                """; }
    }

    @Override
    public List<CssClass<WorkspaceLayoutStyles>> cssClasses() {
        return List.of(
                new wl_root(),
                new wl_ribbon(),
                new wl_ribbon_title(),
                new wl_ribbon_items(),
                new wl_ribbon_button(),
                new wl_ribbon_separator(),
                new wl_ribbon_label(),
                new wl_ribbon_fs(),
                new wl_content(),
                new wl_footer(),
                new wl_footer_separator(),
                new wl_footer_button(),
                new wl_workspace_active(),
                new wl_body_locked(),
                new wl_fullscreen_active(),
                new wl_root_fullscreen(),
                new wl_chrome_hidden()
        );
    }
}
