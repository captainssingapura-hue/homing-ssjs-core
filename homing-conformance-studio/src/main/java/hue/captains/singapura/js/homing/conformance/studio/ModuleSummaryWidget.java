package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * RFC 0044 — the conformance workspace's Summary pane: a pure party consumer
 * that re-renders a module card whenever the Navigator's selection is
 * redirected as {@code NavigateTo}. The selection already carries
 * {@code {label, kind, summary}} — no fetch. For a module leaf the card shows
 * the simple name, the FQCN (from {@code summary}), and the kind badge; this
 * is the natural home for a conformance-status chip once the engine lands.
 */
public final class ModuleSummaryWidget extends WorkspaceWidget<WorkspaceWidget._None, ModuleSummaryWidget> {

    public static final ModuleSummaryWidget INSTANCE = new ModuleSummaryWidget();

    private ModuleSummaryWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ModuleSummaryWidget> {}

    @Override protected _Construct<_None, ModuleSummaryWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Summary"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;'",
                "        + 'padding:16px;font-family:system-ui,sans-serif;';",
                "",
                "    var card = branch.createElement('card', 'div');",
                "    card.style.cssText = 'border:1px solid var(--color-border,rgba(0,0,0,.15));'",
                "        + 'border-radius:8px;padding:16px 18px;max-width:640px;'",
                "        + 'background:var(--color-surface,#fff);box-shadow:0 1px 3px rgba(0,0,0,.06);';",
                "",
                "    var badge = branch.createElement('badge', 'div');",
                "    badge.style.cssText = 'font-size:11px;text-transform:uppercase;'",
                "        + 'letter-spacing:0.06em;color:var(--st-gray-mid,#888);margin-bottom:6px;';",
                "",
                "    var titleEl = branch.createElement('title', 'div');",
                "    titleEl.style.cssText = 'font-size:18px;font-weight:600;margin-bottom:8px;'",
                "        + 'color:var(--color-text-primary,#1a1a2e);';",
                "",
                "    var fqcnEl = branch.createElement('fqcn', 'code');",
                "    fqcnEl.style.cssText = 'display:block;font-size:12px;line-height:1.5;'",
                "        + 'color:var(--st-gray-mid,#555);word-break:break-all;';",
                "",
                "    card.appendChild(badge);",
                "    card.appendChild(titleEl);",
                "    card.appendChild(fqcnEl);",
                "    root.appendChild(card);",
                "",
                "    function render(node) {",
                "        if (!node) {",
                "            badge.textContent = '';",
                "            titleEl.textContent = 'Nothing selected';",
                "            fqcnEl.textContent = 'Select a module in the Navigator to see its summary.';",
                "            fqcnEl.style.fontStyle = 'italic';",
                "            return;",
                "        }",
                "        badge.textContent = node.kind || '';",
                "        titleEl.textContent = node.label || '(unnamed)';",
                "        // For a module leaf the summary IS the FQCN; for branches it's a count.",
                "        var isModule = node.kind === 'module';",
                "        fqcnEl.textContent = (node.summary && node.summary.length) ? node.summary : '(no summary)';",
                "        fqcnEl.style.fontStyle = (node.summary && node.summary.length) ? 'normal' : 'italic';",
                "        fqcnEl.style.fontFamily = isModule ? 'ui-monospace,Menlo,Consolas,monospace' : 'inherit';",
                "    }",
                "    render(null);",
                "",
                "    var __actorId  = null;",
                "    var __navParty = (workspaceCtx && workspaceCtx.navParty)",
                "                   ? workspaceCtx.navParty : null;",
                "    if (__navParty) {",
                "        __actorId = 'conformance/summary-' + Math.random().toString(36).slice(2, 8);",
                "        __navParty.joinActor({",
                "            id: __actorId,",
                "            parentSecretary: 'navigation',",
                "            reactors: {",
                "                NavigateTo: function (msg) { render(msg.node); }",
                "            }",
                "        });",
                "    }",
                "",
                "    return {",
                "        root: root,",
                "        setActive: function (active) {},",
                "        partyDeregister: function () {",
                "            if (__actorId && __navParty) {",
                "                try { __navParty.leave(__actorId); } catch (e) {}",
                "            }",
                "        }",
                "    };"
        );
    }
}
