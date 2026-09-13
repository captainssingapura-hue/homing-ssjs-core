package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;
import hue.captains.singapura.js.homing.core.js.viewParty;

import java.util.List;

/**
 * RFC 0063 — the monitor's chrome, and a {@code TreeRenderer} per snapshot.
 *
 * <p><b>What it imports is the point.</b> {@link viewParty} and not
 * {@code domOpsParty}: the tree arrives as frozen data and the widget holds no
 * handle that could dissolve anything (D4). The rows are drawn by the
 * substrate's {@code TreeRenderer} over {@link PartyMonitorAdapterModule}'s
 * output — no bespoke rendering here, per the renderer's own promise.</p>
 *
 * <p><b>A snapshot is immutable</b>, so each refresh builds a new renderer in
 * a new sub-branch and dissolves the previous one. {@code setData} is never
 * called twice on one renderer: {@code TreeRenderer} is flat and cannot
 * release a previous render, and the sub-branch is the unit that can. The
 * monitor's own row is selected through {@code selectPath} — the honest proof
 * the view is live (D9). Refresh is manual (D7); the note under the header
 * says <i>none collected</i>, never <i>no leaks</i> (D8).</p>
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
                .add(new ModuleImports<>(List.of(new TreeRendererModule.TreeRenderer()),
                        TreeRendererModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PartyMonitorAdapterModule.partySnapshotToTree(),
                        new PartyMonitorAdapterModule.partySnapshotStats(),
                        new PartyMonitorAdapterModule.pathToBranch()
                ), PartyMonitorAdapterModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PartyMonitorStyles.pm_root(),
                        new PartyMonitorStyles.pm_head(),
                        new PartyMonitorStyles.pm_title(),
                        new PartyMonitorStyles.pm_count(),
                        new PartyMonitorStyles.pm_btn(),
                        new PartyMonitorStyles.pm_note(),
                        new PartyMonitorStyles.pm_note_leaked(),
                        new PartyMonitorStyles.pm_tree()
                ), PartyMonitorStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PartyMonitorRendererModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderPartyMonitor()));
    }
}
