package hue.captains.singapura.js.homing.workspace.state;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The user-facing handle for one workspace instance — used both for
 * display in the switcher and as the {@code &name=} query param in
 * URLs. Unified display + URL handle to keep the model small: one name
 * field, no separate slug.
 *
 * <p>Grammar: 1-50 characters, ASCII letters / digits / hyphens /
 * underscores, must start with a letter. URL-safe by construction (no
 * encoding noise in bookmarks). Case-preserving — {@code "MorningRun"}
 * stays {@code "MorningRun"}.</p>
 *
 * <p>Per the Names Are Types doctrine, no raw {@code String} crosses
 * the workspace-catalogue contract as a workspace name; the wrapper
 * carries both the grammar invariant and the typed intent.</p>
 *
 * <p>Uniqueness is per-{@link WorkspaceKind} — two workspaces of
 * different kinds can share a name; two workspaces of the same kind
 * cannot. Enforced at catalogue-write time, not at this type's
 * construction.</p>
 *
 * @param value the underlying handle string
 * @since RFC 0031 V1 — multi-instance workspaces (UUID + name).
 */
public record WorkspaceName(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z][A-Za-z0-9_-]{0,49}");

    public WorkspaceName {
        Objects.requireNonNull(value, "WorkspaceName.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "WorkspaceName.value '" + value + "' — must be 1-50 chars, "
                  + "letters / digits / hyphens / underscores, starting with a letter");
        }
    }

    /** The conventional name for the auto-created first workspace of any kind. */
    public static final WorkspaceName DEFAULT = new WorkspaceName("default");

    public static WorkspaceName of(String value) { return new WorkspaceName(value); }

    @Override public String toString() { return value; }
}
