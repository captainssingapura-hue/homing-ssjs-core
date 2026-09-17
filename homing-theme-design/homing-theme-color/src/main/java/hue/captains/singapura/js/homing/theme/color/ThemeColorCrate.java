package hue.captains.singapura.js.homing.theme.color;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;

import java.util.List;

/**
 * RFC 0044 crate for {@code homing-theme-color}: the global palette, served as
 * a CSS group module like any other. Requires nothing — the palette leans on
 * nothing, by definition (it is the prior).
 */
public final class ThemeColorCrate implements Crate {

    public static final ThemeColorCrate INSTANCE = new ThemeColorCrate();

    private ThemeColorCrate() {}

    @Override
    public String name() {
        return "homing-theme-color";
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                // RFC 0066 — the global palette as a node of the CSS graph; the prior.
                CrateEntry.of(GlobalColorPalette.INSTANCE));
    }
}
