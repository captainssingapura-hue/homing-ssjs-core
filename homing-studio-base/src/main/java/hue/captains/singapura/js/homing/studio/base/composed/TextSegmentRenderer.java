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
 * RFC 0024 Phase P1c — text segment renderer (RFC 0018 grammar). Walks
 * the server-parsed Block / Inline AST and renders into the article
 * element. No markdown library — the grammar (paragraphs + lists +
 * quotes + inline emphasis/code/refs) is small enough that the
 * renderer fits inline.
 *
 * @since RFC 0024 Phase P1c
 */
public record TextSegmentRenderer() implements DomModule<TextSegmentRenderer> {

    public static final TextSegmentRenderer INSTANCE = new TextSegmentRenderer();

    public record renderTextSegment() implements Exportable._Constant<TextSegmentRenderer> {}

    @Override
    public ImportsFor<TextSegmentRenderer> imports() {
        return ImportsFor.<TextSegmentRenderer>builder()
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_section(),
                        new StudioStyles.st_section_title(),
                        new StudioStyles.st_doc(),
                        new StudioStyles.st_error()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<TextSegmentRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderTextSegment()));
    }
}
