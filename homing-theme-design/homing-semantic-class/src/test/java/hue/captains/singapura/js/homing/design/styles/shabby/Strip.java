package hue.captains.singapura.js.homing.design.styles.shabby;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Copy;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Raised;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StatedTaxonomy;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Shabby;

/** Imports a colour its CSS never uses, and uses one it never imports - its marks right. */
public record Strip() implements CastingStyle<ColorScheme.Base, Shabby, StatedTaxonomy.Strip, RoleRoot> {
    @Override public StylingRes<ColorScheme.Base, StatedTaxonomy.Strip> style() {
        return new StylingRes.CssContent<>(new Raised(), new Copy());
    }
}
