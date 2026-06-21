package hue.captains.singapura.js.homing.studio.workspace;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DocTreeRendererModule;
import hue.captains.singapura.js.homing.libs.MarkedJs;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.composed.CodeSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.DocumentaryWidgetSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.ImageSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.MarkdownSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.RelationSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.SvgSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.TableSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.TextSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;
import hue.captains.singapura.js.homing.studio.base.image.ImageViewerRenderer;
import hue.captains.singapura.js.homing.studio.base.table.TableViewerRenderer;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Studio Workspace's content pane — a Party consumer that renders the
 * <b>opened</b> document in place, beside the Navigator. The expensive sibling
 * of {@link SummaryWidget}: where Summary re-labels a card on every cheap
 * selection, this pane fetches and renders the whole rigid-tree doc, so it
 * reacts ONLY to the intentional <em>open</em> signal — never to plain browsing.
 *
 * <p><b>Two-tier navigation (RFC 0028 + RFC 0039/0040).</b> The {@link TreeWidget}
 * publishes two message tiers to the {@code NavigatorParty}: {@code NodeSelected}
 * on every arrow-move / single click (redirected as {@code NavigateTo} — cheap,
 * for Summary), and {@code NodeOpened} on Enter / double-click (redirected by
 * the {@code NavigatorSecretary} as {@code OpenDoc} — the deliberate open). This
 * widget joins as an Actor and reacts to {@code OpenDoc} only, so the costly
 * fetch + render happens on intent, not on every selection move.</p>
 *
 * <p><b>Render.</b> On {@code OpenDoc} it reads the node's leveled tree
 * {@code path} ({@code [l0, l1, …]} — its structural position, not a stamped id)
 * and fetches {@code /doc-tree?l0=..&l1=..} ({@code DocTreeGetAction} resolves it
 * via {@code ForestPathResolver}), then mounts the substrate
 * {@code renderDocTree} ({@link DocTreeRendererModule}) with the same per-segment
 * {@code renderContent} dispatch {@code DocTreeWidget} uses — identical reading
 * experience (foldable TOC, synced highlight) inline in the pane. No export pill:
 * that floating control belongs to the standalone doc page, not a workspace pane.</p>
 *
 * <p><b>Kind-gated.</b> The kinds {@code /doc-tree} can transform render here:
 * {@code composed} (ComposedDoc + RigidDoc) and {@code doc} / {@code markdown}
 * (legacy markdown docs, rendered flat as one node so the existing corpus reads
 * in the workspace without migration). Other kinds (svg / plan / app) and branch
 * catalogue nodes can't render here, so the pane degrades to a hint pointing at
 * the Summary pane's Open button rather than erroring.</p>
 *
 * <p><b>Re-render discipline (No DOM Destruction).</b> Each open dissolves the
 * previous render's DomOpsParty sub-branch and mints a fresh, uniquely-named one
 * ({@code branch.dissolve()} cascades DOM + party teardown), so a re-open never
 * collides element names or leaks listeners. A monotonic sequence guards the
 * async fetch: a stale response whose open was superseded is dropped.</p>
 *
 * <p>Paramless ({@link WorkspaceWidget._None}) — opened in one click from the
 * picker with no form. Nothing to gate on workspace-active transitions, so
 * {@code setActive} is a required no-op.</p>
 *
 * @since homing-studio-workspace — studio navigation Party (content pane)
 */
public final class DocContentWidget extends WorkspaceWidget<WorkspaceWidget._None, DocContentWidget> {

    public static final DocContentWidget INSTANCE = new DocContentWidget();

    private DocContentWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DocContentWidget> {}

