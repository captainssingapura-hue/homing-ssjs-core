package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

/**
 * RFC 0059 Phase 2 — a <code>```mermaid</code> fence, drawn, for the rigid
 * readers.
 *
 * <p>Diagrams used to reach one reader only. {@code DocReaderRenderer} handed a
 * whole markdown document to {@code marked}, then swept the result for
 * {@code code.language-mermaid} and rendered what it found — a DOM scan, because
 * after {@code marked} the DOM was all there was. Everything downstream of the
 * rigid pipeline ({@code DocTreeWidget}, {@code DocContentWidget}, the composed
 * viewer) got a code listing where the author wrote a diagram.</p>
 *
 * <p>What changed is upstream: {@code MarkdownDocNormalizer} now splits fenced
 * blocks into typed {@code code} segments carrying their {@code language}, so
 * "is this a diagram?" is a question about <b>data</b>, answered before any DOM
 * exists. {@link CodeSegmentRenderer} asks it and calls this module; no
 * {@code querySelectorAll}, no scan, and the presence gate is free.</p>
 *
 * <h2>What it deliberately keeps from the old code</h2>
 * <ul>
 *   <li><b>Lazy import.</b> The proxy is dynamic-imported on first diagram, so a
 *       diagram-free doc never touches the network.</li>
 *   <li><b>The fence survives failure.</b> Offline, blocked CDN or a malformed
 *       diagram leaves the source visible with a one-line note saying which.</li>
 *   <li><b>One CDN seam.</b> Only {@code MermaidProxyModule} reaches out, and its
 *       URL is deployment-overridable.</li>
 * </ul>
 *
 * <p>The copy left in {@code DocReaderRenderer} was deliberate and brief: it kept
 * the standalone reader safe while this landed. Phase 3 then pointed that reader
 * at the same tree and deleted its pipeline outright, so this is the only
 * implementation there is.</p>
 *
 * @since homing-studio-base — RFC 0059 Phase 2
 */
public record MermaidPlateModule() implements DomModule<MermaidPlateModule> {

    public static final MermaidPlateModule INSTANCE = new MermaidPlateModule();

    /** The {@code renderMermaidPlate(branch, parent, source)} JS function. */
    public record renderMermaidPlate() implements Exportable._Constant<MermaidPlateModule> {}

    @Override
    public ImportsFor<MermaidPlateModule> imports() {
        return ImportsFor.<MermaidPlateModule>builder()
                .add(new ModuleImports<>(List.of(
                        // The light ground a themed page owes a diagram that
                        // never hears about the theme.
                        new StudioStyles.st_mermaid(),
                        // And the note left behind when there is no diagram.
                        new StudioStyles.st_mermaid_note()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<MermaidPlateModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderMermaidPlate()));
    }
}
