package hue.captains.singapura.js.homing.design.styles.plain;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Raised;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StatedTaxonomy;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;

/** Plain's strip: a raised row. */
public record Strip() implements CastingStyle<ColorScheme.Base, Plain, StatedTaxonomy.Strip, RoleRoot> {
    @Override public StylingRes<ColorScheme.Base, StatedTaxonomy.Strip> style() {
        return new StylingRes.CssContent<>(new Raised());
    }
}
