package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * RFC 0044 — the Crate-Studio dependency-graph pane. Fetches the backend-
 * generated Mermaid {@code flowchart} from {@code /crate-graph} and renders it
 * to SVG via the Mermaid proxy (dynamic-imported, so an unreachable CDN
 * degrades to a note rather than failing the whole widget — the same pattern
 * the doc renderer uses). A static diagram for now; interactivity comes later.
 */
public final class CrateGraphWidget extends WorkspaceWidget<WorkspaceWidget._None, CrateGraphWidget> {

    public static final CrateGraphWidget INSTANCE = new CrateGraphWidget();

    private CrateGraphWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, CrateGraphWidget> {}

    @Override protected _Construct<_None, CrateGraphWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Dependency Graph"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;'",
                "        + 'padding:16px;text-align:center;font-family:system-ui,sans-serif;';",
                "",
                "    var status = branch.createElement('status', 'div');",
                "    status.style.cssText = 'padding:8px;color:#888;font-size:13px;text-align:left;';",
                "    status.textContent = 'Loading dependency graph\\u2026';",
                "    root.appendChild(status);",
                "",
                "    var MERMAID_PROXY_URL = '/module?class=hue.captains.singapura.js.homing.libs.MermaidProxyModule';",
                "    function fail(msg) { status.style.color = '#c00'; status.textContent = msg; }",
                "",
                "    fetch('/crate-graph')",
                "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.text(); })",
                "        .then(function (code) {",
                "            return import(MERMAID_PROXY_URL).then(function (mod) {",
                "                var id = 'crate-graph-' + Math.random().toString(36).slice(2, 8);",
                "                return mod.renderMermaid(id, code).then(function (svg) {",
                "                    if (status.parentNode) root.removeChild(status);",
                "                    var wrap = document.createElement('div');",
                "                    wrap.style.cssText = 'display:inline-block;max-width:100%;';",
                "                    wrap.appendChild(document.createRange().createContextualFragment(svg));",
                "                    root.appendChild(wrap);",
                "                });",
                "            }).catch(function (err) {",
                "                fail('Mermaid could not be loaded (offline or blocked CDN). '",
                "                    + 'Point the proxy at a reachable URL via ExternalModuleUrlRegistry.');",
                "                console.error('[CrateGraphWidget] mermaid load/render failed:', err);",
                "            });",
                "        })",
                "        .catch(function (err) {",
                "            fail('Failed to load graph: ' + (err && err.message ? err.message : String(err)));",
                "        });",
                "",
                "    return {",
                "        root: root,",
                "        setActive: function (active) {}",
                "    };"
        );
    }
}
