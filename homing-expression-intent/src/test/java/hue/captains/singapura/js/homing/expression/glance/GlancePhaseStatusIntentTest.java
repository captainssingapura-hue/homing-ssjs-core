package hue.captains.singapura.js.homing.expression.glance;

import hue.captains.singapura.js.homing.realm.plan.PhaseState;
import hue.captains.singapura.js.homing.realm.plan.Status;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the Glance phase-status family: one intent per canonical {@code PhaseState},
 * all bound to the {@code realm.plan.Status} dimension, with stable, unique,
 * glance-scoped keys that reference (never re-declare) the canonical value.
 */
class GlancePhaseStatusIntentTest {

    @Test
    void oneIntentPerCanonicalState() {
        assertEquals(PhaseState.values().length, GlancePhaseStatusIntent.values().length);
        var covered = Arrays.stream(GlancePhaseStatusIntent.values())
                .map(GlancePhaseStatusIntent::state)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(PhaseState.class)));
        assertEquals(EnumSet.allOf(PhaseState.class), covered, "every PhaseState is referenced exactly once");
    }

    @Test
    void allBindToThePlanStatusDimension() {
        for (GlancePhaseStatusIntent intent : GlancePhaseStatusIntent.values()) {
            assertSame(Status.INSTANCE, intent.domain(), intent + " binds to realm.plan.Status");
        }
    }

    @Test
    void keysAreGlanceScopedStableAndUnique() {
        assertEquals("glance.phase-status.blocked", GlancePhaseStatusIntent.BLOCKED.key());
        assertEquals("glance.phase-status.not-started", GlancePhaseStatusIntent.NOT_STARTED.key());
        long distinct = Arrays.stream(GlancePhaseStatusIntent.values())
                .map(GlancePhaseStatusIntent::key)
                .distinct()
                .count();
        assertEquals(4, distinct);
        for (GlancePhaseStatusIntent intent : GlancePhaseStatusIntent.values()) {
            assertTrue(intent.key().startsWith("glance.phase-status."), intent + " is glance-scoped");
        }
    }
}
