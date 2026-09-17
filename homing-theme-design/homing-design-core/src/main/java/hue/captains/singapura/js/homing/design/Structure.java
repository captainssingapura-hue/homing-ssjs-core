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
        public record divider_color_edge() implements DesignClass<Divider, Color.Edge> {}
        public record divider_shape_rule() implements DesignClass<Divider, Shape.Rule> {}
    }

    /** The hairline: the quiet one-pixel edge between things that touch. */
    record Hairline() implements Structure {
        public record hairline_color_edge() implements DesignClass<Hairline, Color.Edge> {}
        public record hairline_shape_rule() implements DesignClass<Hairline, Shape.Rule> {}
    }

    /** The bar: an accent along the leading edge of a card, a row, a callout — the rule that says "this is one of those". */
    record Bar() implements Structure {
        public record bar_color_edge() implements DesignClass<Bar, Color.Edge> {}
        public record bar_shape_rule() implements DesignClass<Bar, Shape.Rule> {}
    }

    /** The spine: a one-pixel line along the leading edge of a column — a contents list, a nav. */
    record Spine() implements Structure {
        public record spine_color_edge() implements DesignClass<Spine, Color.Edge> {}
        public record spine_shape_rule() implements DesignClass<Spine, Shape.Rule> {}
    }

    /** The marker: a short leading-edge line that appears when its row is hovered or current, and is absent otherwise. */
    record Marker() implements Structure {
        public record marker_color_edge() implements DesignClass<Marker, Color.Edge> {}
        public record marker_shape_rule() implements DesignClass<Marker, Shape.Rule> {}
    }

    /** The cap: the quiet line above a footer, a meta row — the hairline's upper twin. */
    record Cap() implements Structure {
        public record cap_color_edge() implements DesignClass<Cap, Color.Edge> {}
        public record cap_shape_rule() implements DesignClass<Cap, Shape.Rule> {}
    }

    record Backdrop() implements Structure {
        public record backdrop_asset_illustration() implements DesignClass<Backdrop, Asset.Illustration> {}
        public record backdrop_color_surface() implements DesignClass<Backdrop, Color.Surface> {}
        public record backdrop_effect_filter() implements DesignClass<Backdrop, Effect.Filter> {}
    }
}
