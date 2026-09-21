package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The components workbench's Summary pane: a card for the node the
 * Navigator selected — the selection's name-path is asked of
 * {@code GET /component-tree?path=} for what the node is: a composition,
 * a vehicle, a catalogue, or a component with its shape, its tag, its
 * module, the crate that ships it and the words it is reached by. A pure
 * party consumer: it re-renders on every {@code NavigateTo}.
 */
public final class ComponentSummaryWidget extends WorkspaceWidget<WorkspaceWidget._None, ComponentSummaryWidget> {

    public static final ComponentSummaryWidget INSTANCE = new ComponentSummaryWidget();

    private ComponentSummaryWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ComponentSummaryWidget> {}

    @Override protected _Construct<_None, ComponentSummaryWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Component"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(List.of(
                new ComponentsStyles.cw_pane(), new ComponentsStyles.cw_card(), new ComponentsStyles.cw_kicker(), new ComponentsStyles.cw_title(),
                new ComponentsStyles.cw_summary(), new ComponentsStyles.cw_fact(), new ComponentsStyles.cw_key(), new ComponentsStyles.cw_code(),
                new ComponentsStyles.cw_empty()), ComponentsStyles.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    css.addClass(root, cw_pane);",
                "    var empty = branch.createElement('empty', 'div');",
                "    css.addClass(empty, cw_empty);",
                "    empty.textContent = 'Select a vehicle, a family or a component in the Navigator.';",
                "    root.appendChild(empty);",
                "",
                "    var card = branch.createElement('card', 'div');",
                "    css.addClass(card, cw_card);",
                "    var kicker = branch.createElement('kicker', 'div');",
                "    css.addClass(kicker, cw_kicker);",
                "    card.appendChild(kicker);",
                "    var title = branch.createElement('title', 'h2');",
                "    css.addClass(title, cw_title);",
                "    card.appendChild(title);",
                "    var summary = branch.createElement('summary', 'div');",
                "    css.addClass(summary, cw_summary);",
                "    card.appendChild(summary);",
                "    var facts = {};",
                "    ['shape', 'tag', 'module', 'crate', 'path', 'count'].forEach(function (k) {",
                "        var row = branch.createElement('fact-' + k, 'div');",
                "        css.addClass(row, cw_fact);",
                "        var key = branch.createElement('key-' + k, 'span');",
                "        css.addClass(key, cw_key);",
                "        key.textContent = k;",
                "        row.appendChild(key);",
                "        var value = branch.createElement('value-' + k, 'span');",
                "        if (k !== 'count') css.addClass(value, cw_code);",
                "        row.appendChild(value);",
                "        facts[k] = { row: row, value: value };",
                "    });",
                "    var order = ['shape', 'tag', 'module', 'crate', 'path', 'count'];",
                "",
                "    function fact(k, text) {",
                "        var f = facts[k];",
                "        if (text == null || text === '') { if (f.row.parentNode) card.removeChild(f.row); return; }",
                "        f.value.textContent = String(text);",
                "        if (!f.row.parentNode) card.appendChild(f.row);",
                "    }",
                "    function render(d) {",
                "        if (empty.parentNode) root.removeChild(empty);",
                "        kicker.textContent = d.kind === 'component' ? d.shape + ' component' : d.kind;",
                "        title.textContent = d.label || d.segment;",
                "        summary.textContent = d.summary || (d.kind === 'composition' ? d.vehicles + ' vehicles' : '');",
                "        order.forEach(function (k) { fact(k, null); });",
                "        if (d.kind === 'component') {",
                "            fact('shape', d.shape); fact('tag', d.tag); fact('module', d.module); fact('crate', d.crate); fact('path', d.path);",
                "        } else {",
                "            fact('crate', d.crate); fact('count', d.components + ' component' + (d.components === 1 ? '' : 's'));",
                "        }",
                "        if (!card.parentNode) root.appendChild(card);",
                "    }",
                "    function show(node) {",
                "        if (!node) return;",
                "        fetch('/component-tree?path=' + encodeURIComponent(node.namePath || ''))",
                "            .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
                "            .then(render)",
                "            .catch(function (err) { console.error('[ComponentSummaryWidget] fetch failed:', err); });",
                "    }",
                "",
                "    var __actorId  = null;",
                "    var __navParty = (workspaceCtx && workspaceCtx.navParty) ? workspaceCtx.navParty : null;",
                "    if (__navParty) {",
                "        __actorId = 'components/summary-' + Math.random().toString(36).slice(2, 8);",
                "        __navParty.joinActor({ id: __actorId, parentSecretary: 'navigation', reactors: { NavigateTo: function (msg) { show(msg.node); } } });",
                "    }",
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
