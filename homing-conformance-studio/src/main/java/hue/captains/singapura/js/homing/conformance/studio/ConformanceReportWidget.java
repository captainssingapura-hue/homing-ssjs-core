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
 * per-type section, each with its own accent and a <b>foldable rule set</b> —
 * click it to reveal the rules (id + intent) every module in that section is
 * held to, joined from the report's shared {@code ruleSets}. Every module is
 * listed (clean ones compactly); each finding is coloured by severity and
 * disposition (error / allowed / pre-existing).</p>
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
                "    // Keyed by the type's SLUG (what the report carries). Downstream",
                "    // extension types fall through to a default accent + their slug.",
                "    var TYPE_META = {",
                "        'consumer':         { color: '#3b82f6', label: 'Consumer' },",
                "        'primitive':        { color: '#8b5cf6', label: 'Primitive' },",
                "        'secretary':        { color: '#0d9488', label: 'Secretary' },",
                "        'pure-logic':       { color: '#64748b', label: 'Pure logic' },",
                "        'manager-injector': { color: '#d97706', label: 'ManagerInjector' },",
                "        'generated-css':    { color: '#db2777', label: 'Generated CSS' },",
                "        'bundled-external': { color: '#6b7280', label: 'Bundled external' }",
                "    };",
                "    var TYPE_ORDER = ['consumer','primitive','secretary','pure-logic',",
                "        'manager-injector','generated-css','bundled-external'];",
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
                "        // Three states: error (red), warning-only i.e. baselined/pre-existing (amber),",
                "        // clean (green). A passing module with findings is amber, not green.",
                "        var warn = m.pass && m.findings.length > 0;",
                "        var mColor = !m.pass ? '#c0392b' : (warn ? '#b7791f' : '#2e7d32');",
                "        var mMark  = !m.pass ? '\\u2717 ' : (warn ? '\\u26a0 ' : '\\u2713 ');",
                "        var head = el('div', 'font-family:ui-monospace,Menlo,Consolas,monospace;font-size:12px;'",
                "            + 'color:' + mColor + ';');",
                "        head.textContent = mMark + short",
                "            + (m.findings.length ? ' (' + m.findings.length + ')' : '');",
                "        head.title = m.moduleClass;",
                "        wrap.appendChild(head);",
                "        m.findings.forEach(function (f) { wrap.appendChild(renderFinding(f)); });",
                "        return wrap;",
                "    }",
                "",
                "    function render(report, modules) {",
                "        var ok = report.errorCount === 0;",
                "        var warned = ok && report.warningCount > 0;",
                "        var hText = !ok ? ('\\u2717 ' + report.errorCount + ' error(s)')",
                "            : (warned ? ('\\u26a0 Conformant \\u00b7 ' + report.warningCount + ' warning(s)')",
                "                      : '\\u2713 Conformant');",
                "        var h = el('div', 'font-size:16px;font-weight:700;', hText);",
                "        h.style.color = !ok ? '#c0392b' : (warned ? '#b7791f' : '#2e7d32');",
                "        body.appendChild(h);",
                "        body.appendChild(el('div', 'color:var(--st-gray-mid,#666);font-size:12px;margin:2px 0 6px;',",
                "            report.moduleCount + ' modules \\u00b7 ' + report.warningCount + ' warning(s) \\u00b7 '",
                "            + report.baselineSize + ' baselined \\u00b7 allowPreExisting=' + report.allowPreExisting));",
                "",
                "        // Rule sets are shared reference data (report.ruleSets): id -> {title, rules}.",
                "        var ruleSetsById = {};",
                "        (report.ruleSets || []).forEach(function (rs) { ruleSetsById[rs.id] = rs; });",
                "",
                "        var byType = {};",
                "        modules.forEach(function (m) { (byType[m.type] = byType[m.type] || []).push(m); });",
                "        // Standard types first (fixed order), then any downstream types present.",
                "        var order = TYPE_ORDER.concat(Object.keys(byType).filter(",
                "            function (t) { return TYPE_ORDER.indexOf(t) < 0; }));",
                "",
                "        order.forEach(function (type) {",
                "            var ms = byType[type];",
                "            if (!ms || !ms.length) return;",
                "            var meta = TYPE_META[type] || { color: '#888', label: type };",
                "            var withFindings = ms.filter(function (m) { return m.findings.length; });",
                "            var sec = el('div', 'margin-top:14px;border-left:3px solid ' + meta.color",
                "                + ';padding-left:12px;');",
                "            var sh = el('div', 'font-weight:600;font-size:13px;color:' + meta.color + ';',",
                "                meta.label + '  (' + ms.length + ' module' + (ms.length !== 1 ? 's' : '') + ')');",
                "            sec.appendChild(sh);",
                "",
                "            // Foldable rule set — the rules every module in this section is held to.",
                "            var setId = ms[0].ruleSet;",
                "            var rs = ruleSetsById[setId];",
                "            var ruleCount = (rs && rs.rules) ? rs.rules.length : 0;",
                "            var open = false;",
                "            var rulesBox = el('div', 'display:none;margin:3px 0 3px 16px;');",
                "            if (rs && rs.rules) rs.rules.forEach(function (r) {",
                "                var rr = el('div', 'font-size:11px;line-height:1.5;margin:1px 0;');",
                "                rr.appendChild(el('code', 'font-family:ui-monospace,Menlo,Consolas,monospace;'",
                "                    + 'color:' + meta.color + ';', r.id));",
                "                rr.appendChild(el('span', 'color:var(--st-gray-mid,#777);', '  \\u2014  ' + r.intent));",
                "                rulesBox.appendChild(rr);",
                "            });",
                "            function foldLabel() {",
                "                return (open ? '\\u25be' : '\\u25b8') + ' rule set: ' + setId",
                "                    + '  (' + ruleCount + ' rule' + (ruleCount !== 1 ? 's' : '') + ')  \\u00b7  '",
                "                    + (ms.length - withFindings.length) + ' clean / ' + withFindings.length + ' with findings';",
                "            }",
                "            var toggle = el('div', 'cursor:pointer;user-select:none;color:var(--st-gray-mid,#888);'",
                "                + 'font-size:11px;margin:1px 0 2px;', foldLabel());",
                "            toggle.title = ruleCount ? 'Show the rules applied to these modules' : '';",
                "            toggle.onclick = function () {",
                "                open = !open; rulesBox.style.display = open ? 'block' : 'none';",
                "                toggle.textContent = foldLabel();",
                "            };",
                "            sec.appendChild(toggle);",
                "            sec.appendChild(rulesBox);",
                "",
                "            // Every module in the section (clean ones too), findings inline.",
                "            ms.forEach(function (m) { sec.appendChild(renderModule(m)); });",
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
