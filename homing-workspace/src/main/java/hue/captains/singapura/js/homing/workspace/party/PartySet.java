package hue.captains.singapura.js.homing.workspace.party;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The complete set of {@link Party}s a workspace declares — one entry
 * per concern. Construction validates that every Party name is unique
 * and that no two Parties share the same message ADT class (per the
 * Parties-stay-disjoint discipline; sharing an ADT would let messages
 * intended for one Party leak into another's dispatch by class match).
 *
 * <p>Workspaces typically expose this via a {@code partySet()} method
 * the framework reads at shell-construction time.</p>
 *
 * @param parties the Parties, in declaration order; immutable copy
 * @since RFC 0028 cycle 1
 */
public record PartySet(List<Party<?>> parties) {

    public PartySet {
        Objects.requireNonNull(parties, "PartySet.parties");
        parties = List.copyOf(parties);

        Set<String> seenNames = new HashSet<>();
        Set<Class<?>> seenTypes = new HashSet<>();
        for (Party<?> p : parties) {
            if (!seenNames.add(p.name())) {
                throw new IllegalArgumentException(
                        "PartySet has duplicate Party name: '" + p.name() + "'");
            }
            if (!seenTypes.add(p.messageType())) {
                throw new IllegalArgumentException(
                        "PartySet has duplicate Party messageType: " + p.messageType().getName()
                        + " — each Party must have its own scoped ADT class");
            }
        }
    }

    /** Empty PartySet — a degenerate workspace that uses no messaging. */
    public static PartySet empty() {
        return new PartySet(List.of());
    }
}
