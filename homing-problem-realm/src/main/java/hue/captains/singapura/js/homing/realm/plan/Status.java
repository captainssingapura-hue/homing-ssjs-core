package hue.captains.singapura.js.homing.realm.plan;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.Line;
import com.hominglabs.rebar.core.desc.Summary;
import com.hominglabs.rebar.realm.problem.LeafDomain;

import java.util.List;

/**
 * The canonical, conceptual <strong>status</strong> of a plan phase — the single
 * source of truth for what phase statuses exist, independent of any display
 * usage. A leaf: it is the dimension itself, not a place that holds sub-domains.
 *
 * <p>Its canonical values are {@link PhaseState}. Usage-layer Expression Intents
 * ({@code Glance × PhaseState}, …) <em>reference</em> these values — they never
 * re-declare them, so the four states can never drift or diverge in meaning
 * across usages, only in realization.</p>
 */
public record Status() implements LeafDomain<PlanTracking> {

    public static final Status INSTANCE = new Status();

    @Override public PlanTracking parent() { return PlanTracking.INSTANCE; }

    @Override public Desc desc() {
        return new Desc(new Summary(
                "The canonical, conceptual status of a plan phase — the single source of "
              + "truth for what phase statuses exist, independent of any display usage."),
                List.of(new Line(
                "Canonical values: PhaseState. Usage-layer intents reference these; they "
              + "never redefine them.")));
    }
}
