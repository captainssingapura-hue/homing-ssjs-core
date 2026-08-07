package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * RFC 0044 — the Conformance-result pane. Already wired into the navigation
 * party (tracks the selected module via {@code NavigateTo}) so the substrate
 * is proven end to end, but the findings themselves are a placeholder: the
 * rule engine that produces them lands in later phases (served-artifact hook,
 * then the engine + default rule sets). When it does, this pane renders the
 * per-module {@code Finding} list; today it names the selected module and
 * states where the verdict comes from.
 */
public final class ModuleConformanceWidget extends WorkspaceWidget<WorkspaceWidget._None, ModuleConformanceWidget> {

    public static final ModuleConformanceWidget INSTANCE = new ModuleConformanceWidget();

    private ModuleConformanceWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ModuleConformanceWidget> {}

    @Override protected _Construct<_None, ModuleConformanceWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Conformance"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;'",
                "        + 'padding:16px;font-family:system-ui,sans-serif;';",
                "",
                "    var titleEl = branch.createElement('title', 'div');",
                "    titleEl.style.cssText = 'font-size:15px;font-weight:600;margin-bottom:10px;'",
                "        + 'color:var(--color-text-primary,#1a1a2e);';",
                "    titleEl.textContent = 'Conformance';",
                "",
                "    var note = branch.createElement('note', 'p');",
                "    note.style.cssText = 'margin:0 0 14px;line-height:1.5;'",
                "        + 'color:var(--st-gray-mid,#666);font-size:13px;';",
                "",
                "    var pill = branch.createElement('pill', 'div');",
                "    pill.style.cssText = 'display:inline-block;font-size:12px;padding:3px 10px;'",
                "        + 'border-radius:12px;background:var(--color-surface-raised,#f0f0f3);'",
                "        + 'color:var(--st-gray-mid,#666);';",
                "    pill.textContent = 'engine pending';",
                "",
                "    root.appendChild(titleEl);",
                "    root.appendChild(note);",
                "    root.appendChild(pill);",
                "",
                "    function render(node) {",
                "        if (node && node.kind === 'module') {",
                "            note.innerHTML = 'Selected module: <code>' + (node.summary || node.label) + '</code>.<br>'",
                "                + 'The rule engine that produces findings for this module lands in a later phase '",
                "                + '(served-artifact hook, then engine + default rule sets).';",
                "        } else {",
                "            note.textContent = 'Select a module in the Navigator. Its conformance verdict will appear '",
                "                + 'here once the rule engine is wired (later phase).';",
                "        }",
                "    }",
                "    render(null);",
                "",
                "    var __actorId  = null;",
                "    var __navParty = (workspaceCtx && workspaceCtx.navParty)",
                "                   ? workspaceCtx.navParty : null;",
                "    if (__navParty) {",
                "        __actorId = 'conformance/verdict-' + Math.random().toString(36).slice(2, 8);",
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
