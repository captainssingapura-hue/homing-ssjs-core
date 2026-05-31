package hue.captains.singapura.js.homing.workspace.events;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * Proper ES-module declaration for the RFC 0034 checkpoint store.
 * Replaces the old {@link CheckpointStore#allJs()} bundle pattern.
 *
 * <p>Per <em>Generated and Hand-Written Live Apart</em>: hand-written
 * side. The JS lives in
 * {@code src/main/resources/.../CheckpointStoreModule.js}.</p>
 *
 * @since RFC 0034 P2-prep Cycle B — bundle retrofit
 */
public record CheckpointStoreModule() implements DomModule<CheckpointStoreModule> {

    public static final CheckpointStoreModule INSTANCE = new CheckpointStoreModule();

    /** The IndexedDB-backed checkpoint store class. */
    public record CheckpointStore() implements Exportable._Class<CheckpointStoreModule> {}

    /** Factory function — adjacent-module convention. */
    public record createCheckpointStore() implements Exportable._Constant<CheckpointStoreModule> {}

    @Override
    public ImportsFor<CheckpointStoreModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<CheckpointStoreModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new CheckpointStore(),
                new createCheckpointStore()
        ));
    }
}
