package hue.captains.singapura.js.homing.realm.presentation;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L1_Domain;

/**
 * Progress tracking — presenting a plan or tracker's state: progress, phased
 * steps, tasks, and decisions over time (S0 surface F-tracker).
 *
 * <p>Deferred L2 sub-domains: progress meter, step, panel, task checklist,
 * dependency, acceptance, effort.</p>
 */
public record ProgressTracking() implements L1_Domain<SitePresentation> {

    public static final ProgressTracking INSTANCE = new ProgressTracking();

    @Override public SitePresentation parent() { return SitePresentation.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of(
                "Presenting a plan or tracker's state — progress, phased steps, tasks, and "
              + "decisions over time.",
                "Deferred sub-domains: progress-meter, step, panel, task, dependency, "
              + "acceptance, effort.");
    }
}
