package hue.captains.singapura.js.homing.conformance;

import hue.captains.singapura.js.homing.demo.studio.DemoBaseStudio;
import hue.captains.singapura.js.homing.demo.studio.multi.MultiStudio;
import hue.captains.singapura.js.homing.skills.SkillsStudio;
import hue.captains.singapura.js.homing.studio.base.Studio;

import java.util.List;

/**
 * Mirrors {@code DemoStudioServer.main()}'s umbrella and asserts that
 * every Plan reachable as a catalogue leaf (via {@code Entry.of(this, plan)})
 * is also present in some {@code Studio.plans()} list. Otherwise the
 * tile renders but {@code /plan?id=…} returns 404 (Defect 0005).
 */
class DemoPlanRegistrationConformanceTest extends PlanRegistrationConformanceTest {

    @Override
    protected List<? extends Studio<?>> studios() {
        return List.of(
                MultiStudio.INSTANCE,
                DemoBaseStudio.INSTANCE,
                SkillsStudio.INSTANCE
        );
    }
}
