package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.Widget;
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
 * RFC 0024 Phase P1c — the {@link Widget}-side rewrite of
 * {@code ComposedViewer}. Fetches a {@link ComposedDoc}'s JSON via
 * {@code /doc?id=<uuid>}, builds a two-column layout (TOC sidebar +
 * body), and dispatches each segment into its own sub-branch under
 * the widget's L2 branch.
 *
 * <h2>Branch tree under the widget's L2 branch</h2>
 *
 * <pre>{@code
 * widgetBranch (L2 from the shell)
 *   ├── 'layout-host' element        (st_layout container appended to parent)
 *   ├── 'toc-sidebar'                (TOC branch)
 *   │     └── 'toc-host' element     (rendered TOC list)
 *   └── 'body'                       (body branch)
 *         └── 'body-host' element    (segments appended below)
 *               (each segment renders into its own sub-branch under 'body')
 * }</pre>
 *
 * <h2>Recursive composedDoc</h2>
 *
 * <p>Segment kind {@code "composed"} ({@link ComposedSegment}) mounts a
 * fresh ComposedWidget into its own sub-branch via a self-callable
 * dispatch. The orchestrator passes a {@code __renderingStack} param
 * forward; cycle detected when the same doc id appears twice.</p>
 *
 * <p>Per-segment renderers are separate EsModules (per Modest File Size
 * doctrine — the legacy {@code ComposedViewerRenderer.js} was 528 lines).
 * The orchestrator imports them and dispatches by segment kind.</p>
 *
 * @since RFC 0024 Phase P1c
 */
public final class ComposedWidget extends DocWidget<ComposedWidget.Params, ComposedWidget> {

    public static final ComposedWidget INSTANCE = new ComposedWidget();

    private ComposedWidget() {}

    /** @param id UUID of the ComposedDoc to render. */
    public record Params(String id) implements Widget._Param {}

    private record mountInto() implements Widget._MountInto<Params, ComposedWidget> {}

