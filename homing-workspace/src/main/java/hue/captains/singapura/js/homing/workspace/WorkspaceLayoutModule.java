package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * RFC 0025 Ext1b b.2e — workspace chrome skeleton.
 *
 * <p>Vends the {@code WorkspaceLayout} JS class — a reusable structural
 * skeleton for every workspace's chrome widget:</p>
 *
 * <pre>
 *   ┌─ Ribbon (title + workspace-specific items + fullscreen toggle) ┐
 *   ├─ Content (locked size — MultiTabPane mounts here) ─────────────┤
 *   └─ Footer (optional — present iff workspace's footerItems is non-empty)
 * </pre>
 *
 * <p>Structure fixed; contents customisable per-workspace via typed Java
 * {@link RibbonItem} and {@link FooterItem} lists (serialized via
 * {@link WorkspaceLayoutJson}). The framework prepends the workspace
 * title (left of ribbon) and appends the fullscreen toggle (right of
 * ribbon) automatically — workspaces don't need to wire either.</p>
 *
 * <h2>Usage from a workspace chrome widget</h2>
 *
 * <pre>{@code
 * var layout = new WorkspaceLayout({
 *     container    : host,                           // your widget's host element
 *     title        : "Animals Playground",           // workspace.title()
 *     ribbonItems  : WORKSPACE_RIBBON_ITEMS,         // emitted JSON from ribbonItemsJson()
 *     footerItems  : WORKSPACE_FOOTER_ITEMS,         // emitted JSON from footerItemsJson()
 *     onAction     : function (actionId) { ... }    // ribbon/footer Button click dispatch
 * });
 * new MultiTabPane({ container: layout.contentEl, ... });
 * }</pre>
 *
 * <h2>Fullscreen</h2>
 *
 * <p>Toggle via the ribbon's fullscreen button (always present) or
 * {@code layout.toggleFullScreen()} / {@code layout.setFullScreen(bool)}.
 * Body gets the {@code wl-fullscreen-active} class; CSS pushes the
 * workspace root to {@code position:fixed; inset:0; z-index:9000} and
 * hides {@code .st-header} via {@code display:none} (both invisible
 * and unoperatable). Escape exits. State is session-only in V0 — URL
 * persistence is RFC 0025 W7 territory.</p>
 *
 * @since RFC 0025 Ext1b b.2e — workspace chrome (Ribbon + Footer + fullscreen)
 */
public record WorkspaceLayoutModule() implements DomModule<WorkspaceLayoutModule> {

    /** The single export — the {@code WorkspaceLayout} JS class. */
    public record WorkspaceLayout() implements Exportable._Constant<WorkspaceLayoutModule> {}

    public static final WorkspaceLayoutModule INSTANCE = new WorkspaceLayoutModule();

    @Override
    public ImportsFor<WorkspaceLayoutModule> imports() {
        return ImportsFor.<WorkspaceLayoutModule>builder()
                .add(new ModuleImports<>(
                        List.of(
                                new WorkspaceLayoutStyles.wl_root(),
                                new WorkspaceLayoutStyles.wl_ribbon(),
                                new WorkspaceLayoutStyles.wl_ribbon_title(),
                                new WorkspaceLayoutStyles.wl_ribbon_items(),
                                new WorkspaceLayoutStyles.wl_ribbon_button(),
                                new WorkspaceLayoutStyles.wl_ribbon_button_hover(),
                                new WorkspaceLayoutStyles.wl_ribbon_separator(),
                                new WorkspaceLayoutStyles.wl_ribbon_label(),
                                new WorkspaceLayoutStyles.wl_ribbon_fs(),
                                new WorkspaceLayoutStyles.wl_content(),
                                new WorkspaceLayoutStyles.wl_footer(),
                                new WorkspaceLayoutStyles.wl_footer_separator(),
                                new WorkspaceLayoutStyles.wl_footer_button(),
                                new WorkspaceLayoutStyles.wl_workspace_active(),
                                new WorkspaceLayoutStyles.wl_body_locked(),
                                new WorkspaceLayoutStyles.wl_fullscreen_active(),
                                new WorkspaceLayoutStyles.wl_root_fullscreen(),
                                new WorkspaceLayoutStyles.wl_chrome_hidden()),
                        WorkspaceLayoutStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceLayoutModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WorkspaceLayout()));
    }
}
