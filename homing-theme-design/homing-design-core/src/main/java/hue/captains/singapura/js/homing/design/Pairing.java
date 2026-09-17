package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.design.Target.Color;

/**
 * Pairing — ink that sits <i>on</i> a strongly coloured surface. The
 * component knows which surface it put the text on, so the pairing is the
 * component's to state; the design guarantees the contrast. Only the
 * surfaces that are strongly coloured need a pairing; ink on a layer is
 * {@link Text} or {@link Emphasis}.
 */
public interface Pairing extends Semantic {

    record OnPrimary() implements Pairing {
        public record Color_Ink()    implements DesignClass<OnPrimary, Color.Ink> {}
        public record Color_Fill()   implements DesignClass<OnPrimary, Color.Fill> {}
        public record Color_Stroke() implements DesignClass<OnPrimary, Color.Stroke> {}
        public record Color_Edge()   implements DesignClass<OnPrimary, Color.Edge> {}
    }

    record OnSecondary() implements Pairing {
        public record Color_Ink()    implements DesignClass<OnSecondary, Color.Ink> {}
        public record Color_Fill()   implements DesignClass<OnSecondary, Color.Fill> {}
        public record Color_Stroke() implements DesignClass<OnSecondary, Color.Stroke> {}
    }

    record OnDanger() implements Pairing {
        public record Color_Ink()    implements DesignClass<OnDanger, Color.Ink> {}
        public record Color_Fill()   implements DesignClass<OnDanger, Color.Fill> {}
        public record Color_Stroke() implements DesignClass<OnDanger, Color.Stroke> {}
    }

    record OnWarning() implements Pairing {
        public record Color_Ink()    implements DesignClass<OnWarning, Color.Ink> {}
        public record Color_Fill()   implements DesignClass<OnWarning, Color.Fill> {}
        public record Color_Stroke() implements DesignClass<OnWarning, Color.Stroke> {}
    }

    record OnSuccess() implements Pairing {
        public record Color_Ink()    implements DesignClass<OnSuccess, Color.Ink> {}
        public record Color_Fill()   implements DesignClass<OnSuccess, Color.Fill> {}
        public record Color_Stroke() implements DesignClass<OnSuccess, Color.Stroke> {}
    }

    record OnInfo() implements Pairing {
        public record Color_Ink()    implements DesignClass<OnInfo, Color.Ink> {}
        public record Color_Fill()   implements DesignClass<OnInfo, Color.Fill> {}
        public record Color_Stroke() implements DesignClass<OnInfo, Color.Stroke> {}
    }

    record OnInverted() implements Pairing {
        public record Color_Ink()    implements DesignClass<OnInverted, Color.Ink> {}
        public record Color_Fill()   implements DesignClass<OnInverted, Color.Fill> {}
        public record Color_Stroke() implements DesignClass<OnInverted, Color.Stroke> {}
        public record Color_Edge()   implements DesignClass<OnInverted, Color.Edge> {}
    }

    record OnOverlay() implements Pairing {
        public record Color_Ink()    implements DesignClass<OnOverlay, Color.Ink> {}
        public record Color_Fill()   implements DesignClass<OnOverlay, Color.Fill> {}
    }
}
