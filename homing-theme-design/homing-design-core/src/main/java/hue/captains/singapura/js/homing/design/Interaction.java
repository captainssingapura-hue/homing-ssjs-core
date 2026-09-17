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
        public record Color_Surface()     implements DesignClass<Interactive, Color.Surface> {}
        public record Color_Ink()         implements DesignClass<Interactive, Color.Ink> {}
        public record Color_Edge()        implements DesignClass<Interactive, Color.Edge> {}
        public record Shape_Shadow()      implements DesignClass<Interactive, Shape.Shadow> {}
        public record Motion_Ease()       implements DesignClass<Interactive, Motion.Ease> {}
        public record Motion_Transform()  implements DesignClass<Interactive, Motion.Transform> {}
        public record Effect_Opacity()    implements DesignClass<Interactive, Effect.Opacity> {}
        public record Affordance_Cursor() implements DesignClass<Interactive, Affordance.Cursor> {}
        public record Sound_Cue()         implements DesignClass<Interactive, Sound.Cue> {}
    }

    record Selected() implements Interaction {
        public record Color_Surface() implements DesignClass<Selected, Color.Surface> {}
        public record Color_Ink()     implements DesignClass<Selected, Color.Ink> {}
        public record Color_Edge()    implements DesignClass<Selected, Color.Edge> {}
        public record Shape_Rule()    implements DesignClass<Selected, Shape.Rule> {}
        public record Type_Weight()   implements DesignClass<Selected, Type.Weight> {}
    }

    record Current() implements Interaction {
        public record Color_Surface() implements DesignClass<Current, Color.Surface> {}
        public record Color_Ink()     implements DesignClass<Current, Color.Ink> {}
        public record Color_Edge()    implements DesignClass<Current, Color.Edge> {}
        public record Shape_Rule()    implements DesignClass<Current, Shape.Rule> {}
        public record Type_Weight()   implements DesignClass<Current, Type.Weight> {}
    }

    record Focus() implements Interaction {
        public record Color_Edge()   implements DesignClass<Focus, Color.Edge> {}
        public record Shape_Rule()   implements DesignClass<Focus, Shape.Rule> {}
        public record Shape_Shadow() implements DesignClass<Focus, Shape.Shadow> {}
    }

    record Dragging() implements Interaction {
        public record Effect_Opacity()    implements DesignClass<Dragging, Effect.Opacity> {}
        public record Shape_Shadow()      implements DesignClass<Dragging, Shape.Shadow> {}
        public record Motion_Transform()  implements DesignClass<Dragging, Motion.Transform> {}
        public record Affordance_Cursor() implements DesignClass<Dragging, Affordance.Cursor> {}
    }

    record DropTarget() implements Interaction {
        public record Color_Surface()  implements DesignClass<DropTarget, Color.Surface> {}
        public record Color_Edge()     implements DesignClass<DropTarget, Color.Edge> {}
        public record Shape_Rule()     implements DesignClass<DropTarget, Shape.Rule> {}
        public record Motion_Animate() implements DesignClass<DropTarget, Motion.Animate> {}
    }
}
