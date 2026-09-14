package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.workspace.shell.WorkspaceGroup;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceGroupRegistry;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpecRegistry;

import java.util.List;

/**
 * RFC 0058 — the conformance studio's one workspace group: it holds the one
 * kind, {@link ConformanceWorkspaceSpec}, and is what the landing catalogue
 * places. The kind is a path inside it — {@code #ws/workspaces/conformance} —
 * never a leaf of its own.
 */
public final class ConformanceWorkspaceGroup {

    private ConformanceWorkspaceGroup() {}

    /** The group's id — what {@code ?ws_group=} carries and the placement writes. */
    public static final String ID = "conformance";

    /** Register the spec and the group, idempotently; return the group. */
    public static WorkspaceGroup register() {
        var spec = ConformanceWorkspaceSpec.INSTANCE;
        if (WorkspaceSpecRegistry.INSTANCE.get(spec.kind()).isEmpty()) {
            WorkspaceSpecRegistry.INSTANCE.register(spec);
        }
        return WorkspaceGroupRegistry.INSTANCE.get(ID).orElseGet(() ->
                WorkspaceGroupRegistry.INSTANCE.register(WorkspaceGroup.of(
                        ID, "Conformance", "The module Navigator plus Summary, Full Content, and Conformance panes.",
                        List.of(spec))));
    }
}
