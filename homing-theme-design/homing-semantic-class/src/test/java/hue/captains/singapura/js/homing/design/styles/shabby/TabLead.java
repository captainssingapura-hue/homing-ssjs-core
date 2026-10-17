package hue.captains.singapura.js.homing.design.styles.shabby;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.semantic.BaseScheme;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StatedTaxonomy;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Shabby;

/** Delegates to itself. */
public record TabLead() implements CastingStyle<BaseScheme, Shabby, StatedTaxonomy.Tab, StatedTaxonomy.Lead> {
    @Override public StylingRes<BaseScheme, StatedTaxonomy.Tab> style() {
        return new StylingRes.Delegation<>(new TabLead());
    }
}
