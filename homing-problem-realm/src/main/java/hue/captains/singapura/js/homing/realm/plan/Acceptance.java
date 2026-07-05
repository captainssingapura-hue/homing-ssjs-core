package hue.captains.singapura.js.homing.realm.plan;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L1_Domain;

/**
 * Acceptance — the plan-level success criteria: the ship gates that define what
 * "done" means, and whether each is met.
 */
public record Acceptance() implements L1_Domain<PlanTracking> {

    public static final Acceptance INSTANCE = new Acceptance();

    @Override public PlanTracking parent() { return PlanTracking.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("The plan-level success criteria — the ship gates that define done.");
    }
}
