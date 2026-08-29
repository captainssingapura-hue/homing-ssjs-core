package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.ui.StudioElements;

import java.util.List;

/**
 * RFC 0053 — JS renderer for {@link CatalogueTreeView}. Fetches
 * {@code /catalogue-parity} and draws the catalogue as an indented tree, each row
 * linking to the authentic URL that vertex resolves at today.
 *
 * <p>Composed entirely from {@link StudioElements} builders, so it mints no raw
 * DOM of its own and needs no {@code DomOpsParty} branch — the anchors go through
 * {@code ListItem}'s own {@code href.set}, which is what the {@code no-raw-href}
 * rule requires.</p>
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
                .add(new ModuleImports<>(List.of(
                        new StudioElements.Panel(),
                        new StudioElements.Listing(),
                        new StudioElements.ListItem()
                ), StudioElements.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CatalogueTreeViewRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderCatalogueTreeView()));
    }
}
