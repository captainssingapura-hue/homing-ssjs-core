package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.design.Target.Color;
import hue.captains.singapura.js.homing.design.Target.Motion;
import hue.captains.singapura.js.homing.design.Target.Shape;
import hue.captains.singapura.js.homing.design.Target.Size;
import hue.captains.singapura.js.homing.design.Target.Type;

/**
 * Text — the kinds of writing on a page. Each is set in a face, a weight, a
 * scale and a treatment, and inked; {@code Link} is also decorated and
 * eases; {@code Code} also sits on a surface with a corner and an inset.
 */
public interface Text extends Semantic {

    record Body() implements Text {
        public record body_type_face() implements DesignClass<Body, Type.Face> {}
        public record body_type_weight() implements DesignClass<Body, Type.Weight> {}
        public record body_type_scale() implements DesignClass<Body, Type.Scale> {}
        public record body_type_treatment() implements DesignClass<Body, Type.Treatment> {}
        public record body_color_ink() implements DesignClass<Body, Color.Ink> {}
    }

    record Heading() implements Text {
        public record heading_type_face() implements DesignClass<Heading, Type.Face> {}
        public record heading_type_weight() implements DesignClass<Heading, Type.Weight> {}
        public record heading_type_scale() implements DesignClass<Heading, Type.Scale> {}
        public record heading_type_treatment() implements DesignClass<Heading, Type.Treatment> {}
        public record heading_color_ink() implements DesignClass<Heading, Color.Ink> {}
    }

    record Display() implements Text {
        public record display_type_face() implements DesignClass<Display, Type.Face> {}
        public record display_type_weight() implements DesignClass<Display, Type.Weight> {}
        public record display_type_scale() implements DesignClass<Display, Type.Scale> {}
        public record display_type_treatment() implements DesignClass<Display, Type.Treatment> {}
        public record display_color_ink() implements DesignClass<Display, Color.Ink> {}
    }

    record Caption() implements Text {
        public record caption_type_face() implements DesignClass<Caption, Type.Face> {}
        public record caption_type_weight() implements DesignClass<Caption, Type.Weight> {}
        public record caption_type_scale() implements DesignClass<Caption, Type.Scale> {}
        public record caption_type_treatment() implements DesignClass<Caption, Type.Treatment> {}
        public record caption_color_ink() implements DesignClass<Caption, Color.Ink> {}
    }

    record Label() implements Text {
        public record label_type_face() implements DesignClass<Label, Type.Face> {}
        public record label_type_weight() implements DesignClass<Label, Type.Weight> {}
        public record label_type_scale() implements DesignClass<Label, Type.Scale> {}
        public record label_type_treatment() implements DesignClass<Label, Type.Treatment> {}
        public record label_color_ink() implements DesignClass<Label, Color.Ink> {}
    }

    record Code() implements Text {
        public record code_type_face() implements DesignClass<Code, Type.Face> {}
        public record code_type_weight() implements DesignClass<Code, Type.Weight> {}
        public record code_type_scale() implements DesignClass<Code, Type.Scale> {}
        public record code_type_treatment() implements DesignClass<Code, Type.Treatment> {}
        public record code_color_ink() implements DesignClass<Code, Color.Ink> {}
        public record code_color_surface() implements DesignClass<Code, Color.Surface> {}
        public record code_shape_corner() implements DesignClass<Code, Shape.Corner> {}
        public record code_size_inset() implements DesignClass<Code, Size.Inset> {}
    }

    record Link() implements Text {
        public record link_type_weight() implements DesignClass<Link, Type.Weight> {}
        public record link_type_decoration() implements DesignClass<Link, Type.Decoration> {}
        public record link_color_ink() implements DesignClass<Link, Color.Ink> {}
        public record link_motion_ease() implements DesignClass<Link, Motion.Ease> {}
    }
}
