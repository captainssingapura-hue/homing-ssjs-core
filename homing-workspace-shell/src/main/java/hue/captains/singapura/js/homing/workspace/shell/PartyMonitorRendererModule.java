package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.viewParty;

import java.util.List;

/**
 * RFC 0063 — draws the branch tree from a {@code viewParty()} snapshot.
 *
 * <p><b>What it imports is the point.</b> {@link viewParty}, and not
 * {@code domOpsParty}: the renderer receives the tree as frozen data and holds
 * no handle that could dissolve anything — observation by construction (D4).
 * Its own elements come through the branch every widget is handed, so the
 * view doctrines grade it like any other view; the tree it draws arrives as
 * facts.</p>
 *
 * <p>Each refresh draws into a fresh sub-branch and dissolves the previous
 * one, the move {@code MermaidPlate} makes for its fence. A collected owner is
 * drawn in the original demo's red and counted; the note under the header
 * says <i>none collected</i>, never <i>no leaks</i> (D8). The monitor's own
 * branch is outlined (D9).</p>
 *
 * <p>Refresh is manual (D7) — a button, and once on mount. No timer.</p>
 */
public record PartyMonitorRendererModule() implements DomModule<PartyMonitorRendererModule> {

    /** {@code renderPartyMonitor(branch, host, opts) → { refresh }}. */
    public record renderPartyMonitor() implements Exportable._Constant<PartyMonitorRendererModule> {}

    public static final PartyMonitorRendererModule INSTANCE = new PartyMonitorRendererModule();

    @Override
    public ImportsFor<PartyMonitorRendererModule> imports() {
        return ImportsFor.<PartyMonitorRendererModule>builder()
                // The projection, and ONLY the projection.
                .add(new ModuleImports<>(List.of(new viewParty()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PartyMonitorStyles.pm_root(),
                        new PartyMonitorStyles.pm_head(),
                        new PartyMonitorStyles.pm_title(),
                        new PartyMonitorStyles.pm_count(),
                        new PartyMonitorStyles.pm_btn(),
                        new PartyMonitorStyles.pm_note(),
                        new PartyMonitorStyles.pm_note_leaked(),
                        new PartyMonitorStyles.pm_tree(),
                        new PartyMonitorStyles.pm_node(),
                        new PartyMonitorStyles.pm_node_header(),
                        new PartyMonitorStyles.pm_node_header_leaked(),
                        new PartyMonitorStyles.pm_node_header_self(),
                        new PartyMonitorStyles.pm_icon(),
                        new PartyMonitorStyles.pm_name(),
                        new PartyMonitorStyles.pm_depth_badge(),
                        new PartyMonitorStyles.pm_owner(),
                        new PartyMonitorStyles.pm_owner_dot(),
                        new PartyMonitorStyles.pm_owner_dot_alive(),
                        new PartyMonitorStyles.pm_owner_dot_leaked(),
                        new PartyMonitorStyles.pm_badge(),
                        new PartyMonitorStyles.pm_elements(),
                        new PartyMonitorStyles.pm_chip(),
                        new PartyMonitorStyles.pm_branches(),
                        new PartyMonitorStyles.pm_folded()
                ), PartyMonitorStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PartyMonitorRendererModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderPartyMonitor()));
    }
}
