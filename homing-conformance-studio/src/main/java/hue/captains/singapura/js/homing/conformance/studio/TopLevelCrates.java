package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.grid.RelationGridCrate;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.studio.base.StudioBaseCrate;
import hue.captains.singapura.js.homing.studio.workspace.StudioWorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.WorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceCodecsCrate;
import hue.captains.singapura.js.homing.workspace.persistence.WorkspacePersistenceCrate;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;

import java.util.List;

/**
 * RFC 0044 — the Crate-Studio's <b>top-level (owned)</b> crates for
 * homing-ssjs-core's self-conformance: the nine first-party crates. The studio
 * browses the modules of THESE crates; their transitive {@code requires}
 * closure ({@code CrateClosure.of(ALL)}) adds the external crates — here just
 * {@code LibsCrate} (bundled third-party JS) — which appear in the dependency
 * graph but are not module-browsed.
 *
 * <p>A downstream registers its OWN crates here instead; the homing framework
 * crates then appear as external dependencies in its graph.</p>
 */
public final class TopLevelCrates {

    private TopLevelCrates() {}

    public static final List<Crate> ALL = List.of(
            CoreJsCrate.INSTANCE,
            ServerCrate.INSTANCE,
            StudioBaseCrate.INSTANCE,
            WorkspaceCrate.INSTANCE,
            WorkspaceCodecsCrate.INSTANCE,
            WorkspacePersistenceCrate.INSTANCE,
            WorkspaceShellCrate.INSTANCE,
            StudioWorkspaceCrate.INSTANCE,
            ConformanceStudioCrate.INSTANCE,
            // RFC 0050 — the Relation Grid family. Its crate test proves the
            // modules are DECLARED; listing it here is what gets them RULE-GRADED.
            RelationGridCrate.INSTANCE);
}
