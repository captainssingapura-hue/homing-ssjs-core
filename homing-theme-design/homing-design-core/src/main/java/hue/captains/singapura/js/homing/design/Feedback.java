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
        public record Color_Surface() implements DesignClass<Danger, Color.Surface> {}
        public record Color_Ink()     implements DesignClass<Danger, Color.Ink> {}
        public record Color_Edge()    implements DesignClass<Danger, Color.Edge> {}
        public record Color_Fill()    implements DesignClass<Danger, Color.Fill> {}
        public record Color_Stroke()  implements DesignClass<Danger, Color.Stroke> {}
        public record Motion_Animate() implements DesignClass<Danger, Motion.Animate> {}
        public record Asset_Icon()    implements DesignClass<Danger, Asset.Icon> {}
        public record Sound_Cue()     implements DesignClass<Danger, Sound.Cue> {}
    }

    record Warning() implements Feedback {
        public record Color_Surface() implements DesignClass<Warning, Color.Surface> {}
        public record Color_Ink()     implements DesignClass<Warning, Color.Ink> {}
        public record Color_Edge()    implements DesignClass<Warning, Color.Edge> {}
        public record Color_Fill()    implements DesignClass<Warning, Color.Fill> {}
        public record Color_Stroke()  implements DesignClass<Warning, Color.Stroke> {}
        public record Asset_Icon()    implements DesignClass<Warning, Asset.Icon> {}
        public record Sound_Cue()     implements DesignClass<Warning, Sound.Cue> {}
    }

    record Success() implements Feedback {
        public record Color_Surface() implements DesignClass<Success, Color.Surface> {}
        public record Color_Ink()     implements DesignClass<Success, Color.Ink> {}
        public record Color_Edge()    implements DesignClass<Success, Color.Edge> {}
        public record Color_Fill()    implements DesignClass<Success, Color.Fill> {}
        public record Color_Stroke()  implements DesignClass<Success, Color.Stroke> {}
        public record Asset_Icon()    implements DesignClass<Success, Asset.Icon> {}
        public record Sound_Cue()     implements DesignClass<Success, Sound.Cue> {}
    }

    record Info() implements Feedback {
        public record Color_Surface() implements DesignClass<Info, Color.Surface> {}
        public record Color_Ink()     implements DesignClass<Info, Color.Ink> {}
        public record Color_Edge()    implements DesignClass<Info, Color.Edge> {}
        public record Color_Fill()    implements DesignClass<Info, Color.Fill> {}
        public record Color_Stroke()  implements DesignClass<Info, Color.Stroke> {}
        public record Asset_Icon()    implements DesignClass<Info, Asset.Icon> {}
        public record Sound_Cue()     implements DesignClass<Info, Sound.Cue> {}
    }
}
