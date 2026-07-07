package hue.captains.singapura.js.homing.color.studio;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Color Studio's tree pane — browses the ColorGroup taxonomy. Fetches
 * {@code GET /color-tree} ({@link ColorTreeGetAction}) as canonical
 * {@code TreeNode} JSON and renders it with the generic {@code TreeRenderer}
 * (zero bespoke tree JS). On selection it publishes {@code NodeSelected} to the
 * {@code colornav} Party ({@code workspaceCtx.colorParty}); the
 * {@code ColorNavSecretary} redirects it as {@code NavigateTo}, which the
 * {@code ColorListWidget} consumes.
 */
public final class ColorTreeWidget extends WorkspaceWidget<WorkspaceWidget._None, ColorTreeWidget> {

    public static final ColorTreeWidget INSTANCE = new ColorTreeWidget();

    private ColorTreeWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ColorTreeWidget> {}

    @Override protected _Construct<_None, ColorTreeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Color Tree"; }
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
                "    status.textContent = 'Loading colours\\u2026';",
                "    container.appendChild(status);",
                "",
                "    // Join the colornav Party as the SOURCE actor (if exposed).",
                "    var __actorId    = null;",
                "    var __colorParty = (workspaceCtx && workspaceCtx.colorParty)",
                "                     ? workspaceCtx.colorParty : null;",
                "    if (__colorParty) {",
                "        __actorId = 'color/tree-' + Math.random().toString(36).slice(2, 8);",
                "        __colorParty.joinActor({",
                "            id: __actorId,",
                "            parentSecretary: 'colornav',",
                "            reactors: {",
                "                NavigateTo: function (msg) {",
                "                    console.log('[ColorTreeWidget] NavigateTo', msg.node);",
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
                "    fetch('/color-tree')",
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
                "                    if (__colorParty && __actorId) {",
                "                        __colorParty.tellFrom(__actorId,",
                "                            { kind: 'NodeSelected', node: sel });",
                "                    }",
                "                }",
                "            });",
                "        })",
                "        .catch(function (err) {",
                "            status.style.color = '#c00';",
                "            status.textContent = 'Failed to load colour tree: '",
                "                + (err && err.message ? err.message : String(err));",
                "            console.error('[ColorTreeWidget] tree fetch failed:', err);",
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
                "            if (__actorId && __colorParty) {",
                "                try { __colorParty.leave(__actorId); } catch (e) {}",
                "            }",
                "        }",
                "    };"
        );
    }
}
