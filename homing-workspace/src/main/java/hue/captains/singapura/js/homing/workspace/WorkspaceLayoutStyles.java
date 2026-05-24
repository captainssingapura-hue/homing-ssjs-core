package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * Typed CSS for {@link WorkspaceLayoutModule}. Every visual treatment
 * named, no raw cssText, per RFC 0027.
 *
 * <h2>Layout shape</h2>
 *
 * <pre>
 * ┌─ wl_root (flex column, overflow:hidden — "locked in size") ───┐
 * │ ┌─ wl_ribbon (flex row, flex-shrink:0) ──────────────────┐   │
 * │ │  wl_ribbon_title  …  wl_ribbon_items  …  wl_ribbon_fs  │   │
 * │ └────────────────────────────────────────────────────────┘   │
 * │ ┌─ wl_content (flex:1, min-height:0, overflow:hidden) ────┐  │
 * │ │  MultiTabPane mounts here                              │  │
 * │ └────────────────────────────────────────────────────────┘  │
 * │ ┌─ wl_footer (flex-shrink:0, omitted if no items) ────────┐  │
 * │ │  wl_footer_items                                       │  │
 * │ └────────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p>{@link #wl_fullscreen_active} is added to {@code &lt;body&gt;}
 * when full-screen is on. CSS rules in {@link #wl_root} react: the
 * workspace's root jumps to {@code position:fixed; inset:0; z-index}
 * above the studio chrome. Sibling rule ({@link #wl_chrome_hidden})
 * targets {@code .st-header} (and any future studio chrome surfaces)
 * with {@code display:none} so they're both invisible AND
 * unoperatable.</p>
 *
 * @since RFC 0025 Ext1b b.2e — workspace chrome (ribbon + footer + fullscreen)
 */
public record WorkspaceLayoutStyles() implements CssGroup<WorkspaceLayoutStyles> {

    public static final WorkspaceLayoutStyles INSTANCE = new WorkspaceLayoutStyles();

    /**
     * The workspace's root flex column. Locked in size: overflow:hidden
     * prevents anything inside from pushing the document taller; ribbon
     * + footer are flex-shrink:0, content is flex:1 so the column
     * partitions cleanly. Fullscreen mode (body class) repositions to
     * viewport-fixed.
     *
     * <p>Lives inside the chrome's {@code host} div (a block container
     * with {@code flex:1} inside {@code .st-main}'s flex column). So
     * {@code height:100%} of host resolves to host's pixel height.
     * No {@code flex:1} on this record itself — host is not a flex
     * container.</p>
     */
    public record wl_root() implements CssClass<WorkspaceLayoutStyles> {
        // ─── Subtle border: 1px solid with the framework's --color-border
        //     token + a small radius so the workspace reads as a single
        //     bordered surface distinct from surrounding studio chrome.
        //     box-sizing:border-box so the border doesn't push the inner
        //     100%-sized chain past its allotted box.
        //     Fullscreen mode (wl_root_fullscreen) overrides border + radius
        //     back to none since the workspace then IS the viewport.
        @Override public String body() { return """
                position: relative;
                display: flex;
                flex-direction: column;
                width: 100%;
                height: 100%;
                overflow: hidden;
                background: var(--color-surface, #fff);
                border: 1px solid var(--color-border, #ddd);
                border-radius: 4px;
                box-sizing: border-box;
                """; }
    }

    public record wl_ribbon() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 8px;
                padding: 4px 10px;
                background: var(--color-surface-raised, #f5f5f5);
                border-bottom: 1px solid var(--color-border, #ddd);
                flex-shrink: 0;
                font: 13px sans-serif;
                color: var(--color-text-primary);
                """; }
    }

    public record wl_ribbon_title() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                font-weight: 600;
                color: var(--color-text-primary);
                """; }
    }

    public record wl_ribbon_items() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 6px;
                margin-left: 16px;
                flex: 1;
                """; }
    }

    public record wl_ribbon_button() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 26px;
                height: 22px;
                border: 1px solid transparent;
                border-radius: 3px;
                background: transparent;
                color: var(--color-text-primary);
                cursor: pointer;
                font: 14px sans-serif;
                line-height: 1;
                """; }
    }

    public record wl_ribbon_button_hover() implements CssClass<WorkspaceLayoutStyles> {
        // Applied on mouseover by the chrome JS to avoid pseudo-class
        // wiring at this layer.
        @Override public String body() { return """
                background: color-mix(in srgb, var(--color-accent) 15%, transparent);
                border-color: var(--color-border, #ddd);
                """; }
    }

    public record wl_ribbon_separator() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                width: 1px;
                height: 16px;
                background: var(--color-border, #ddd);
                """; }
    }

    public record wl_ribbon_label() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                color: var(--color-text-muted, #888);
                font: 12px sans-serif;
                """; }
    }

    /** Right-edge fullscreen toggle. */
    public record wl_ribbon_fs() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                margin-left: auto;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 26px;
                height: 22px;
                border: 1px solid transparent;
                border-radius: 3px;
                background: transparent;
                color: var(--color-text-primary);
                cursor: pointer;
                font: 14px sans-serif;
                line-height: 1;
                """; }
    }

    public record wl_content() implements CssClass<WorkspaceLayoutStyles> {
        // NOT display:flex — children (e.g. SplitPane root) use position absolute
        // / width:100%; height:100% to fill, which requires a block parent with
        // explicit height. flex:1 + min-height:0 gives us that height inside
        // wl_root's flex column.
        @Override public String body() { return """
                flex: 1;
                min-height: 0;
                position: relative;
                overflow: hidden;
                """; }
    }

    public record wl_footer() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 8px;
                padding: 3px 10px;
                background: var(--color-surface-raised, #f5f5f5);
                border-top: 1px solid var(--color-border, #ddd);
                flex-shrink: 0;
                font: 11px sans-serif;
                color: var(--color-text-muted, #888);
                """; }
    }

    public record wl_footer_separator() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                width: 1px;
                height: 12px;
                background: var(--color-border, #ddd);
                """; }
    }

    public record wl_footer_button() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 22px;
                height: 18px;
                border: 1px solid transparent;
                border-radius: 3px;
                background: transparent;
                color: var(--color-text-primary);
                cursor: pointer;
                font: 12px sans-serif;
                line-height: 1;
                """; }
    }

    /**
     * Applied to {@code &lt;body&gt;} as long as a workspace is mounted.
     * Anchors body + .st-root to exact viewport height so the workspace's
     * own overflow:hidden chain can clip without the document scrolling.
     *
     * <p><b>Why this exists.</b> {@code .st-root}'s base style is
     * {@code min-height:100vh} — it can grow taller than the viewport
     * when content pushes it. That breaks "workspace locked in size"
     * because even with {@code wl_root}'s {@code overflow:hidden}, the
     * workspace's allotted height is unbounded; tall widget content
     * grows the wrapper, the wrapper grows {@code .st-root}, and the
     * browser shows a document scrollbar. This class pins the chain
     * to {@code height:100vh; overflow:hidden} for the duration of the
     * workspace's life, restored on unmount.</p>
     */
    public record wl_workspace_active() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return ""; /* marker — rules apply via body selector below */ }
    }

    /**
     * Companion rule for {@link #wl_workspace_active}. Body becomes
     * exactly viewport tall, scrolling forbidden. {@code .st-root}
     * shrinks from "at least 100vh" to "exactly 100vh" so its flex:1
     * descendant ({@code .st-main}) has a bounded vertical box for
     * the workspace's chain.
     */
    public record wl_body_locked() implements CssClass<WorkspaceLayoutStyles> {
        // The selector here piggybacks on the class name. JS adds the same
        // generated name to body when the workspace mounts. Browser-native
        // pseudo-selector logic isn't accessible via typed CssClass; we use
        // a JS-side toggle to apply the rules to body + .st-root directly.
        @Override public String body() { return """
                height: 100vh !important;
                overflow: hidden !important;
                """; }
    }

    /**
     * Applied to {@code &lt;body&gt;} when full-screen mode is active.
     * Combined with selectors below to push the workspace to fixed
     * viewport position and hide studio chrome.
     */
    public record wl_fullscreen_active() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return ""; /* marker class — rules live in companion records */ }
    }

    /**
     * When {@code body.wl-fullscreen-active} is set, the workspace's
     * own root jumps to viewport-fixed. The chrome adds this class to
     * its root element in response to the body class.
     */
    public record wl_root_fullscreen() implements CssClass<WorkspaceLayoutStyles> {
        // Drop border + radius in fullscreen — the workspace IS the viewport
        // here, a framed edge would look like a leftover artefact.
        @Override public String body() { return """
                position: fixed;
                inset: 0;
                z-index: 9000;
                height: 100vh;
                border: none;
                border-radius: 0;
                """; }
    }

    /**
     * Marker class the chrome adds to studio-chrome elements to hide
     * during fullscreen. CSS sets {@code display:none} — both
     * invisible AND unoperatable (no events, no tab order, no
     * accidental clicks bleeding through).
     */
    public record wl_chrome_hidden() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                display: none !important;
                """; }
    }

    @Override
    public List<CssClass<WorkspaceLayoutStyles>> cssClasses() {
        return List.of(
                new wl_root(),
                new wl_ribbon(),
                new wl_ribbon_title(),
                new wl_ribbon_items(),
                new wl_ribbon_button(),
                new wl_ribbon_button_hover(),
                new wl_ribbon_separator(),
                new wl_ribbon_label(),
                new wl_ribbon_fs(),
                new wl_content(),
                new wl_footer(),
                new wl_footer_separator(),
                new wl_footer_button(),
                new wl_workspace_active(),
                new wl_body_locked(),
                new wl_fullscreen_active(),
                new wl_root_fullscreen(),
                new wl_chrome_hidden()
        );
    }

    @Override
    public CssImportsFor<WorkspaceLayoutStyles> cssImports() {
        return CssImportsFor.none(this);
    }
}
