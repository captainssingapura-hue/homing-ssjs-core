package hue.captains.singapura.js.homing.design.styles.shabby;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.semantic.BaseScheme;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Safe;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Shabby;
import hue.captains.singapura.js.homing.design.styling.StyledTaxonomy;

/** Answers CssContent, with no CSS in its jar. */
public record Meter() implements CastingStyle<BaseScheme, Shabby, StyledTaxonomy.Meter, RoleRoot> {
    @Override public StylingRes<BaseScheme, StyledTaxonomy.Meter> style() {
        return new StylingRes.CssContent<>(new Safe());
    }
}
