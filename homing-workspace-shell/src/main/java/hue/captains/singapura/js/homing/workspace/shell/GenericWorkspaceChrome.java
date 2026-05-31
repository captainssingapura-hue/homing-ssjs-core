package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.Widget;
import hue.captains.singapura.js.homing.studio.base.widget.DocWidget;

import java.util.List;

/**
 * The thin chrome widget hosted by {@link GenericWorkspace}. Its body
 * JS does only three things:
 *
 * <ol>
 *   <li>Inline the {@link WorkspaceSpecRegistry} as a JS object keyed
 *       by {@code spec.kind}.</li>
 *   <li>Look up the spec by the URL's {@code ws_kind} parameter.</li>
 *   <li>Call {@code mountWorkspaceShell(branch, parent, spec)}.</li>
 * </ol>
 *
 * <p>Everything else — workspace layout mount, ribbon/footer wiring,
 * MultiTabPane, Party bootstrap, persistence, event log, replay,
 * checkpoints, Web Locks — lives behind {@code mountWorkspaceShell} in
 * {@code WorkspaceShellChromeModule.js}. No workspace-specific bodyJs
 * ever exists; per-workspace customisation is the
 * {@link WorkspaceSpec}, a stateless declarative record.</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition
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
                        WorkspaceShellChromeModule.INSTANCE)
        );
    }

    @Override
    protected List<String> bodyJs() {
        // Serialize every registered spec into a single JS object literal,
        // keyed by spec.kind. ws_kind from the URL picks one.
        final String specsJson = WorkspaceSpecJson.allAsObject(
                WorkspaceSpecRegistry.INSTANCE.all());

        return List.of(
                "    const SPECS = " + specsJson + ";",
                "    const wsKind = (params && params.ws_kind) || '';",
                "    const spec = SPECS[wsKind];",
                "    if (!spec) {",
                "        const err = document.createElement('div');",
                "        err.style.cssText = 'padding:20px; font-family:sans-serif;';",
                "        err.textContent = 'Unknown workspace kind: \"' + wsKind + '\"'",
                "                        + ' (registered: ' + Object.keys(SPECS).join(', ') + ')';",
                "        parent.appendChild(err);",
                "        return;",
                "    }",
                "    mountWorkspaceShell(branch, parent, spec);"
        );
    }
}
