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
 * The components workbench (kind {@code "components"}): the catalogue of
 * components the studio's crates deliver — one tree, derived from the crate
 * closure, every vehicle grafted under one root — and a pane for the node
 * selected. Beside the conformance workbench in the same group, on the same
 * shell and the same navigation bus, over the logical side of the crates
 * where the other browses the physical.
 */
public final class ComponentsWorkspaceSpec implements WorkspaceSpec {

    public static final ComponentsWorkspaceSpec INSTANCE = new ComponentsWorkspaceSpec();

    private ComponentsWorkspaceSpec() {}

    @Override public String kind()  { return "components"; }
    @Override public String title() { return "Components"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        WidgetGroup nav     = WidgetGroup.of("Navigation");
        WidgetGroup details = WidgetGroup.of("Details");
        return List.of(
            WidgetEntry.of(ComponentTreeWidget.class, WidgetLabel.of("Components"))
                    .withIcon(new WidgetIcon.Emoji("🧱"))
                    .withGroup(nav),
            WidgetEntry.of(ComponentSummaryWidget.class, WidgetLabel.of("Component"))
                    .withIcon(new WidgetIcon.Emoji("📇"))
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
}
