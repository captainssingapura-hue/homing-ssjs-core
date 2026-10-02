package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The logical-focus tree: structure, and nothing else. The same shape as the
 * DomOpsParty and unlike it in doing no work: no DOM, no keys, no holder. A
 * container holds a branch, a leaf joins one, a member is moved as its place
 * in the rendered UI changes; the party says what mutated and can be read
 * whole. One per page, {@code focusParty}, the root branch. The steward
 * reads it and never writes it; a component joins and leaves and never
 * knows a steward exists.
 */
public record FocusPartyModule() implements EsModule<FocusPartyModule> {

    /** A party's tree: {@code root}, {@code find(id)}, {@code inspect()}, {@code walk()} - never itself, always Stationed or Mobile. */
    public record FocusParty() implements Exportable._Class<FocusPartyModule> {}
    /** The page's party, {@code focusParty}: the one the steward is bound to, with {@code on(fn)} for its notices. */
    public record StationedFocusParty() implements Exportable._Class<FocusPartyModule> {}
    /** Any other party: a widget's own, made by the party of parties, grafted by a host; it only forwards. */
    public record MobileFocusParty() implements Exportable._Class<FocusPartyModule> {}
    /** The party of parties: the stationed one and every mobile one; its singleton is {@code focusParties}. */
    public record FocusParties() implements Exportable._Class<FocusPartyModule> {}
    /** A branch: {@code join(name, component)}, {@code createBranch(name, holder)}, {@code adopt(m)}, {@code dissolve()}, {@code inspect()}. */
    public record FocusBranch() implements Exportable._Class<FocusPartyModule> {}
    /** A member: {@code component}, {@code id}, {@code path}, {@code in}, {@code branch}, {@code parent()}, {@code leave()}. */
    public record FocusMembership() implements Exportable._Class<FocusPartyModule> {}

    public static final FocusPartyModule INSTANCE = new FocusPartyModule();

    @Override public ImportsFor<FocusPartyModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<FocusPartyModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new FocusParty(), new StationedFocusParty(), new MobileFocusParty(), new FocusParties(),
                new FocusBranch(), new FocusMembership(), new focusParty(), new focusParties()));
    }
}
