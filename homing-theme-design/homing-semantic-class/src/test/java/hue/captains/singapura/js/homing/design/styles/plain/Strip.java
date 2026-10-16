package hue.captains.singapura.js.homing.design.styles.plain;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.semantic.BaseScheme;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Raised;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StatedTaxonomy;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;

/** Plain's strip: a raised row. */
public record Strip() implements CastingStyle<BaseScheme, Plain, StatedTaxonomy.Strip, RoleRoot> {
    @Override public StylingRes<BaseScheme, StatedTaxonomy.Strip> style() {
        return new StylingRes.CssContent<>(new Raised());
    }
}
