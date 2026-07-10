package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.Widget;
import hue.captains.singapura.js.homing.core.js.DocTreeRendererModule;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.libs.MarkedJs;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;
import hue.captains.singapura.js.homing.studio.base.export.HtmlExportModule;
import hue.captains.singapura.js.homing.studio.base.image.ImageViewerRenderer;
import hue.captains.singapura.js.homing.studio.base.table.TableViewerRenderer;
import hue.captains.singapura.js.homing.studio.base.widget.DocWidget;

import java.util.List;

/**
 * RFC 0039 — the rigid-tree {@link ComposedDoc} viewer. The doc-tree sibling of
 * {@code ComposedWidget}: it fetches the two-part payload from
 * {@code /doc-tree?id=<uuid>} ({@link DocTreeGetAction}) and mounts the
 * substrate {@code renderDocTree} ({@link DocTreeRendererModule}), wiring the
 * injected {@code renderContent} callback to the existing per-segment renderers
 * (RFC 0024 P1c).
 *
 * <p>The TOC is the substrate {@code TreeRenderer} over the structure, and its
 * selection navigates the body — no {@code TocSidebarRenderer}. {@code
 * ComposedSegment} is structure (a graft), so there is no recursive
 * {@code mountComposed} re-entry and no client cycle stack — the widget is
 * markedly simpler than {@code ComposedWidget}. Parallel-first: both coexist
 * (legacy {@code /doc?id=} vs {@code /doc-tree?id=}) until the migration flips.</p>
 *
 * @since homing-studio-base — RFC 0039 rigid-tree doc
 */
public final class DocTreeWidget extends DocWidget<DocTreeWidget.Params, DocTreeWidget> {

    public static final DocTreeWidget INSTANCE = new DocTreeWidget();

    private DocTreeWidget() {}

    /** @param id UUID of the ComposedDoc to render as a rigid tree. */
    public record Params(String id) implements Widget._Param {}

    private record mountInto() implements Widget._MountInto<Params, DocTreeWidget> {}

