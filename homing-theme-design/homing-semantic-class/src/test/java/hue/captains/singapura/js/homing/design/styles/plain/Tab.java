package hue.captains.singapura.js.homing.design.styles.plain;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.semantic.BaseScheme;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Copy;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Danger;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Page;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Raised;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Safe;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.design.styling.StatedTaxonomy;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;

/** Plain's tab: copy on the page, raised under the pointer, danger's mark when picked, its keys' rings in safe. */
public record Tab() implements CastingStyle<BaseScheme, Plain, StatedTaxonomy.Tab, RoleRoot> {
    @Override public StylingRes<BaseScheme, StatedTaxonomy.Tab> style() {
        return new StylingRes.CssContent<>(new Copy(), new Page(), new Raised(), new Danger(), new Safe());
    }
}
