package hue.captains.singapura.js.homing.workspace.catalogue;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * Proper ES-module declaration for the RFC 0031 workspace catalogue
 * store. Replaces the old {@link WorkspaceCatalogueBundle#allJs()}
 * bundle pattern (concatenated string spliced into a chrome's
 * {@code mountInto} closure) with a real served module that consumers
 * import by name.
 *
 * <p>Per <em>Generated and Hand-Written Live Apart</em>: this is the
 * hand-written side. The JS lives in
 * {@code src/main/resources/.../WorkspaceCatalogueModule.js} and is
 * committed to the repo.</p>
 *
 * @since RFC 0034 P2-prep Cycle B — bundle retrofit
 */
public record WorkspaceCatalogueModule() implements DomModule<WorkspaceCatalogueModule> {

    public static final WorkspaceCatalogueModule INSTANCE = new WorkspaceCatalogueModule();

    /** The IndexedDB-backed catalogue store class. */
    public record WorkspaceCatalogueStore() implements Exportable._Class<WorkspaceCatalogueModule> {}

    /** Factory function — adjacent-module convention. */
    public record createWorkspaceCatalogueStore() implements Exportable._Constant<WorkspaceCatalogueModule> {}

    @Override
    public ImportsFor<WorkspaceCatalogueModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<WorkspaceCatalogueModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new WorkspaceCatalogueStore(),
                new createWorkspaceCatalogueStore()
        ));
    }
}
