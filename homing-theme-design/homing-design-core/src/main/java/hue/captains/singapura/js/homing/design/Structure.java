package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.design.Target.Asset;
import hue.captains.singapura.js.homing.design.Target.Color;
import hue.captains.singapura.js.homing.design.Target.Effect;
import hue.captains.singapura.js.homing.design.Target.Shape;

/**
 * Structure — the visible lines of the page that belong to no component's
 * content. {@code Divider} is the rule between things; {@code Backdrop} is
 * the scene behind everything, when a design has one — an asset, a surface,
 * a filter, never an event handler.
 */
public interface Structure extends Semantic {

    record Divider() implements Structure {
        public record Color_Edge() implements DesignClass<Divider, Color.Edge> {}
        public record Shape_Rule() implements DesignClass<Divider, Shape.Rule> {}
    }

    record Backdrop() implements Structure {
        public record Asset_Illustration() implements DesignClass<Backdrop, Asset.Illustration> {}
        public record Color_Surface()      implements DesignClass<Backdrop, Color.Surface> {}
        public record Effect_Filter()      implements DesignClass<Backdrop, Effect.Filter> {}
    }
}
