package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * RFC 0064 — the CSS graph workbench: the manager's dependency graph and
 * load order, visualised, with a plan bar that asks what a theme switch
 * would do before anything is touched, and runs it when asked. A studio
 * diagnostic, beside the party monitor.
 */
public final class CssGraphWorkbenchWidget
        extends WorkspaceWidget<WorkspaceWidget._None, CssGraphWorkbenchWidget> {

    public static final CssGraphWorkbenchWidget INSTANCE = new CssGraphWorkbenchWidget();

    private CssGraphWorkbenchWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, CssGraphWorkbenchWidget> {}

    @Override protected _Construct<_None, CssGraphWorkbenchWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "CSS Graph"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.SINGLETON; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new CssGraphRendererModule.renderCssGraph()),
                        CssGraphRendererModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var host = branch.createElement('host', 'div');",
                "    var bench = renderCssGraph(branch, host, {});",
                "    return {",
                "        root: host,",
                "        setActive: function () { /* no keys to own; the bar is buttons */ },",
                "        partyDeregister: function () { bench.dispose(); },",
                "        refresh: bench.refresh",
                "    };"
        );
    }
}
