package hue.captains.singapura.js.homing.design.styles.loudly;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.BaseColours;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Glare;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Page;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Loudly;
import hue.captains.singapura.js.homing.design.styling.StyledTaxonomy;

/** Loudly's meter: the glare its scheme adds, on the page. */
public record Meter() implements CastingStyle<BaseColours.LoudScheme, Loudly, StyledTaxonomy.Meter, RoleRoot> {
    @Override public StylingRes<BaseColours.LoudScheme, StyledTaxonomy.Meter> style() {
        return new StylingRes.CssContent<>(new Glare(), new Page());
    }
}
