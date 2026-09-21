package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.party.PartyModule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The keyboard party's face to the DOM, one per page: it owns the party,
 * captures keys on the document while someone holds the keyboard, and turns
 * them into messages the secretary routes to the holder. Lazy, and blind to
 * why a member claims. Physical focus is never touched.
 */
public record KeyboardStewardModule() implements DomModule<KeyboardStewardModule> {

    /** A branch component: {@code new KeyboardSteward(branch, { onEvent? })}; join, leave, claim, release, holder, dispose. */
    public record KeyboardSteward() implements BranchComponent<KeyboardStewardModule> {
        @Override public String summary() { return "One per page: the keyboard party, its one holder, and the keys captured for it; lazy, and blind to why."; }
    }

    public static final KeyboardStewardModule INSTANCE = new KeyboardStewardModule();

    @Override
    public ImportsFor<KeyboardStewardModule> imports() {
        return ImportsFor.<KeyboardStewardModule>builder()
                .add(new ModuleImports<>(List.of(new PartyModule.Party()), PartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeyboardSecretaryModule.KeyboardSecretary()), KeyboardSecretaryModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeyboardEventsModule.KeyboardEvents()), KeyboardEventsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<KeyboardStewardModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new KeyboardSteward()));
    }
}
