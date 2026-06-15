package hue.captains.singapura.js.homing.studio.workspace;

import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Studio Workspace — a reusable {@link WorkspaceSpec} for browsing a
 * studio's catalogues inside the V2 workspace shell. Mounted by
 * {@code GenericWorkspace} at
 * {@code ?app=genericWorkspace&ws_kind=studio}.
 *
 * <p>Pure declarations — no body JS, no chrome wiring. This milestone
 * declares a single widget, the {@link TreeWidget}, pinned at boot so the
 * studio navigation tree is always visible. The tree's data comes from
 * {@code GET /catalogue-tree} ({@link CatalogueTreeGetAction}), which the
 * hosting studio wires with its own root catalogue via
 * {@code Fixtures.harnessGetActions()}.</p>
 *
 * <p>The pinned {@code TreeWidget} uses a blank {@code catalogueId}, so the
 * endpoint serves the whole studio forest. Additional widgets (a doc
 * viewer opened from tree selection, a search panel, …) and the studio
 * navigation Party land in follow-ups; the spec surface already supports
 * them ({@code parties()}, {@code actionDispatch()}).</p>
 *
 * <p>Registration is explicit, not class-load: {@code StudioStarterFixtures}
 * registers this spec into the {@code WorkspaceSpecRegistry} (RFC 0040), so a
 * downstream studio on the starter gets it with no wiring. This is a plain
 * value — the old class-init "touch" ({@code SPEC_INIT} field) is retired.</p>
 *
 * @since homing-studio-workspace — Studio Workspace, first widget (tree view)
 */
public final class StudioWorkspaceSpec implements WorkspaceSpec {

    public static final StudioWorkspaceSpec INSTANCE = new StudioWorkspaceSpec();

    private StudioWorkspaceSpec() {}

    @Override public String kind()  { return "studio"; }
    @Override public String title() { return "Studio"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        return List.of(
                WidgetEntry.of(TreeWidget.class, WidgetLabel.of("Navigator"))
                        .withIcon(new WidgetIcon.Emoji("🌳")) // 🌳
                        .withGroup(WidgetGroup.of("Navigation"))
                        .withDefaults(navigatorDefaults()),
                WidgetEntry.of(SummaryWidget.class, WidgetLabel.of("Summary"))
                        .withIcon(new WidgetIcon.Emoji("📄")) // 📄
                        .withGroup(WidgetGroup.of("Navigation"))
        );
    }

    @Override
    public List<PartyDecl> parties() {
        // The navigation message bus. No pre-declared actors — the TreeWidget
        // joins dynamically as the source; future content/detail panes join as
        // consumers. Exposed to widgets as workspaceCtx.navParty.
        return List.of(
                PartyDecl.of("navigation",
                             NavigatorSecretaryModule.INSTANCE, "NavigatorSecretary")
                         .exposedAs("navParty")
                         .build()
        );
    }

    // No pinnedSpawns(): the Navigator is a regular pickable widget. Pinned
    // entries are filtered OUT of the picker (they auto-spawn at boot), so
    // pinning the sole widget would leave the picker empty. Open it with the
    // ➕ button like any other widget.

    /** Picker-form defaults for the Navigator. Blank {@code catalogueId} →
     *  the endpoint serves the whole studio forest. */
    private static Map<String, String> navigatorDefaults() {
        var d = new LinkedHashMap<String, String>();
        d.put("catalogueId", "");
        return d;
    }
}
