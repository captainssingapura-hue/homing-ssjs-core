package hue.captains.singapura.js.homing.design.styles.plain;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.semantic.BaseScheme;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Flag;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Page;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StyledExtension;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;

/** Plain's badge, the extension's leaf: a flag on the page. */
public record Badge() implements CastingStyle<BaseScheme, Plain, StyledExtension.Badge, RoleRoot> {
    @Override public StylingRes<BaseScheme, StyledExtension.Badge> style() {
        return new StylingRes.CssContent<>(new Flag(), new Page());
    }
}
