package hue.captains.singapura.js.homing.workspace.state;

import java.util.Map;

/**
 * SPI for forward-migrating {@link WorkspaceState} across envelope
 * schema bumps. Each implementation handles one version step
 * ({@link #fromVersion} → {@link #toVersion}); the runtime chains them to
 * upgrade a saved state of any older version to
 * {@link WorkspaceState#CURRENT_SCHEMA_VERSION}.
 *
 * <h2>Encoding-level migrations</h2>
 *
 * <p>Migrations operate on the JSON tree representation ({@code Map<String,
 * Object>}), not on the {@link WorkspaceState} record. This is deliberate
 * for two reasons:</p>
 *
 * <ul>
 *   <li>The in-memory record always reflects the current schema, so it
 *       cannot hold older-shape data. The migration's job is to reshape
 *       the JSON tree so that, after the chain runs, the final tree
 *       deserialises cleanly into the current record.</li>
 *   <li>The {@code Map<String, Object>} keys here are JSON field names,
 *       not framework identifiers. The {@code Names Are Types} doctrine
 *       binds identity in framework state; encoding-level shape
 *       manipulation is exempt — the strings are the wire format, not
 *       the in-memory contract.</li>
 * </ul>
 *
 * <h2>Asymmetric versioning policy</h2>
 *
 * <p>The envelope is strict: every schema bump ships a corresponding
 * migration; unknown future versions fail loudly. Widget params are
 * tolerant: unknown fields ignored, missing fields default. Migrations
 * under this SPI handle the envelope side only — widgets evolve their
 * own params under the tolerance policy without involving this SPI.</p>
 *
 * @since RFC 0029 cycle 1
 */
public interface WorkspaceStateMigration {

    /** The version this migration starts from. */
    int fromVersion();

    /** The version this migration produces. Always {@code fromVersion() + 1} in V1. */
    int toVersion();

    /**
     * Transform the JSON tree from {@link #fromVersion} shape to
     * {@link #toVersion} shape. Pure: same input produces the same
     * output; no I/O; no clock.
     *
     * @param oldStateJson the saved state's JSON tree at
     *                     {@code fromVersion} shape; treated immutably
     * @return a JSON tree at {@code toVersion} shape, ready to be either
     *         deserialised into {@link WorkspaceState} (if
     *         {@code toVersion} matches the current schema) or fed into
     *         the next migration in the chain
     */
    Map<String, Object> migrate(Map<String, Object> oldStateJson);
}
