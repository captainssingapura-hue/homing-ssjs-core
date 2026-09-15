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
 * <p>Two exports, by authority: {@code PreferenceViewInstance} is read-only and
 * is what every component holds; {@code PreferenceStewardInstance} writes and
 * is held only by the surfaces that let a user choose. A change notification
 * carries nothing — a listener goes to the view for details.</p>
 */
public record PreferenceSteward() implements EsModule<PreferenceSteward> {

    public static final PreferenceSteward INSTANCE = new PreferenceSteward();

    /** The writer — remember, forget. Held only by surfaces that let a user choose. */
    public record PreferenceStewardInstance() implements Exportable._Constant<PreferenceSteward> {}

    /** Read-only — preferred, override, resolve, onChange. What every component holds. */
    public record PreferenceViewInstance() implements Exportable._Constant<PreferenceSteward> {}

    @Override
    public ImportsFor<PreferenceSteward> imports() {
        return ImportsFor.<PreferenceSteward>builder()
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PreferenceSteward> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PreferenceStewardInstance(), new PreferenceViewInstance()));
    }
}
