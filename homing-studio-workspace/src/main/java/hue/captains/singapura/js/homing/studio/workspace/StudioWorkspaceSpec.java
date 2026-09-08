package hue.captains.singapura.js.homing.studio.workspace;

import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.Arrangement;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangement;
import hue.captains.singapura.js.homing.workspace.shell.PaneDirection;
import hue.captains.singapura.js.homing.workspace.shell.ShapePane;
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
 * <p>Pure declarations — no body JS, no chrome wiring. Three widgets, all
 * pickable, joined by the studio navigation {@code Party}:</p>
 * <ul>
 *   <li>{@link TreeWidget} (Navigator) — the catalogue tree. Publishes the two
 *       navigation tiers: {@code NodeSelected} on browse (every arrow / click)
 *       and {@code NodeOpened} on intent (Enter / double-click).</li>
 *   <li>{@link SummaryWidget} (Summary) — the cheap detail card. Reacts to the
 *       redirected {@code NavigateTo} on every selection.</li>
 *   <li>{@link DocContentWidget} (Document) — the expensive content pane. Reacts
 *       to the redirected {@code OpenDoc} only, rendering the opened doc in place
 *       (foldable TOC + body) so the costly fetch is paid on intent, not browse.</li>
 * </ul>
 *
 * <p>The Navigator uses a blank {@code catalogueId}, so {@code GET
 * /catalogue-tree} ({@link CatalogueTreeGetAction}) serves the whole studio
 * forest. The content pane fetches {@code /doc-tree} by the selected node's
 * leveled path. The {@code NavigatorSecretary} redirects both tiers
 * ({@code NodeSelected→NavigateTo}, {@code NodeOpened→OpenDoc}); the spec also
 * supports {@code actionDispatch()} for future wiring.</p>
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

    // ── RFC 0060 — the arrangement this workspace opens in ───────────────────

    /**
     * Navigator down the left, and on the right a Summary card above the
     * Document it describes.
     *
     * <p>The shape follows the widgets' <b>two-speed</b> relationship rather than
     * taste. Navigator publishes two tiers; Summary reacts to <i>every</i>
     * selection while Document reacts to intent <i>only</i>. So Navigator and
     * Summary move together as you arrow around, and Document is the stable
     * destination — which is why the first two are adjacent and the Document gets
     * the largest area.</p>
     *
     * <p>Its own playbook rather than a shipped shape: {@code TRIPLE_COLUMN} would
     * put all three in columns and leave the Document — which renders a foldable
     * TOC beside an 820px body — at half the width, and {@code IDE} has the
     * detail/main ratio the wrong way round. The shipped set is a starting point,
     * not a constraint (RFC 0060 D7).</p>
     */
    private static final PaneArrangement STUDIO_SHAPE =
            PaneArrangement.named("studio")
                    .root("document")
                    // Navigator takes 20% down the left; the document keeps 80%.
                    .splitWithRatio("document", PaneDirection.LEFT, "navigator", 0.80)
                    // Of that 80%, the Summary card takes the top quarter.
                    .splitWithRatio("document", PaneDirection.UP, "summary", 0.75)
                    .build();

    private static final ShapePane NAVIGATOR = STUDIO_SHAPE.pane("navigator");
    private static final ShapePane SUMMARY   = STUDIO_SHAPE.pane("summary");
    private static final ShapePane DOCUMENT  = STUDIO_SHAPE.pane("document");

    @Override
    public Arrangement arrangement() {
        return STUDIO_SHAPE.allocate()
                .place(NAVIGATOR, TreeWidget.class)
                .place(SUMMARY,   SummaryWidget.class)
                .place(DOCUMENT,  DocContentWidget.class)
                .build();
    }
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
                        .withGroup(WidgetGroup.of("Navigation")),
                WidgetEntry.of(DocContentWidget.class, WidgetLabel.of("Document"))
                        .withIcon(new WidgetIcon.Emoji("📖")) // 📖
                        .withGroup(WidgetGroup.of("Navigation"))
        );
    }

    @Override
    public List<PartyDecl> parties() {
        // The navigation message bus. No pre-declared actors — the TreeWidget
        // joins dynamically as the source; the Summary (NavigateTo) and Document
        // (OpenDoc) panes join as consumers. Exposed as workspaceCtx.navParty.
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
