package hue.captains.singapura.js.homing.realm.presentation;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.realm.problem.L1_Domain;

/**
 * Catalogue organization — presenting and browsing a collection: how a set of
 * documents or apps is organized for discovery (S0 surface F-catalogue).
 *
 * <p>Deferred L2 sub-domains: tile, listing, app-launcher, section heading,
 * kind signal, search &amp; filter, sidebar, table-of-contents.</p>
 */
public record CatalogueOrganization() implements L1_Domain<SitePresentation> {

    public static final CatalogueOrganization INSTANCE = new CatalogueOrganization();

    @Override public SitePresentation parent() { return SitePresentation.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of(
                "Presenting and browsing a collection — how a set of documents or apps is "
              + "organized for discovery.",
                "Deferred sub-domains: tile, listing, app-launcher, section, kind-signal, "
              + "filter, sidebar, toc.");
    }
}
