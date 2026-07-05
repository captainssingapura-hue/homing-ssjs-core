package hue.captains.singapura.js.homing.realm.plan;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L1_Domain;

/**
 * Open Questions — the design decisions the plan must settle: those still in
 * flight and those resolved (the tracker's decisions). Open-question management.
 */
public record OpenQuestions() implements L1_Domain<PlanTracking> {

    public static final OpenQuestions INSTANCE = new OpenQuestions();

    @Override public PlanTracking parent() { return PlanTracking.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("The design decisions the plan must settle — open questions in flight "
              + "and resolved.");
    }
}
