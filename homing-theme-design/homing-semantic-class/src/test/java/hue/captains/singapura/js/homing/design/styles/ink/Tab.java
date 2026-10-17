package hue.captains.singapura.js.homing.design.styles.ink;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.semantic.BaseScheme;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Copy;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Danger;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Raised;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StatedTaxonomy;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Ink;

/** Ink's tab, whole: copy on the raised, danger when picked. */
public record Tab() implements CastingStyle<BaseScheme, Ink, StatedTaxonomy.Tab, RoleRoot> {
    @Override public StylingRes<BaseScheme, StatedTaxonomy.Tab> style() {
        return new StylingRes.CssContent<>(new Copy(), new Raised(), new Danger());
    }
}
