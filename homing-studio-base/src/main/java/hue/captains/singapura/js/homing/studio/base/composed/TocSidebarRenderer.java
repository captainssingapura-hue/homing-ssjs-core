package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

/**
 * RFC 0024 Phase P1c — TOC sidebar renderer for {@link ComposedWidget}.
 * Extracted from the legacy {@code ComposedViewerRenderer.js} per the
 * Modest File Size doctrine.
 *
 * <p>Exports {@code renderTocSidebar(branch, tocEl, tocEntries)}: walks
 * the entries and creates anchor links owned by the provided
 * {@code branch}. Each anchor's CSS class encodes its heading level
 * (st_toc_h1 / h2 / h3). The orchestrator's scroll-spy IntersectionObserver
 * toggles st_toc_active on the matching anchor as the user scrolls.</p>
 *
 * @since RFC 0024 Phase P1c
 */
public record TocSidebarRenderer() implements DomModule<TocSidebarRenderer> {

    public static final TocSidebarRenderer INSTANCE = new TocSidebarRenderer();

    public record renderTocSidebar() implements Exportable._Constant<TocSidebarRenderer> {}

    @Override
    public ImportsFor<TocSidebarRenderer> imports() {
        return ImportsFor.<TocSidebarRenderer>builder()
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_toc_item(),
                        new StudioStyles.st_toc_h1(),
                        new StudioStyles.st_toc_h2(),
                        new StudioStyles.st_toc_h3(),
                        new StudioStyles.st_loading()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<TocSidebarRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderTocSidebar()));
    }
}
