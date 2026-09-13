package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * RFC 0063 — the DomOpsParty monitor: the branch tree, visible from inside
 * itself, without the power to touch it.
 *
 * <p>The widget is thin on purpose. It hands its branch and host to
 * {@link PartyMonitorRendererModule} and passes its own branch name so the
 * renderer can select it — the honest proof the view is live. Keys flow to the
 * tree only while workspace-active, as the navigator does it. It imports the
 * renderer and nothing else; the renderer imports {@code viewParty()} and
 * nothing else. Nowhere in the chain is {@code domOpsParty}.</p>
 *
 * <p>Provision is declared (D10): a kind that wants the monitor lists it in
 * {@code widgetEntries()}. {@code SINGLETON}, because two monitors of one tree
 * is one more than useful and the picker then focuses the live one.</p>
 */
public final class DomOpsPartyMonitorWidget
        extends WorkspaceWidget<WorkspaceWidget._None, DomOpsPartyMonitorWidget> {

    public static final DomOpsPartyMonitorWidget INSTANCE = new DomOpsPartyMonitorWidget();

    private DomOpsPartyMonitorWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DomOpsPartyMonitorWidget> {}

    @Override protected _Construct<_None, DomOpsPartyMonitorWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Party Monitor"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.SINGLETON; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new PartyMonitorRendererModule.renderPartyMonitor()),
                        PartyMonitorRendererModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var host = branch.createElement('host', 'div');",
                "    var monitor = renderPartyMonitor(branch, host, { selfName: branch.name });",
                "",
                "    // Keyboard, the way the navigator does it: TreeRenderer owns the key",
                "    // semantics, the widget owns WHEN keys flow. Forward keydown only",
                "    // while workspace-active (RFC 0049), so two trees never fight over",
                "    // the arrows; preventDefault on a consumed key so the page does not",
                "    // scroll.",
                "    var __keyHandler = function (ev) {",
                "        if (monitor.handleKeydown(ev)) ev.preventDefault();",
                "    };",
                "",
                "    return {",
                "        root: host,",
                "        setActive: function (active) {",
                "            if (active) document.addEventListener('keydown', __keyHandler);",
                "            else        document.removeEventListener('keydown', __keyHandler);",
                "        },",
                "        partyDeregister: function () {",
                "            // Belt-and-braces: drop the listener on teardown even if",
                "            // setActive(false) was never called.",
                "            document.removeEventListener('keydown', __keyHandler);",
                "        },",
                "        refresh: monitor.refresh",
                "    };"
        );
    }
}
