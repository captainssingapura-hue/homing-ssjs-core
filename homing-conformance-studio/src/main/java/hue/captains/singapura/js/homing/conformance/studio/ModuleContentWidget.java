package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * RFC 0044 — the Full-Content pane: on an intentional open ({@code OpenDoc})
 * of a module leaf it fetches the module's served JavaScript from the standard
 * module endpoint ({@code GET /module?class=<FQCN>}) and shows it verbatim.
 *
 * <p>The FQCN arrives on the selection's {@code summary} (set by
 * {@link ModuleTreeGetAction}), so no path resolution is needed — the pane
 * fetches straight from the served artifact. This is the real assembled JS,
 * which is exactly what Phase 5 will segment (prologue / body / epilogue) and
 * the engine will run rules over; here it is shown whole. Only module leaves
 * are fetched (a package / root open is ignored). A monotonic sequence guard
 * drops stale responses when the user opens several modules quickly.</p>
 */
public final class ModuleContentWidget extends WorkspaceWidget<WorkspaceWidget._None, ModuleContentWidget> {

    public static final ModuleContentWidget INSTANCE = new ModuleContentWidget();

    private ModuleContentWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ModuleContentWidget> {}

    @Override protected _Construct<_None, ModuleContentWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Full Content"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    root.style.cssText = 'height:100%;display:flex;flex-direction:column;'",
                "        + 'box-sizing:border-box;font-family:system-ui,sans-serif;';",
                "",
                "    var header = branch.createElement('header', 'div');",
                "    header.style.cssText = 'padding:8px 12px;font-size:12px;'",
                "        + 'border-bottom:1px solid var(--color-border,rgba(0,0,0,.12));'",
                "        + 'color:var(--st-gray-mid,#666);font-family:ui-monospace,Menlo,Consolas,monospace;';",
                "    header.textContent = 'Open a module to view its served JavaScript.';",
                "",
                "    var pre = branch.createElement('pre', 'pre');",
                "    pre.style.cssText = 'margin:0;flex:1;overflow:auto;padding:12px;'",
                "        + 'font:12px/1.5 ui-monospace,Menlo,Consolas,monospace;'",
                "        + 'white-space:pre;tab-size:2;'",
                "        + 'background:var(--color-surface,#fff);color:var(--color-text-primary,#1a1a2e);';",
                "",
                "    root.appendChild(header);",
                "    root.appendChild(pre);",
                "",
                "    var __seq = 0;",
                "    function openNode(node) {",
                "        if (!node || node.kind !== 'module' || !node.summary) return;",
                "        var fqcn = node.summary;",
                "        var mine = ++__seq;",
                "        header.textContent = fqcn;",
                "        pre.textContent = 'Loading\\u2026';",
                "        fetch('/module?class=' + encodeURIComponent(fqcn))",
                "            .then(function (r) {",
                "                if (!r.ok) throw new Error('HTTP ' + r.status);",
                "                return r.text();",
                "            })",
                "            .then(function (text) {",
                "                if (mine !== __seq) return;",  // a newer open superseded this
                "                pre.textContent = text;",
                "            })",
                "            .catch(function (err) {",
                "                if (mine !== __seq) return;",
                "                pre.style.color = '#c00';",
                "                pre.textContent = 'Failed to load served JS: '",
                "                    + (err && err.message ? err.message : String(err));",
                "            });",
                "    }",
                "",
                "    var __actorId  = null;",
                "    var __navParty = (workspaceCtx && workspaceCtx.navParty)",
                "                   ? workspaceCtx.navParty : null;",
                "    if (__navParty) {",
                "        __actorId = 'conformance/content-' + Math.random().toString(36).slice(2, 8);",
                "        __navParty.joinActor({",
                "            id: __actorId,",
                "            parentSecretary: 'navigation',",
                "            reactors: {",
                "                OpenDoc: function (msg) { openNode(msg.node); }",
                "            }",
                "        });",
                "    }",
                "",
                "    return {",
                "        root: root,",
                "        setActive: function (active) {},",
                "        partyDeregister: function () {",
                "            if (__actorId && __navParty) {",
                "                try { __navParty.leave(__actorId); } catch (e) {}",
                "            }",
                "        }",
                "    };"
        );
    }
}
