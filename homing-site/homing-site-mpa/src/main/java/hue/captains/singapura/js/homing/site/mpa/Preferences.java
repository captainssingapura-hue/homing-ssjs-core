package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.server.PreferenceSteward;

import java.util.List;

/**
 * The user's preferences, on the bar: a button that names the theme the page
 * wears and opens a menu to change it — a design, then the colours it is
 * offered in — plus the way back to the site's default.
 *
 * <p>A pick goes to the steward ({@code remember("theme", slug)}) and nowhere
 * else; the CSS manager follows the store and switches every loaded sheet,
 * and {@code onThemeApplied} is when the button and the marks are re-read.
 * The menu never touches a stylesheet. What it knows about themes it learns
 * from {@code /themes} ({@link ThemesGetAction}) on first open.</p>
 *
 * <p>The theme is the first preference; the module is where the next ones
 * go when they arrive.</p>
 */
public record Preferences() implements DomModule<Preferences> {

    public record mountPreferences() implements Exportable._Constant<Preferences> {}

    public static final Preferences INSTANCE = new Preferences();

    @Override
    public ImportsFor<Preferences> imports() {
        return ImportsFor.<Preferences>builder()
                .add(new ModuleImports<>(List.of(new domOpsParty()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PreferenceSteward.PreferenceStewardInstance(),
                        new PreferenceSteward.PreferenceViewInstance()
                ), PreferenceSteward.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new MpaStyles.mpa_prefs(),
                        new MpaStyles.mpa_prefs_btn(),
                        new MpaStyles.mpa_prefs_btn_label(),
                        new MpaStyles.mpa_prefs_scrim(),
                        new MpaStyles.mpa_prefs_menu(),
                        new MpaStyles.mpa_prefs_label(),
                        new MpaStyles.mpa_prefs_list(),
                        new MpaStyles.mpa_prefs_item(),
                        new MpaStyles.mpa_prefs_name(),
                        new MpaStyles.mpa_prefs_note(),
                        new MpaStyles.mpa_prefs_dots(),
                        new MpaStyles.mpa_prefs_dot()
                ), MpaStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<Preferences> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new mountPreferences()));
    }
}
