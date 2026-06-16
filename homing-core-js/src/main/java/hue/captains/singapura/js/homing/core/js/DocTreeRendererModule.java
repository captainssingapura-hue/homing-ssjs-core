package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * RFC 0039 — the doc rigid-tree renderer (the JS {@code renderDoc} view).
 * Draws a doc-tree payload (<code>{structure, content}</code>) as a two-pane
 * document: a TOC ({@link TreeRendererModule TreeRenderer} over the structure)
 * beside a body of one {@code <section>} per node, the content dispatched by an
 * injected per-segment renderer.
 *
 * <p>The headline: the TOC's <b>selection event navigates the body</b> to the
 * active node's content — the very same {@code TreeRenderer} that drives
 * catalogue navigation now drives intra-doc navigation, with no bespoke TOC
 * widget (the legacy {@code TocSidebarRenderer} retires).</p>
 *
 * <p>Substrate-level on purpose: the per-segment dispatch is <i>injected</i>
 * (a {@code renderContent} callback), so this module stays in homing-core-js,
 * free of studio segment kinds. The doc widget wires the callback to the RFC
 * 0024 P1c per-segment renderers. Imports only {@code TreeRenderer}.</p>
 *
 * @since homing-core-js — RFC 0039 rigid-tree doc
 */
public record DocTreeRendererModule() implements DomModule<DocTreeRendererModule> {

    public static final DocTreeRendererModule INSTANCE = new DocTreeRendererModule();

    /** The {@code renderDocTree(opts)} JS function. */
    public record renderDocTree() implements Exportable._Constant<DocTreeRendererModule> {}

    @Override
    public ImportsFor<DocTreeRendererModule> imports() {
        return ImportsFor.<DocTreeRendererModule>builder()
                .add(new ModuleImports<>(List.of(new TreeRendererModule.TreeRenderer()),
                        TreeRendererModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DocTreeRendererModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderDocTree()));
    }
}
