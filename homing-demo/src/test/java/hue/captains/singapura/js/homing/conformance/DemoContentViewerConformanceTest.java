package hue.captains.singapura.js.homing.conformance;

import hue.captains.singapura.js.homing.demo.studio.DemoBaseStudio;
import hue.captains.singapura.js.homing.demo.studio.multi.MultiStudio;
import hue.captains.singapura.js.homing.skills.SkillsStudio;
import hue.captains.singapura.js.homing.studio.base.DefaultFixtures;
import hue.captains.singapura.js.homing.studio.base.Fixtures;
import hue.captains.singapura.js.homing.studio.base.Studio;
import hue.captains.singapura.js.homing.studio.base.Umbrella;

import java.util.List;

/**
 * Mirrors {@code DemoStudioServer.main()}'s umbrella and asserts the
 * ContentViewer registry is consistent with the composed app set —
 * every {@code ContentViewer.app()} must be present in either
 * {@code harnessApps()} or one of the studios' {@code apps()}, otherwise
 * the doc router would fail at request time with "No app registered with
 * this simple name".
 */
class DemoContentViewerConformanceTest extends ContentViewerConformanceTest {

    private static final List<Studio<?>> STUDIOS = List.of(
            MultiStudio.INSTANCE,
            DemoBaseStudio.INSTANCE,
            SkillsStudio.INSTANCE
    );

    private static final Umbrella<Studio<?>> UMBRELLA = new Umbrella.Group<>(
            "Homing Multi-Studio Demo",
            "Composed multi-studio umbrella under test.",
            STUDIOS.stream().<Umbrella<Studio<?>>>map(Umbrella.Solo::new).toList());

    @Override
    protected Fixtures<?> fixtures() {
        return new DefaultFixtures<>(UMBRELLA);
    }

    @Override
    protected List<? extends Studio<?>> studios() {
        return STUDIOS;
    }
}
