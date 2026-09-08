package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.FooterItem;
import hue.captains.singapura.js.homing.workspace.RibbonItem;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.tao.ontology.Stateless;

import java.util.List;
import java.util.Map;

/**
 * Declarative specification for one workspace kind. Implementations are
 * <b>stateless singletons</b> (typically a {@code final class} with an
 * {@code INSTANCE} static field) — they declare what a workspace kind
 * looks like, with zero embedded behaviour.
 *
 * <p>A {@code WorkspaceSpec} carries everything {@link GenericWorkspaceChrome}
 * needs to mount the workspace's chrome via
 * {@code mountWorkspaceShell(branch, parent, specJson)}:</p>
 *
 * <ul>
 *   <li>Static identity — {@link #kind()}, {@link #title()}.</li>
 *   <li>Widget registry — {@link #widgetEntries()}.</li>
 *   <li>Ribbon / footer — {@link #ribbonItems()}, {@link #footerItems()}.</li>
 *   <li>Party declarations — {@link #parties()} ({@link PartyDecl} per
 *       Party to construct at boot).</li>
 *   <li>Action dispatch — {@link #actionDispatch()} (ribbon/footer
 *       {@code actionId} to a typed {@link ActionDispatch}).</li>
 *   <li>Widget params codec references — {@link #widgetCodecs()}
 *       ({@link WidgetCodecRef} per widget kind that needs a non-empty
 *       codec).</li>
 * </ul>
 *
 * <p>Doctrines applied:</p>
 *
 * <ul>
 *   <li><b>{@link Stateless}</b> — pure declarations, so any two instances
 *       of an implementation are interchangeable and the singleton is a
 *       convenience rather than a requirement. Deliberately not
 *       {@code StatelessFunctionalObject}: a spec transforms nothing, and
 *       unlike a function its <i>identity</i> is load-bearing — {@link #kind()}
 *       is a wire identifier and an IDB scoping key.</li>
 *   <li><b>Names Are Types</b> — every cross-language identifier
 *       (party name, actor path, action id, widget kind) is carried in
 *       a typed record, not a loose string at the boundary.</li>
 *   <li><b>Make It Impossible Not Forbidden</b> — workspace authors
 *       cannot hand-write chrome JS. The only way to express a workspace
 *       is to fill in this declarative surface; anything the surface
 *       doesn't support has to land as a substrate primitive first.</li>
 *   <li><b>A Good Design Doesn't Pick Up Its Substrate</b> — the spec
 *       doesn't mention the persistence layer, event log, checkpoint
 *       store, Web Lock, or any other RFC infrastructure; the substrate
 *       handles those uniformly for every spec.</li>
 * </ul>
 *
 * @since post-RFC-0034 workspace chrome decomposition
 */
public interface WorkspaceSpec extends Stateless {

    /**
     * Stable wire identifier — appears as {@code ?ws_kind=<kind>} on the
     * URL and as the IDB scoping key (per RFC 0031). Must be unique
     * across all registered specs; validation lives in
     * {@link WorkspaceSpecRegistry#register(WorkspaceSpec)}.
     */
    String kind();

    /** Title shown in the workspace's Ribbon and the browser tab. */
    String title();

    /**
     * The section this kind sits under in the workspace switcher — the role
     * {@code Theme.group()} plays for the theme picker (RFC 0057). Defaulted so
     * no existing spec breaks: every kind lands in one section until its spec
     * says otherwise.
     */
    default String group() { return "Workspaces"; }

    /** Widget types this workspace exposes via its picker. */
    List<WidgetEntry> widgetEntries();

    /** Ribbon content (workspace-specific middle section). */
    default List<RibbonItem> ribbonItems() { return List.of(); }

    /** Footer content; empty list suppresses the footer slot entirely. */
    default List<FooterItem> footerItems() { return List.of(); }

    /**
     * Parties this workspace constructs at boot. Each {@link PartyDecl}
     * names the Secretary's JS module + initial actors + (optionally)
     * the {@code workspaceCtx} key the constructed Party is exposed
     * under to widgets.
     */
    default List<PartyDecl> parties() { return List.of(); }

    /**
     * Workspace-specific dispatch table: ribbon/footer {@code actionId}
     * → typed {@link ActionDispatch}. The chrome calls into this when
     * a ribbon or footer action fires; the substrate interprets the
     * typed value, no per-workspace JS required.
     */
    default Map<String, ActionDispatch> actionDispatch() { return Map.of(); }

    /**
     * Per-widget-kind params codec module references. The substrate
     * dynamic-imports each and registers them with
     * {@code WidgetParamsCodecRegistry} before persistence attach.
     * Widget kinds not listed get the substrate's identity codec.
     */
    default List<WidgetCodecRef> widgetCodecs() { return List.of(); }


    /**
     * RFC 0060 — the arrangement this workspace <b>starts in</b>: its panes, and
     * the widgets in each.
     *
     * <p>The default is {@link PaneArrangements#SINGLE} — one pane taking the whole
     * space. A workspace that has been told nothing should not pre-commit its
     * reader to a shape, which is what the old 2×2 did.</p>
     *
     * <p>Take a shipped design and say what goes where; the starter set is pure
     * geometry precisely so two workspaces can share one:</p>
     *
     * <pre>{@code
     * @Override public Arrangement arrangement() {
     *     return PaneArrangements.MAIN_AND_OUTPUT
     *             .allocate()
     *             .place(MainAndOutput.MAIN,   DocViewWidget.class)
     *             .place(MainAndOutput.OUTPUT, LogWidget.class)
     *             .build();
     * }
     * }</pre>
     *
     * <p>Widgets that once went in {@code pinnedSpawns()} belong here — in
     * {@link PaneArrangements#SINGLE}'s one pane when the workspace wants no
     * particular shape:</p>
     *
     * <pre>{@code
     * @Override public Arrangement arrangement() {
     *     return PaneArrangements.SINGLE.allocate()
     *             .place(Single.MAIN, DocViewWidget.class)
     *             .build();
     * }
     * }</pre>
     *
     * <p><b>It is a SEED, not a template</b> (D10). It applies only to a workspace
     * with no saved state; a saved layout always wins, and editing this method
     * never reshapes a workspace someone already has open. That is a property of
     * the event-sourced model, not a limitation of this method.</p>
     *
     * <p>Seeding a widget here does <b>not</b> hide it from the picker (D20). The
     * arrangement says what a workspace <i>opens</i> with; whether a second copy
     * may be opened is the widget's own {@code lifecycleHint()} to declare.</p>
     */
    default Arrangement arrangement() { return PaneArrangements.SINGLE.empty(); }

    /**
     * RFC 0047 — the workspace's <b>global tab budget</b>: the maximum number
     * of tabs that may exist across every pane at once. It is a single shared
     * pool, not a per-pane ration — splitting a pane never changes it, and when
     * it is reached the "+" affordance disables on every pane together. Default
     * 16; a dense workspace (many widgets on screen) can raise it, a focused one
     * lower it.
     */
    default int maxTabs() { return 16; }
}
