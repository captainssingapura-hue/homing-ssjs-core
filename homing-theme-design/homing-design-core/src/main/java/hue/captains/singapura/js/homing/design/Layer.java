package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.design.Target.Color;
import hue.captains.singapura.js.homing.design.Target.Effect;
import hue.captains.singapura.js.homing.design.Target.Shape;

/**
 * Layer — what an element sits on, relative to the page. The current
 * surface tokens (base, raised, recessed, inverted) generalised to every
 * target a layer shows in: its face, its ink, its edge, its shadow, its
 * corner. {@code Overlay} is the scrim and the sheet above everything.
 */
public interface Layer extends Semantic {

    record Base() implements Layer {
        public record base_color_surface() implements DesignClass<Base, Color.Surface> {}
        public record base_color_ink() implements DesignClass<Base, Color.Ink> {}
        public record base_color_edge() implements DesignClass<Base, Color.Edge> {}
        public record base_color_scrollbar() implements DesignClass<Base, Color.Scrollbar> {}
    }

    record Raised() implements Layer {
        public record raised_color_surface() implements DesignClass<Raised, Color.Surface> {}
        public record raised_color_ink() implements DesignClass<Raised, Color.Ink> {}
        public record raised_color_edge() implements DesignClass<Raised, Color.Edge> {}
        public record raised_shape_shadow() implements DesignClass<Raised, Shape.Shadow> {}
        public record raised_shape_corner() implements DesignClass<Raised, Shape.Corner> {}
        public record raised_shape_rule() implements DesignClass<Raised, Shape.Rule> {}
    }

    record Recessed() implements Layer {
        public record recessed_color_surface() implements DesignClass<Recessed, Color.Surface> {}
        public record recessed_color_ink() implements DesignClass<Recessed, Color.Ink> {}
        public record recessed_color_edge() implements DesignClass<Recessed, Color.Edge> {}
        public record recessed_shape_shadow() implements DesignClass<Recessed, Shape.Shadow> {}
        public record recessed_shape_corner() implements DesignClass<Recessed, Shape.Corner> {}
    }

    record Inverted() implements Layer {
        public record inverted_color_surface() implements DesignClass<Inverted, Color.Surface> {}
        public record inverted_color_ink() implements DesignClass<Inverted, Color.Ink> {}
        public record inverted_color_edge() implements DesignClass<Inverted, Color.Edge> {}
    }

    record Overlay() implements Layer {
        public record overlay_color_surface() implements DesignClass<Overlay, Color.Surface> {}
        public record overlay_color_ink() implements DesignClass<Overlay, Color.Ink> {}
        public record overlay_color_edge() implements DesignClass<Overlay, Color.Edge> {}
        public record overlay_shape_shadow() implements DesignClass<Overlay, Shape.Shadow> {}
        public record overlay_shape_corner() implements DesignClass<Overlay, Shape.Corner> {}
        public record overlay_effect_filter() implements DesignClass<Overlay, Effect.Filter> {}
    }
}
