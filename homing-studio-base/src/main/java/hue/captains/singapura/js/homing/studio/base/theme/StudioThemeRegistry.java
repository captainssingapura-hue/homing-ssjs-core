package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;
import hue.captains.singapura.js.homing.server.ThemeRegistry;

import java.util.List;

/**
 * RFC 0002-ext1 Phase 10 — registry of theme artifacts shipped by
 * {@code homing-studio-base}.
 *
 * <p>Every {@link Theme} the studio supports has a registered
 * {@link ThemeVariables} (the variable values, served at {@code /theme-vars})
 * and a {@link ThemeGlobals} (the raw global rules, served at
 * {@code /theme-globals}).</p>
 *
 * <p>Adding a new theme: implement {@link Theme} + nested {@code Vars} +
 * nested {@code Globals} (mirroring {@link HomingDefault}), then append all
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
                HomingJazzDrums.INSTANCE,
                HomingTurboC.INSTANCE
        );
    }

    @Override public List<ThemeVariables<?>> variables() {
        return List.of(
                HomingDefault.Vars.INSTANCE,
                HomingCarbon.Vars.INSTANCE,
                HomingForest.Vars.INSTANCE,
                HomingSunset.Vars.INSTANCE,
                HomingBauhaus.Vars.INSTANCE,
                HomingForbiddenCity.Vars.INSTANCE,
                HomingLetterpress.Vars.INSTANCE,
                HomingMapleBridge.Vars.INSTANCE,
                HomingRetro90s.Vars.INSTANCE,
                HomingJazzDrums.Vars.INSTANCE,
                HomingTurboC.Vars.INSTANCE
        );
    }

    @Override public List<ThemeGlobals<?>> globals() {
        return List.of(
                HomingDefault.Globals.INSTANCE,
                HomingCarbon.Globals.INSTANCE,
                HomingForest.Globals.INSTANCE,
                HomingSunset.Globals.INSTANCE,
                HomingBauhaus.Globals.INSTANCE,
                HomingForbiddenCity.Globals.INSTANCE,
                HomingLetterpress.Globals.INSTANCE,
                HomingMapleBridge.Globals.INSTANCE,
                HomingRetro90s.Globals.INSTANCE,
                HomingJazzDrums.Globals.INSTANCE,
                HomingTurboC.Globals.INSTANCE
        );
    }
}
