package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.Widget;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.widget.DocWidget;

import java.util.List;

/**
 * The thin chrome widget hosted by {@link GenericWorkspace}. Its body
 * JS does four things (RFC 0058):
 *
 * <ol>
 *   <li>Inline the registered {@link WorkspaceGroup}s and {@link WorkspaceSpec}s
 *       as JS objects keyed by id and kind.</li>
 *   <li>Pick the <b>group</b> from the URL's {@code ws_group} — or, for a legacy
 *       {@code ws_kind} address, the group holding that kind, or a group of one
 *       when none does.</li>
 *   <li>Pick the <b>kind</b> from the anchor ({@code #ws/<section>/<kind>}),
 *       then the legacy {@code ws_kind}, then the group's default — and append
 *       the inner crumbs to the trail the server stamped.</li>
 *   <li>Call {@code mountWorkspaceShell(branch, parent, spec)}, with the group on
 *       the spec so the switcher draws exactly this group's tree; reload on a
 *       hash change, which is how a kind change on the same address takes.</li>
 * </ol>
 *
 * <p>Everything else — workspace layout mount, ribbon/footer wiring,
 * MultiTabPane, Party bootstrap, persistence, event log, replay,
 * checkpoints, Web Locks — lives behind {@code mountWorkspaceShell} in
 * {@code WorkspaceShellChromeModule.js}. No workspace-specific bodyJs
 * ever exists; per-workspace customisation is the
 * {@link WorkspaceSpec}, a stateless declarative record.</p>
 *
 * <p><b>Why the page still carries every group.</b> This module is served once,
 * without request context, so it cannot inline only the requested group; a
 * served {@code /workspace-group} route would, but the seam every studio's
 * fixtures share ({@code DefaultFixtures}) sits below this crate. So the bytes
 * of every group ride along while the page <em>uses</em> only its own — the
 * behavioural leak (the switcher offering every kind in the deployment) is
 * closed; the byte leak waits on that route.</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition; RFC 0058 groups
 */
public final class GenericWorkspaceChrome
        extends DocWidget<GenericWorkspace.Params, GenericWorkspaceChrome> {

    public static final GenericWorkspaceChrome INSTANCE = new GenericWorkspaceChrome();

    private GenericWorkspaceChrome() {}

    private record mountInto()
            implements Widget._MountInto<GenericWorkspace.Params, GenericWorkspaceChrome> {}

    @Override public String simpleName() { return "generic-workspace-chrome"; }
    @Override public Class<GenericWorkspace.Params> paramsType() { return GenericWorkspace.Params.class; }
    @Override public String title() { return "Workspace"; }

    @Override
    protected Widget._MountInto<GenericWorkspace.Params, GenericWorkspaceChrome> mountInto() {
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
                        new WorkspaceGroupPathModule.innerCrumbs(),
                        new WorkspaceGroupPathModule.soloGroup()),
                        WorkspaceGroupPathModule.INSTANCE),
                new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE)
        );
    }

    @Override
    protected List<String> bodyJs() {
        final String specsJson  = WorkspaceSpecJson.allAsObject(WorkspaceSpecRegistry.INSTANCE.all());
        final String groupsJson = WorkspaceGroupJson.allAsObject(WorkspaceGroupRegistry.INSTANCE.all());

        return List.of(
                "    const SPECS  = " + specsJson + ";",
                "    const GROUPS = " + groupsJson + ";",
                "    const wsGroup = (params && params.ws_group) || '';",
                "    const wsKind  = (params && params.ws_kind)  || '';",
                "    // The group: named, or the one holding a legacy kind, or a group of one.",
                "    let group = GROUPS[wsGroup] || null;",
                "    if (!group && wsKind && SPECS[wsKind]) group = soloGroup(SPECS[wsKind]);",
                "    if (!group) {",
                "        const err = document.createElement('div');",
                "        err.style.cssText = 'padding:20px; font-family:sans-serif;';",
                "        err.textContent = 'Unknown workspace group: \"' + (wsGroup || wsKind) + '\"'",
                "                        + ' (registered: ' + Object.keys(GROUPS).join(', ') + ')';",
                "        parent.appendChild(err);",
                "        return;",
                "    }",
                "    // The kind: the anchor's, else the legacy param's, else the default.",
                "    const resolved = resolveKind(group, HrefManagerInstance.hash(), wsKind);",
                "    const spec = SPECS[resolved.kind];",
                "    spec.group  = group;",
                "    spec.anchor = resolved.anchor;",
                "    spec.notice = resolved.notice;",
                "    if (page && typeof page.extendCrumbs === 'function') {",
                "        page.extendCrumbs(innerCrumbs(group, resolved.kind));",
                "    }",
                "    if (resolved.notice) console.warn('[GenericWorkspaceChrome] ' + resolved.notice);",
                "    // A ws/ anchor that was not the kind's true path shows as what it resolved to.",
                "    if (resolved.canonicalised && resolved.anchor) HrefManagerInstance.replaceHash(resolved.anchor);",
                "    // A kind change is an anchor on this same address; the browser does",
                "    // not reload for that, so the chrome does (RFC 0058 D7 — Phase 3 remounts in place).",
                "    HrefManagerInstance.onHashChange(function () { HrefManagerInstance.reload(); });",
                "    mountWorkspaceShell(branch, parent, spec);"
        );
    }
}
