package hue.captains.singapura.js.homing.studio.base.ui;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;

import java.util.List;

/**
 * A tree on the left, whatever the selected row is about on the right — built
 * once and used by both surfaces that want it.
 *
 * <p>The catalogue listing and the theme picker had arrived at the same shape
 * independently: create a nav element, create a detail element, construct a
 * {@code TreeRenderer} over the nav, and repaint the detail from
 * {@code onSelect}. Same fifteen lines, two copies, and they had already drifted
 * — one put the detail first and divided by the golden ratio, the other put the
 * tree first and sized it to content.</p>
 *
 * <p>What differs between them is only what the detail draws, which is the
 * caller's business and stays there: {@code onSelect} receives the selection and
 * the body element, and does as it likes with both.</p>
 *
 * <p>The tree is on the LEFT in both, now. Table of contents beside content is
 * the pattern a reader already knows, and the reason the catalogue did not do it
 * before — not knowing how wide a tree wanted to be — is answered by
 * {@link MasterDetailStyles#md_nav()} sizing to its content.</p>
 */
public record MasterDetail() implements DomModule<MasterDetail> {

    /** Builds the pair into a host and returns { navEl, bodyEl, renderer }. */
    public record mountMasterDetail() implements Exportable._Constant<MasterDetail> {}

    public static final MasterDetail INSTANCE = new MasterDetail();

    @Override
    public ImportsFor<MasterDetail> imports() {
        return ImportsFor.<MasterDetail>builder()
                .add(new ModuleImports<>(List.of(new TreeRendererModule.TreeRenderer()),
                        TreeRendererModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new MasterDetailStyles.md_split(),
                        new MasterDetailStyles.md_nav(),
                        new MasterDetailStyles.md_body()
                ), MasterDetailStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<MasterDetail> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new mountMasterDetail()));
    }
}
