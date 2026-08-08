package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * RFC 0044 — the <b>crate-level</b> Conformance pane. The crate is the
 * aggregation point: this pane shows the selected crate's orphans + rolled-up
 * illegal imports. It reacts to the same {@code NavigateTo} as every pane and
 * <b>derives</b> its crate from the selection — a crate node directly (its
 * label), a module leaf via the backend {@code modules[fqcn].crate} map — so
 * selecting a module lights this pane with that module's crate.
 */
public final class CrateConformanceWidget extends WorkspaceWidget<WorkspaceWidget._None, CrateConformanceWidget> {

    public static final CrateConformanceWidget INSTANCE = new CrateConformanceWidget();

    private CrateConformanceWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, CrateConformanceWidget> {}

    @Override protected _Construct<_None, CrateConformanceWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Crate Conformance"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;'",
                "        + 'padding:16px;font-family:system-ui,sans-serif;font-size:13px;';",
                "    var body = branch.createElement('body', 'div');",
                "    root.appendChild(body);",
                "",
                "    var data = null, lastNode = null;",
                "",
                "    function line(text, color, mono) {",
                "        var d = document.createElement('div');",
                "        d.style.cssText = 'margin:2px 0;line-height:1.5;'",
                "            + (color ? 'color:' + color + ';' : '')",
                "            + (mono ? 'font-family:ui-monospace,Menlo,Consolas,monospace;font-size:12px;' : '');",
                "        d.textContent = text;",
                "        body.appendChild(d);",
                "        return d;",
                "    }",
                "    function section(title, items) {",
                "        var h = line(title + ' (' + items.length + ')', 'var(--st-gray-mid,#888)', false);",
                "        h.style.marginTop = '12px'; h.style.fontWeight = '600';",
                "        if (!items.length) { line('none', 'var(--st-gray-mid,#999)', false).style.fontStyle = 'italic'; }",
                "        else items.forEach(function (f) { line('\\u2717 ' + f, '#c0392b', true); });",
                "    }",
                "",
                "    function crateOf(node) {",
                "        if (!node) return null;",
                "        if (node.hasChildren) return node.label;",           // crate node
                "        if (node.summary && data.modules[node.summary]) return data.modules[node.summary].crate;",
                "        return null;",
                "    }",
                "",
                "    function render(node) {",
                "        body.textContent = '';",
                "        if (!data) { line('Loading conformance\\u2026', 'var(--st-gray-mid,#888)', false); return; }",
                "        var name = crateOf(node);",
                "        var cr = name ? data.crates[name] : null;",
                "        if (!cr) { var e = line('Select a crate or module to see its crate conformance.',",
                "            'var(--st-gray-mid,#888)', false); e.style.fontStyle = 'italic'; return; }",
                "        var head = line((cr.ok ? '\\u2713 ' : '\\u2717 ') + cr.name,",
                "            cr.ok ? '#2e7d32' : '#c0392b', false);",
                "        head.style.fontSize = '16px'; head.style.fontWeight = '700';",
                "        var issues = cr.orphans.length + cr.illegalImports.length;",
                "        line(cr.modules + ' modules \\u00b7 ' + (cr.ok ? 'conformant' : issues + ' issue(s)'),",
                "            'var(--st-gray-mid,#666)', false);",
                "        section('Orphan modules', cr.orphans);",
                "        section('Illegal imports', cr.illegalImports);",
                "    }",
                "",
                "    fetch('/crate-conformance')",
                "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
                "        .then(function (d) { data = d; render(lastNode); })",
                "        .catch(function (err) {",
                "            body.textContent = 'Failed to load conformance: '",
                "                + (err && err.message ? err.message : String(err));",
                "        });",
                "",
                "    var __actorId  = null;",
                "    var __navParty = (workspaceCtx && workspaceCtx.navParty) ? workspaceCtx.navParty : null;",
                "    if (__navParty) {",
                "        __actorId = 'conformance/crate-' + Math.random().toString(36).slice(2, 8);",
                "        __navParty.joinActor({",
                "            id: __actorId, parentSecretary: 'navigation',",
                "            reactors: { NavigateTo: function (msg) { lastNode = msg.node; render(msg.node); } }",
                "        });",
                "    }",
                "    render(null);",
                "",
                "    return {",
                "        root: root,",
                "        setActive: function (active) {},",
                "        partyDeregister: function () {",
                "            if (__actorId && __navParty) { try { __navParty.leave(__actorId); } catch (e) {} }",
                "        }",
                "    };"
        );
    }
}
