package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

/**
 * RFC 0053 — JS renderer for {@link CatalogueTreeView}. Draws the catalogue as an
 * INTERACTIVE tree with the generic {@code TreeRenderer}: collapsible branches,
 * arrow-key navigation, Enter / double-click to open.
 *
 * <p>Same wiring as the studio workspace's tree widgets — a {@code DomOpsParty}
 * branch, a container, and the canonical {@code TreeNode} JSON. Two differences,
 * both because this is an MPA page rather than a workspace pane: the branch comes
 * off the global {@code domOpsParty} singleton instead of a widget host, and
 * activating a node is a real navigation through the href manager rather than a
 * party message to a sibling pane.</p>
 *
 * <p>The tree costs no per-tree-kind rendering code — {@code TreeRenderer} reads
 * only the substrate's universal dimensions. What this module adds is the join
 * back to the parity verdicts: the renderer addresses rows by child-index path
 * (positional, by RFC 0040's design), so {@code /catalogue-parity} ships the
 * verdicts keyed the same way and selection looks them up.</p>
 *
 * @since RFC 0053
 */
public record CatalogueTreeViewRenderer() implements DomModule<CatalogueTreeViewRenderer> {

    public record renderCatalogueTreeView()
            implements Exportable._Constant<CatalogueTreeViewRenderer> {}

    public static final CatalogueTreeViewRenderer INSTANCE = new CatalogueTreeViewRenderer();

    @Override
    public ImportsFor<CatalogueTreeViewRenderer> imports() {
        return ImportsFor.<CatalogueTreeViewRenderer>builder()
                // The generic renderer — the whole point of the substrate.
                .add(new ModuleImports<>(List.of(new TreeRendererModule.TreeRenderer()),
                        TreeRendererModule.INSTANCE))
                // The root party, so this page owns a branch every element is
                // minted through and no raw DOM factory is needed.
                .add(new ModuleImports<>(List.of(new domOpsParty()),
                        DomOpsPartyModule.INSTANCE))
                // Activation navigates; window.location is rule-forbidden in
                // consumer JS, so it goes through the injected href manager.
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_root(),
                        new StudioStyles.st_main(),
                        new StudioStyles.st_section(),
                        new StudioStyles.st_section_title(),
                        new StudioStyles.st_subtitle(),
                        new StudioStyles.st_loading(),
                        new StudioStyles.st_error()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CatalogueTreeViewRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderCatalogueTreeView()));
    }
}
