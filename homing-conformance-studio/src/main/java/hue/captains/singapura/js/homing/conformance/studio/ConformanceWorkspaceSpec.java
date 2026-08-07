package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.studio.workspace.NavigatorSecretaryModule;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;

import java.util.List;

/**
 * RFC 0044 — the dedicated conformance workspace (kind {@code "conformance"}).
 * A purpose-built {@link WorkspaceSpec}: its own widget roster — Navigator +
 * Summary + Full Content + Conformance — sitting on the reused shell
 * (GenericWorkspace chrome, tree renderer, party bus), NOT on the studio's
 * catalogue content model. New widgets are added here as later phases land,
 * with no catalogue/Doc constraints.
 *
 * <p>Reuses the generic {@code NavigatorSecretary} bus verbatim — the module
 * navigation protocol is exactly the generic one (select → {@code NavigateTo},
 * open → {@code OpenDoc}); a conformance-specific secretary can replace it if
 * the workspace later needs conformance-specific events (e.g. run/refresh).</p>
 */
public final class ConformanceWorkspaceSpec implements WorkspaceSpec {

    public static final ConformanceWorkspaceSpec INSTANCE = new ConformanceWorkspaceSpec();

    private ConformanceWorkspaceSpec() {}

    @Override public String kind()  { return "conformance"; }
    @Override public String title() { return "Conformance"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        WidgetGroup nav     = WidgetGroup.of("Navigation");
        WidgetGroup details = WidgetGroup.of("Details");
        return List.of(
            WidgetEntry.of(ModuleTreeWidget.class, WidgetLabel.of("Modules"))
                    .withIcon(new WidgetIcon.Emoji("🧩"))   // 🧩
                    .withGroup(nav),
            WidgetEntry.of(ModuleSummaryWidget.class, WidgetLabel.of("Summary"))
                    .withIcon(new WidgetIcon.Emoji("📄"))   // 📄
                    .withGroup(details),
            WidgetEntry.of(ModuleContentWidget.class, WidgetLabel.of("Full Content"))
                    .withIcon(new WidgetIcon.Emoji("📜"))   // 📜
                    .withGroup(details),
            WidgetEntry.of(ModuleConformanceWidget.class, WidgetLabel.of("Conformance"))
                    .withIcon(new WidgetIcon.Emoji("✅"))         // ✅
                    .withGroup(details)
        );
    }

    @Override
    public List<PartyDecl> parties() {
        return List.of(
            PartyDecl.of("navigation", NavigatorSecretaryModule.INSTANCE, "NavigatorSecretary")
                     .exposedAs("navParty")
                     .build()
        );
    }

    // No pinnedSpawns: the workspace opens empty and the user picks widgets from
    // the picker — the same first-run behaviour as the studio workspace. (A
    // pinned entry is also hidden from the picker, so pinning the Navigator
    // without a reliable auto-spawn would make it unreachable.)
}
