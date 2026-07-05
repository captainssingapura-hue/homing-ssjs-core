package hue.captains.singapura.js.homing.realm.presentation;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L1_Domain;

/**
 * Consistent site chrome &amp; navigation — the persistent frame that orients a
 * reader on every page (S0 surface F-chrome).
 *
 * <p>Deferred L2 sub-domains (added when their expression-intent families are
 * built): brand identity, breadcrumb wayfinding, header bar, page footer.</p>
 */
public record SiteChrome() implements L1_Domain<SitePresentation> {

    public static final SiteChrome INSTANCE = new SiteChrome();

    @Override public SitePresentation parent() { return SitePresentation.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of(
                "The persistent frame that orients a reader on every page — identity, "
              + "wayfinding, and the page frame.",
                "Deferred sub-domains: brand, breadcrumb, header-bar, footer.");
    }
}
