package hue.captains.singapura.js.homing.theme.type;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;

import java.util.List;

/** RFC 0044 crate for {@code homing-theme-type}: the global type palette, a prior. */
public final class ThemeTypeCrate implements Crate {

    public static final ThemeTypeCrate INSTANCE = new ThemeTypeCrate();

    private ThemeTypeCrate() {}

    @Override public String name() { return "homing-theme-type"; }

    @Override public List<CrateEntry> entries() {
        return List.of(CrateEntry.of(GlobalTypePalette.INSTANCE));
    }
}