    @Override public String simpleName() { return "composed-widget"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public String title() { return "doc"; }

    @Override
    protected Widget._MountInto<Params, ComposedWidget> mountInto() {
        return new mountInto();
    }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new domOpsParty()), DomOpsPartyModule.INSTANCE),
                new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE),
                new ModuleImports<>(List.of(new MarkedJs.marked()), MarkedJs.INSTANCE),
                new ModuleImports<>(List.of(new TableViewerRenderer.renderTable()),
                        TableViewerRenderer.INSTANCE),
                new ModuleImports<>(List.of(new ImageViewerRenderer.renderImage()),
                        ImageViewerRenderer.INSTANCE),
                // Per-segment renderer modules — each its own EsModule per the
                // Modest File Size doctrine. Each exports its render<X>Segment
                // function; the orchestrator dispatches by kind.
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
                        DocumentaryWidgetSegmentRenderer.INSTANCE),
                new ModuleImports<>(List.of(new ComposedSegmentRenderer.renderComposedSegment()),
                        ComposedSegmentRenderer.INSTANCE),
                new ModuleImports<>(List.of(new TocSidebarRenderer.renderTocSidebar()),
                        TocSidebarRenderer.INSTANCE),
                new ModuleImports<>(List.of(new HtmlExportModule.exportPageAsHtml()),
                        HtmlExportModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new StudioStyles.st_layout(),
                        new StudioStyles.st_sidebar(),
                        new StudioStyles.st_sidebar_title(),
                        new StudioStyles.st_toc(),
                        new StudioStyles.st_toc_active(),
                        new StudioStyles.st_doc_meta(),
                        new StudioStyles.st_loading(),
                        new StudioStyles.st_error()
                ), StudioStyles.INSTANCE));
    }

    @Override
    protected List<String> bodyJs() {
        return List.of(
                "    // Accept either params.url (leveled-tree URL, the new shape) or",
                "    // params.id (legacy direct-UUID URL). Build the fetch URL from",
                "    // whichever is provided; recursive embeds use params.url so the",
                "    // root context (and thus the registration relief) propagates.",
                "    var fetchUrl;",
                "    if (params.url) {",
                "        fetchUrl = params.url;",
                "    } else if (params.id) {",
                "        fetchUrl = '/doc?id=' + encodeURIComponent(params.id);",
                "    } else {",
                "        var errEl = branch.createElement('noId', 'div');",
                "        css.addClass(errEl, st_error);",
                "        errEl.textContent = 'No composed-doc reference supplied. Use ?id=<uuid> or pass url= via segment.';",
                "        parent.appendChild(errEl);",
                "        return;",
                "    }",
                "    // Per-mount owner — sub-branches for layout, toc, body, segments",
                "    // all activate against this. Document-lifetime in MPA mode.",
                "    var ownerLabel = params.id || params.url;",
                "    var owner = Object.freeze({ toString: function(){",
                "        return 'composedWidget:' + ownerLabel;",
                "    } });",
                "",
                "    // Cycle-detection stack tracks the UUID of each ComposedDoc",
                "    // encountered. Recursive embeds add the embedded doc's UUID",
                "    // (params.id), tracked here for downstream segments to check.",
                "    var stackEntry = params.id || params.url;",
                "    var renderingStack = (params.__renderingStack || []).concat([stackEntry]);",
                "",
                "    // ── Two-column layout ──",
                "    var layoutHost = branch.createElement('layoutHost', 'div');",
                "    css.addClass(layoutHost, st_layout);",
                "    parent.appendChild(layoutHost);",
                "",
                "    var tocBranch = branch.createBranch('tocSidebar');",
                "    tocBranch.activate(owner);",
                "    var sidebar = tocBranch.createElement('sidebar', 'aside');",
                "    css.addClass(sidebar, st_sidebar);",
                "    var sidebarTitle = tocBranch.createElement('sidebarTitle', 'div');",
                "    css.addClass(sidebarTitle, st_sidebar_title);",
                "    sidebarTitle.textContent = 'In this document';",
                "    var tocEl = tocBranch.createElement('toc', 'nav');",
                "    css.addClass(tocEl, st_toc);",
                "    sidebar.appendChild(sidebarTitle);",
                "    sidebar.appendChild(tocEl);",
                "    layoutHost.appendChild(sidebar);",
                "",
                "    var bodyBranch = branch.createBranch('body');",
                "    bodyBranch.activate(owner);",
                "    var bodyHost = bodyBranch.createElement('bodyHost', 'div');",
                "    var loading = bodyBranch.createElement('loading', 'div');",
                "    css.addClass(loading, st_loading);",
                "    loading.textContent = 'Loading\\u2026';",
                "    bodyHost.appendChild(loading);",
                "    layoutHost.appendChild(bodyHost);",
                "",
                "    // Dispatch context handed to each per-segment renderer — keeps",
                "    // renderers ignorant of orchestrator internals. The recursive",
                "    // ComposedSegmentRenderer uses ctx.mountComposed to re-enter the",
                "    // orchestrator with a nested doc + extended renderingStack.",
                "    var ctx = {",
                "        renderingStack: renderingStack,",
                "        mountComposed:  mountInto",
                "    };",
                "",
                "    fetch(fetchUrl)",
                "        .then(function(r){",
                "            if (!r.ok) throw new Error('HTTP ' + r.status);",
                "            return r.json();",
                "        })",
                "        .then(function(payload){",
                "            // Remove the loading placeholder before rendering segments.",
                "            // 'loading' is an element on bodyBranch, not a sub-branch — direct",
                "            // remove() is fine since bodyBranch's element map sees it on dissolve.",
                "            loading.remove();",
                "",
                "            // Meta row — always rendered (holds title/category/summary + export button).",
                "            var meta = bodyBranch.createElement('meta', 'div');",
                "            css.addClass(meta, st_doc_meta);",
                "            if (payload.title) {",
                "                var titleSpan = document.createElement('span');",
                "                titleSpan.textContent = payload.title;",
                "                titleSpan.style.cssText = 'font-size:24px;font-weight:600;';",
                "                meta.appendChild(titleSpan);",
                "            }",
                "            if (payload.category) {",
                "                var catSpan = document.createElement('span');",
                "                catSpan.style.cssText = 'margin-left:12px;font-size:11px;color:var(--st-gray-mid,#666);text-transform:uppercase;letter-spacing:0.05em;';",
                "                catSpan.textContent = payload.category;",
                "                meta.appendChild(catSpan);",
                "            }",
                "            if (payload.summary) {",
                "                var sumP = document.createElement('p');",
                "                sumP.style.cssText = 'margin:4px 0 16px 0;color:var(--st-gray-mid,#666);';",
                "                sumP.textContent = payload.summary;",
                "                meta.appendChild(sumP);",
                "            }",
                "            bodyHost.appendChild(meta);",
                "",
                "            // Export button — floating pill, position:fixed bottom-right.",
                "            // data-export-exclude: stripped by exportPageAsHtml before download",
                "            // so it does not appear in the saved file.",
                "            var _exportSlug = (payload.title || 'doc')",
                "                .replace(/[^\\w\\s-]/g, '').replace(/\\s+/g, '-').replace(/-+/g, '-').trim()",
                "                || 'doc';",
                "            var exportBtn = bodyBranch.createElement('exportBtn', 'button');",
                "            exportBtn.textContent = 'Export HTML';",
                "            exportBtn.setAttribute('data-export-exclude', '');",
                "            exportBtn.style.cssText = 'position:fixed;bottom:24px;right:24px;z-index:200;"
                        + "cursor:pointer;font:13px system-ui,sans-serif;padding:6px 16px;"
                        + "border:1px solid var(--color-border,rgba(0,0,0,.2));border-radius:20px;"
                        + "background:var(--color-surface,#fff);color:inherit;"
                        + "box-shadow:0 2px 8px rgba(0,0,0,.15);opacity:0.85;'",
                "            exportBtn.addEventListener('click', function() {",
                "                exportBtn.disabled = true;",
                "                exportBtn.textContent = 'Exporting\\u2026';",
                "                exportPageAsHtml(_exportSlug + '.html')",
                "                    .then(function() {",
                "                        exportBtn.textContent = 'Export HTML';",
                "                        exportBtn.disabled = false;",
                "                    })",
                "                    .catch(function(err) {",
                "                        exportBtn.textContent = 'Export failed';",
                "                        exportBtn.disabled = false;",
                "                        console.error('exportPageAsHtml failed:', err);",
                "                    });",
                "            });",
                "            document.body.appendChild(exportBtn);",
                "",
                "            renderTocSidebar(tocBranch, tocEl, payload.toc || []);",
                "",
                "            var segments = payload.segments || [];",
                "            for (var i = 0; i < segments.length; i++) {",
                "                var seg = segments[i];",
                "                var segBranch = bodyBranch.createBranch('seg-' + i);",
                "                segBranch.activate(owner);",
                "                switch (seg.kind) {",
                "                    case 'text':     renderTextSegment(segBranch, bodyHost, seg, ctx); break;",
                "                    case 'markdown': renderMarkdownSegment(segBranch, bodyHost, seg, ctx); break;",
                "                    case 'code':     renderCodeSegment(segBranch, bodyHost, seg, ctx); break;",
                "                    case 'svg':      renderSvgSegment(segBranch, bodyHost, seg, ctx); break;",
                "                    case 'table':    renderTableSegment(segBranch, bodyHost, seg, ctx); break;",
                "                    case 'image':    renderImageSegment(segBranch, bodyHost, seg, ctx); break;",
                "                    case 'relation': renderRelationSegment(segBranch, bodyHost, seg, ctx); break;",
                "                    case 'documentary-widget':",
                "                                     renderDocumentaryWidgetSegment(segBranch, bodyHost, seg, ctx); break;",
                "                    case 'composed': renderComposedSegment(segBranch, bodyHost, seg, ctx); break;",
                "                    default:",
                "                        var unk = segBranch.createElement('unknown', 'div');",
                "                        css.addClass(unk, st_error);",
                "                        unk.textContent = 'Unknown segment kind: ' + seg.kind;",
                "                        bodyHost.appendChild(unk);",
                "                }",
                "            }",
                "",
                "            // Scroll-spy: highlight TOC entry as its anchor enters view.",
                "            if (typeof IntersectionObserver === 'function') {",
                "                var tocLinks = tocEl.querySelectorAll('a[data-anchor]');",
                "                var byAnchor = {};",
                "                for (var li = 0; li < tocLinks.length; li++) {",
                "                    byAnchor[tocLinks[li].getAttribute('data-anchor')] = tocLinks[li];",
                "                }",
                "                var observer = new IntersectionObserver(function(entries){",
                "                    entries.forEach(function(e){",
                "                        if (e.isIntersecting) {",
                "                            for (var li2 = 0; li2 < tocLinks.length; li2++) {",
                "                                css.removeClass(tocLinks[li2], st_toc_active);",
                "                            }",
                "                            var link = byAnchor[e.target.id];",
                "                            if (link) css.addClass(link, st_toc_active);",
                "                        }",
                "                    });",
                "                }, { rootMargin: '0px 0px -70% 0px', threshold: 0 });",
                "                var anchored = bodyHost.querySelectorAll('[id]');",
                "                for (var ai = 0; ai < anchored.length; ai++) observer.observe(anchored[ai]);",
                "            }",
                "        })",
                "        .catch(function(err){",
                "            var errEl = bodyBranch.createElement('fetchError', 'div');",
                "            css.addClass(errEl, st_error);",
                "            errEl.textContent = 'Failed to load composed doc: ' + (err && err.message ? err.message : String(err));",
                "            bodyHost.replaceChildren(errEl);",
                "        });"
        );
    }
}
