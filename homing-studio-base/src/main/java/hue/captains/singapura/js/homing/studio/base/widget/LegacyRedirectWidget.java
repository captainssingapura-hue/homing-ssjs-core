package hue.captains.singapura.js.homing.studio.base.widget;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.Widget;

import java.util.List;

/**
 * RFC 0040 — the {@link SingleWidgetWorkspace}'s fallback widget for doc kinds
 * it does not render itself (plain {@code doc} → DocReader, {@code plan} →
 * plan viewer, {@code app} → app launch, …). Given the leveled tree-path
 * locator, it resolves the doc's own canonical {@code url()} (from
 * {@code /open-refs}) and performs a <b>client-side redirect</b> to it.
 *
 * <p>This is what lets the legacy server-side {@code /open} redirect retire
 * completely: every catalogue leaf now opens uniformly into
 * {@code ?app=singleWidgetWorkspace&l0=…}; the kinds the workspace can render
 * (svg, composed) render in place, and every other kind bounces to its
 * existing per-kind viewer here, on the client. The user never sees a uuid in
 * the URL they clicked — only after the bounce, on the (deprecated) legacy
 * page, which is itself slated for replacement by the rigid-tree document.</p>
 *
 * @since homing-studio-base — RFC 0040 leveled Open
 */
public final class LegacyRedirectWidget extends DocWidget<LegacyRedirectWidget.Params, LegacyRedirectWidget> {

    public static final LegacyRedirectWidget INSTANCE = new LegacyRedirectWidget();

    private LegacyRedirectWidget() {}

    /** Locator carried as the leveled path ({@code l0,l1,…}); no typed field. */
    public record Params() implements Widget._Param {}

    private record mountInto() implements Widget._MountInto<Params, LegacyRedirectWidget> {}

    @Override public String simpleName() { return "legacy-redirect"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public String title() { return "opening"; }

    @Override
    protected Widget._MountInto<Params, LegacyRedirectWidget> mountInto() {
        return new mountInto();
    }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of();
    }

    @Override
    protected List<String> bodyJs() {
        return List.of(
                "    var msg = branch.createElement('msg', 'div');",
                "    msg.style.cssText = 'padding:40px;color:var(--st-gray-mid,#666);'",
                "        + 'font-family:sans-serif;';",
                "    msg.textContent = 'Opening\\u2026';",
                "    parent.appendChild(msg);",
                "",
                "    // The leveled path locator (RFC 0040): treeId (reserved) + l0,l1,..",
                "    function pathQuery() {",
                "        var q = [];",
                "        if (params.treeId) q.push('treeId=' + encodeURIComponent(params.treeId));",
                "        for (var i = 0; params['l' + i] !== undefined; i++) {",
                "            q.push('l' + i + '=' + encodeURIComponent(params['l' + i]));",
                "        }",
                "        return q.length ? q.join('&') : null;",
                "    }",
                "    var pq = pathQuery();",
                "    if (!pq) { msg.textContent = 'No locator supplied.'; return; }",
                "    // /open-refs resolves the path to the doc and returns its own",
                "    // canonical url() — the per-kind legacy viewer. Bounce to it.",
                "    fetch('/open-refs?' + pq)",
                "        .then(function(r){ return r.ok ? r.json() : null; })",
                "        .then(function(info){",
                "            if (info && info.url) { window.location.replace(info.url); }",
                "            else { msg.textContent = 'Could not resolve this item.'; }",
                "        })",
                "        .catch(function(err){",
                "            msg.textContent = 'Failed to open: '",
                "                + (err && err.message ? err.message : String(err));",
                "        });"
        );
    }
}
