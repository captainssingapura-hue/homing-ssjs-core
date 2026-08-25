package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.libs.LibsCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueAppHost;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueHostRenderer;
import hue.captains.singapura.js.homing.studio.base.app.DocReader;
import hue.captains.singapura.js.homing.studio.base.app.DocReaderRenderer;
import hue.captains.singapura.js.homing.studio.base.app.DocTreeViewer;
import hue.captains.singapura.js.homing.studio.base.app.SvgViewer;
import hue.captains.singapura.js.homing.studio.base.app.tree.TreeAppHost;
import hue.captains.singapura.js.homing.studio.base.composed.CaptionRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.CodeSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.ComposedSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.ComposedViewer;
import hue.captains.singapura.js.homing.studio.base.composed.ComposedWidget;
import hue.captains.singapura.js.homing.studio.base.composed.DocTreeWidget;
import hue.captains.singapura.js.homing.studio.base.composed.DocumentaryWidgetSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.ImageSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.ListSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.MarkdownSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.ParagraphSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.RelationSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.SvgSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.TableSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.TextSegmentRenderer;
import hue.captains.singapura.js.homing.studio.base.composed.TocSidebarRenderer;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;
import hue.captains.singapura.js.homing.studio.base.css.Util;
import hue.captains.singapura.js.homing.studio.base.export.HtmlExportModule;
import hue.captains.singapura.js.homing.studio.base.graph.StudioGraphInspector;
import hue.captains.singapura.js.homing.studio.base.graph.StudioGraphInspectorRenderer;
import hue.captains.singapura.js.homing.studio.base.image.ImageViewer;
import hue.captains.singapura.js.homing.studio.base.image.ImageViewerRenderer;
import hue.captains.singapura.js.homing.studio.base.table.TableViewer;
import hue.captains.singapura.js.homing.studio.base.table.TableViewerRenderer;
import hue.captains.singapura.js.homing.studio.base.theme.HomingJazzDrumsBg;
import hue.captains.singapura.js.homing.studio.base.theme.HomingMapleBridgeBg;
import hue.captains.singapura.js.homing.studio.base.theme.HomingRetro90sBg;
import hue.captains.singapura.js.homing.studio.base.theme.StudioVarsJsModule;
import hue.captains.singapura.js.homing.studio.base.theme.ThemesIntro;
import hue.captains.singapura.js.homing.studio.base.theme.ThemesIntroRenderer;
import hue.captains.singapura.js.homing.studio.base.tracker.PlanAppHost;
import hue.captains.singapura.js.homing.studio.base.tracker.PlanHostRenderer;
import hue.captains.singapura.js.homing.studio.base.ui.StudioElements;
import hue.captains.singapura.js.homing.studio.base.ui.layout.ModalModule;
import hue.captains.singapura.js.homing.studio.base.ui.layout.MultiTabPaneDragModule;
import hue.captains.singapura.js.homing.studio.base.ui.layout.FocusManagerModule;
import hue.captains.singapura.js.homing.studio.base.ui.layout.MultiTabPaneModule;
import hue.captains.singapura.js.homing.studio.base.ui.layout.SplitPaneModule;
import hue.captains.singapura.js.homing.studio.base.widget.LegacyRedirectWidget;
import hue.captains.singapura.js.homing.studio.base.widget.SingleWidgetWorkspace;
import hue.captains.singapura.js.homing.studio.base.widget.SvgWidget;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-studio-base}: its app hosts,
 * composed-doc segment renderers, viewers, CSS/SVG groups, theme backgrounds,
 * layout primitives, and widget shells. Requires the crates whose modules it
 * imports — the core-js substrate, the server managers, and the bundled libs.
 * Entries were seeded by {@code CrateSeed}; completeness is guarded by the
 * OrphanCheck.
 */
public final class StudioBaseCrate implements Crate {

    public static final StudioBaseCrate INSTANCE = new StudioBaseCrate();

    private StudioBaseCrate() {}

    @Override
    public String name() {
        return "homing-studio-base";
    }

    @Override
    public List<Crate> requires() {
        return List.of(CoreJsCrate.INSTANCE, ServerCrate.INSTANCE, LibsCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(CatalogueAppHost.INSTANCE),
                CrateEntry.of(CatalogueHostRenderer.INSTANCE),
                CrateEntry.of(DocReader.INSTANCE),
                CrateEntry.of(DocReaderRenderer.INSTANCE),
                CrateEntry.of(DocTreeViewer.INSTANCE),
                CrateEntry.of(SvgViewer.INSTANCE),
                CrateEntry.of(TreeAppHost.INSTANCE),
                CrateEntry.of(CaptionRenderer.INSTANCE),
                CrateEntry.of(CodeSegmentRenderer.INSTANCE),
                CrateEntry.of(ComposedSegmentRenderer.INSTANCE),
                CrateEntry.of(ComposedViewer.INSTANCE),
                CrateEntry.of(ComposedWidget.INSTANCE),
                CrateEntry.of(DocTreeWidget.INSTANCE),
                CrateEntry.of(DocumentaryWidgetSegmentRenderer.INSTANCE),
                CrateEntry.of(ImageSegmentRenderer.INSTANCE),
                CrateEntry.of(ListSegmentRenderer.INSTANCE),
                CrateEntry.of(MarkdownSegmentRenderer.INSTANCE),
                CrateEntry.of(ParagraphSegmentRenderer.INSTANCE),
                CrateEntry.of(RelationSegmentRenderer.INSTANCE),
                CrateEntry.of(SvgSegmentRenderer.INSTANCE),
                CrateEntry.of(TableSegmentRenderer.INSTANCE),
                CrateEntry.of(TextSegmentRenderer.INSTANCE),
                CrateEntry.of(TocSidebarRenderer.INSTANCE),
                CrateEntry.of(StudioStyles.INSTANCE),
                CrateEntry.of(Util.INSTANCE),
                CrateEntry.of(HtmlExportModule.INSTANCE),
                CrateEntry.of(StudioGraphInspector.INSTANCE),
                CrateEntry.of(StudioGraphInspectorRenderer.INSTANCE),
                CrateEntry.of(ImageViewer.INSTANCE),
                CrateEntry.of(ImageViewerRenderer.INSTANCE),
                CrateEntry.of(TableViewer.INSTANCE),
                CrateEntry.of(TableViewerRenderer.INSTANCE),
                CrateEntry.of(HomingJazzDrumsBg.INSTANCE),
                CrateEntry.of(HomingMapleBridgeBg.INSTANCE),
                CrateEntry.of(HomingRetro90sBg.INSTANCE),
                CrateEntry.of(StudioVarsJsModule.INSTANCE),
                CrateEntry.of(ThemesIntro.INSTANCE),
                CrateEntry.of(ThemesIntroRenderer.INSTANCE),
                CrateEntry.of(PlanAppHost.INSTANCE),
                CrateEntry.of(PlanHostRenderer.INSTANCE),
                CrateEntry.of(StudioElements.INSTANCE),
                CrateEntry.of(ModalModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(FocusManagerModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(MultiTabPaneDragModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(MultiTabPaneModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(SplitPaneModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(LegacyRedirectWidget.INSTANCE),
                CrateEntry.of(SingleWidgetWorkspace.INSTANCE),
                CrateEntry.of(SvgWidget.INSTANCE));
    }
}