    @Override protected _Construct<_None, DocContentWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Document"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        // The doc-tree render stack — the SAME set DocTreeWidget uses, minus the
        // HTML-export pill (a pane has no standalone-page export). The
        // StudioStyles import gives typed classes for the pane's own
        // container/empty/loading/error elements AND auto-injects the `css`
        // manager binding (ManagerInjector) — no inline styles. DocTreeRenderer
        // brings TreeRenderer transitively; each per-segment renderer its deps.
        return List.of(
                new ModuleImports<>(List.of(
                        new StudioStyles.st_doc_pane(), new StudioStyles.st_doc_empty(),
                        new StudioStyles.st_loading(), new StudioStyles.st_error()
                ), StudioStyles.INSTANCE),
                new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()), HrefManager.INSTANCE),
                new ModuleImports<>(List.of(new MarkedJs.marked()), MarkedJs.INSTANCE),
                new ModuleImports<>(List.of(new TableViewerRenderer.renderTable()), TableViewerRenderer.INSTANCE),
                new ModuleImports<>(List.of(new ImageViewerRenderer.renderImage()), ImageViewerRenderer.INSTANCE),
                new ModuleImports<>(List.of(new DocTreeRendererModule.renderDocTree()),
                        DocTreeRendererModule.INSTANCE),
                new ModuleImports<>(List.of(new TextSegmentRenderer.renderTextSegment()),
                        TextSegmentRenderer.INSTANCE),
                new ModuleImports<>(List.of(new MarkdownSegmentRenderer.renderMarkdownSegment()),
                        MarkdownSegmentRenderer.INSTANCE),
                new ModuleImports<>(List.of(new CodeSegmentRenderer.renderCodeSegment()),
                        CodeSegmentRenderer.INSTANCE),
                new ModuleImports<>(List.of(new SvgSegmentRenderer.renderSvgSegment()),
                        SvgSegmentRenderer.INSTANCE),
                new ModuleImports<>(List.of(new TableSegmentRenderer.renderTableSegment()),
                        TableSegmentRenderer.INSTANCE),
                new ModuleImports<>(List.of(new ImageSegmentRenderer.renderImageSegment()),
                        ImageSegmentRenderer.INSTANCE),
                new ModuleImports<>(List.of(new RelationSegmentRenderer.renderRelationSegment()),
                        RelationSegmentRenderer.INSTANCE),
                new ModuleImports<>(List.of(new DocumentaryWidgetSegmentRenderer.renderDocumentaryWidgetSegment()),
                        DocumentaryWidgetSegmentRenderer.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        // Each string is one JS line (joined with '\n' at emit time).
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    css.addClass(root, st_doc_pane);",
                "",
                "    // Empty state — shown until the user OPENS a doc (Enter / double-",
                "    // click in the Navigator). Plain selection never reaches this pane.",
                "    var empty = branch.createElement('empty', 'div');",
                "    css.addClass(empty, st_doc_empty);",
                "    var EMPTY_MSG = 'Open a document from the Navigator \\u2014 press Enter '",
                "        + 'or double-click a leaf \\u2014 to read it here.';",
                "    empty.textContent = EMPTY_MSG;",
                "    root.appendChild(empty);",
                "",
                "    // Per-open render scope: a fresh DomOpsParty sub-branch each open,",
                "    // dissolved on the next (No DOM Destruction). __seq names branches",
                "    // uniquely AND guards the async fetch against a superseded open.",
                "    var ctx        = { renderingStack: [] };",
                "    var __owner    = Object.freeze({ toString: function () { return 'docContentWidget'; } });",
                "    var __renderBr = null;",
                "    var __seq      = 0;",
                "",
                "    function tearDown() {",
                "        if (empty.parentNode) root.removeChild(empty);",
                "        if (__renderBr) { try { __renderBr.dissolve(); } catch (e) {} __renderBr = null; }",
                "    }",
                "    function showHint(msg) {",
                "        tearDown();",
                "        empty.textContent = msg || EMPTY_MSG;",
                "        root.appendChild(empty);",
                "    }",
                "",
                "    // Dispatch ONE content object to its per-segment renderer (RFC 0024",
                "    // P1c) — the renderDocTree `renderContent` callback. Identical to",
                "    // DocTreeWidget's, the only studio-specific glue; the renderer stays",
                "    // substrate-level.",
                "    function renderContent(seg, host, segBranch, key) {",
                "        segBranch.activate(__owner);",
                "        switch (seg.kind) {",
                "            case 'text':     renderTextSegment(segBranch, host, seg, ctx); break;",
                "            case 'markdown': renderMarkdownSegment(segBranch, host, seg, ctx); break;",
                "            case 'code':     renderCodeSegment(segBranch, host, seg, ctx); break;",
                "            case 'svg':      renderSvgSegment(segBranch, host, seg, ctx); break;",
                "            case 'table':    renderTableSegment(segBranch, host, seg, ctx); break;",
                "            case 'image':    renderImageSegment(segBranch, host, seg, ctx); break;",
                "            case 'relation': renderRelationSegment(segBranch, host, seg, ctx); break;",
                "            case 'documentary-widget':",
                "                             renderDocumentaryWidgetSegment(segBranch, host, seg, ctx); break;",
                "            default:",
                "                var unk = segBranch.createElement('unknown', 'div');",
                "                css.addClass(unk, st_error);",
                "                unk.textContent = 'Unknown content kind: ' + seg.kind;",
                "                host.appendChild(unk);",
                "        }",
                "    }",
                "",
                "    // Open a node IN PLACE. The kinds /doc-tree can transform render",
                "    // here: 'composed' (ComposedDoc + RigidDoc) and 'doc'/'markdown'",
                "    // (legacy markdown docs, rendered flat). Other kinds (svg / plan /",
                "    // app) + branch catalogues degrade to a hint (use Summary's Open).",
                "    var RENDERABLE = { composed: 1, doc: 1, markdown: 1 };",
                "    function openNode(node) {",
                "        if (!node || !node.path || node.kind === 'catalogue') {",
                "            showHint('That item has no readable document.');",
                "            return;",
                "        }",
                "        if (!RENDERABLE[node.kind]) {",
                "            showHint('\\u201c' + (node.label || 'This item') + '\\u201d is a '",
                "                + node.kind + ' document \\u2014 open it from the Summary pane.');",
                "            return;",
                "        }",
                "        tearDown();",
                "        var rb = branch.createBranch('docRender' + (++__seq));",
                "        rb.activate(__owner);",
                "        __renderBr = rb;",
                "        var mySeq = __seq;",
                "        var host = rb.createElement('host', 'div');",
                "        root.appendChild(host);",
                "        var loading = rb.createElement('loading', 'div');",
                "        css.addClass(loading, st_loading);",
                "        loading.textContent = 'Loading\\u2026';",
                "        host.appendChild(loading);",
                "",
                "        var pq = node.path.map(function (p, i) {",
                "            return 'l' + i + '=' + encodeURIComponent(p);",
                "        }).join('&');",
                "        fetch('/doc-tree?' + pq)",
                "            .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
                "            .then(function (payload) {",
                "                if (mySeq !== __seq) return;   // a newer open superseded this fetch",
                "                loading.remove();",
                "                renderDocTree({",
                "                    branch:        rb,",
                "                    container:     host,",
                "                    payload:       payload,",
                "                    renderContent: renderContent,",
                "                    expandDepth:   99",
                "                });",
                "            })",
                "            .catch(function (err) {",
                "                if (mySeq !== __seq) return;",
                "                loading.remove();",
                "                var errEl = rb.createElement('error', 'div');",
                "                css.addClass(errEl, st_error);",
                "                errEl.textContent = 'Failed to load document: '",
                "                    + (err && err.message ? err.message : String(err));",
                "                host.appendChild(errEl);",
                "            });",
                "    }",
                "",
                "    // ── NavigatorParty wiring ───────────────────────────────────",
                "    // Pure consumer of the OPEN tier. Joins and renders on each OpenDoc",
                "    // broadcast (Enter / double-click); ignores the NavigateTo (select)",
                "    // tier entirely — that is Summary's cheap concern.",
                "    var __actorId  = null;",
                "    var __navParty = (workspaceCtx && workspaceCtx.navParty)",
                "                   ? workspaceCtx.navParty : null;",
                "    if (__navParty) {",
                "        __actorId = 'studio/doc-content-' + Math.random().toString(36).slice(2, 8);",
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
                "            tearDown();",
                "            if (__actorId && __navParty) {",
                "                try { __navParty.leave(__actorId); } catch (e) {}",
                "            }",
                "        }",
                "    };"
        );
    }
}
