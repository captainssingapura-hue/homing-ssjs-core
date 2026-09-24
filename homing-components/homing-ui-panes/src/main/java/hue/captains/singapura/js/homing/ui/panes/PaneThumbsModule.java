package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * {@code PaneThumbs}, a picture of where the panes are, to pick one by: a
 * button apiece, laid out in the proportions the panes really have on the
 * screen, inside a frame of the same shape as the room they fill. You choose a
 * pane by pointing at the small one in the same place as the big one.
 *
 * <p>It MEASURES rather than reads a layout. A grid could describe its own
 * tree and this could draw that — and then it would work for a grid and
 * nothing else: not a dock that floats, not two workspaces side by side, not a
 * pane somebody simply put on the page. Rectangles are what every pane has in
 * common, so rectangles are what this knows; the union of them is the room,
 * and each pane's share of it is where its thumbnail goes. The picture is
 * therefore always of what is really there.</p>
 *
 * <p>A pane that cannot be chosen is shown all the same, dimmed: where it is
 * belongs to the picture, and a gap would be a lie about the room.</p>
 */
public record PaneThumbsModule() implements DomModule<PaneThumbsModule> {

    /** The class. */
    public record PaneThumbs() implements BranchComponent<PaneThumbsModule> {
        @Override public String summary() { return "The panes as a small map of themselves: one box each, where it really is, and a press chooses it."; }
    }

    public static final PaneThumbsModule INSTANCE = new PaneThumbsModule();

    @Override
    public ImportsFor<PaneThumbsModule> imports() {
        return ImportsFor.<PaneThumbsModule>builder()
                .add(new ModuleImports<>(List.of(
                        new PaneStyles.mtp_thumbs(),
                        new PaneStyles.mtp_thumb(),
                        new PaneStyles.mtp_thumb_on(),
                        new PaneStyles.mtp_thumb_off(),
                        new PaneStyles.mtp_thumb_label()
                ), PaneStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PaneThumbsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PaneThumbs()));
    }
}
