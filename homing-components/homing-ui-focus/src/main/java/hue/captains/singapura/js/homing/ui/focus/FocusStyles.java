package hue.captains.singapura.js.homing.ui.focus;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Box.Inline;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Interaction.Current;
import static hue.captains.singapura.js.homing.design.Interaction.Focus;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Motion;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Caption;
import static hue.captains.singapura.js.homing.design.Text.Code;

/** The monitor's classes: a tree of rows, each indented by its depth, the holder's row lit. */
public record FocusStyles() implements CssGroup<FocusStyles> {

    public static final FocusStyles INSTANCE = new FocusStyles();

    /** The tree: rows in a column, in a code face. */
    public record fm_tree() implements CssClass<FocusStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Code.class, Type.Face.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 2px;
            min-width: 0;
            """;
        }
    }

    /** A row: a node of the tree, indented by its depth through the runtime variable the monitor sets. */
    public record fm_row() implements CssClass<FocusStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--fm-depth")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Interactive.class, Motion.Ease.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            display: flex;
            align-items: baseline;
            gap: 8px;
            padding: 2px 8px;
            padding-inline-start: calc(8px + var(--fm-depth, 0) * 18px);
            white-space: nowrap;
            """;
        }
    }

    /** The row of the one that holds the keys: the current surface, the ring drawn now, the body's ink. */
    public record fm_row_holder() implements CssClass<FocusStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Current.class, Color.Surface.class), of(Current.class, Color.Edge.class), of(Focus.class, Shape.Rule.class), of(Focus.class, Color.Edge.class), of(Body.class, Color.Ink.class), of(Inline.class, Shape.Corner.class)); }
        @Override public String body() { return ""; }
    }

    /** The node's kind: root, branch (a holder of one), leaf. */
    public record fm_kind() implements CssClass<FocusStyles> {
        @Override public String body() { return "flex: none; inline-size: 5.5em; opacity: 0.7;"; }
    }

    /** The node's name. */
    public record fm_name() implements CssClass<FocusStyles> {
        @Override public String body() { return "flex: none;"; }
    }

    /** The component's class, after the name. */
    public record fm_component() implements CssClass<FocusStyles> {
        @Override public String body() { return "opacity: 0.6; overflow: hidden; text-overflow: ellipsis;"; }
    }

    /** A holder that is not in the tree, a member by id the older way, named below it. */
    public record fm_outside() implements CssClass<FocusStyles> {
        @Override public String body() { return "margin-top: 6px; padding: 2px 8px; font-style: italic;"; }
    }

    /** The steward by state, above the tree: active, and where Tab goes next; or dormant on what has the native focus. */
    public record fm_state() implements CssClass<FocusStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Caption.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "margin-bottom: 6px; padding: 2px 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"; }
    }

    /** The state line while the steward is dormant: the body's ink, so the eye sees the keys are elsewhere. */
    public record fm_state_dormant() implements CssClass<FocusStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Color.Ink.class)); }
        @Override public String body() { return "font-style: italic;"; }
    }

    @Override
    public List<CssClass<FocusStyles>> cssClasses() {
        return List.of(new fm_tree(), new fm_row(), new fm_row_holder(), new fm_kind(), new fm_name(), new fm_component(), new fm_outside(), new fm_state(), new fm_state_dormant());
    }
}
