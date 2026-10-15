package hue.captains.singapura.js.homing.design.styles.shabby;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StatedTaxonomy;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Shabby;

/** Delegates to itself. */
public record TabLead() implements CastingStyle<ColorScheme.Base, Shabby, StatedTaxonomy.Tab, StatedTaxonomy.Lead> {
    @Override public StylingRes<ColorScheme.Base, StatedTaxonomy.Tab> style() {
        return new StylingRes.Delegation<>(new TabLead());
    }
}
