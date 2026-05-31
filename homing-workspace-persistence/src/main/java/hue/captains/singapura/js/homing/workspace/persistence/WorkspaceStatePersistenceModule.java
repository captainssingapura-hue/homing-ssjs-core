package hue.captains.singapura.js.homing.workspace.persistence;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceStateCodecsModule;
import hue.captains.singapura.js.homing.workspace.persistence.WidgetParamsCodecRegistryModule;

import java.util.List;

/**
 * Proper ES-module declaration for the hand-written RFC 0029 workspace
 * state persistence pieces — the final retirement of the
 * {@code WorkspaceStatePersistence.allJs()} bundle.
 *
 * <h2>Why a separate Maven module</h2>
 *
 * <p>The persistence facade calls
 * {@code WorkspaceStateCodec.transformTo(...)} and {@code transformFrom(...)},
 * which means the JS module imports those codec names. The codec module
 * lives in {@code homing-workspace-codecs}; that module already depends
 * on {@code homing-workspace} (for the typed records). Putting this
 * persistence Java declaration in {@code homing-workspace} would close
 * the cycle (workspace → codecs → workspace).</p>
 *
 * <p>The clean break: a third module that depends on both
 * {@code homing-workspace} (Java records) and
 * {@code homing-workspace-codecs} (codec module reference), and is itself
 * depended on by chrome modules.</p>
 *
 * <h2>Exported names</h2>
 *
 * <p>The single classpath JS resource ({@code WorkspaceStatePersistenceModule.js})
 * concatenates the five hand-written source files in dependency order:
 * registry → store → capture → persister → facade. Each contributes one or
 * more top-level declarations; all are re-exported by name.</p>
 *
 * @since RFC 0034 P2-prep Cycle C — bundle retrofit completion
 */
public record WorkspaceStatePersistenceModule()
        implements DomModule<WorkspaceStatePersistenceModule> {

    public static final WorkspaceStatePersistenceModule INSTANCE =
            new WorkspaceStatePersistenceModule();

    public record createLocalStorageStore()
            implements Exportable._Constant<WorkspaceStatePersistenceModule> {}
    public record captureLiveWorkspace()
            implements Exportable._Constant<WorkspaceStatePersistenceModule> {}
    public record createWorkspaceStatePersister()
            implements Exportable._Constant<WorkspaceStatePersistenceModule> {}
    public record browserScheduler()
            implements Exportable._Constant<WorkspaceStatePersistenceModule> {}
    public record WorkspaceStatePersistence()
            implements Exportable._Constant<WorkspaceStatePersistenceModule> {}

    @Override
    public ImportsFor<WorkspaceStatePersistenceModule> imports() {
        // The facade calls WorkspaceStateCodec.transformTo / transformFrom;
        // declare that as a proper ES module import so the framework's
        // EsModuleWriter prepends the right `import { WorkspaceStateCodec }
        // from "<codec-module-url>";` line at the top of the served file.
        return ImportsFor.<WorkspaceStatePersistenceModule>builder()
                .add(new ModuleImports<>(List.of(
                        new WorkspaceStateCodecsModule.WorkspaceStateCodec()),
                        WorkspaceStateCodecsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new WidgetParamsCodecRegistryModule.WidgetParamsCodecRegistry()),
                        WidgetParamsCodecRegistryModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceStatePersistenceModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new createLocalStorageStore(),
                new captureLiveWorkspace(),
                new createWorkspaceStatePersister(),
                new browserScheduler(),
                new WorkspaceStatePersistence()
        ));
    }
}
