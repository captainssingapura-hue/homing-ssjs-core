package hue.captains.singapura.js.homing.realm.plan;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L1_Domain;

/**
 * Progress — the completion of the plan, tracked across its phases and their
 * tasks ("track progress of the entire plan"). A magnitude (a fraction toward
 * done), distinct in shape from a discrete status.
 */
public record Progress() implements L1_Domain<PlanTracking> {

    public static final Progress INSTANCE = new Progress();

    @Override public PlanTracking parent() { return PlanTracking.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("The completion of the plan, tracked across its phases and their tasks "
              + "— a magnitude toward done.");
    }
}
