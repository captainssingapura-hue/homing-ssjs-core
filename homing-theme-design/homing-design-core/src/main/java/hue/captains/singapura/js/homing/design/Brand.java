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
        public record mark_asset_icon() implements DesignClass<Mark, Asset.Icon> {}
        public record mark_asset_illustration() implements DesignClass<Mark, Asset.Illustration> {}
        public record mark_color_fill() implements DesignClass<Mark, Color.Fill> {}
        public record mark_color_stroke() implements DesignClass<Mark, Color.Stroke> {}
    }

    record House() implements Brand {
        public record house_type_face() implements DesignClass<House, Type.Face> {}
        public record house_type_weight() implements DesignClass<House, Type.Weight> {}
        public record house_type_treatment() implements DesignClass<House, Type.Treatment> {}
        public record house_type_scale() implements DesignClass<House, Type.Scale> {}
        public record house_color_ink() implements DesignClass<House, Color.Ink> {}
    }
}
