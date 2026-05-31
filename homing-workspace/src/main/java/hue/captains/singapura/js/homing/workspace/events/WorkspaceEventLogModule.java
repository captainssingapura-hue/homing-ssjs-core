package hue.captains.singapura.js.homing.workspace.events;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * Proper ES-module declaration for the RFC 0030 event log substrate.
 * Replaces the old {@link WorkspaceEventLog#allJs()} bundle (which
 * concatenated {@code EventLogStore.js} + {@code WorkspaceEventLog.js}
 * into a chrome's {@code mountInto}).
 *
 * <p>The two pieces ship together because the {@code WorkspaceEventLog}
 * facade composes the {@code EventLogStore} backend. Consumers cherry-
 * pick what they need via named imports.</p>
 *
 * <p>Per <em>Generated and Hand-Written Live Apart</em>: hand-written
 * side. The JS lives in
 * {@code src/main/resources/.../WorkspaceEventLogModule.js} — one file,
 * both class declarations.</p>
 *
 * @since RFC 0034 P2-prep Cycle B — bundle retrofit
 */
public record WorkspaceEventLogModule() implements DomModule<WorkspaceEventLogModule> {

    public static final WorkspaceEventLogModule INSTANCE = new WorkspaceEventLogModule();

    public record EventLogStore()       implements Exportable._Class<WorkspaceEventLogModule> {}
    public record createEventLogStore() implements Exportable._Constant<WorkspaceEventLogModule> {}
    public record WorkspaceEventLog()   implements Exportable._Class<WorkspaceEventLogModule> {}

    @Override
    public ImportsFor<WorkspaceEventLogModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<WorkspaceEventLogModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new EventLogStore(),
                new createEventLogStore(),
                new WorkspaceEventLog()
        ));
    }
}
