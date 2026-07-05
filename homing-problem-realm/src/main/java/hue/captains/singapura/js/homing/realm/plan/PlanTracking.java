package hue.captains.singapura.js.homing.realm.plan;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L0_Domain;

/**
 * Plan Tracking — the domain topic of tracking a plan: its goals, the open
 * questions it must settle, the phased work that carries it out, the gates that
 * define done, and the progress toward them.
 *
 * <p>Conceptual layer. Its functional aspects are the direct children below —
 * grounded in the framework's own <em>Plans as Living Containers</em> model
 * (a plan exposes objectives, decisions, phases, acceptance, plus per-phase
 * tasks / dependencies / metrics and a derived progress).</p>
 */
public record PlanTracking() implements L0_Domain {

    public static final PlanTracking INSTANCE = new PlanTracking();

    @Override public Desc desc() {
        return Descs.of(
                "Tracking a plan — its goals, open questions, phased work, gates, and progress. "
              + "Conceptual layer; display usages reference it.");
    }
}
