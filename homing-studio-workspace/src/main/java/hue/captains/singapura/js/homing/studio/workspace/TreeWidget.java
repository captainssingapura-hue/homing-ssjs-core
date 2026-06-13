package hue.captains.singapura.js.homing.studio.workspace;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Studio Workspace's first widget — a navigation tree. Fetches a
 * catalogue subtree as canonical {@code TreeNode} JSON from
 * {@code GET /catalogue-tree?id=<catalogueId>} (served by
 * {@link CatalogueTreeGetAction}) and renders it with the generic
 * {@code TreeRenderer} from homing-core-js. Zero per-tree-kind JS: the
 * renderer reads only the substrate's universal dimensions.
 *
 * <p>{@link Params#catalogueId()} selects which catalogue subtree to show;
 * blank means the whole studio forest (the endpoint's default). For the
 * pinned use in {@link StudioWorkspaceSpec} the default is blank.</p>
 *
 * <p>Tree selections are published to the {@code NavigatorParty} (exposed
 * as {@code workspaceCtx.navParty}): on select the widget sends a
 * {@code NodeSelected} message; the {@code NavigatorSecretary} redirects it
 * as a {@code NavigateTo} broadcast to every member. This widget also joins
 * as a member and logs the redirect it receives, so the round-trip is
 * observable with just the tree present; a future content/detail pane joins
 * the same Party to render the selection. When the workspace provides no
 * Party, selection falls back to a console log.</p>
 *
 * <p>Nothing to gate on workspace-active transitions (no document
 * listeners, no audio) — {@code setActive} is a required no-op.</p>
 *
 * @since homing-studio-workspace — Studio Workspace, first widget (tree view)
 */
public final class TreeWidget extends WorkspaceWidget<TreeWidget.Params, TreeWidget> {

    public static final TreeWidget INSTANCE = new TreeWidget();

    private TreeWidget() {}

    /**
     * @param catalogueId name-slug of the catalogue to root the tree at;
     *                    blank → the whole studio forest.
     *
     * <p>The {@code DEFAULTS} constant signals the picker to skip its params
     * form and deliver {@code {}} — opening the Navigator is a one-click
     * action, no {@code catalogueId} prompt. The widget's JS-side default
     * (blank → whole forest) applies. See {@code WidgetEntriesJson}.</p>
     */
    public record Params(String catalogueId) implements WorkspaceWidget._Param {
        public static final Params DEFAULTS = new Params("");
    }

    private record construct() implements WorkspaceWidget._Construct<Params, TreeWidget> {}

    @Override protected _Construct<Params, TreeWidget> construct() { return new construct(); }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public String title() { return "Navigator"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new TreeRendererModule.TreeRenderer()),
                        TreeRendererModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        // Each string is one JS line (joined with '\n' at emit time).
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    root.style.cssText = 'height:100%;overflow:auto;padding:8px 4px;'",
                "        + 'box-sizing:border-box;font-family:system-ui,sans-serif;';",
                "    var container = branch.createElement('treeContainer', 'div');",
                "    root.appendChild(container);",
                "",
                "    var status = branch.createElement('status', 'div');",
                "    status.style.cssText = 'padding:6px 8px;color:#888;font-size:12px;';",
                "    status.textContent = 'Loading tree\\u2026';",
                "    container.appendChild(status);",
                "",
                "    // ── NavigatorParty wiring ───────────────────────────────────",
                "    // Join as an actor if the workspace exposed the Party. The tree",
                "    // is the SOURCE: on select it sends NodeSelected; the Secretary",
                "    // redirects it as a NavigateTo broadcast. We also react to that",
                "    // broadcast so the round-trip is observable with only the tree.",
                "    var __actorId  = null;",
                "    var __navParty = (workspaceCtx && workspaceCtx.navParty)",
                "                   ? workspaceCtx.navParty : null;",
                "    if (__navParty) {",
                "        __actorId = 'studio/tree-' + Math.random().toString(36).slice(2, 8);",
                "        __navParty.joinActor({",
                "            id: __actorId,",
                "            parentSecretary: 'navigation',",
                "            reactors: {",
                "                NavigateTo: function (msg) {",
                "                    console.log('[TreeWidget] NavigateTo', msg.node);",
                "                }",
                "            }",
                "        });",
                "    }",
                "",
                "    // Keyboard nav is gated by workspace-active (setActive below):",
                "    // only the active tab forwards keydown to the renderer, so",
                "    // multiple Navigators never fight over arrow keys. The renderer",
                "    // owns the key semantics (Up/Down move + select, Right/Left",
                "    // expand/fold); we just decide WHEN keys flow.",
                "    var __renderer = null;",
                "    var __keyHandler = function (ev) {",
                "        if (__renderer && __renderer.handleKeydown(ev)) ev.preventDefault();",
                "    };",
                "",
                "    var cid = (params && params.catalogueId) ? params.catalogueId : '';",
                "    var url = '/catalogue-tree?id=' + encodeURIComponent(cid);",
                "    fetch(url)",
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
                "                    console.log('[TreeWidget] selected', sel);",
                "                    // Publish the selection; the Secretary redirects it.",
                "                    if (__navParty && __actorId) {",
                "                        __navParty.tellFrom(__actorId,",
                "                            { kind: 'NodeSelected', node: sel });",
                "                    }",
                "                }",
                "            });",
                "        })",
                "        .catch(function (err) {",
                "            status.style.color = '#c00';",
                "            status.textContent = 'Failed to load tree: '",
                "                + (err && err.message ? err.message : String(err));",
                "            console.error('[TreeWidget] tree fetch failed:', err);",
                "        });",
                "",
                "    return {",
                "        root: root,",
                "        // Arrow-key navigation is active-gated: attach the keydown",
                "        // listener only while this tab is workspace-active (the",
                "        // framework's mature mechanism — same as MovingAnimalWidget).",
                "        setActive: function (active) {",
                "            if (active) document.addEventListener('keydown', __keyHandler);",
                "            else        document.removeEventListener('keydown', __keyHandler);",
                "        },",
                "        partyDeregister: function () {",
                "            // Belt-and-braces: drop the key listener on teardown even",
                "            // if setActive(false) wasn't called first.",
                "            document.removeEventListener('keydown', __keyHandler);",
                "            if (__actorId && __navParty) {",
                "                try { __navParty.leave(__actorId); } catch (e) {}",
                "            }",
                "        }",
                "    };"
        );
    }
}
