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
        public record on_primary_color_ink() implements DesignClass<OnPrimary, Color.Ink> {}
        public record on_primary_color_fill() implements DesignClass<OnPrimary, Color.Fill> {}
        public record on_primary_color_stroke() implements DesignClass<OnPrimary, Color.Stroke> {}
        public record on_primary_color_edge() implements DesignClass<OnPrimary, Color.Edge> {}
    }

    record OnSecondary() implements Pairing {
        public record on_secondary_color_ink() implements DesignClass<OnSecondary, Color.Ink> {}
        public record on_secondary_color_fill() implements DesignClass<OnSecondary, Color.Fill> {}
        public record on_secondary_color_stroke() implements DesignClass<OnSecondary, Color.Stroke> {}
    }

    record OnDanger() implements Pairing {
        public record on_danger_color_ink() implements DesignClass<OnDanger, Color.Ink> {}
        public record on_danger_color_fill() implements DesignClass<OnDanger, Color.Fill> {}
        public record on_danger_color_stroke() implements DesignClass<OnDanger, Color.Stroke> {}
    }

    record OnWarning() implements Pairing {
        public record on_warning_color_ink() implements DesignClass<OnWarning, Color.Ink> {}
        public record on_warning_color_fill() implements DesignClass<OnWarning, Color.Fill> {}
        public record on_warning_color_stroke() implements DesignClass<OnWarning, Color.Stroke> {}
    }

    record OnSuccess() implements Pairing {
        public record on_success_color_ink() implements DesignClass<OnSuccess, Color.Ink> {}
        public record on_success_color_fill() implements DesignClass<OnSuccess, Color.Fill> {}
        public record on_success_color_stroke() implements DesignClass<OnSuccess, Color.Stroke> {}
    }

    record OnInfo() implements Pairing {
        public record on_info_color_ink() implements DesignClass<OnInfo, Color.Ink> {}
        public record on_info_color_fill() implements DesignClass<OnInfo, Color.Fill> {}
        public record on_info_color_stroke() implements DesignClass<OnInfo, Color.Stroke> {}
    }

    record OnInverted() implements Pairing {
        public record on_inverted_color_ink() implements DesignClass<OnInverted, Color.Ink> {}
        public record on_inverted_color_fill() implements DesignClass<OnInverted, Color.Fill> {}
        public record on_inverted_color_stroke() implements DesignClass<OnInverted, Color.Stroke> {}
        public record on_inverted_color_edge() implements DesignClass<OnInverted, Color.Edge> {}
    }

    record OnOverlay() implements Pairing {
        public record on_overlay_color_ink() implements DesignClass<OnOverlay, Color.Ink> {}
        public record on_overlay_color_fill() implements DesignClass<OnOverlay, Color.Fill> {}
    }
}
