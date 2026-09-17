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
        public record Color_Surface()   implements DesignClass<Base, Color.Surface> {}
        public record Color_Ink()       implements DesignClass<Base, Color.Ink> {}
        public record Color_Edge()      implements DesignClass<Base, Color.Edge> {}
        public record Color_Scrollbar() implements DesignClass<Base, Color.Scrollbar> {}
    }

    record Raised() implements Layer {
        public record Color_Surface() implements DesignClass<Raised, Color.Surface> {}
        public record Color_Ink()     implements DesignClass<Raised, Color.Ink> {}
        public record Color_Edge()    implements DesignClass<Raised, Color.Edge> {}
        public record Shape_Shadow()  implements DesignClass<Raised, Shape.Shadow> {}
        public record Shape_Corner()  implements DesignClass<Raised, Shape.Corner> {}
        public record Shape_Rule()    implements DesignClass<Raised, Shape.Rule> {}
    }

    record Recessed() implements Layer {
        public record Color_Surface() implements DesignClass<Recessed, Color.Surface> {}
        public record Color_Ink()     implements DesignClass<Recessed, Color.Ink> {}
        public record Color_Edge()    implements DesignClass<Recessed, Color.Edge> {}
        public record Shape_Shadow()  implements DesignClass<Recessed, Shape.Shadow> {}
        public record Shape_Corner()  implements DesignClass<Recessed, Shape.Corner> {}
    }

    record Inverted() implements Layer {
        public record Color_Surface() implements DesignClass<Inverted, Color.Surface> {}
        public record Color_Ink()     implements DesignClass<Inverted, Color.Ink> {}
        public record Color_Edge()    implements DesignClass<Inverted, Color.Edge> {}
    }

    record Overlay() implements Layer {
        public record Color_Surface() implements DesignClass<Overlay, Color.Surface> {}
        public record Color_Ink()     implements DesignClass<Overlay, Color.Ink> {}
        public record Color_Edge()    implements DesignClass<Overlay, Color.Edge> {}
        public record Shape_Shadow()  implements DesignClass<Overlay, Shape.Shadow> {}
        public record Shape_Corner()  implements DesignClass<Overlay, Shape.Corner> {}
        public record Effect_Filter() implements DesignClass<Overlay, Effect.Filter> {}
    }
}
