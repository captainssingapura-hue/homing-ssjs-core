package hue.captains.singapura.js.homing.ui.elements;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.design.Deployment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The crate holds every served module and imports only what it requires; every class wears something. */
class UiElementsCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(UiElementsCrate.INSTANCE));
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(UiElementsCrate.INSTANCE));
    }

    @Test
    void theElementsPaintNothingOfTheirOwn() {
        var worn = Deployment.wornBy(List.of(ElementStyles.INSTANCE));
        assertTrue(worn.size() >= 20, "eight classes wear over twenty pairs; found " + worn.size());
        for (CssClass<ElementStyles> c : ElementStyles.INSTANCE.cssClasses()) {
            String body = c.body();
            assertFalse(body.contains("#") || body.contains("rgb") || body.contains("px solid"),
                    c.getClass().getSimpleName() + " holds a value the substrate should bind: " + body);
        }
    }
}
