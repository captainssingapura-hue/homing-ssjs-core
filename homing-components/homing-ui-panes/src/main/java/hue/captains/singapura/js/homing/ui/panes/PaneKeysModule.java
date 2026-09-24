package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code PaneKeys}: the pane's keys while it holds them, as one function over
 * the pane's own surface — the arrows and Home/End changing the active tab,
 * Shift+arrows reordering along the rail, Shift+Down asking to detach, Enter
 * having the widget activate itself, Escape yielding, the menu key. What a
 * key means for a container of tabs, and nothing about how the pane is
 * built; the pane's {@code keyDown} is this. Headless, and pure: it imports
 * nothing — a pure module importing a DOM module would load a second,
 * theme-less copy of it and of the one-per-document steward behind it — so
 * the yield goes through {@code pane.yieldKeys()}.
 */
public record PaneKeysModule() implements EsModule<PaneKeysModule> {

    /** The class of statics: {@code keyDown(pane, ev)}. */
    /** One, several or none, and the order they are asked in: {@code of}, {@code keyDown}, {@code chord}. */
    public record PaneSchemes() implements Exportable._Constant<PaneKeysModule> {}

    public record PaneKeys() implements Exportable._Constant<PaneKeysModule> {}

    /**
     * The other scheme that ships: what a browser does. Ctrl+Tab forward,
     * Ctrl+Shift+Tab back, the page keys the same, and the row WRAPS — which
     * the container's scheme deliberately does not. It answers on the bar and
     * through the chord alike, because moving between tabs while you are
     * typing is the whole of what it imitates.
     *
     * <p>A browser keeps Ctrl+Tab and the page keys for its own tabs and does
     * not hand them to a page, so inside a browser tab this scheme is silent;
     * the keys arrive in a desktop shell, or under a fullscreen keyboard lock.
     * It therefore answers Ctrl+Shift+← and Ctrl+Shift+→ as well: the same
     * movement by a chord nobody reserves.</p>
     */
    public record BrowserKeys() implements Exportable._Constant<PaneKeysModule> {}

    public static final PaneKeysModule INSTANCE = new PaneKeysModule();

    @Override public ImportsFor<PaneKeysModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<PaneKeysModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PaneSchemes(), new PaneKeys(), new BrowserKeys()));
    }
}
