package hue.captains.singapura.js.homing.ui.dialog;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.design.Deployment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The crate holds every served module and imports only what it requires; no class holds a value the substrate should bind. */
class UiDialogCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(UiDialogCrate.INSTANCE));
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(UiDialogCrate.INSTANCE));
    }

    @Test
    void theDialogPaintsNothingOfItsOwn() {
        var worn = Deployment.wornBy(List.of(DialogStyles.INSTANCE));
        assertTrue(worn.size() >= 4, "the dialog's own four classes wear its scrim, foot and cap; the frame is the pane's; found " + worn.size());
        for (CssClass<DialogStyles> c : DialogStyles.INSTANCE.cssClasses()) {
            String body = c.body();
            assertFalse(body.contains("#") || body.contains("rgb") || body.contains("px solid"),
                    c.getClass().getSimpleName() + " holds a value the substrate should bind: " + body);
        }
    }
}
