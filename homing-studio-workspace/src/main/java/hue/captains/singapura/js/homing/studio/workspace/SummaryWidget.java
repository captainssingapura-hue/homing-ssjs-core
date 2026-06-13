package hue.captains.singapura.js.homing.studio.workspace;

import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The detail pane for the Studio Workspace — a Party consumer that listens
 * for the {@code NavigatorParty}'s {@code NavigateTo} broadcast and shows the
 * selected node's summary as a card, mirroring the catalogue view's entry
 * cards (title + category/kind badge + summary).
 *
 * <p>Pure consumer: it never publishes. It joins {@code workspaceCtx.navParty}
 * and re-renders its card whenever the {@link TreeWidget} selection is
 * redirected to it. The selection message already carries
 * {@code {label, kind, summary}} — no fetch needed. With no Party present it
 * shows a static empty state.</p>
 *
 * <p>Paramless ({@link WorkspaceWidget._None}) — the picker opens it in one
 * click with no form. Nothing to gate on workspace-active transitions, so
 * {@code setActive} is a required no-op.</p>
 *
 * @since homing-studio-workspace — studio navigation Party (consumer pane)
 */
public final class SummaryWidget extends WorkspaceWidget<WorkspaceWidget._None, SummaryWidget> {

    public static final SummaryWidget INSTANCE = new SummaryWidget();

    private SummaryWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, SummaryWidget> {}

    @Override protected _Construct<_None, SummaryWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Summary"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<String> constructBodyJs() {
        // Each string is one JS line (joined with '\n' at emit time).
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;'",
                "        + 'padding:16px;font-family:system-ui,sans-serif;';",
                "",
                "    // Card — mirrors the catalogue view's entry cards.",
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
                "    var summaryEl = branch.createElement('summary', 'p');",
                "    summaryEl.style.cssText = 'margin:0;line-height:1.5;'",
                "        + 'color:var(--st-gray-mid,#555);';",
                "",
                "    // Open button — opens the selected leaf's doc in a new page in",
                "    // regular MPA mode. We only carry the node's id (the doc UUID);",
                "    // /open?id=<uuid> resolves it to the doc's own url() server-side",
                "    // (authoritative, per-kind) and redirects. No URL grammar is",
                "    // re-derived here, and the generic tree substrate stays untouched.",
                "    // Branch nodes (kind 'catalogue') have no doc page → button hidden.",
                "    var openBtn = branch.createElement('openBtn', 'button');",
                "    openBtn.textContent = 'Open \\u2197';",  // ↗
                "    openBtn.style.cssText = 'margin-top:14px;cursor:pointer;font:13px system-ui;'",
                "        + 'padding:5px 14px;border:1px solid var(--color-border,rgba(0,0,0,.2));'",
                "        + 'border-radius:14px;background:var(--color-surface-raised,#f3f3f3);'",
                "        + 'color:inherit;display:none;';",
                "    var __openId = '';",
                "    openBtn.addEventListener('click', function () {",
                "        if (__openId) window.open('/open?id=' + encodeURIComponent(__openId),",
                "                                  '_blank', 'noopener');",
                "    });",
                "",
                "    card.appendChild(badge);",
                "    card.appendChild(titleEl);",
                "    card.appendChild(summaryEl);",
                "    card.appendChild(openBtn);",
                "    root.appendChild(card);",
                "",
                "    function render(node) {",
                "        if (!node) {",
                "            badge.textContent = '';",
                "            titleEl.textContent = 'Nothing selected';",
                "            summaryEl.textContent = 'Select a node in the Navigator to see its summary.';",
                "            summaryEl.style.fontStyle = 'italic';",
                "            __openId = '';",
                "            openBtn.style.display = 'none';",
                "            return;",
                "        }",
                "        badge.textContent = node.kind || '';",
                "        titleEl.textContent = node.label || node.id || '(unnamed)';",
                "        var s = (node.summary && node.summary.length) ? node.summary : '(no summary)';",
                "        summaryEl.textContent = s;",
                "        summaryEl.style.fontStyle = (node.summary && node.summary.length) ? 'normal' : 'italic';",
                "        // Doc leaves carry their UUID as the node id and a doc kind;",
                "        // catalogue branches use kind 'catalogue' and a synthetic id.",
                "        var isDocLeaf = node.id && node.kind && node.kind !== 'catalogue';",
                "        __openId = isDocLeaf ? node.id : '';",
                "        openBtn.style.display = __openId ? 'inline-block' : 'none';",
                "    }",
                "    render(null);",
                "",
                "    // ── NavigatorParty wiring ───────────────────────────────────",
                "    // Pure consumer: join and re-render on each redirected selection.",
                "    var __actorId  = null;",
                "    var __navParty = (workspaceCtx && workspaceCtx.navParty)",
                "                   ? workspaceCtx.navParty : null;",
                "    if (__navParty) {",
                "        __actorId = 'studio/summary-' + Math.random().toString(36).slice(2, 8);",
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
