package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * {@code AddTab}, the standalone control for putting a new tab somewhere:
 * WHERE, picked off {@link PaneThumbsModule.PaneThumbs}, and WHAT, picked off
 * the kinds a {@link TabSourceModule.TabSource} can make. Then one button,
 * which does exactly what a strip's own plus would do, through the same
 * source.
 *
 * <p>The two questions are separate on purpose — a room and a thing. They are
 * answered by different parts of what a person knows, and one list of "this
 * kind in that pane" would be the product of the two and read as neither.</p>
 *
 * <p>It is a control, not a policy: it asks the source whether a pane will
 * take another and shows the answer; it never decides that a dock is full and
 * never chooses for you. The whole of it is the NATIVE world — a button per
 * thumbnail, a list, a button — so the browser's own focus walks it and
 * nothing here joins the keyboard party, which is what a small control should
 * be and what leaves the party to the containers that need it.</p>
 */
public record AddTabModule() implements DomModule<AddTabModule> {

    /** The class. */
    public record AddTab() implements BranchComponent<AddTabModule> {
        @Override public String summary() { return "Pick a pane off a small map of them, pick what to mount off a list, and the button puts it there."; }
    }

    public static final AddTabModule INSTANCE = new AddTabModule();

    @Override
    public ImportsFor<AddTabModule> imports() {
        return ImportsFor.<AddTabModule>builder()
                .add(new ModuleImports<>(List.of(
                        new PaneStyles.mtp_new(),
                        new PaneStyles.mtp_new_pick(),
                        new PaneStyles.mtp_add(),
                        new PaneStyles.mtp_add_off()
                ), PaneStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new PaneThumbsModule.PaneThumbs()), PaneThumbsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<AddTabModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new AddTab()));
    }
}
