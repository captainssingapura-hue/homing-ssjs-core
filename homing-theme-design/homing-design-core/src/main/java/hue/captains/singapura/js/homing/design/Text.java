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

    /** The eyebrow: a short tracked label above or beside a title, set small and loud. */
    record Kicker() implements Text {
        public record kicker_type_face() implements DesignClass<Kicker, Type.Face> {}
        public record kicker_type_weight() implements DesignClass<Kicker, Type.Weight> {}
        public record kicker_type_scale() implements DesignClass<Kicker, Type.Scale> {}
        public record kicker_type_treatment() implements DesignClass<Kicker, Type.Treatment> {}
        public record kicker_color_ink() implements DesignClass<Kicker, Color.Ink> {}
    }

    /**
     * Rendered prose — markdown, with no class on any element inside it. The one
     * place a design writes a body with element selectors nested: the headings,
     * links, code and tables of a document are the design's to set, per element,
     * inside the one class the document wears.
     */
    record Prose() implements Text {
        public record prose_type_face() implements DesignClass<Prose, Type.Face> {}
        public record prose_type_scale() implements DesignClass<Prose, Type.Scale> {}
        public record prose_type_weight() implements DesignClass<Prose, Type.Weight> {}
        public record prose_type_treatment() implements DesignClass<Prose, Type.Treatment> {}
        public record prose_type_decoration() implements DesignClass<Prose, Type.Decoration> {}
        public record prose_color_ink() implements DesignClass<Prose, Color.Ink> {}
        public record prose_color_surface() implements DesignClass<Prose, Color.Surface> {}
        public record prose_color_edge() implements DesignClass<Prose, Color.Edge> {}
        public record prose_shape_rule() implements DesignClass<Prose, Shape.Rule> {}
        public record prose_shape_corner() implements DesignClass<Prose, Shape.Corner> {}
    }

    /** The lede: the paragraph under a title, set a little larger and a little quieter. */
    record Lede() implements Text {
        public record lede_type_scale() implements DesignClass<Lede, Type.Scale> {}
        public record lede_type_treatment() implements DesignClass<Lede, Type.Treatment> {}
        public record lede_color_ink() implements DesignClass<Lede, Color.Ink> {}
    }

    /** A figure that is read at a glance: a percentage, a count, a glyph — set large in the display face. */
    record Numeral() implements Text {
        public record numeral_type_face() implements DesignClass<Numeral, Type.Face> {}
        public record numeral_type_weight() implements DesignClass<Numeral, Type.Weight> {}
        public record numeral_type_scale() implements DesignClass<Numeral, Type.Scale> {}
        public record numeral_type_treatment() implements DesignClass<Numeral, Type.Treatment> {}
        public record numeral_color_ink() implements DesignClass<Numeral, Color.Ink> {}
    }

    record Link() implements Text {
        public record link_type_weight() implements DesignClass<Link, Type.Weight> {}
        public record link_type_decoration() implements DesignClass<Link, Type.Decoration> {}
        public record link_color_ink() implements DesignClass<Link, Color.Ink> {}
        public record link_motion_ease() implements DesignClass<Link, Motion.Ease> {}
    }
}
