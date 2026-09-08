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
 * RFC 0004-ext1 — a doc's typed References, and the category the catalogue filed
 * it under. RFC 0059 Phase 3 lifted both out of {@code DocReaderRenderer}.
 *
 * <p>They are <b>Doc metadata, not document structure</b>: the tree describes
 * what the document says, while these describe where it sits and what it points
 * at. That is why they arrive from {@code /doc-refs?id=} beside the tree rather
 * than as nodes inside it.</p>
 *
 * <p>This was the one thing the standalone reader had that the rigid readers did
 * not, and it was not droppable: 159 of the self-studio's 171 docs declare at
 * least one reference. Landing it here rather than in the reader means it lands
 * once, for every reader that renders a doc tree.</p>
 *
 * @since homing-studio-base — RFC 0059 Phase 3
 */
public record DocRefsModule() implements DomModule<DocRefsModule> {

    public static final DocRefsModule INSTANCE = new DocRefsModule();

    /** The {@code attachDocRefs({branch, docId, metaHost, refsParent})} JS function. */
    public record attachDocRefs() implements Exportable._Constant<DocRefsModule> {}

    @Override
    public ImportsFor<DocRefsModule> imports() {
        return ImportsFor.<DocRefsModule>builder()
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_section(),
                        new StudioStyles.st_section_title(),
                        new StudioStyles.st_card(),
                        new StudioStyles.st_card_title(),
                        new StudioStyles.st_card_summary(),
                        new StudioStyles.st_card_link(),
                        new StudioStyles.st_doc_category()
                ), StudioStyles.INSTANCE))
                // A reference is a link, and a link is the href manager's to write.
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DocRefsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new attachDocRefs()));
    }
}
