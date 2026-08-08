package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.libs.LibsCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.studio.base.StudioBaseCrate;
import hue.captains.singapura.js.homing.studio.workspace.StudioWorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.WorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceCodecsCrate;
import hue.captains.singapura.js.homing.workspace.persistence.WorkspacePersistenceCrate;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;

import java.util.List;

/**
 * RFC 0044 — the homing-ssjs-core crate set, in dependency (bottom-up) order.
 * The interim crate index the studio + report enumerate over, until a downstream
 * app crate's transitive {@code requires()} closure supersedes it. Hand-listed
 * and auditable — same doctrine as {@code HomingLibsRegistry}; each entry's own
 * {@code OrphanCheck} keeps that crate complete.
 */
public final class KnownCrates {

    private KnownCrates() {}

    public static final List<Crate> ALL = List.of(
            CoreJsCrate.INSTANCE,
            ServerCrate.INSTANCE,
            LibsCrate.INSTANCE,
            StudioBaseCrate.INSTANCE,
            WorkspaceCrate.INSTANCE,
            WorkspaceCodecsCrate.INSTANCE,
            WorkspacePersistenceCrate.INSTANCE,
            WorkspaceShellCrate.INSTANCE,
            StudioWorkspaceCrate.INSTANCE,
            ConformanceStudioCrate.INSTANCE);
}
