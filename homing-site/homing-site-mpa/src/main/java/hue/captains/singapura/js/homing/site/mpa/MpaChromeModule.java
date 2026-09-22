package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.KeyboardStewardModule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.HrefManager;

import java.util.List;

/**
 * The page chrome: a bar with the brand, the trail and the preferences
 * button, over the slot the app is mounted in.
 *
 * <p>{@code mountChrome(root, chrome)} draws the bar into {@code root} and
 * returns the slot. The scaffold {@link AppPage} writes calls it first, then
 * imports the app and hands it the slot — so the app is any {@code
 * AppModule}, mounted where it always is, and extends nothing to be a page
 * under this chrome. {@code chrome} is what the server stamped: the brand
 * and the crumbs, both frozen data.</p>
 */
public record MpaChromeModule() implements DomModule<MpaChromeModule> {

    /** The class. */
    public record MpaChrome() implements BranchComponent<MpaChromeModule> {
        @Override public String summary() { return "The page chrome: a bar with the brand, the trail and the preferences button, over the slot the app is mounted in."; }
    }

    public static final MpaChromeModule INSTANCE = new MpaChromeModule();

    @Override
    public ImportsFor<MpaChromeModule> imports() {
        return ImportsFor.<MpaChromeModule>builder()
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()), HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(new PreferencesButtonModule.PreferencesButton()), PreferencesButtonModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeyboardStewardModule.KeyboardStewardInstance()), KeyboardStewardModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new MpaStyles.mpa_page(),
                        new MpaStyles.mpa_root(),
                        new MpaStyles.mpa_header(),
                        new MpaStyles.mpa_brand(),
                        new MpaStyles.mpa_brand_mark(),
                        new MpaStyles.mpa_brand_word(),
                        new MpaStyles.mpa_crumbs(),
                        new MpaStyles.mpa_crumb(),
                        new MpaStyles.mpa_crumb_sep(),
                        new MpaStyles.mpa_main()
                ), MpaStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<MpaChromeModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new MpaChrome()));
    }
}