    @Override public String simpleName() { return "doc-tree-widget"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public String title() { return "doc"; }

    @Override
    protected Widget._MountInto<Params, DocTreeWidget> mountInto() {
        return new mountInto();
    }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new domOpsParty()), DomOpsPartyModule.INSTANCE),
                new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()), HrefManager.INSTANCE),
                new ModuleImports<>(List.of(new MarkedJs.marked()), MarkedJs.INSTANCE),
                new ModuleImports<>(List.of(new TableViewerRenderer.renderTable()), TableViewerRenderer.INSTANCE),
                new ModuleImports<>(List.of(new ImageViewerRenderer.renderImage()), ImageViewerRenderer.INSTANCE),
                // The doc-tree renderer (TOC + body + selection→navigate).
                new ModuleImports<>(List.of(new DocTreeRendererModule.renderDocTree()),
                        DocTreeRendererModule.INSTANCE),
                // Node caption (the highlighted header) — injected as renderCaption.
                new ModuleImports<>(List.of(new CaptionRenderer.renderCaption()),
                        CaptionRenderer.INSTANCE),
                // Standalone HTML export (DocReader export) — same pill as ComposedWidget.
                new ModuleImports<>(List.of(new HtmlExportModule.exportPageAsHtml()),
                        HtmlExportModule.INSTANCE),
                // Per-segment renderers (no ComposedSegmentRenderer — embeds are structure).
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
                new ModuleImports<>(List.of(
                        new ListSegmentRenderer.renderUnorderedListSegment(),
                        new ListSegmentRenderer.renderOrderedListSegment()),
                        ListSegmentRenderer.INSTANCE),
                new ModuleImports<>(List.of(new ParagraphSegmentRenderer.renderParagraphSegment()),
                        ParagraphSegmentRenderer.INSTANCE),
                new ModuleImports<>(List.of(new DocumentaryWidgetSegmentRenderer.renderDocumentaryWidgetSegment()),
                        DocumentaryWidgetSegmentRenderer.INSTANCE),
                new ModuleImports<>(List.of(
                        new StudioStyles.st_loading(),
                        new StudioStyles.st_error()
                ), StudioStyles.INSTANCE));
    }

    @Override
    protected List<String> bodyJs() {
        return List.of(
                "    // Two locators (symmetric with ComposedWidget): a direct uuid",
                "    // (?id=, the standalone doc-tree-viewer) or a leveled tree path",
                "    // (?l0=&l1=…, the Navigator's leveled Open). Both hit /doc-tree.",
                "    var fetchUrl, ownerLabel;",
                "    if (params.id) {",
                "        fetchUrl = '/doc-tree?id=' + encodeURIComponent(params.id);",
                "        ownerLabel = params.id;",
                "    } else if (params.treeId || params['l0'] !== undefined) {",
                "        var pq = [];",
                "        if (params.treeId) pq.push('treeId=' + encodeURIComponent(params.treeId));",
                "        for (var i = 0; params['l' + i] !== undefined; i++) {",
                "            pq.push('l' + i + '=' + encodeURIComponent(params['l' + i]));",
                "        }",
                "        fetchUrl = '/doc-tree?' + pq.join('&');",
                "        ownerLabel = pq.join('&');",
                "    } else {",
                "        var noId = branch.createElement('noId', 'div');",
                "        css.addClass(noId, st_error);",
                "        noId.textContent = 'No doc reference supplied. Use ?id=<uuid> or a tree path ?l0=…';",
                "        parent.appendChild(noId);",
                "        return;",
                "    }",
                "",
                "    var owner = Object.freeze({ toString: function(){",
                "        return 'docTreeWidget:' + ownerLabel;",
                "    } });",
                "    var ctx = { renderingStack: [] };",
                "",
                "    var bodyBranch = branch.createBranch('body');",
                "    bodyBranch.activate(owner);",
                "    var bodyHost = bodyBranch.createElement('bodyHost', 'div');",
                "    parent.appendChild(bodyHost);",
                "",
                "    var loading = bodyBranch.createElement('loading', 'div');",
                "    css.addClass(loading, st_loading);",
                "    loading.textContent = 'Loading\\u2026';",
                "    bodyHost.appendChild(loading);",
                "",
                "    // Dispatch ONE content object to its per-segment renderer (RFC 0024 P1c).",
                "    // This is the renderDocTree `renderContent` callback — the only studio-",
                "    // specific glue; the renderer itself stays substrate-level.",
                "    function renderContent(seg, host, segBranch, key) {",
                "        segBranch.activate(owner);",
                "        switch (seg.kind) {",
                "            case 'text':     renderTextSegment(segBranch, host, seg, ctx); break;",
                "            case 'markdown': renderMarkdownSegment(segBranch, host, seg, ctx); break;",
                "            case 'code':     renderCodeSegment(segBranch, host, seg, ctx); break;",
                "            case 'svg':      renderSvgSegment(segBranch, host, seg, ctx); break;",
                "            case 'table':    renderTableSegment(segBranch, host, seg, ctx); break;",
                "            case 'image':    renderImageSegment(segBranch, host, seg, ctx); break;",
                "            case 'relation': renderRelationSegment(segBranch, host, seg, ctx); break;",
                "            case 'ulist':    renderUnorderedListSegment(segBranch, host, seg, ctx); break;",
                "            case 'olist':    renderOrderedListSegment(segBranch, host, seg, ctx); break;",
                "            case 'paragraph': renderParagraphSegment(segBranch, host, seg, ctx); break;",
                "            case 'documentary-widget':",
                "                             renderDocumentaryWidgetSegment(segBranch, host, seg, ctx); break;",
                "            default:",
                "                var unk = segBranch.createElement('unknown', 'div');",
                "                css.addClass(unk, st_error);",
                "                unk.textContent = 'Unknown content kind: ' + seg.kind;",
                "                host.appendChild(unk);",
                "        }",
                "    }",
                "    // Expose the dispatcher so list segments can render their items recursively.",
                "    ctx.renderContent = renderContent;",
                "",
                "    fetch(fetchUrl)",
                "        .then(function(r){ if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
                "        .then(function(payload){",
                "            loading.remove();",
                "            renderDocTree({",
                "                branch:        bodyBranch,",
                "                container:     bodyHost,",
                "                payload:       payload,",
                "                renderContent: renderContent,",
                "                renderCaption: renderCaption,",
                "                expandDepth:   99",
                "            });",
                "",
                "            // Floating Export HTML pill — same control as ComposedWidget.",
                "            // The TOC (data-export-chrome) and body (data-export-content)",
                "            // are marked by renderDocTree; this pill is data-export-exclude.",
                "            var rootDims = (payload.structure && payload.structure.dimensions) || [];",
                "            var docTitle = '';",
                "            for (var di = 0; di < rootDims.length; di++) {",
                "                if (rootDims[di].key === 'displayLabel') { docTitle = rootDims[di].text || ''; break; }",
                "            }",
                "            var _slug = (docTitle || 'doc')",
                "                .replace(/[^\\w\\s-]/g, '').replace(/\\s+/g, '-').replace(/-+/g, '-').trim() || 'doc';",
                "            document.title = docTitle || document.title;",
                "            var exportBar = bodyBranch.createElement('exportBar', 'div');",
                "            exportBar.setAttribute('data-export-exclude', '');",
                "            exportBar.style.cssText = 'position:fixed;bottom:24px;right:24px;z-index:200;'",
                "                + 'display:flex;align-items:center;gap:8px;font:13px system-ui,sans-serif;'",
                "                + 'padding:6px 12px;border:1px solid var(--color-border,rgba(0,0,0,.2));'",
                "                + 'border-radius:20px;background:var(--color-surface,#fff);color:inherit;'",
                "                + 'box-shadow:0 2px 8px rgba(0,0,0,.15);opacity:0.9;';",
                "            var chromeLabel = document.createElement('label');",
                "            chromeLabel.style.cssText = 'display:flex;align-items:center;gap:4px;cursor:pointer;user-select:none;';",
                "            var chromeChk = document.createElement('input');",
                "            chromeChk.type = 'checkbox'; chromeChk.checked = true; chromeChk.style.cssText = 'margin:0;';",
                "            var chromeTxt = document.createElement('span');",
                "            chromeTxt.textContent = 'Include page chrome';",
                "            chromeLabel.appendChild(chromeChk); chromeLabel.appendChild(chromeTxt);",
                "            var exportBtn = document.createElement('button');",
                "            exportBtn.textContent = 'Export HTML';",
                "            exportBtn.style.cssText = 'cursor:pointer;font:inherit;padding:4px 12px;'",
                "                + 'border:1px solid var(--color-border,rgba(0,0,0,.2));border-radius:14px;'",
                "                + 'background:transparent;color:inherit;';",
                "            exportBtn.addEventListener('click', function() {",
                "                var includeChrome = chromeChk.checked;",
                "                exportBtn.disabled = true; exportBtn.textContent = 'Exporting\\u2026';",
                "                var suffix = includeChrome ? '' : '-content';",
                "                exportPageAsHtml(_slug + suffix + '.html', { includeChrome: includeChrome })",
                "                    .then(function() { exportBtn.textContent = 'Export HTML'; exportBtn.disabled = false; })",
                "                    .catch(function(err) {",
                "                        exportBtn.textContent = 'Export failed'; exportBtn.disabled = false;",
                "                        console.error('exportPageAsHtml failed:', err);",
                "                    });",
                "            });",
                "            exportBar.appendChild(chromeLabel); exportBar.appendChild(exportBtn);",
                "            document.body.appendChild(exportBar);",
                "        })",
                "        .catch(function(err){",
                "            loading.remove();",
                "            var errEl = bodyBranch.createElement('error', 'div');",
                "            css.addClass(errEl, st_error);",
                "            errEl.textContent = 'Failed to load doc tree: '",
                "                + (err && err.message ? err.message : String(err));",
                "            bodyHost.appendChild(errEl);",
                "        });"
        );
    }
}
