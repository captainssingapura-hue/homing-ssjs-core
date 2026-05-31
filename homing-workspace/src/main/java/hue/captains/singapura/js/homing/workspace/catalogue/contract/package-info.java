/**
 * RFC 0031 V1 — Substrate-free contract for the workspace catalogue.
 *
 * <p>The metadata layer above per-workspace persistence (events +
 * checkpoints). Each user-visible workspace instance has one entry in
 * the catalogue; the catalogue answers "which workspace does this URL
 * open" and "what workspaces exist for this kind" at boot time.</p>
 *
 * <h2>Contract surface</h2>
 *
 * <ul>
 *   <li>{@link WorkspaceCatalogueEntry} — one entry row
 *       ({@code id, kind, name, createdAt, lastOpenedAt, isDefault})</li>
 *   <li>{@link WorkspaceCatalogue} — service interface
 *       ({@code lookupByUuid / lookupByName / resolveDefault / listByKind /
 *       create / rename / delete / setDefault / touch})</li>
 * </ul>
 *
 * <h2>Reused identifiers</h2>
 *
 * <p>{@link hue.captains.singapura.js.homing.workspace.state.WorkspaceKind},
 * {@link hue.captains.singapura.js.homing.workspace.state.WorkspaceInstanceId},
 * {@link hue.captains.singapura.js.homing.workspace.state.WorkspaceName}.</p>
 *
 * @since RFC 0031 V1.
 */
package hue.captains.singapura.js.homing.workspace.catalogue.contract;
