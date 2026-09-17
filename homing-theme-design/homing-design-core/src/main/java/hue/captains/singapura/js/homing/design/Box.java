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
        public record Size_Inset()   implements DesignClass<Control, Size.Inset> {}
        public record Size_Gap()     implements DesignClass<Control, Size.Gap> {}
        public record Size_Extent()  implements DesignClass<Control, Size.Extent> {}
        public record Shape_Corner() implements DesignClass<Control, Shape.Corner> {}
        public record Shape_Rule()   implements DesignClass<Control, Shape.Rule> {}
    }

    record Inline() implements Box {
        public record Size_Inset()   implements DesignClass<Inline, Size.Inset> {}
        public record Size_Gap()     implements DesignClass<Inline, Size.Gap> {}
        public record Shape_Corner() implements DesignClass<Inline, Shape.Corner> {}
        public record Shape_Rule()   implements DesignClass<Inline, Shape.Rule> {}
    }

    record Container() implements Box {
        public record Size_Inset()   implements DesignClass<Container, Size.Inset> {}
        public record Size_Gap()     implements DesignClass<Container, Size.Gap> {}
        public record Shape_Corner() implements DesignClass<Container, Shape.Corner> {}
        public record Shape_Rule()   implements DesignClass<Container, Shape.Rule> {}
    }

    record Section() implements Box {
        public record Size_Inset() implements DesignClass<Section, Size.Inset> {}
        public record Size_Gap()   implements DesignClass<Section, Size.Gap> {}
    }
}
