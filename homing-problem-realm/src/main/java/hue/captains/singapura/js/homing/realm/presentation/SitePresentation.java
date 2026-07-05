package hue.captains.singapura.js.homing.realm.presentation;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L0_Domain;

/**
 * Problem Realm root — the studio's problem of presenting a documentation site
 * coherently: one consistent surface across chrome, catalogues, documents, and
 * trackers.
 *
 * <p>UI-agnostic of any single realization; each node below is a presentation
 * <em>concern</em>, never a CSS class. Derived from the S0 visual-expression
 * audit (see {@code homing-self-studio/docs/site-presentation-subdomains}).</p>
 */
public record SitePresentation() implements L0_Domain {

    public static final SitePresentation INSTANCE = new SitePresentation();

    @Override public Desc desc() {
        return Descs.of(
                "Presenting a documentation site coherently — one consistent surface across "
              + "chrome, catalogues, documents, and trackers.",
                "Problem Realm root. Each child is a presentation concern, not a realization.",
                "Derived from the S0 visual-expression audit.");
    }
}
