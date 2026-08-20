package hue.captains.singapura.js.homing.studio.base.ui.layout;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0048 — {@code FocusScope}: a small, reusable <b>give-up-focus wrapper</b>.
 * A container that owns a "you are inside me now" mode (a
 * {@link MultiTabPaneModule MultiTabPane} pane entered deep, a modal, …) wraps
 * its content in a {@code FocusScope} and is told, uniformly, when the content
 * wants to hand focus back.
 *
 * <p>The whole point is that a widget must work <b>anywhere</b>, so the wrapper
 * imposes <b>nothing special</b> on it — it rides only the two mechanisms every
 * DOM element already has. {@code new FocusScope(hostEl, onGiveUp)} listens on
 * {@code hostEl} (bubble phase) and calls {@code onGiveUp()} on <b>either</b>:</p>
 *
 * <ol>
 *   <li>an <b>un-consumed {@code Escape}</b> — a {@code keydown} that bubbled all
 *       the way up because no widget inside called {@code stopPropagation}; or</li>
 *   <li>a <b>release event</b> — a bubbling
 *       {@code CustomEvent('homing-focus', { detail: { intent: 'release' } })}.</li>
 * </ol>
 *
 * <p>The dual signal is what makes it trap-free without the container ever
 * stealing a key: a widget that uses {@code Escape} for its own function keeps
 * it (standard {@code stopPropagation}); a widget that <em>swallows Escape
 * entirely</em> (a terminal, an Excel-like grid) instead binds its own shortcut
 * to {@code dispatchEvent} the release event. Both routes reach the same wrapper.
 * Because every container uses this one class, give-up-focus behaves identically
 * everywhere — a portable DOM convention, not an injected API.</p>
 *
 * <p>Dependency-free; builds no DOM and applies no styling (it only attaches two
 * listeners), so it carries no CSS/DOM-ownership obligations of its own.</p>
 */
public record FocusScopeModule() implements DomModule<FocusScopeModule> {

    /** The single export — the {@code FocusScope} JS class. */
    public record FocusScope() implements Exportable._Constant<FocusScopeModule> {}

    public static final FocusScopeModule INSTANCE = new FocusScopeModule();

    @Override
    public ImportsFor<FocusScopeModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<FocusScopeModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new FocusScope()));
    }
}
