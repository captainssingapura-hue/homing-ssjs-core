package hue.captains.singapura.js.homing.design.styles.plain;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.semantic.BaseScheme;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Alarm;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Risk;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Safe;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Warning;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;
import hue.captains.singapura.js.homing.design.styling.StyledTaxonomy;

/** Plain's meter, on the colour axis: its ground along the risk scale, its edge the warning, its mark the alarm; sweeping up its scale. */
public record Meter() implements CastingStyle<BaseScheme, Plain, StyledTaxonomy.Meter, RoleRoot> {
    @Override public StylingRes<BaseScheme, StyledTaxonomy.Meter> style() {
        return new StylingRes.CssContent<>(new Risk(), new Warning(), new Alarm(), new Safe());
    }
}
