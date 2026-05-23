package hue.captains.singapura.js.homing.studio.base.widget;

import hue.captains.singapura.js.homing.core.AppModule;

/**
 * RFC 0025 L cleanup — abstract base for <i>workspace-style</i>
 * AppModules: apps whose nature is "behaviours trapped in a page"
 * rather than "a page with some behaviour." Examples: MultiTabPane
 * demo, Modal demo, SplitPane demo, the future WorkspaceShell. They
 * own the viewport; they have their own interactive surface; they
 * cannot coexist with theme-side event handlers that gate
 * {@code pointer-events: none} on the body.
 *
 * <h2>What this base does for you</h2>
 *
 * <ol>
 *   <li><b>Opts out of theme backdrop interactivity.</b>
 *       {@link AppModule#acceptsBackdropInteractivity()} returns
 *       {@code false}. Themes that install universal
 *       {@code pointer-events: none} (Maple Bridge, Jazz Drums, Retro
 *       90s) honour this via the {@code homing-bg-passive} body class
 *       — the visual backdrop stays; only its event handling steps
 *       aside.</li>
 *   <li><b>Full-bleed main slot.</b>
 *       {@link StandardMPA#fullbleedMain()} returns {@code true}. The
 *       framework's chrome bootstrap neutralises {@code .st-main}'s
 *       1280px max-width and 36/32/64 padding, then turns the slot
 *       into a flex container. The widget's host element becomes
 *       {@code flex:1} and fills the available area cleanly — no
 *       4-line incantation needed in each widget's {@code bodyJs()}.</li>
 * </ol>
 *
 * <h2>When to use this base vs {@link SingleWidgetMPA}</h2>
 *
 * <ul>
 *   <li>Apps that are <b>pages with some behaviour</b> (catalogue
 *       browser, doc reader, plan tracker, single-widget viewer like
 *       SvgViewer or ComposedViewer): {@link SingleWidgetMPA}. The
 *       theme owns the chrome; the widget contributes a slice of
 *       content. The doc-reading max-width + padding helps.</li>
 *   <li>Apps that are <b>behaviours trapped in a page</b>
 *       (MultiTabPane workspace, IDE-style layout, drawing canvas,
 *       slideshow viewer, future {@code WorkspaceShell}): this base.
 *       The viewport is the work area; theme constraints fight it.</li>
 * </ul>
 *
 * <p>If you find yourself overriding either
 * {@code acceptsBackdropInteractivity} or {@code fullbleedMain} on a
 * {@link SingleWidgetMPA} subclass, the right base is probably this
 * one. The typed split exists so the choice is made at declaration
 * time, not in scattered overrides.</p>
 *
 * @param <P> typed URL Params record this viewer accepts
 * @param <M> self-type (CRTP)
 * @since RFC 0025 L cleanup — extracted from the WorkspaceMPA pattern
 *        that emerged across three demo apps in L1/L2/L3.
 */
public abstract class WorkspaceMPA<P extends AppModule._Param, M extends WorkspaceMPA<P, M>>
        extends SingleWidgetMPA<P, M> {

    @Override
    public final boolean acceptsBackdropInteractivity() {
        return false;
    }

    @Override
    protected final boolean fullbleedMain() {
        return true;
    }
}
