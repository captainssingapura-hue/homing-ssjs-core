package hue.captains.singapura.js.homing.design.styles.loudly;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Glare;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Page;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Loudly;
import hue.captains.singapura.js.homing.design.styling.StyledTaxonomy;

/** Loudly's meter: the glare its scheme adds, on the page. */
public record Meter() implements CastingStyle<ResolvingColours.Loud, Loudly, StyledTaxonomy.Meter, RoleRoot> {
    @Override public StylingRes<ResolvingColours.Loud, StyledTaxonomy.Meter> style() {
        return new StylingRes.CssContent<>(new Glare(), new Page());
    }
}
