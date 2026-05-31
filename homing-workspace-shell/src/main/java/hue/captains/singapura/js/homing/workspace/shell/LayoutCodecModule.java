package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceStateCodecsModule;

import java.util.List;

/**
 * {@code LayoutCodec} — pure converter between MultiTabPane's native
 * layout-tree shape and the typed {@code LayoutNode} record tree (RFC
 * 0029). Two static methods, both pure, both round-trip losslessly.
 *
 * <p>First class extracted from {@code AnimalsPlaygroundChrome.bodyJs()}
 * as the workspace-shell chrome decomposition begins. Lives in
 * {@code homing-workspace-shell} so every workspace gets it free.</p>
 *
 * <p>Functional Object discipline: the JS export is a class with static
 * methods, no instance state, no closures over module-scope vars.
 * Testable in isolation — pass fixtures, verify output. The future
 * GraalVM harness can exercise it without a DOM.</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition
 */
public record LayoutCodecModule() implements DomModule<LayoutCodecModule> {

    public static final LayoutCodecModule INSTANCE = new LayoutCodecModule();

    /** The pure converter class — exposes {@code mtToTyped} / {@code typedToMt}. */
    public record LayoutCodec() implements Exportable._Class<LayoutCodecModule> {}

    @Override
    public ImportsFor<LayoutCodecModule> imports() {
        return ImportsFor.<LayoutCodecModule>builder()
                .add(new ModuleImports<>(List.of(
                        new WorkspaceStateCodecsModule.LayoutNode(),
                        new WorkspaceStateCodecsModule.PaneId(),
                        new WorkspaceStateCodecsModule.Orientation()),
                        WorkspaceStateCodecsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<LayoutCodecModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new LayoutCodec()));
    }
}
