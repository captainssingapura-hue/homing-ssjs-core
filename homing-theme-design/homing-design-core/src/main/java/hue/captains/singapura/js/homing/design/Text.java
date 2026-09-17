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
        public record Type_Face()      implements DesignClass<Body, Type.Face> {}
        public record Type_Weight()    implements DesignClass<Body, Type.Weight> {}
        public record Type_Scale()     implements DesignClass<Body, Type.Scale> {}
        public record Type_Treatment() implements DesignClass<Body, Type.Treatment> {}
        public record Color_Ink()      implements DesignClass<Body, Color.Ink> {}
    }

    record Heading() implements Text {
        public record Type_Face()      implements DesignClass<Heading, Type.Face> {}
        public record Type_Weight()    implements DesignClass<Heading, Type.Weight> {}
        public record Type_Scale()     implements DesignClass<Heading, Type.Scale> {}
        public record Type_Treatment() implements DesignClass<Heading, Type.Treatment> {}
        public record Color_Ink()      implements DesignClass<Heading, Color.Ink> {}
    }

    record Display() implements Text {
        public record Type_Face()      implements DesignClass<Display, Type.Face> {}
        public record Type_Weight()    implements DesignClass<Display, Type.Weight> {}
        public record Type_Scale()     implements DesignClass<Display, Type.Scale> {}
        public record Type_Treatment() implements DesignClass<Display, Type.Treatment> {}
        public record Color_Ink()      implements DesignClass<Display, Color.Ink> {}
    }

    record Caption() implements Text {
        public record Type_Face()      implements DesignClass<Caption, Type.Face> {}
        public record Type_Weight()    implements DesignClass<Caption, Type.Weight> {}
        public record Type_Scale()     implements DesignClass<Caption, Type.Scale> {}
        public record Type_Treatment() implements DesignClass<Caption, Type.Treatment> {}
        public record Color_Ink()      implements DesignClass<Caption, Color.Ink> {}
    }

    record Label() implements Text {
        public record Type_Face()      implements DesignClass<Label, Type.Face> {}
        public record Type_Weight()    implements DesignClass<Label, Type.Weight> {}
        public record Type_Scale()     implements DesignClass<Label, Type.Scale> {}
        public record Type_Treatment() implements DesignClass<Label, Type.Treatment> {}
        public record Color_Ink()      implements DesignClass<Label, Color.Ink> {}
    }

    record Code() implements Text {
        public record Type_Face()      implements DesignClass<Code, Type.Face> {}
        public record Type_Weight()    implements DesignClass<Code, Type.Weight> {}
        public record Type_Scale()     implements DesignClass<Code, Type.Scale> {}
        public record Type_Treatment() implements DesignClass<Code, Type.Treatment> {}
        public record Color_Ink()      implements DesignClass<Code, Color.Ink> {}
        public record Color_Surface()  implements DesignClass<Code, Color.Surface> {}
        public record Shape_Corner()   implements DesignClass<Code, Shape.Corner> {}
        public record Size_Inset()     implements DesignClass<Code, Size.Inset> {}
    }

    record Link() implements Text {
        public record Type_Weight()      implements DesignClass<Link, Type.Weight> {}
        public record Type_Decoration()  implements DesignClass<Link, Type.Decoration> {}
        public record Color_Ink()        implements DesignClass<Link, Color.Ink> {}
        public record Motion_Ease()      implements DesignClass<Link, Motion.Ease> {}
    }
}
