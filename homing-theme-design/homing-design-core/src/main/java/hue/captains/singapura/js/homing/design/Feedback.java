package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.design.Target.Asset;
import hue.captains.singapura.js.homing.design.Target.Color;
import hue.captains.singapura.js.homing.design.Target.Motion;
import hue.captains.singapura.js.homing.design.Target.Sound;

/**
 * Feedback — what the system is telling the user about an outcome. The
 * branch a product most often extends: fin-dash adds {@code Up} and
 * {@code Down} here, and a design that fulfils the branch covers them.
 */
public interface Feedback extends Semantic {

    record Danger() implements Feedback {
        public record danger_color_surface() implements DesignClass<Danger, Color.Surface> {}
        public record danger_color_ink() implements DesignClass<Danger, Color.Ink> {}
        public record danger_color_edge() implements DesignClass<Danger, Color.Edge> {}
        public record danger_color_fill() implements DesignClass<Danger, Color.Fill> {}
        public record danger_color_stroke() implements DesignClass<Danger, Color.Stroke> {}
        public record danger_motion_animate() implements DesignClass<Danger, Motion.Animate> {}
        public record danger_asset_icon() implements DesignClass<Danger, Asset.Icon> {}
        public record danger_sound_cue() implements DesignClass<Danger, Sound.Cue> {}
    }

    record Warning() implements Feedback {
        public record warning_color_surface() implements DesignClass<Warning, Color.Surface> {}
        public record warning_color_ink() implements DesignClass<Warning, Color.Ink> {}
        public record warning_color_edge() implements DesignClass<Warning, Color.Edge> {}
        public record warning_color_fill() implements DesignClass<Warning, Color.Fill> {}
        public record warning_color_stroke() implements DesignClass<Warning, Color.Stroke> {}
        public record warning_asset_icon() implements DesignClass<Warning, Asset.Icon> {}
        public record warning_sound_cue() implements DesignClass<Warning, Sound.Cue> {}
    }

    record Success() implements Feedback {
        public record success_color_surface() implements DesignClass<Success, Color.Surface> {}
        public record success_color_ink() implements DesignClass<Success, Color.Ink> {}
        public record success_color_edge() implements DesignClass<Success, Color.Edge> {}
        public record success_color_fill() implements DesignClass<Success, Color.Fill> {}
        public record success_color_stroke() implements DesignClass<Success, Color.Stroke> {}
        public record success_asset_icon() implements DesignClass<Success, Asset.Icon> {}
        public record success_sound_cue() implements DesignClass<Success, Sound.Cue> {}
    }

    record Info() implements Feedback {
        public record info_color_surface() implements DesignClass<Info, Color.Surface> {}
        public record info_color_ink() implements DesignClass<Info, Color.Ink> {}
        public record info_color_edge() implements DesignClass<Info, Color.Edge> {}
        public record info_color_fill() implements DesignClass<Info, Color.Fill> {}
        public record info_color_stroke() implements DesignClass<Info, Color.Stroke> {}
        public record info_asset_icon() implements DesignClass<Info, Asset.Icon> {}
        public record info_sound_cue() implements DesignClass<Info, Sound.Cue> {}
    }
}
