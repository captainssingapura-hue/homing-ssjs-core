package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The components workbench's Navigator: the catalogue of components the
 * studio's crates deliver, composed from the crate closure and fed by
 * {@code GET /component-tree} ({@link ComponentTreeGetAction}) — root →
 * vehicle → family → component. Structurally the crate Navigator with the
 * feed swapped; the tree renderer and the party protocol are shared
 * verbatim: a click or an arrow-move sends {@code NodeSelected} (redirected
 * as {@code NavigateTo}, which the Summary pane renders), Enter or a
 * double-click {@code NodeOpened}.
 */
public final class ComponentTreeWidget extends WorkspaceWidget<WorkspaceWidget._None, ComponentTreeWidget> {

    public static final ComponentTreeWidget INSTANCE = new ComponentTreeWidget();

    private ComponentTreeWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ComponentTreeWidget> {}

    @Override protected _Construct<_None, ComponentTreeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Components"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new TreeRendererModule.TreeRenderer()), TreeRendererModule.INSTANCE),
                new ModuleImports<>(List.of(new ComponentsStyles.cw_tree(), new ComponentsStyles.cw_status(), new ComponentsStyles.cw_status_failed()), ComponentsStyles.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    css.addClass(root, cw_tree);",
                "    var container = branch.createElement('treeContainer', 'div');",
                "    root.appendChild(container);",
                "",
                "    var status = branch.createElement('status', 'div');",
                "    css.addClass(status, cw_status);",
                "    status.textContent = 'Loading components\\u2026';",
                "    container.appendChild(status);",
                "",
                "    var __actorId  = null;",
                "    var __navParty = (workspaceCtx && workspaceCtx.navParty) ? workspaceCtx.navParty : null;",
                "    if (__navParty) {",
                "        __actorId = 'components/tree-' + Math.random().toString(36).slice(2, 8);",
                "        __navParty.joinActor({ id: __actorId, parentSecretary: 'navigation', reactors: { NavigateTo: function (msg) {} } });",
                "    }",
                "",
                "    var __renderer = null;",
                "    var __keyHandler = function (ev) { if (__renderer && __renderer.handleKeydown(ev)) ev.preventDefault(); };",
                "",
                "    fetch('/component-tree')",
                "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
                "        .then(function (treeJson) {",
                "            container.removeChild(status);",
                "            __renderer = new TreeRenderer({",
                "                branch: branch, container: container, data: treeJson, expandDepth: 2,",
                "                onSelect:   function (sel) { if (__navParty && __actorId) __navParty.tellFrom(__actorId, { kind: 'NodeSelected', node: sel }); },",
                "                onActivate: function (sel) { if (__navParty && __actorId) __navParty.tellFrom(__actorId, { kind: 'NodeOpened', node: sel }); }",
                "            });",
                "        })",
                "        .catch(function (err) {",
                "            css.addClass(status, cw_status_failed);",
                "            status.textContent = 'Failed to load the components: ' + (err && err.message ? err.message : String(err));",
                "            console.error('[ComponentTreeWidget] fetch failed:', err);",
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
                "            if (__actorId && __navParty) { try { __navParty.leave(__actorId); } catch (e) {} }",
                "        }",
                "    };"
        );
    }
}
