package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.libs.MarkedJs;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;
import hue.captains.singapura.js.homing.studio.base.export.HtmlExportModule;
import hue.captains.singapura.js.homing.studio.base.ui.StudioElements;

import java.util.List;

/**
 * Shared JS renderer for {@link DocReader}. Fetches the markdown via
 * {@code /doc?id=<uuid>} and the typed References list via {@code /doc-refs?id=<uuid>}
 * (RFC 0004-ext1), renders with marked.js, builds a TOC sidebar from the document's headings,
 * and emits a "References" section beneath the body with stable {@code id="ref:<name>"}
 * anchors so markdown citations like {@code [label](#ref:<name>)} navigate natively.
 *
 * @deprecated Renderer for the deprecated {@link DocReader} app. Prefer
 *             {@code ComposedWidget}'s renderer for new docs.
 */
@Deprecated
public record DocReaderRenderer() implements DomModule<DocReaderRenderer> {

    public record renderDocReader() implements Exportable._Constant<DocReaderRenderer> {}

    public static final DocReaderRenderer INSTANCE = new DocReaderRenderer();

    @Override
    public ImportsFor<DocReaderRenderer> imports() {
        return ImportsFor.<DocReaderRenderer>builder()
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(new MarkedJs.marked()), MarkedJs.INSTANCE))
                .add(new ModuleImports<>(List.of(new HtmlExportModule.exportPageAsHtml()),
                        HtmlExportModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioElements.Header()
                ), StudioElements.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_root(),
                        new StudioStyles.st_main(),
                        new StudioStyles.st_layout(),
                        new StudioStyles.st_sidebar(),
                        new StudioStyles.st_sidebar_title(),
                        new StudioStyles.st_toc(),
                        new StudioStyles.st_toc_item(),
                        new StudioStyles.st_toc_h1(),
                        new StudioStyles.st_toc_h2(),
                        new StudioStyles.st_toc_h3(),
                        new StudioStyles.st_toc_active(),
                        new StudioStyles.st_doc(),
                        new StudioStyles.st_doc_meta(),
                        new StudioStyles.st_section(),
                        new StudioStyles.st_section_title(),
                        new StudioStyles.st_card(),
                        new StudioStyles.st_card_title(),
                        new StudioStyles.st_card_summary(),
                        new StudioStyles.st_card_link(),
                        new StudioStyles.st_loading(),
                        new StudioStyles.st_error()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DocReaderRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderDocReader()));
    }
}
