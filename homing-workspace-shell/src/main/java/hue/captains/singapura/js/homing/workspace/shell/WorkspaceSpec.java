package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.FooterItem;
import hue.captains.singapura.js.homing.workspace.RibbonItem;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;

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
 *   <li><b>Functional Object</b> — stateless, pure declarations.</li>
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
public interface WorkspaceSpec {

    /**
     * Stable wire identifier — appears as {@code ?ws_kind=<kind>} on the
     * URL and as the IDB scoping key (per RFC 0031). Must be unique
     * across all registered specs; validation lives in
     * {@link WorkspaceSpecRegistry#register(WorkspaceSpec)}.
     */
    String kind();

    /** Title shown in the workspace's Ribbon and the browser tab. */
    String title();

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
     * Widget {@code simpleName}s to auto-spawn as <b>pinned</b> tabs at
     * boot — typically a welcome / introduction doc and any always-on
     * scratchpad. Each name must match an entry in
     * {@link #widgetEntries()}; missing names are logged + skipped.
     *
     * <p>Spec-level concern, independent of the widget class's default
     * {@code lifecycleHint()} — a workspace can pin any widget without
     * modifying the widget itself. Phase 14
     * ({@code PinnedTabSpawnerModule}) consumes this list in boot
     * order, mounting each into a target slot (default {@code 'tl'})
     * and setting the first as workspace-active.</p>
     */
    default List<String> pinnedSpawns() { return List.of(); }
}
