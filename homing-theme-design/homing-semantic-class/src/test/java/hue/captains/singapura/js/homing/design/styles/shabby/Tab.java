package hue.captains.singapura.js.homing.design.styles.shabby;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Copy;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StatedTaxonomy;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Shabby;

/** Names its class in its CSS. */
public record Tab() implements CastingStyle<ColorScheme.Base, Shabby, StatedTaxonomy.Tab, RoleRoot> {
    @Override public StylingRes<ColorScheme.Base, StatedTaxonomy.Tab> style() {
        return new StylingRes.CssContent<>(new Copy());
    }
}
