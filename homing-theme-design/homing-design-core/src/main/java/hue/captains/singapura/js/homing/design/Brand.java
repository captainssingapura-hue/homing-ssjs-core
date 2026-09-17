package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.design.Target.Asset;
import hue.captains.singapura.js.homing.design.Target.Color;
import hue.captains.singapura.js.homing.design.Target.Type;

/**
 * Brand — what identifies the house. {@code Mark} is the logo, as an asset
 * and as a fill; {@code House} is the house's own face and ink, where a
 * design wants the brand set differently from the body.
 */
public interface Brand extends Semantic {

    record Mark() implements Brand {
        public record Asset_Icon()         implements DesignClass<Mark, Asset.Icon> {}
        public record Asset_Illustration() implements DesignClass<Mark, Asset.Illustration> {}
        public record Color_Fill()         implements DesignClass<Mark, Color.Fill> {}
        public record Color_Stroke()       implements DesignClass<Mark, Color.Stroke> {}
    }

    record House() implements Brand {
        public record Type_Face()      implements DesignClass<House, Type.Face> {}
        public record Type_Weight()    implements DesignClass<House, Type.Weight> {}
        public record Type_Treatment() implements DesignClass<House, Type.Treatment> {}
        public record Color_Ink()      implements DesignClass<House, Color.Ink> {}
    }
}
