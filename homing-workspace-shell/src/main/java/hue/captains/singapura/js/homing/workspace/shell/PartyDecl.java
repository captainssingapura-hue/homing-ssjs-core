package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Declarative construction of one Party (RFC 0028). Carries the
 * Secretary's JS module + initial actors + an optional
 * {@code workspaceCtx} key to expose the constructed Party under
 * (so widgets receive {@code workspaceCtx.<exposedAs>}).
 *
 * <p>Wire shape (after JSON serialization):</p>
 * <pre>{@code
 * {
 *   "name":              "animals",
 *   "secretaryModuleUrl": "/module?class=...AnimalsSecretaryModule",
 *   "secretaryExportName":"AnimalsSecretary",
 *   "actors": [{ "id": "animals/ribbon-selector",
 *                "parentSecretary": "animals" }],
 *   "exposedAs":         "animalsParty"
 * }
 * }</pre>
 *
 * <p>The substrate JS:</p>
 * <ol>
 *   <li>{@code await import(secretaryModuleUrl)} → reads the
 *       {@code secretaryExportName} export ({@code initial} + {@code behavior}).</li>
 *   <li>Constructs the Party with name + Secretary at path {@code name}.</li>
 *   <li>Calls {@code joinActor} for each declared actor.</li>
 *   <li>If {@code exposedAs} is set, assigns to {@code workspaceCtx[exposedAs]}.</li>
 * </ol>
 *
 * @since post-RFC-0034 workspace chrome decomposition
 */
public record PartyDecl(
        String name,
        DomModule<?> secretaryModule,
        String secretaryExportName,
        List<PartyActor> actors,
        String exposedAs) {

    public PartyDecl {
        Objects.requireNonNull(name, "PartyDecl.name");
        Objects.requireNonNull(secretaryModule, "PartyDecl.secretaryModule");
        Objects.requireNonNull(secretaryExportName, "PartyDecl.secretaryExportName");
        Objects.requireNonNull(actors, "PartyDecl.actors");
        if (name.isBlank()) throw new IllegalArgumentException("PartyDecl.name must not be blank");
        if (secretaryExportName.isBlank())
            throw new IllegalArgumentException("PartyDecl.secretaryExportName must not be blank");
        actors = List.copyOf(actors);
        // exposedAs may be null — Party not exposed via workspaceCtx (rare).
    }

    /** Start a builder for the common fluent shape. */
    public static Builder of(String name, DomModule<?> secretaryModule, String secretaryExportName) {
        return new Builder(name, secretaryModule, secretaryExportName);
    }

    /** Tiny mutable builder; produces an immutable {@link PartyDecl}. */
    public static final class Builder {
        private final String name;
        private final DomModule<?> secretaryModule;
        private final String secretaryExportName;
        private final List<PartyActor> actors = new ArrayList<>();
        private String exposedAs;

        private Builder(String name, DomModule<?> mod, String exportName) {
            this.name = name;
            this.secretaryModule = mod;
            this.secretaryExportName = exportName;
        }

        /** Add an initial actor that sits under the root Secretary {@code name}. */
        public Builder withActor(String actorId) {
            actors.add(new PartyActor(actorId, name));
            return this;
        }

        /** Add an initial actor under an explicit parent Secretary. */
        public Builder withActor(String actorId, String parentSecretary) {
            actors.add(new PartyActor(actorId, parentSecretary));
            return this;
        }

        /** Expose the constructed Party as {@code workspaceCtx[key]}. */
        public Builder exposedAs(String key) {
            this.exposedAs = key;
            return this;
        }

        public PartyDecl build() {
            return new PartyDecl(name, secretaryModule, secretaryExportName,
                    List.copyOf(actors), exposedAs);
        }
    }
}
