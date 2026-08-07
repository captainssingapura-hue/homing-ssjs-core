package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * RFC 0044 — the conformance workspace Navigator: a module tree. Structurally
 * the studio {@code TreeWidget}, but pointed at {@code GET /module-tree}
 * ({@link ModuleTreeGetAction}) instead of the catalogue feed. A dedicated
 * widget (not a forced reuse of the catalogue Navigator) is what gives the
 * conformance workspace room to grow module-specific navigation later; the
 * genuinely reusable part — the {@code TreeRenderer} and the party protocol —
 * is shared verbatim.
 *
 * <p>Publishes to the navigation party (exposed as {@code workspaceCtx.navParty}):
 * a single click / arrow-move sends {@code NodeSelected} (redirected by the
 * secretary as {@code NavigateTo} — cheap, for the Summary + Conformance
 * panes); Enter / double-click sends {@code NodeOpened} (redirected as
 * {@code OpenDoc} — the intentional signal the Content pane fetches on).</p>
 */
public final class ModuleTreeWidget extends WorkspaceWidget<WorkspaceWidget._None, ModuleTreeWidget> {

    public static final ModuleTreeWidget INSTANCE = new ModuleTreeWidget();

    private ModuleTreeWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ModuleTreeWidget> {}

    @Override protected _Construct<_None, ModuleTreeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Modules"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new TreeRendererModule.TreeRenderer()),
                        TreeRendererModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    root.style.cssText = 'height:100%;overflow:auto;padding:8px 4px;'",
                "        + 'box-sizing:border-box;font-family:system-ui,sans-serif;';",
                "    var container = branch.createElement('treeContainer', 'div');",
                "    root.appendChild(container);",
                "",
                "    var status = branch.createElement('status', 'div');",
                "    status.style.cssText = 'padding:6px 8px;color:#888;font-size:12px;';",
                "    status.textContent = 'Loading modules\\u2026';",
                "    container.appendChild(status);",
                "",
                "    var __actorId  = null;",
                "    var __navParty = (workspaceCtx && workspaceCtx.navParty)",
                "                   ? workspaceCtx.navParty : null;",
                "    if (__navParty) {",
                "        __actorId = 'conformance/tree-' + Math.random().toString(36).slice(2, 8);",
                "        __navParty.joinActor({",
                "            id: __actorId,",
                "            parentSecretary: 'navigation',",
                "            reactors: {",
                "                NavigateTo: function (msg) {",
                "                    console.log('[ModuleTreeWidget] NavigateTo', msg.node);",
                "                }",
                "            }",
                "        });",
                "    }",
                "",
                "    var __renderer = null;",
                "    var __keyHandler = function (ev) {",
                "        if (__renderer && __renderer.handleKeydown(ev)) ev.preventDefault();",
                "    };",
                "",
                "    fetch('/module-tree')",
                "        .then(function (r) {",
                "            if (!r.ok) throw new Error('HTTP ' + r.status);",
                "            return r.json();",
                "        })",
                "        .then(function (treeJson) {",
                "            container.removeChild(status);",
                "            __renderer = new TreeRenderer({",
                "                branch:      branch,",
                "                container:   container,",
                "                data:        treeJson,",
                "                expandDepth: 2,",
                "                onSelect:    function (sel) {",
                "                    if (__navParty && __actorId) {",
                "                        __navParty.tellFrom(__actorId,",
                "                            { kind: 'NodeSelected', node: sel });",
                "                    }",
                "                },",
                "                onActivate:  function (sel) {",
                "                    if (__navParty && __actorId) {",
                "                        __navParty.tellFrom(__actorId,",
                "                            { kind: 'NodeOpened', node: sel });",
                "                    }",
                "                }",
                "            });",
                "        })",
                "        .catch(function (err) {",
                "            status.style.color = '#c00';",
                "            status.textContent = 'Failed to load modules: '",
                "                + (err && err.message ? err.message : String(err));",
                "            console.error('[ModuleTreeWidget] fetch failed:', err);",
                "        });",
                "",
                "    return {",
                "        root: root,",
                "        setActive: function (active) {",
                "            if (active) document.addEventListener('keydown', __keyHandler);",
                "            else        document.removeEventListener('keydown', __keyHandler);",
                "        },",
                "        partyDeregister: function () {",
                "            document.removeEventListener('keydown', __keyHandler);",
                "            if (__actorId && __navParty) {",
                "                try { __navParty.leave(__actorId); } catch (e) {}",
                "            }",
                "        }",
                "    };"
        );
    }
}
