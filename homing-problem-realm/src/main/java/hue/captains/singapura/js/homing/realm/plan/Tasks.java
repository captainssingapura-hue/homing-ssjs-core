package hue.captains.singapura.js.homing.realm.plan;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L1_Domain;

/**
 * Tasks — the actionable checklist items that make up the work of a phase. (A
 * task is conceptually per-phase; kept a flat aspect here, per the flat model —
 * see the package-info note on the deferred {@code Phase} entity.)
 */
public record Tasks() implements L1_Domain<PlanTracking> {

    public static final Tasks INSTANCE = new Tasks();

    @Override public PlanTracking parent() { return PlanTracking.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("The actionable checklist items that make up the work of a phase.");
    }
}
