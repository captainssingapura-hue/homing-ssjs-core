package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.server.ThemeRegistry;

import java.util.List;

/**
 * RFC 0002-ext1 Phase 10 — registry of theme artifacts shipped by
 * {@code homing-studio-base}.
 * <p>Every {@link Theme} the studio supports has a registered
 * {@link GlobalColorPalette.Provision} — its body for the global palette, light
 * and dark, served as the prior of every page — and, when it has more to say
 * than a palette, {@link CssGroupImpl}s carrying per-class overrides
 * (RFC 0066). There is no globals sheet.</p>
 *
 * <p>Adding a new theme: implement {@link Theme} + nested {@code Palette}
 * (mirroring {@link HomingDefault}), append both to the lists below, and add
 * an overrides record to {@link #overrides()} only if the theme needs one.</p>
 * three singletons to the lists below.</p>
 */
public final class StudioThemeRegistry implements ThemeRegistry {

    public static final StudioThemeRegistry INSTANCE = new StudioThemeRegistry();

    @Override public List<Theme> themes() {
        return List.of(
                HomingDefault.INSTANCE,
                HomingCarbon.INSTANCE,
                HomingForest.INSTANCE,
                HomingSunset.INSTANCE,
                HomingBauhaus.INSTANCE,
                HomingForbiddenCity.INSTANCE,
                HomingLetterpress.INSTANCE,
                HomingMapleBridge.INSTANCE,
                HomingRetro90s.INSTANCE,
                HomingTurboC.INSTANCE,
                HomingBrutalist.INSTANCE
        );
    }

    @Override public List<PaletteProvision<?, ?>> palettes() {
        return List.of(
                HomingDefault.Palette.INSTANCE,
                HomingCarbon.Palette.INSTANCE,
                HomingForest.Palette.INSTANCE,
                HomingSunset.Palette.INSTANCE,
                HomingBauhaus.Palette.INSTANCE,
                HomingForbiddenCity.Palette.INSTANCE,
                HomingLetterpress.Palette.INSTANCE,
                HomingMapleBridge.Palette.INSTANCE,
                HomingRetro90s.Palette.INSTANCE,
                HomingTurboC.Palette.INSTANCE,
                HomingBrutalist.Palette.INSTANCE
        );
    }

    /**
     * RFC 0066 — the per-class overrides the studio's themes carry: five themes
     * are palette-only and appear nowhere here; the rest say what differs, one
     * impl per group they touch. Brutalist's word on the workspace's classes is
     * not here — studio-base cannot see workspace-shell — but in
     * {@code homing-studio-workspace}, registered by the starter's fixtures.
     */
    @Override public List<CssGroupImpl<?, ?>> overrides() {
        return List.of(
                HomingLetterpress.Studio.INSTANCE,
                HomingMapleBridge.Studio.INSTANCE,
                HomingRetro90s.Studio.INSTANCE,
                HomingTurboC.Studio.INSTANCE,
                HomingBrutalist.Studio.INSTANCE,
                HomingBrutalist.Dialog.INSTANCE,
                HomingBrutalist.Picker.INSTANCE,
                HomingBrutalist.MasterDetail.INSTANCE
        );
    }
}
