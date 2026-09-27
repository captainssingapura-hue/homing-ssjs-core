package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.party.PartyModule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The keyboard party's face to the DOM, one per page: it owns the party,
 * captures keys on the document while someone holds the keyboard, and turns
 * them into messages the secretary routes to the holder. Lazy, and blind to
 * why a member claims. Where the focus is — the holder, held or lent to a
 * native control of its own, or away — is its marker, whose rules and marks
 * are {@link KeyboardMarkModule}'s (RFC 0066 E3, keyboard §17.5).
 */
public record KeyboardStewardModule() implements DomModule<KeyboardStewardModule> {

    /** A branch component: {@code new KeyboardSteward(branch, { onEvent? })}; join, leave, claim, release, holder, dispose. */
    public record KeyboardSteward() implements BranchComponent<KeyboardStewardModule> {
        @Override public String summary() { return "One per page: the keyboard party, its one holder, and the keys captured for it; lazy, and blind to why."; }
    }
    /** The page's steward, one per document, bound to the focus party. */
    public record KeyboardStewardInstance() implements Exportable._Constant<KeyboardStewardModule> {}

    public static final KeyboardStewardModule INSTANCE = new KeyboardStewardModule();

    @Override
    public ImportsFor<KeyboardStewardModule> imports() {
        return ImportsFor.<KeyboardStewardModule>builder()
                .add(new ModuleImports<>(List.of(new PartyModule.Party()), PartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeyboardSecretaryModule.KeyboardSecretary()), KeyboardSecretaryModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeyboardEventsModule.KeyboardEvents()), KeyboardEventsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeyboardWalkModule.KeyboardWalk()), KeyboardWalkModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeyboardMarkModule.KeyboardMark()), KeyboardMarkModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeyboardShortcutsModule.KeyboardShortcuts()), KeyboardShortcutsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeyboardChordsModule.KeyboardChords()), KeyboardChordsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParty(), new FocusPartyModule.StationedFocusParty()), FocusPartyModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<KeyboardStewardModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new KeyboardSteward(), new KeyboardStewardInstance()));
    }
}
