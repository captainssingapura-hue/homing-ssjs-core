package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.party.PartyModule;

import java.util.List;

/**
 * {@code PartyBootstrap} — Phase 2 of the workspace-shell chrome.
 * Walks {@code spec.parties} (each a {@link PartyDecl} on the wire),
 * dynamic-imports each Secretary's JS module, constructs its
 * {@link hue.captains.singapura.js.homing.workspace.party.PartyModule
 * Party} + initial actors, and exposes constructed Parties under their
 * {@code exposedAs} key on the workspace context object widgets receive.
 *
 * <p>The substrate construction is what V1 inlines at lines 189–207 +
 * 233 of {@code AnimalsPlaygroundChrome.bodyJs()} for the single
 * AnimalsParty case; this class generalises to any number of Parties
 * declared in the spec.</p>
 *
 * <p>Functional Object: stateless class with one method —
 * {@code bootstrap(spec) → Promise<{parties, workspaceCtx}>}. No
 * instance state; the result is fresh per call. The dynamic-import side
 * effect is async, so the orchestrator awaits the returned Promise
 * before proceeding to phases that depend on Parties being in place
 * (notably the {@code actionDispatch.TellParty} interpretation).</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition — Phase 2
 */
public record PartyBootstrapModule() implements DomModule<PartyBootstrapModule> {

    public static final PartyBootstrapModule INSTANCE = new PartyBootstrapModule();

    /** The {@code PartyBootstrap} JS class — exposes {@code bootstrap(spec)}. */
    public record PartyBootstrap() implements Exportable._Class<PartyBootstrapModule> {}

    @Override
    public ImportsFor<PartyBootstrapModule> imports() {
        return ImportsFor.<PartyBootstrapModule>builder()
                .add(new ModuleImports<>(
                        List.of(new PartyModule.Party()),
                        PartyModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PartyBootstrapModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PartyBootstrap()));
    }
}
