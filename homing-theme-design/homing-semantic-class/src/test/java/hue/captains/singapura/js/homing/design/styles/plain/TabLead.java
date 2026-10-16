package hue.captains.singapura.js.homing.design.styles.plain;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.semantic.BaseScheme;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StatedTaxonomy;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;

/** Plain's lead tab: as its tab, by delegation - the same file, under the lead's own class. */
public record TabLead() implements CastingStyle<BaseScheme, Plain, StatedTaxonomy.Tab, StatedTaxonomy.Lead> {
    @Override public StylingRes<BaseScheme, StatedTaxonomy.Tab> style() {
        return new StylingRes.Delegation<>(new Tab());
    }
}
