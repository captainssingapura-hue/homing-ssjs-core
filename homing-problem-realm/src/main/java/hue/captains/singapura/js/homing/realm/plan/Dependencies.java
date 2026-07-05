package hue.captains.singapura.js.homing.realm.plan;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L1_Domain;

/**
 * Dependencies — the ordering constraints between phases: which phase must
 * precede which. A relationship/edge concern (not a status) — the typed-edge
 * layer, applied within a plan.
 */
public record Dependencies() implements L1_Domain<PlanTracking> {

    public static final Dependencies INSTANCE = new Dependencies();

    @Override public PlanTracking parent() { return PlanTracking.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("The ordering constraints between phases — which phase must precede which.");
    }
}
