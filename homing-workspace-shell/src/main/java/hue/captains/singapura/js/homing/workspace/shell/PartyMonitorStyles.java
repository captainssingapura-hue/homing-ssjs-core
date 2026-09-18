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
 * The party monitor: a head over a tree of the live DOM parties. Structure
 * only; the head is a raised band, the tree is set in the code face, a
 * leak note is a warning.
 */
public record PartyMonitorStyles() implements CssGroup<PartyMonitorStyles> {
    public static final PartyMonitorStyles INSTANCE = new PartyMonitorStyles();

    public record pm_root() implements CssClass<PartyMonitorStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Caption.class, Type.Scale.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            height: 100%;
            min-height: 0;
            """; }
    }
    public record pm_head() implements CssClass<PartyMonitorStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Hairline.class, Color.Edge.class), of(Hairline.class, Shape.Rule.class)); }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            """; }
    }
    public record pm_title() implements CssClass<PartyMonitorStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Weight.class), of(Heading.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }
    public record pm_count() implements CssClass<PartyMonitorStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            flex: 1;
            """; }
    }
    public record pm_btn() implements CssClass<PartyMonitorStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
            font: inherit;
            padding: 2px 10px;
            """; }
    }
    public record pm_note() implements CssClass<PartyMonitorStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Caption.class, Type.Treatment.class)); }
        @Override public String body() { return """
            padding: 4px 12px;
            """; }
    }
    /** A leak: the note in the warning ink, upright. Applied beside pm_note; its ink wins by order, and the treatment is the caption's. */
    public record pm_note_leaked() implements CssClass<PartyMonitorStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Warning.class, Color.Ink.class), of(Label.class, Type.Weight.class)); }
        @Override public String body() { return ""; }
    }
    public record pm_tree() implements CssClass<PartyMonitorStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Code.class, Type.Face.class)); }
        @Override public String body() { return """
            flex: 1;
            min-height: 0;
            overflow: auto;
            padding: 8px 12px;
            """; }
    }

    @Override
    public List<CssClass<PartyMonitorStyles>> cssClasses() {
        return List.of(
                new pm_root(), new pm_head(), new pm_title(), new pm_count(), new pm_btn(),
                new pm_note(), new pm_note_leaked(), new pm_tree()
        );
    }
}
