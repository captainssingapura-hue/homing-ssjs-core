package hue.captains.singapura.js.homing.workspace.state;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Registry-key identity for a kind of workspace — names which workspace
 * registration owns a given saved state (so {@link WorkspaceState} can be
 * restored against the right workspace shell). The wrapper that the
 * {@code Names Are Types} doctrine demands — no raw {@code String}
 * crosses framework code as a workspace-kind key.
 *
 * <p>Examples: {@code "AnimalsPlayground"},
 * {@code "BuildingBlocksWorkspace"}. Grammar: PascalCase letters and
 * digits — matches the Java type-name shape since each workspace kind is
 * typically named after its Java class.</p>
 *
 * @param value the underlying registry key
 * @since RFC 0029 cycle 1
 */
public record WorkspaceKind(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Z][A-Za-z0-9]*");

    public WorkspaceKind {
        Objects.requireNonNull(value, "WorkspaceKind.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "WorkspaceKind.value '" + value + "' — PascalCase required (starts uppercase, letters and digits only)");
        }
    }

    public static WorkspaceKind of(String value) {
        return new WorkspaceKind(value);
    }

    @Override public String toString() { return value; }
}
