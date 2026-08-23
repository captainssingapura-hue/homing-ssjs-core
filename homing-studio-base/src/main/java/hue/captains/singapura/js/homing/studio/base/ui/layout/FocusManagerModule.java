package hue.captains.singapura.js.homing.studio.base.ui.layout;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0049 — {@code FocusManager}: a <b>per-tab</b> focus manager. One instance
 * is 1:1 with a tab and owns the tab's <b>immutable</b> (position-independent)
 * focus mechanics — everything that operates on the tab's own content element:
 * {@code inert} on that content, placing native focus into it, the
 * {@code setActive(bool)} lifecycle call, {@code reconcile()} drift-repair, and
 * the widget-<b>originated</b> events (give-up = un-consumed {@code Escape} /
 * {@code homing-focus} release, and {@code intendedFocusIn} = a request to become
 * active).
 *
 * <p>Because its listeners live on the tab's content element, the manager
 * <b>travels with the tab</b> (a DOM re-parent preserves listeners) and is
 * disposed with it. The <b>workspace</b> owns everything positional/mutable —
 * the single selection, exclusivity, visuals, clicks/arrows/Enter — and drives
 * {@code enter} / {@code release}; the manager enacts and reports, it never
 * decides selection. Supersedes RFC 0048's per-pane {@code FocusScope}.</p>
 *
 * <p>Activation focus is one function resolved by precedence (per-request &gt;
 * the tab's registered default &gt; system fallback: focus the content if it
 * isn't already in the tab). {@code setActive} is pure lifecycle — it never
 * focuses — so there is no {@code inert}-vs-{@code setActive} ordering.</p>
 *
 * <p>Dependency-free; builds no DOM and applies no styling of its own.</p>
 */
public record FocusManagerModule() implements DomModule<FocusManagerModule> {

    /** The single export — the {@code FocusManager} JS class. */
    public record FocusManager() implements Exportable._Constant<FocusManagerModule> {}

    public static final FocusManagerModule INSTANCE = new FocusManagerModule();

    @Override
    public ImportsFor<FocusManagerModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<FocusManagerModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new FocusManager()));
    }
}
