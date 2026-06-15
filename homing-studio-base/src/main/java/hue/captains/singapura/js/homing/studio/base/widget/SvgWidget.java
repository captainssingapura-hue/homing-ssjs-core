package hue.captains.singapura.js.homing.studio.base.widget;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.Widget;

import java.util.List;

/**
 * RFC 0024 Phase P1b — pilot widget. The {@link Widget}-side rewrite of
 * the legacy {@code SvgViewer} {@code AppModule}: fetches the SvgDoc's
 * body via {@code /doc?id=&lt;uuid&gt;} and inlines it centered in the
 * widget's branch.
 *
 * <p>Demonstrates the {@link DocWidget} pattern end-to-end:</p>
 *
 * <ul>
 *   <li>Per-mount synthetic owner — minted once at the top of
 *       {@code mountInto}; activates every sub-branch the widget
 *       creates. When the widget swaps, the L2 widget branch dissolves
 *       and this owner becomes unreachable — DomOpsParty's leak
 *       detector flags any forgotten sub-branch.</li>
 *   <li>Phase sub-branches — {@code loadingPhase}, {@code errorPhase},
 *       {@code contentPhase}. At any moment exactly one phase is mounted;
 *       a phase transition dissolves the previous and creates the next.
 *       This is the canonical pattern for transient-state DOM under
 *       DomOpsParty discipline.</li>
 *   <li>Body owns DOM attachment — elements are created via
 *       {@code branch.createElement} (ownership) and appended to
 *       {@code parent} (placement). The branch handles cleanup; the
 *       body handles where in the document tree the content lives.</li>
 * </ul>
 *
 * <p>The legacy {@code SvgViewer} keeps working at
 * {@code /app?app=svg-viewer&id=...}; this widget is invoked via the
 * shell at {@code /app?app=standard-mpa&widget=svg-widget&id=...}.
 * Both paths coexist per the RFC's sibling-cohabitation migration
 * strategy.</p>
 *
 * @since RFC 0024 Phase P1b
 */
public final class SvgWidget extends DocWidget<SvgWidget.Params, SvgWidget> {

    public static final SvgWidget INSTANCE = new SvgWidget();

    private SvgWidget() {}

    /** @param id UUID of the SvgDoc to render (resolved via the doc registry). */
    public record Params(String id) implements Widget._Param {}

    private record mountInto() implements Widget._MountInto<Params, SvgWidget> {}

    @Override public String simpleName() { return "svg-widget"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public String title() { return "svg"; }

    @Override
    protected Widget._MountInto<Params, SvgWidget> mountInto() {
        return new mountInto();
    }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of();
    }

    @Override
    protected List<String> bodyJs() {
        return List.of(
                "    // Per-mount owner. When the widget swaps, this becomes",
                "    // unreachable → DomOpsParty's leak detector catches",
                "    // any sub-branch we forgot to dissolve.",
                "    var owner = Object.freeze({ toString: function(){",
                "        return 'svgWidget:' + (params.id || 'noId');",
                "    } });",
                "",
                "    function showLoading() {",
                "        var b = branch.createBranch('loadingPhase');",
                "        b.activate(owner);",
                "        var el = b.createElement('msg', 'div');",
                "        el.textContent = 'Loading\\u2026';",
                "        el.style.cssText = 'padding:40px;color:#666;font-family:sans-serif;';",
                "        parent.appendChild(el);",
                "    }",
                "    function showError(msg) {",
                "        if (branch.hasBranch('loadingPhase')) branch.dissolveBranch('loadingPhase');",
                "        if (branch.hasBranch('contentPhase')) branch.dissolveBranch('contentPhase');",
                "        var b = branch.createBranch('errorPhase');",
                "        b.activate(owner);",
                "        var el = b.createElement('msg', 'div');",
                "        el.textContent = msg;",
                "        el.style.cssText = 'padding:40px;color:#900;font-family:sans-serif;';",
                "        parent.appendChild(el);",
                "    }",
                "    function showContent(svgText) {",
                "        if (branch.hasBranch('loadingPhase')) branch.dissolveBranch('loadingPhase');",
                "        var b = branch.createBranch('contentPhase');",
                "        b.activate(owner);",
                "        var host = b.createElement('host', 'div');",
                "        host.style.cssText = 'display:flex;align-items:center;justify-content:center;'",
                "                          + 'min-height:60vh;padding:40px;';",
                "        var inner = document.createElement('div');",
                "        inner.style.cssText = 'max-width:80vw;max-height:80vh;display:flex;';",
                "        var range = document.createRange();",
                "        range.selectNodeContents(inner);",
                "        inner.appendChild(range.createContextualFragment(svgText));",
                "        var svgEl = inner.querySelector('svg');",
                "        if (svgEl) {",
                "            svgEl.style.width = '100%';",
                "            svgEl.style.height = '100%';",
                "            svgEl.style.maxWidth = '600px';",
                "            svgEl.style.maxHeight = '600px';",
                "        }",
                "        host.appendChild(inner);",
                "        parent.appendChild(host);",
                "    }",
                "",
                "    // Locator (RFC 0040): legacy uuid (?id=) OR the leveled tree",
                "    // path (?treeId=&l0=&l1=..). The uuid still works; the path is",
                "    // the positional sibling that the Navigator's Open uses, fetched",
                "    // from /open-content with the same path it was opened by.",
                "    function pathQuery() {",
                "        var q = [];",
                "        if (params.treeId) q.push('treeId=' + encodeURIComponent(params.treeId));",
                "        for (var i = 0; params['l' + i] !== undefined; i++) {",
                "            q.push('l' + i + '=' + encodeURIComponent(params['l' + i]));",
                "        }",
                "        return q.length ? q.join('&') : null;",
                "    }",
                "    var pq = pathQuery();",
                "    var fetchUrl = params.id",
                "        ? '/doc?id=' + encodeURIComponent(params.id)",
                "        : (pq ? '/open-content?' + pq : null);",
                "    if (!fetchUrl) {",
                "        showError('No SVG locator supplied. Use ?id=<uuid> or ?l0=...');",
                "        return;",
                "    }",
                "    showLoading();",
                "    fetch(fetchUrl)",
                "        .then(function(r){",
                "            if (!r.ok) throw new Error('HTTP ' + r.status);",
                "            return r.text();",
                "        })",
                "        .then(showContent)",
                "        .catch(function(err){",
                "            showError('Failed to load SVG: ' + (err && err.message ? err.message : String(err)));",
                "        });"
        );
    }
}
