package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.*;

import java.util.List;

/**
 * RFC 0064 — the preference steward: where a preference lives, and the one
 * module that may go there. Resolves a preference in one order everywhere —
 * the address as this page's override, then the stored pick, then the
 * caller's fallback — and keeps only explicit picks. Backed by
 * {@code localStorage}; the server resolves no context and so reads none.
 *
 * <p>Injected as {@code PreferenceStewardInstance}. {@link CssClassManager}
 * asks it which theme to load; the theme picker asks it what is active and
 * tells it what was picked.</p>
 */
public record PreferenceSteward() implements EsModule<PreferenceSteward> {

    public static final PreferenceSteward INSTANCE = new PreferenceSteward();

    public record PreferenceStewardInstance() implements Exportable._Constant<PreferenceSteward> {}

    @Override
    public ImportsFor<PreferenceSteward> imports() {
        return ImportsFor.<PreferenceSteward>builder()
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PreferenceSteward> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PreferenceStewardInstance()));
    }
}
