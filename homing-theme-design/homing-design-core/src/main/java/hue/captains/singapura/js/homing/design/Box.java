package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.design.Target.Shape;
import hue.captains.singapura.js.homing.design.Target.Size;

/**
 * Box — the kind of box an element is, for the geometry a design owns:
 * density (inset, gap, extent) and shape (corner, rule). A component's own
 * layout says where a box goes; the design says how much air it has and
 * how its corners are cut. {@code Control} is a button or a field;
 * {@code Inline} a chip, tag or badge; {@code Container} a card or panel;
 * {@code Section} a region of the page.
 */
public interface Box extends Semantic {

    record Control() implements Box {
        public record control_size_inset() implements DesignClass<Control, Size.Inset> {}
        public record control_size_gap() implements DesignClass<Control, Size.Gap> {}
        public record control_size_extent() implements DesignClass<Control, Size.Extent> {}
        public record control_shape_corner() implements DesignClass<Control, Shape.Corner> {}
        public record control_shape_rule() implements DesignClass<Control, Shape.Rule> {}
    }

    record Inline() implements Box {
        public record inline_size_inset() implements DesignClass<Inline, Size.Inset> {}
        public record inline_size_gap() implements DesignClass<Inline, Size.Gap> {}
        public record inline_shape_corner() implements DesignClass<Inline, Shape.Corner> {}
        public record inline_shape_rule() implements DesignClass<Inline, Shape.Rule> {}
    }

    record Container() implements Box {
        public record container_size_inset() implements DesignClass<Container, Size.Inset> {}
        public record container_size_gap() implements DesignClass<Container, Size.Gap> {}
        public record container_shape_corner() implements DesignClass<Container, Shape.Corner> {}
        public record container_shape_rule() implements DesignClass<Container, Shape.Rule> {}
    }

    record Section() implements Box {
        public record section_size_inset() implements DesignClass<Section, Size.Inset> {}
        public record section_size_gap() implements DesignClass<Section, Size.Gap> {}
    }
}
