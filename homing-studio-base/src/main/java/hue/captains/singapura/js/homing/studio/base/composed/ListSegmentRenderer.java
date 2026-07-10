package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

/**
 * Inline renderer for {@link UnorderedListSegment} / {@link OrderedListSegment}.
 * Each item is a {@link Listable} segment dispatched — recursively, via
 * {@code ctx.renderContent} — into its own {@code <li>} on a per-item sub-branch.
 * Items are never lists (compile-time), so the recursion is depth-1.
 */
public record ListSegmentRenderer() implements DomModule<ListSegmentRenderer> {

    public static final ListSegmentRenderer INSTANCE = new ListSegmentRenderer();

    public record renderUnorderedListSegment() implements Exportable._Constant<ListSegmentRenderer> {}
    public record renderOrderedListSegment() implements Exportable._Constant<ListSegmentRenderer> {}

    @Override
    public ImportsFor<ListSegmentRenderer> imports() {
        return ImportsFor.<ListSegmentRenderer>builder()
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_section(),
                        new StudioStyles.st_error()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ListSegmentRenderer> exports() {
        return new ExportsOf<>(INSTANCE,
                List.of(new renderUnorderedListSegment(), new renderOrderedListSegment()));
    }
}
