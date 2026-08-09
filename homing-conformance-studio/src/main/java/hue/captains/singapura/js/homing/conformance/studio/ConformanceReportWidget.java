package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * RFC 0044 Phase 8 — the <b>Conformance Report</b> pane: the exported report,
 * rendered properly. It dynamic-imports the served {@link ReportCodecsModule}
 * (the polyglot codec's JS target), fetches the whole report from {@code
 * /conformance-report}, and decodes it client-side into typed instances via the
 * generated codec — no raw JSON on screen.
 *
 * <p>The view is <b>polymorphic over module type</b>, mirroring how the policy
 * gives each {@code JsModuleType} its own rule set: modules are grouped into a
 * per-type section, each with its own accent + the rule-set id it is held to,
 * and each finding is coloured by severity and disposition (error / allowed /
 * pre-existing).</p>
 */
public final class ConformanceReportWidget extends WorkspaceWidget<WorkspaceWidget._None, ConformanceReportWidget> {

    public static final ConformanceReportWidget INSTANCE = new ConformanceReportWidget();

    private ConformanceReportWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ConformanceReportWidget> {}

    @Override protected _Construct<_None, ConformanceReportWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Conformance Report"; }
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
                "    // Polymorphic per-type presentation — each JsModuleType renders in its",
                "    // own accented section, the way the policy gives each type a rule set.",
                "    var TYPE_META = {",
                "        CONSUMER:         { color: '#3b82f6', label: 'Consumer' },",
                "        PRIMITIVE:        { color: '#8b5cf6', label: 'Primitive' },",
                "        SECRETARY:        { color: '#0d9488', label: 'Secretary' },",
                "        PURE_LOGIC:       { color: '#64748b', label: 'Pure logic' },",
                "        MANAGER_INJECTOR: { color: '#d97706', label: 'ManagerInjector' },",
                "        GENERATED_CSS:    { color: '#db2777', label: 'Generated CSS' },",
                "        BUNDLED_EXTERNAL: { color: '#6b7280', label: 'Bundled external' }",
                "    };",
                "    var TYPE_ORDER = ['CONSUMER','PRIMITIVE','SECRETARY','PURE_LOGIC',",
                "        'MANAGER_INJECTOR','GENERATED_CSS','BUNDLED_EXTERNAL'];",
                "",
                "    function el(tag, css, text) {",
                "        var d = document.createElement(tag);",
                "        if (css) d.style.cssText = css;",
                "        if (text != null) d.textContent = text;",
                "        return d;",
                "    }",
                "",
                "    function findingColor(f) {",
                "        if (f.severity === 'ERROR') return '#c0392b';",
                "        if (f.disposition === 'ALLOWED') return '#2563eb';",       // intentional
                "        if (f.disposition === 'PRE_EXISTING') return '#b7791f';",  // debt
                "        return '#c0392b';",
                "    }",
                "    function renderFinding(f) {",
                "        var mark = (f.severity === 'ERROR') ? '\\u2717' : '\\u26a0';",
                "        var row = el('div', 'margin:1px 0 1px 18px;font-family:ui-monospace,Menlo,Consolas,'",
                "            + 'monospace;font-size:11px;line-height:1.45;word-break:break-word;color:'",
                "            + findingColor(f) + ';');",
                "        row.textContent = mark + ' [' + f.rule + '] ' + f.message",
                "            + '  \\u00b7 ' + String(f.disposition).toLowerCase().replace('_','-');",
                "        return row;",
                "    }",
                "    function renderModule(m) {",
                "        var wrap = el('div', 'margin:5px 0;');",
                "        var short = m.moduleClass.split('.').pop();",
                "        var head = el('div', 'font-family:ui-monospace,Menlo,Consolas,monospace;font-size:12px;'",
                "            + 'color:' + (m.pass ? '#2e7d32' : '#c0392b') + ';');",
                "        head.textContent = (m.pass ? '\\u2713 ' : '\\u2717 ') + short",
                "            + (m.findings.length ? ' (' + m.findings.length + ')' : '');",
                "        head.title = m.moduleClass;",
                "        wrap.appendChild(head);",
                "        m.findings.forEach(function (f) { wrap.appendChild(renderFinding(f)); });",
                "        return wrap;",
                "    }",
                "",
                "    function render(report, modules) {",
                "        var ok = report.errorCount === 0;",
                "        var h = el('div', 'font-size:16px;font-weight:700;',",
                "            (ok ? '\\u2713 Conformant' : '\\u2717 ' + report.errorCount + ' error(s)'));",
                "        h.style.color = ok ? '#2e7d32' : '#c0392b';",
                "        body.appendChild(h);",
                "        body.appendChild(el('div', 'color:var(--st-gray-mid,#666);font-size:12px;margin:2px 0 6px;',",
                "            report.moduleCount + ' modules \\u00b7 ' + report.warningCount + ' warning(s) \\u00b7 '",
                "            + report.baselineSize + ' baselined \\u00b7 allowPreExisting=' + report.allowPreExisting));",
                "",
                "        var byType = {};",
                "        modules.forEach(function (m) { (byType[m.type] = byType[m.type] || []).push(m); });",
                "",
                "        TYPE_ORDER.forEach(function (type) {",
                "            var ms = byType[type];",
                "            if (!ms || !ms.length) return;",
                "            var meta = TYPE_META[type] || { color: '#888', label: type };",
                "            var withFindings = ms.filter(function (m) { return m.findings.length; });",
                "            var sec = el('div', 'margin-top:14px;border-left:3px solid ' + meta.color",
                "                + ';padding-left:12px;');",
                "            var sh = el('div', 'font-weight:600;font-size:13px;color:' + meta.color + ';',",
                "                meta.label + '  (' + ms.length + ' module' + (ms.length !== 1 ? 's' : '') + ')');",
                "            sec.appendChild(sh);",
                "            sec.appendChild(el('div', 'color:var(--st-gray-mid,#888);font-size:11px;margin:1px 0 4px;',",
                "                'rule set: ' + ms[0].ruleSet + '  \\u00b7  ' + (ms.length - withFindings.length)",
                "                + ' clean / ' + withFindings.length + ' with findings'));",
                "            withFindings.forEach(function (m) { sec.appendChild(renderModule(m)); });",
                "            body.appendChild(sec);",
                "        });",
                "    }",
                "",
                "    var loading = el('div', 'color:var(--st-gray-mid,#888);', 'Loading report\\u2026');",
                "    body.appendChild(loading);",
                "    var CODEC_URL = '/module?class=hue.captains.singapura.js.homing.conformance.studio.ReportCodecsModule';",
                "    Promise.all([",
                "        import(CODEC_URL),",
                "        fetch('/conformance-report').then(function (r) {",
                "            if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
                "    ]).then(function (res) {",
                "        loading.remove();",
                "        var codec = res[0], data = res[1];",
                "        var report = codec.ConformanceReportCodec.transformFrom(data.summary);",
                "        var modules = data.modules.map(function (w) {",
                "            return codec.ModuleResultCodec.transformFrom(w); });",
                "        render(report, modules);",
                "    }).catch(function (err) {",
                "        body.textContent = 'Failed to load report: '",
                "            + (err && err.message ? err.message : String(err));",
                "    });",
                "",
                "    return {",
                "        root: root,",
                "        setActive: function (active) {},",
                "        partyDeregister: function () {}",
                "    };"
        );
    }
}
