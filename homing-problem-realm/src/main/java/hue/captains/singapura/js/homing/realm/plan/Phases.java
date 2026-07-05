package hue.captains.singapura.js.homing.realm.plan;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L1_Domain;

/**
 * Phases — the phased plan of execution: the ordered units of work the plan is
 * carried out in ("display all phases"). Each phase has its own status, tasks,
 * dependencies, and metrics (modelled as their own aspects for now).
 */
public record Phases() implements L1_Domain<PlanTracking> {

    public static final Phases INSTANCE = new Phases();

    @Override public PlanTracking parent() { return PlanTracking.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("The phased plan of execution — the ordered units of work the plan is "
              + "carried out in.");
    }
}
