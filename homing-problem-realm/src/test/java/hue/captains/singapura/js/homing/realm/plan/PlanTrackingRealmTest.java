package hue.captains.singapura.js.homing.realm.plan;

import com.hominglabs.rebar.core.desc.WithDesc;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pins the Plan Tracking functional skeleton: eight functional aspects parent to
 * the {@code PlanTracking} root, each self-describing; plus the parked
 * {@code Status} + {@code PhaseState}.
 */
class PlanTrackingRealmTest {

    @Test
    void functionalAspectsParentToTheRoot() {
        assertSame(PlanTracking.INSTANCE, Objectives.INSTANCE.parent());
        assertSame(PlanTracking.INSTANCE, Phases.INSTANCE.parent());
        assertSame(PlanTracking.INSTANCE, OpenQuestions.INSTANCE.parent());
        assertSame(PlanTracking.INSTANCE, Acceptance.INSTANCE.parent());
        assertSame(PlanTracking.INSTANCE, Progress.INSTANCE.parent());
        assertSame(PlanTracking.INSTANCE, Tasks.INSTANCE.parent());
        assertSame(PlanTracking.INSTANCE, Dependencies.INSTANCE.parent());
        assertSame(PlanTracking.INSTANCE, Metrics.INSTANCE.parent());
    }

    @Test
    void everyNodeDescribesItself() {
        List<WithDesc> nodes = List.of(
                PlanTracking.INSTANCE, Objectives.INSTANCE, Phases.INSTANCE,
                OpenQuestions.INSTANCE, Acceptance.INSTANCE, Progress.INSTANCE,
                Tasks.INSTANCE, Dependencies.INSTANCE, Metrics.INSTANCE, Status.INSTANCE);
        for (WithDesc node : nodes) {
            assertFalse(node.desc().summary().text().isBlank());
        }
    }

    @Test
    void statusIsParkedButPresent() {
        assertSame(PlanTracking.INSTANCE, Status.INSTANCE.parent());
        assertEquals(4, PhaseState.values().length);
    }
}
