package hue.captains.singapura.js.homing.design.styles.shabby;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StyledExtension;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Shabby;

/** Delegates, with a CSS file of its own beside it. */
public record Badge() implements CastingStyle<ColorScheme.Base, Shabby, StyledExtension.Badge, RoleRoot> {
    @Override public StylingRes<ColorScheme.Base, StyledExtension.Badge> style() {
        return new StylingRes.Delegation<>(new hue.captains.singapura.js.homing.design.styles.plain.Badge());
    }
}
