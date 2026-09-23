package hue.captains.singapura.js.homing.ui.elements;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The panel and its builder: a named region with a head, a body and whatever
 * is mounted in it — the workspace's unit of work. It measures nothing: what
 * holds it decides how big it is, and it fills that. {@code Container.Panel}
 * to the design, so the air of its head, the air of its body and the line
 * between them are the design's; its corner and edge are a container's, which
 * is where the keys mark reaches it.
 *
 * <p>It takes no keys: a panel is furniture. It can be told it is the active
 * region — the one being worked in — by call, or it can follow what is
 * mounted in it: {@code watch(component)} reads the one attribute a component
 * writes to say where the keys are, so a dock mounted in a panel lights the
 * panel, and the same dock mounted on a flat surface neither knows nor needs
 * one.</p>
 */
public record PanelModule() implements DomModule<PanelModule> {

    /** A branch component, made through its builder: a head with a name and a slot for what acts on it, and a body the caller mounts into. */
    public record Panel() implements BranchComponent<PanelModule> {
        @Override public String summary() { return "A named region that fills what holds it: a head, a slot for what acts on it, and a body whatever is mounted in it fills."; }
    }

    /** The builder: {@code new PanelBuilder()}; title, fills, size and host set progressively; {@code build(branch)} on a sub-branch of the caller's. */
    public record PanelBuilder() implements Exportable._Constant<PanelModule> {}

    public static final PanelModule INSTANCE = new PanelModule();

    @Override
    public ImportsFor<PanelModule> imports() {
        return ImportsFor.<PanelModule>builder()
                .add(new ModuleImports<>(List.of(
                        new ElementStyles.el_panel(),
                        new ElementStyles.el_panel_head(),
                        new ElementStyles.el_panel_title(),
                        new ElementStyles.el_panel_slot(),
                        new ElementStyles.el_panel_body(),
                        new ElementStyles.el_panel_body_air(),
                        new ElementStyles.el_panel_current(),
                        new ElementStyles.el_panel_framed()
                ), ElementStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PanelModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Panel(), new PanelBuilder()));
    }
}
