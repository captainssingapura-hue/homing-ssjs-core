package hue.captains.singapura.js.homing.realm.presentation;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L1_Domain;

/**
 * Document presentation — presenting a single document's content: prose,
 * structure, and embedded media at a comfortable reading measure (S0 surface
 * F-document).
 *
 * <p>Deferred L2 sub-domains: prose body, meta header, table, figure,
 * masthead.</p>
 */
public record DocumentPresentation() implements L1_Domain<SitePresentation> {

    public static final DocumentPresentation INSTANCE = new DocumentPresentation();

    @Override public SitePresentation parent() { return SitePresentation.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of(
                "Presenting a single document's content — prose, structure, and embedded "
              + "media at a comfortable reading measure.",
                "Deferred sub-domains: prose, meta-header, table, figure, masthead.");
    }
}
