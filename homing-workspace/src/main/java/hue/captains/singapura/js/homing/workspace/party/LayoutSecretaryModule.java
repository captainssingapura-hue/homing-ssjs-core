package hue.captains.singapura.js.homing.workspace.party;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The Secretary for {@code LayoutParty} — extracted as a standalone JS
 * module per the Diligent Secretaries doctrine's third pillar: behaviors
 * must be unit-testable as pure functions, which requires being
 * importable in isolation (rather than inlined in the chrome's bodyJs).
 *
 * <p>Exports a single {@code LayoutSecretary} JS object with two members:
 * {@code initial} (the starting state record) and {@code behavior} (the
 * pure (state, envelope) → Step function). The {@code WorkspaceLayout}
 * chrome imports both and passes them to the Party constructor.</p>
 *
 * <h2>State shape (Diligent Secretaries enrichment)</h2>
 *
 * <pre>{@code
 * {
 *     fullscreen    : boolean,            // current value
 *     lastChangedBy : AgentId | null,     // provenance of most recent change
 *     recentUnknown : [{ kind, from }]    // last N unrecognised messages (bounded)
 * }
 * }</pre>
 *
 * <h2>Message kinds</h2>
 *
 * <ul>
 *   <li>{@code FullscreenToggleRequested} — flip and broadcast {@code FullscreenChanged}.</li>
 *   <li>{@code FullscreenSetRequested(on)} — set if different; broadcast on change; no-op if same.</li>
 *   <li>Anything else — append to {@code recentUnknown} (bounded at 10); no action emitted.</li>
 * </ul>
 *
 * @since RFC 0028 cycle 6 phase 1 — Secretary-as-module refactor for JS test harness
 */
public record LayoutSecretaryModule() implements DomModule<LayoutSecretaryModule> {

    /** The single export — a JS object with {@code initial} and {@code behavior} members. */
    public record LayoutSecretary() implements Exportable._Constant<LayoutSecretaryModule> {}

    public static final LayoutSecretaryModule INSTANCE = new LayoutSecretaryModule();

    @Override
    public ImportsFor<LayoutSecretaryModule> imports() {
        return ImportsFor.<LayoutSecretaryModule>builder().build();
    }

    @Override
    public ExportsOf<LayoutSecretaryModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new LayoutSecretary()));
    }
}
