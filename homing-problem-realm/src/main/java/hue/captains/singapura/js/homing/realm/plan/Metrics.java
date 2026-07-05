package hue.captains.singapura.js.homing.realm.plan;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L1_Domain;

/**
 * Metrics — quantified before/after measurements of a phase's outcome (the
 * tracker's optional per-phase metrics). Measures, not a status.
 */
public record Metrics() implements L1_Domain<PlanTracking> {

    public static final Metrics INSTANCE = new Metrics();

    @Override public PlanTracking parent() { return PlanTracking.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("Quantified before/after measurements of a phase's outcome.");
    }
}
