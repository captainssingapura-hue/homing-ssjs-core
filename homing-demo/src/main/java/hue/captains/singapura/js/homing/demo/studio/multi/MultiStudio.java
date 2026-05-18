package hue.captains.singapura.js.homing.demo.studio.multi;

import hue.captains.singapura.js.homing.core.SvgRef;
import hue.captains.singapura.js.homing.demo.es.CuteAnimal;
import hue.captains.singapura.js.homing.studio.base.Studio;
import hue.captains.singapura.js.homing.studio.base.app.StudioBrand;

/**
 * RFC 0012 — the umbrella studio for the multi-studio demo deploy. Owns
 * {@link MultiStudioHome} (the synthetic L0 launcher) and the two
 * category L1s (Learning / Tooling), all reachable from {@code home()}
 * via the closure walk. The standalone brand is the turtle-logoed
 * "Homing · multi-studio · demo" — the umbrella's brand wins when composed.
 *
 * <p>Contributor studios ({@code DemoBaseStudio}, {@code SkillsStudio})
 * are added as sibling entries in the deploy's {@code Umbrella.Group};
 * each contributes its own catalogues, plans, and apps under the same
 * Bootstrap.</p>
 */
public record MultiStudio() implements Studio<MultiStudioHome> {

    public static final MultiStudio INSTANCE = new MultiStudio();

    @Override
    public MultiStudioHome home() { return MultiStudioHome.INSTANCE; }

    @Override
    public StudioBrand standaloneBrand() {
        return new StudioBrand(
                "Homing · multi-studio · demo",
                MultiStudioHome.class,
                new SvgRef<>(CuteAnimal.INSTANCE, new CuteAnimal.turtle()));
    }
}
