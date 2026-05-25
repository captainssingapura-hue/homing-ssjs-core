package hue.captains.singapura.js.homing.workspace.party;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0028 cycle 2 — the JS runtime for one Party. Workspaces import
 * the {@link Party} JS class and instantiate one per concern with a
 * tree of Secretary descriptors (each carrying its own state and
 * behaviour function). The runtime provides registration, dispatch,
 * action execution, and inspection.
 *
 * <h2>Wiring contract</h2>
 *
 * <p>The workspace shell constructs each Party at init time, then
 * joins chrome Actors immediately and widget Actors as widgets are
 * added. Consumers cancel their Party membership by calling
 * {@code party.leave(actorId)}; chrome glue invokes the widget-side
 * {@code partyDeregister} hook on tab close to drive this.</p>
 *
 * <pre>{@code
 * import { Party } from "...PartyModule.js";
 *
 * const layoutParty = new Party({
 *     name: "layout",
 *     root: {
 *         path: "workspace",
 *         initial: { fullscreen: false },
 *         behavior: layoutBehavior   // (state, envelope) => Step
 *     }
 * });
 *
 * layoutParty.joinActor({
 *     id: "workspace/ribbon/fullscreenToggle",
 *     parentSecretary: "workspace",
 *     reactors: { }   // sender-only — no incoming handlers
 * });
 *
 * layoutParty.tellFrom("workspace/ribbon/fullscreenToggle",
 *                       { kind: "FullscreenToggleRequested" });
 * }</pre>
 *
 * <p>Message shape on the wire: plain JS objects with a {@code kind}
 * discriminator (e.g. {@code { kind: "FullscreenChanged", on: true }}).
 * Reactors are keyed by {@code kind} strings; behaviour functions
 * pattern-match on {@code envelope.message.kind}.</p>
 *
 * @since RFC 0028 cycle 2
 */
public record PartyModule() implements DomModule<PartyModule> {

    /** The single export — the {@code Party} JS class. */
    public record Party() implements Exportable._Constant<PartyModule> {}

    public static final PartyModule INSTANCE = new PartyModule();

    @Override
    public ImportsFor<PartyModule> imports() {
        // No CSS, no other modules — Party is a pure JS runtime that
        // touches no DOM, holds no theming, has no dependencies.
        return ImportsFor.<PartyModule>builder().build();
    }

    @Override
    public ExportsOf<PartyModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Party()));
    }
}
