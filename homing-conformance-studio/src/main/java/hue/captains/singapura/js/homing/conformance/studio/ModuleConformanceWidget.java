package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * RFC 0044 — the <b>module-level</b> Conformance pane. Shows the selected
 * module's own per-module findings (its slice of the layering rule today;
 * richer once the rule engine lands). It reacts to the same {@code NavigateTo}
 * as every pane: a module leaf lights this pane with that module; a crate node
 * clears it (a crate isn't a module). Data comes from {@code /crate-conformance}
 * ({@code modules[fqcn]}).
 */
public final class ModuleConformanceWidget extends WorkspaceWidget<WorkspaceWidget._None, ModuleConformanceWidget> {

    public static final ModuleConformanceWidget INSTANCE = new ModuleConformanceWidget();

    private ModuleConformanceWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ModuleConformanceWidget> {}

    @Override protected _Construct<_None, ModuleConformanceWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Module Conformance"; }
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
                "            + (mono ? 'font-family:ui-monospace,Menlo,Consolas,monospace;font-size:12px;word-break:break-all;' : '');",
                "        d.textContent = text;",
                "        body.appendChild(d);",
                "        return d;",
                "    }",
                "",
                "    function render(node) {",
                "        body.textContent = '';",
                "        if (!data) { line('Loading conformance\\u2026', 'var(--st-gray-mid,#888)', false); return; }",
                "        var isModule = node && !node.hasChildren && node.summary;",
                "        var mr = isModule ? data.modules[node.summary] : null;",
                "        if (!mr) { var e = line('Select a module to see its conformance.',",
                "            'var(--st-gray-mid,#888)', false); e.style.fontStyle = 'italic'; return; }",
                "        var head = line((mr.ok ? '\\u2713 ' : '\\u2717 ') + (node.label || mr.moduleClass),",
                "            mr.ok ? '#2e7d32' : '#c0392b', false);",
                "        head.style.fontSize = '16px'; head.style.fontWeight = '700';",
                "        line('crate ' + mr.crate + ' \\u00b7 ' + mr.form, 'var(--st-gray-mid,#666)', false);",
                "        line(mr.moduleClass, 'var(--st-gray-mid,#888)', true);",
                "        var h = line('Findings (' + mr.findings.length + ')', 'var(--st-gray-mid,#888)', false);",
                "        h.style.marginTop = '12px'; h.style.fontWeight = '600';",
                "        if (!mr.findings.length) {",
                "            line('\\u2713 imports conformant', '#2e7d32', false);",
                "        } else {",
                "            mr.findings.forEach(function (f) { line('\\u2717 ' + f, '#c0392b', true); });",
                "        }",
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
                "        __actorId = 'conformance/module-' + Math.random().toString(36).slice(2, 8);",
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
