package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.*;

import java.util.List;

/**
 * Framework-level EsModule that provides CSS class management.
 * <p>Subsumes {@link CssLoader}: handles CSS file loading and provides
 * type-safe CSS class operations (addClass, removeClass, etc.) using
 * frozen CssClass objects.</p>
 */
public record CssClassManager() implements EsModule<CssClassManager> {

    public static final CssClassManager INSTANCE = new CssClassManager();

    public record CssClassManagerInstance() implements Exportable._Constant<CssClassManager> {}

    @Override
    public ImportsFor<CssClassManager> imports() {
        // RFC 0064 — the theme a group loads under is the steward's to say;
        // the argument a served group module carries is only the fallback.
        return ImportsFor.<CssClassManager>builder()
                .add(new ModuleImports<>(List.of(new PreferenceSteward.PreferenceViewInstance()),
                        PreferenceSteward.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CssClassManager> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new CssClassManagerInstance()));
    }
}
