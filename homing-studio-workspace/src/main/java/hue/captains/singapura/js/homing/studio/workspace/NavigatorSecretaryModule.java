package hue.captains.singapura.js.homing.studio.workspace;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The Secretary for the Studio Workspace's {@code NavigatorParty} — the
 * navigation message bus. Per the Diligent Secretaries doctrine, a pure
 * {@code (state, envelope) → Step} routing function plus an initial state,
 * exported as a single {@code NavigatorSecretary} JS object.
 *
 * <p><b>Just redirects.</b> The Secretary holds no UI and renders nothing.
 * Its sole job is to relay a tree selection to every Party member: an
 * incoming {@code NodeSelected} is rebroadcast as {@code NavigateTo}. The
 * {@link TreeWidget} is the source actor; any widget that joins the Party
 * (a future content / detail pane) receives the redirect and decides what
 * to do with it. Decoupling the producer from the consumer is the whole
 * point — the tree never names who consumes its selections.</p>
 *
 * <h2>State shape</h2>
 * <pre>{@code
 * {
 *     lastSelected : node | null,        // most recent selection (diligence)
 *     recentUnknown: [{ kind, from }]    // last 10 unrecognised messages, bounded
 * }
 * }</pre>
 *
 * <h2>Message kinds</h2>
 * <ul>
 *   <li>{@code NodeSelected(node)} — record + {@code BroadcastToMembers}
 *       a {@code NavigateTo(node)}.</li>
 *   <li>Anything else — append to {@code recentUnknown} (bounded at 10);
 *       no action.</li>
 * </ul>
 *
 * @since homing-studio-workspace — studio navigation Party
 */
public record NavigatorSecretaryModule() implements DomModule<NavigatorSecretaryModule> {

    /** The single export — a JS object with {@code initial} and {@code behavior}. */
    public record NavigatorSecretary() implements Exportable._Constant<NavigatorSecretaryModule> {}

    public static final NavigatorSecretaryModule INSTANCE = new NavigatorSecretaryModule();

    @Override
    public ImportsFor<NavigatorSecretaryModule> imports() {
        return ImportsFor.<NavigatorSecretaryModule>builder().build();
    }

    @Override
    public ExportsOf<NavigatorSecretaryModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new NavigatorSecretary()));
    }
}
