package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.NodeContentModule;
import hue.captains.singapura.js.homing.core.js.TocSyncSecretaryModule;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;

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
 * <p>The per-segment dispatch is <i>injected</i> (a {@code renderContent}
 * callback), so this module names no segment kind of its own; the doc widget
 * wires the callback to the RFC 0024 P1c per-segment renderers.</p>
 *
 * <p><b>It used to live in homing-core-js,</b> on the reasoning that an injected
 * dispatch made it substrate. The dependency argument held — it imports only
 * {@code TreeRenderer}, {@code NodeContent} and {@code TocSyncSecretary}, all of
 * which are still substrate and still there. What did not hold is the bill: from
 * core-js it could not reach {@code StudioStyles}, so it styled itself with
 * {@code style.cssText} and baked colours, and carried thirteen baselined
 * conformance findings for it — eleven inline-style writes and two literal
 * colours. It bought dependency purity and paid in conformance. A document
 * reader is a studio concern, and this is where studio concerns can be written
 * properly.</p>
 *
 * <p>Moving it changed nothing else: its consumers ({@code DocTreeWidget} here,
 * {@code DocContentWidget} in homing-studio-workspace) were always above core-js,
 * and nothing inside core-js ever depended on it. Converting those inline styles
 * to typed classes is deliberately NOT part of the move — that is a behavioural
 * diff and belongs in its own commit.</p>
 *
 * @since homing-core-js — RFC 0039 rigid-tree doc (relocated to studio-base)
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
                .add(new ModuleImports<>(List.of(new NodeContentModule.NodeContent()),
                        NodeContentModule.INSTANCE))
                // The TOC↔body coordinator, which this module used to hold inline.
                .add(new ModuleImports<>(List.of(new TocSyncSecretaryModule.TocSyncSecretary()),
                        TocSyncSecretaryModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DocTreeRendererModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderDocTree()));
    }
}
