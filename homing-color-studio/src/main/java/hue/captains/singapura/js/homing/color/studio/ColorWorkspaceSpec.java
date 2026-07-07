package hue.captains.singapura.js.homing.color.studio;

import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;

import java.util.List;

/**
 * The Color Studio — a separate {@link WorkspaceSpec} (kind {@code "color"}) for
 * browsing and managing the ColorGroup taxonomy in the V2 workspace shell.
 * Orthogonal to the studio workspace; mounted by {@code GenericWorkspace} at
 * {@code ?app=genericWorkspace&ws_kind=color}.
 *
 * <p>Two widgets, joined by the {@code colornav} Party (exposed as
 * {@code workspaceCtx.colorParty}):</p>
 * <ul>
 *   <li>{@link ColorTreeWidget} (Color Tree) — the group tree; publishes
 *       {@code NodeSelected} on every selection.</li>
 *   <li>{@link ColorListWidget} (Colours) — reacts to the redirected
 *       {@code NavigateTo} and lists the selected group's colours.</li>
 * </ul>
 */
public final class ColorWorkspaceSpec implements WorkspaceSpec {

    public static final ColorWorkspaceSpec INSTANCE = new ColorWorkspaceSpec();

    private ColorWorkspaceSpec() {}

    @Override public String kind()  { return "color"; }
    @Override public String title() { return "Colours"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        return List.of(
                WidgetEntry.of(ColorTreeWidget.class, WidgetLabel.of("Color Tree"))
                        .withIcon(new WidgetIcon.Emoji("🎨"))
                        .withGroup(WidgetGroup.of("Colours")),
                WidgetEntry.of(ColorListWidget.class, WidgetLabel.of("Colours"))
                        .withIcon(new WidgetIcon.Emoji("🌈"))
                        .withGroup(WidgetGroup.of("Colours"))
        );
    }

    @Override
    public List<PartyDecl> parties() {
        return List.of(
                PartyDecl.of("colornav",
                             ColorNavSecretaryModule.INSTANCE, "ColorNavSecretary")
                         .exposedAs("colorParty")
                         .build()
        );
    }
}
