package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The secretary of the keyboard party: one field, {@code holder}, and a pure
 * behaviour — a claim evicts and tells, a release or a member leaving clears,
 * a key goes to the holder or nowhere. Blind to why and how: the party is
 * told results. No DOM, no clock; tested as steps.
 */
public record KeyboardSecretaryModule() implements EsModule<KeyboardSecretaryModule> {

    /** {@code initial} and {@code behavior(state, envelope) → { newState, actions }}. */
    public record KeyboardSecretary() implements Exportable._Constant<KeyboardSecretaryModule> {}

    public static final KeyboardSecretaryModule INSTANCE = new KeyboardSecretaryModule();

    @Override public ImportsFor<KeyboardSecretaryModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<KeyboardSecretaryModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new KeyboardSecretary()));
    }
}
