package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.design.Target.Color;
import hue.captains.singapura.js.homing.design.Target.Effect;
import hue.captains.singapura.js.homing.design.Target.Motion;
import hue.captains.singapura.js.homing.design.Target.Type;

/**
 * Emphasis — how much an element asks for attention relative to its
 * neighbours. {@code Primary} is what the current tokens call the accent;
 * {@code Muted} is what they call muted text, generalised to any target.
 */
public interface Emphasis extends Semantic {

    record Primary() implements Emphasis {
        public record primary_color_surface() implements DesignClass<Primary, Color.Surface> {}
        public record primary_color_ink() implements DesignClass<Primary, Color.Ink> {}
        public record primary_color_edge() implements DesignClass<Primary, Color.Edge> {}
        public record primary_color_fill() implements DesignClass<Primary, Color.Fill> {}
        public record primary_color_stroke() implements DesignClass<Primary, Color.Stroke> {}
        public record primary_type_weight() implements DesignClass<Primary, Type.Weight> {}
        public record primary_motion_ease() implements DesignClass<Primary, Motion.Ease> {}
    }

    record Secondary() implements Emphasis {
        public record secondary_color_surface() implements DesignClass<Secondary, Color.Surface> {}
        public record secondary_color_ink() implements DesignClass<Secondary, Color.Ink> {}
        public record secondary_color_edge() implements DesignClass<Secondary, Color.Edge> {}
        public record secondary_color_fill() implements DesignClass<Secondary, Color.Fill> {}
        public record secondary_type_weight() implements DesignClass<Secondary, Type.Weight> {}
    }

    record Tertiary() implements Emphasis {
        public record tertiary_color_surface() implements DesignClass<Tertiary, Color.Surface> {}
        public record tertiary_color_ink() implements DesignClass<Tertiary, Color.Ink> {}
        public record tertiary_color_edge() implements DesignClass<Tertiary, Color.Edge> {}
        public record tertiary_type_weight() implements DesignClass<Tertiary, Type.Weight> {}
    }

    record Muted() implements Emphasis {
        public record muted_color_ink() implements DesignClass<Muted, Color.Ink> {}
        public record muted_color_fill() implements DesignClass<Muted, Color.Fill> {}
        public record muted_color_stroke() implements DesignClass<Muted, Color.Stroke> {}
        public record muted_color_edge() implements DesignClass<Muted, Color.Edge> {}
        public record muted_effect_opacity() implements DesignClass<Muted, Effect.Opacity> {}
        public record muted_type_decoration() implements DesignClass<Muted, Type.Decoration> {}
    }
}
