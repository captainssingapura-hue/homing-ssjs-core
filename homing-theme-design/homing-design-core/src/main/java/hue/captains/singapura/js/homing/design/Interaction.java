package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.design.Target.Affordance;
import hue.captains.singapura.js.homing.design.Target.Color;
import hue.captains.singapura.js.homing.design.Target.Effect;
import hue.captains.singapura.js.homing.design.Target.Motion;
import hue.captains.singapura.js.homing.design.Target.Shape;
import hue.captains.singapura.js.homing.design.Target.Sound;
import hue.captains.singapura.js.homing.design.Target.Type;

/**
 * Interaction — what an element affords or is currently doing with the
 * user. {@code Interactive} is "the user may act on this"; its states
 * (hover, active, disabled) are the template's, and a design binds the
 * motion, the cursor and the cue that make the affordance perceivable.
 * {@code Selected} and {@code Current} are the two kinds of "this one";
 * {@code Focus} is the ring; {@code Dragging} and {@code DropTarget} are the
 * two ends of a drag.
 */
public interface Interaction extends Semantic {

    record Interactive() implements Interaction {
        public record interactive_color_surface() implements DesignClass<Interactive, Color.Surface> {}
        public record interactive_color_ink() implements DesignClass<Interactive, Color.Ink> {}
        public record interactive_color_edge() implements DesignClass<Interactive, Color.Edge> {}
        public record interactive_shape_shadow() implements DesignClass<Interactive, Shape.Shadow> {}
        public record interactive_motion_ease() implements DesignClass<Interactive, Motion.Ease> {}
        public record interactive_motion_transform() implements DesignClass<Interactive, Motion.Transform> {}
        public record interactive_effect_opacity() implements DesignClass<Interactive, Effect.Opacity> {}
        public record interactive_affordance_cursor() implements DesignClass<Interactive, Affordance.Cursor> {}
        public record interactive_sound_cue() implements DesignClass<Interactive, Sound.Cue> {}
    }

    record Selected() implements Interaction {
        public record selected_color_surface() implements DesignClass<Selected, Color.Surface> {}
        public record selected_color_ink() implements DesignClass<Selected, Color.Ink> {}
        public record selected_color_edge() implements DesignClass<Selected, Color.Edge> {}
        public record selected_shape_rule() implements DesignClass<Selected, Shape.Rule> {}
        public record selected_type_weight() implements DesignClass<Selected, Type.Weight> {}
    }

    record Current() implements Interaction {
        public record current_color_surface() implements DesignClass<Current, Color.Surface> {}
        public record current_color_ink() implements DesignClass<Current, Color.Ink> {}
        public record current_color_edge() implements DesignClass<Current, Color.Edge> {}
        public record current_shape_rule() implements DesignClass<Current, Shape.Rule> {}
        public record current_type_weight() implements DesignClass<Current, Type.Weight> {}
        public record current_shape_shadow() implements DesignClass<Current, Shape.Shadow> {}
    }

    record Focus() implements Interaction {
        public record focus_color_edge() implements DesignClass<Focus, Color.Edge> {}
        public record focus_shape_rule() implements DesignClass<Focus, Shape.Rule> {}
        public record focus_shape_shadow() implements DesignClass<Focus, Shape.Shadow> {}
    }

    record Dragging() implements Interaction {
        public record dragging_effect_opacity() implements DesignClass<Dragging, Effect.Opacity> {}
        public record dragging_shape_shadow() implements DesignClass<Dragging, Shape.Shadow> {}
        public record dragging_motion_transform() implements DesignClass<Dragging, Motion.Transform> {}
        public record dragging_affordance_cursor() implements DesignClass<Dragging, Affordance.Cursor> {}
    }

    record DropTarget() implements Interaction {
        public record drop_target_color_surface() implements DesignClass<DropTarget, Color.Surface> {}
        public record drop_target_color_edge() implements DesignClass<DropTarget, Color.Edge> {}
        public record drop_target_shape_rule() implements DesignClass<DropTarget, Shape.Rule> {}
        public record drop_target_motion_animate() implements DesignClass<DropTarget, Motion.Animate> {}
    }
}
