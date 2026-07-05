package hue.captains.singapura.js.homing.realm.presentation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pins the presentation Problem-Realm tree: the four surfaces parent to the
 * root, and the root describes itself. (The former {@code PresentationCommons}
 * node was retired — its {@code Status} was a borrowed presentation vocabulary,
 * not a domain; real subject concepts now live in their own subject domains,
 * e.g. {@code realm.plan}.)
 */
class PresentationRealmTest {

    @Test
    void surfacesParentToTheRoot() {
        assertSame(SitePresentation.INSTANCE, SiteChrome.INSTANCE.parent());
        assertSame(SitePresentation.INSTANCE, CatalogueOrganization.INSTANCE.parent());
        assertSame(SitePresentation.INSTANCE, DocumentPresentation.INSTANCE.parent());
        assertSame(SitePresentation.INSTANCE, ProgressTracking.INSTANCE.parent());
    }

    @Test
    void rootDescribesItself() {
        assertFalse(SitePresentation.INSTANCE.desc().summary().text().isBlank());
    }
}
