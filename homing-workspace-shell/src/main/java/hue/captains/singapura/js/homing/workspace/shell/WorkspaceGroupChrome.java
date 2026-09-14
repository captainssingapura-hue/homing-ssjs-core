package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.Widget;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.widget.DocWidget;

import java.util.List;

/**
 * RFC 0058 — the thin chrome widget hosted by {@link WorkspaceGroupApp}. Its
 * body JS does four things:
 *
 * <ol>
 *   <li>Inline the registered {@link WorkspaceGroup}s and the specs they hold,
 *       keyed by id and kind.</li>
 *   <li>Pick the <b>group</b> from the URL's {@code ws_group}.</li>
 *   <li>Pick the <b>kind</b> from the anchor ({@code #ws/<section>/<kind>}),
 *       else the group's default — canonicalising a known kind under a wrong
 *       section in place, noticing a {@code ws/} anchor that lied — and append
 *       the inner crumbs (section, kind) to the trail the server stamped.</li>
 *   <li>Call {@code mountWorkspaceShell(branch, parent, spec)} with the group on
 *       the spec, so the switcher draws exactly this group's tree and navigates
 *       by anchor; reload on a hash change, which is how a kind change on the
 *       same address takes (D7 — Phase 3 remounts in place).</li>
 * </ol>
 *
 * <p>Everything else — layout, ribbon, MultiTabPane, parties, persistence,
 * replay — lives behind {@code mountWorkspaceShell}, shared with the legacy
 * {@code GenericWorkspaceChrome}. The two chromes differ only in how they
 * arrive at a spec.</p>
 *
 * <p><b>Why the page carries every group.</b> This module is served once, without
 * request context, so it cannot inline only the requested group; a served
 * {@code /workspace-group} route would, but the seam every studio's fixtures
 * share ({@code DefaultFixtures}) sits below this crate. The bytes of every
 * group ride along while the page <em>uses</em> only its own.</p>
 */
public final class WorkspaceGroupChrome
        extends DocWidget<WorkspaceGroupApp.Params, WorkspaceGroupChrome> {

    public static final WorkspaceGroupChrome INSTANCE = new WorkspaceGroupChrome();

    private WorkspaceGroupChrome() {}

    private record mountInto()
            implements Widget._MountInto<WorkspaceGroupApp.Params, WorkspaceGroupChrome> {}

    @Override public String simpleName() { return "workspace-group-chrome"; }
    @Override public Class<WorkspaceGroupApp.Params> paramsType() { return WorkspaceGroupApp.Params.class; }
    @Override public String title() { return "Workspaces"; }

    @Override
    protected Widget._MountInto<WorkspaceGroupApp.Params, WorkspaceGroupChrome> mountInto() {
        return new mountInto();
    }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(
                        new WorkspaceShellChromeModule.mountWorkspaceShell()),
                        WorkspaceShellChromeModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new WorkspaceGroupPathModule.resolveKind(),
                        new WorkspaceGroupPathModule.innerCrumbs()),
                        WorkspaceGroupPathModule.INSTANCE),
                new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE)
        );
    }

    @Override
    protected List<String> bodyJs() {
        // Only the specs a group holds: a kind no group holds is the legacy
        // app's to serve, not this one's.
        var held = WorkspaceGroupRegistry.INSTANCE.all().stream()
                .flatMap(g -> g.specs().stream()).toList();
        final String specsJson  = WorkspaceSpecJson.allAsObject(held);
        final String groupsJson = WorkspaceGroupJson.allAsObject(WorkspaceGroupRegistry.INSTANCE.all());

        return List.of(
                "    const SPECS  = " + specsJson + ";",
                "    const GROUPS = " + groupsJson + ";",
                "    const wsGroup = (params && params.ws_group) || '';",
                "    const group = GROUPS[wsGroup];",
                "    if (!group) {",
                "        const err = branch.createElement('err', 'div');",
                "        err.textContent = 'Unknown workspace group: \"' + wsGroup + '\"'",
                "                        + ' (registered: ' + Object.keys(GROUPS).join(', ') + ')';",
                "        parent.appendChild(err);",
                "        return;",
                "    }",
                "    // The kind: the anchor's, else the group's default.",
                "    const resolved = resolveKind(group, HrefManagerInstance.hash());",
                "    const spec = SPECS[resolved.kind];",
                "    spec.group  = group;",
                "    spec.anchor = resolved.anchor;",
                "    spec.notice = resolved.notice;",
                "    if (page && typeof page.extendCrumbs === 'function') {",
                "        page.extendCrumbs(innerCrumbs(group, resolved.kind));",
                "    }",
                "    if (resolved.notice) console.warn('[WorkspaceGroupChrome] ' + resolved.notice);",
                "    // A ws/ anchor that was not the kind's true path shows as what it resolved to.",
                "    if (resolved.canonicalised && resolved.anchor) HrefManagerInstance.replaceHash(resolved.anchor);",
                "    // A kind change is an anchor on this same address; the browser does not",
                "    // reload for that, so the chrome does.",
                "    HrefManagerInstance.onHashChange(function () { HrefManagerInstance.reload(); });",
                "    mountWorkspaceShell(branch, parent, spec);"
        );
    }
}
