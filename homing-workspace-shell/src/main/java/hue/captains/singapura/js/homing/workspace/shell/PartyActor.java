package hue.captains.singapura.js.homing.workspace.shell;

import java.util.Objects;

/**
 * Declaration of one Actor that joins a {@link PartyDecl}'s Party at
 * boot. The chrome calls {@code party.joinActor({id, parentSecretary,
 * reactors: {}})} for each; these are sender-only initial Actors
 * (typically ribbon controls). Widget Actors join dynamically when
 * widgets construct — those are not declared here.
 *
 * @param id              actor path identifier ({@code "animals/ribbon-selector"})
 * @param parentSecretary the Secretary path this Actor sits under
 * @since post-RFC-0034 workspace chrome decomposition
 */
public record PartyActor(String id, String parentSecretary) {
    public PartyActor {
        Objects.requireNonNull(id, "PartyActor.id");
        Objects.requireNonNull(parentSecretary, "PartyActor.parentSecretary");
        if (id.isBlank()) throw new IllegalArgumentException("PartyActor.id must not be blank");
        if (parentSecretary.isBlank())
            throw new IllegalArgumentException("PartyActor.parentSecretary must not be blank");
    }
}
