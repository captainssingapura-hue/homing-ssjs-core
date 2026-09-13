package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0063 — a party snapshot as a {@code TreeNode} tree, and the two facts
 * the monitor's header needs about it. Pure: no DOM, no party, no state.
 *
 * <p>{@code TreeRenderer}'s headline promise is that any conforming tree
 * renders with no bespoke code. This is what makes the party's tree conform —
 * a branch as a row, its elements as leaf rows beneath it, its sub-branches
 * after them; depth and the collected mark in the badge, the owner label in
 * the note. A bespoke renderer was built first, against that promise, and
 * retired in review.</p>
 *
 * <p>{@code pathToBranch} computes the child-index path {@code selectPath}
 * wants, over exactly the ordering {@code partySnapshotToTree} produces. That
 * shared ordering — elements before sub-branches — is the one fact the two
 * functions must agree on, and the adapter test pins it.</p>
 */
public record PartyMonitorAdapterModule() implements DomModule<PartyMonitorAdapterModule> {

    public record partySnapshotToTree() implements Exportable._Constant<PartyMonitorAdapterModule> {}
    public record partySnapshotStats()  implements Exportable._Constant<PartyMonitorAdapterModule> {}
    public record pathToBranch()        implements Exportable._Constant<PartyMonitorAdapterModule> {}

    public static final PartyMonitorAdapterModule INSTANCE = new PartyMonitorAdapterModule();

    @Override
    public ImportsFor<PartyMonitorAdapterModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<PartyMonitorAdapterModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new partySnapshotToTree(), new partySnapshotStats(), new pathToBranch()));
    }
}
