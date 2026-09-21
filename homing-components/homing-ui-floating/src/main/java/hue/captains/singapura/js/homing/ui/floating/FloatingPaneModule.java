package hue.captains.singapura.js.homing.ui.floating;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import java.util.List;

/**
 * {@code FloatingPane}: one pane raised above the page — a head that moves it,
 * a body that holds what it shows, a grip that sizes it — on a sub-branch of
 * its own. {@code Container.Pane.Floating} to the design. The desk holds the
 * stack; a pane alone knows its place and its measure and reports a move or
 * a resize to whoever gave it a sink.
 */
public record FloatingPaneModule() implements DomModule<FloatingPaneModule> {

    /** The class: {@code new FloatingPane(branch, {id, title, x, y, w, h, z, closable?, onEvent?})}. */
    public record FloatingPane() implements BranchComponent<FloatingPaneModule> {
        @Override public String summary() { return "A pane that floats on a desk: a head that names and moves it, a body, a grip that sizes it."; }
    }

    public static final FloatingPaneModule INSTANCE = new FloatingPaneModule();

    @Override
    public ImportsFor<FloatingPaneModule> imports() {
        return ImportsFor.<FloatingPaneModule>builder()
                .add(new ModuleImports<>(List.of(new FloatEventsModule.FloatEvents()), FloatEventsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FloatingStyles.fp_frame(),
                        new FloatingStyles.fp_hoverable(),
                        new FloatingStyles.fp_held(),
                        new FloatingStyles.fp_active(),
                        new FloatingStyles.fp_head(),
                        new FloatingStyles.fp_head_held(),
                        new FloatingStyles.fp_title(),
                        new FloatingStyles.fp_close(),
                        new FloatingStyles.fp_body(),
                        new FloatingStyles.fp_grip()
                ), FloatingStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<FloatingPaneModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new FloatingPane()));
    }
}
