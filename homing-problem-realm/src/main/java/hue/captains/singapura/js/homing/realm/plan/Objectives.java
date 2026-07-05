package hue.captains.singapura.js.homing.realm.plan;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L1_Domain;

/**
 * Objectives — the plan's high-level goals: what it is trying to achieve, the
 * "why", read before the questions, phases, and gates. Descriptive; carries no
 * status.
 */
public record Objectives() implements L1_Domain<PlanTracking> {

    public static final Objectives INSTANCE = new Objectives();

    @Override public PlanTracking parent() { return PlanTracking.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("The plan's high-level goals — what it is trying to achieve, the \"why\".");
    }
}
