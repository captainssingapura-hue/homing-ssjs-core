package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.design.Target.Color;
import hue.captains.singapura.js.homing.design.Target.Effect;
import hue.captains.singapura.js.homing.design.Target.Type;

/**
 * Emphasis — how much an element asks for attention relative to its
 * neighbours. {@code Primary} is what the current tokens call the accent;
 * {@code Muted} is what they call muted text, generalised to any target.
 */
public interface Emphasis extends Semantic {

    record Primary() implements Emphasis {
        public record Color_Surface() implements DesignClass<Primary, Color.Surface> {}
        public record Color_Ink()     implements DesignClass<Primary, Color.Ink> {}
        public record Color_Edge()    implements DesignClass<Primary, Color.Edge> {}
        public record Color_Fill()    implements DesignClass<Primary, Color.Fill> {}
        public record Color_Stroke()  implements DesignClass<Primary, Color.Stroke> {}
        public record Type_Weight()   implements DesignClass<Primary, Type.Weight> {}
    }

    record Secondary() implements Emphasis {
        public record Color_Surface() implements DesignClass<Secondary, Color.Surface> {}
        public record Color_Ink()     implements DesignClass<Secondary, Color.Ink> {}
        public record Color_Edge()    implements DesignClass<Secondary, Color.Edge> {}
        public record Color_Fill()    implements DesignClass<Secondary, Color.Fill> {}
        public record Type_Weight()   implements DesignClass<Secondary, Type.Weight> {}
    }

    record Tertiary() implements Emphasis {
        public record Color_Surface() implements DesignClass<Tertiary, Color.Surface> {}
        public record Color_Ink()     implements DesignClass<Tertiary, Color.Ink> {}
        public record Color_Edge()    implements DesignClass<Tertiary, Color.Edge> {}
        public record Type_Weight()   implements DesignClass<Tertiary, Type.Weight> {}
    }

    record Muted() implements Emphasis {
        public record Color_Ink()      implements DesignClass<Muted, Color.Ink> {}
        public record Color_Fill()     implements DesignClass<Muted, Color.Fill> {}
        public record Color_Stroke()   implements DesignClass<Muted, Color.Stroke> {}
        public record Color_Edge()     implements DesignClass<Muted, Color.Edge> {}
        public record Effect_Opacity() implements DesignClass<Muted, Effect.Opacity> {}
    }
}
