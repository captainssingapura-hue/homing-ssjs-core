package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

/**
 * RFC 0024 Phase P1c — recursive {@link ComposedSegment} renderer. The
 * pivotal NEW renderer that makes ComposedDoc recursively embeddable.
 *
 * <p>Each ComposedSegment mounts a fresh ComposedWidget into a sub-branch
 * of the segment's branch. The mount uses {@code ctx.mountComposed} —
 * the orchestrator's own mount function, threaded through the context —
 * so the recursion bottoms out at exactly the same code path as the
 * top-level mount. Cycle detection: {@code ctx.renderingStack} is the
 * list of doc ids currently being rendered; if the nested doc's id
 * appears in the stack, a placeholder is shown instead of recursing.</p>
 *
 * <p>Why this works without help from the runtime:</p>
 * <ul>
 *   <li>The orchestrator function {@code mountInto(branch, parent, params)}
 *       is self-callable; recursion is just calling it with a sub-branch
 *       and a {@code __renderingStack} param.</li>
 *   <li>DomOpsParty handles the ownership tree — each nested mount's
 *       branch is a child of the parent segment's branch, so a top-level
 *       dissolve cascades through every recursive level.</li>
 *   <li>The {@code __renderingStack} param is reconstructed at every
 *       mount entry by appending the current doc id. Cycles are detected
 *       by a single {@code indexOf} check.</li>
 * </ul>
 *
 * @since RFC 0024 Phase P1c
 */
public record ComposedSegmentRenderer() implements DomModule<ComposedSegmentRenderer> {

    public static final ComposedSegmentRenderer INSTANCE = new ComposedSegmentRenderer();

    public record renderComposedSegment() implements Exportable._Constant<ComposedSegmentRenderer> {}

    @Override
    public ImportsFor<ComposedSegmentRenderer> imports() {
        return ImportsFor.<ComposedSegmentRenderer>builder()
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_section(),
                        new StudioStyles.st_section_title(),
                        new StudioStyles.st_error()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ComposedSegmentRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderComposedSegment()));
    }
}
