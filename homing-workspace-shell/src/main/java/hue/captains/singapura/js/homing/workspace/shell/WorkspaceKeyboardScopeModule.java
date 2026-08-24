package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0052 — the workspace's <b>keyboard-scope watcher</b>
 * ({@code KeyboardScope}): one orthogonal fact, kept live — does this
 * workspace own the keyboard right now? Split from the
 * {@link WorkspaceFocusCoordinatorModule focus coordinator} when the
 * 250-effective-line ratchet fired, on the seam the rule exists to find:
 * scope is a property of the DOCUMENT's focus, not of the coordinator's
 * selection.
 *
 * <p>The keyboard axis always has a home (a cursor pane, or an entered
 * widget), but that home only receives keystrokes while focus is inside the
 * workspace — or nowhere, which is exactly when the shallow keyboard acts.
 * Focus parked in page chrome leaves the locus real but DEAF, so RFC 0052's
 * keyboard-locus mark is gated on this watcher: shown iff the keyboard would
 * actually act. The scope test mirrors {@code ShallowKeyboard._inScope}
 * deliberately — one notion of "belongs to this workspace", not two that can
 * drift apart.</p>
 *
 * <p>Synchronous by construction, no timers: {@code focusin} reports where
 * focus arrived, {@code focusout} carries {@code relatedTarget} (null means
 * heading for {@code <body>}, which IS in scope), and window focus/blur covers
 * the document losing the keys to another application. Constructed and
 * disposed by the coordinator.</p>
 */
public record WorkspaceKeyboardScopeModule() implements DomModule<WorkspaceKeyboardScopeModule> {

    /** The single export — the {@code KeyboardScope} JS watcher class. */
    public record KeyboardScope() implements Exportable._Constant<WorkspaceKeyboardScopeModule> {}

    public static final WorkspaceKeyboardScopeModule INSTANCE = new WorkspaceKeyboardScopeModule();

    @Override
    public ImportsFor<WorkspaceKeyboardScopeModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<WorkspaceKeyboardScopeModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new KeyboardScope()));
    }
}
