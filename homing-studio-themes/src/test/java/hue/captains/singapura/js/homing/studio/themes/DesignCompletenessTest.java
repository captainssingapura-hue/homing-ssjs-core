package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.design.Deployment;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.server.ServedModules;
import hue.captains.singapura.js.homing.studio.workspace.StudioWorkspaceCrate;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The completeness law over the studio's designs: every pair any served
 * component wears has an answer from every design, and every answer is valid
 * for its target. One deployment per design; the findings are the test.
 */
class DesignCompletenessTest {

    /** Everything the studio's closure wears, read off the served groups — the same set the renderer cuts to. */
    static Set<DesignClass<?>> worn() {
        var served = ServedModules.of(List.of(StudioWorkspaceCrate.INSTANCE));
        var groups = new ArrayList<CssGroup<?>>();
        for (var m : served.byName().values()) if (m instanceof CssGroup<?> g) groups.add(g);
        return Deployment.wornBy(groups);
    }

    @Test
    void theStudioWearsSomething() {
        var worn = worn();
        assertTrue(worn.size() > 100, "the studio's ten groups wear over a hundred pairs; found " + worn.size());
    }

    @Test
    void everyDesign_answersEveryPairTheStudioWears_validly() {
        var worn = worn();
        for (Theme t : StudioThemeRegistry.INSTANCE.themes()) {
            Design d = (Design) t;
            var r = Deployment.of(worn, d).resolve();
            assertEquals(List.of(), r.findings(), () -> d.slug() + ": " + r.findings());
            assertEquals(worn.size(), r.impls().size(), d.slug() + " resolved fewer pairs than worn");
        }
    }

    @Test
    void aDesignOverDefault_hasItsOwnWord_forMostOfWhatIsWorn() {
        var worn = worn();
        for (Design d : List.of(HomingNeoBrutalism.INSTANCE, HomingNeoFuturism.INSTANCE)) {
            long own = worn.stream().filter(p -> !d.impl(p).equals(HomingDefault.INSTANCE.impl(p))).count();
            assertTrue(own * 2 > worn.size(), d.slug() + " says only " + own + " of " + worn.size() + " pairs in its own words");
        }
    }

    @Test
    void theThreeDesigns_areListedDefaultFirst() {
        var slugs = StudioThemeRegistry.INSTANCE.themes().stream().map(Theme::slug).toList();
        assertEquals(List.of("default", "neo-brutalism", "neo-futurism"), slugs);
        assertFalse(StudioThemeRegistry.INSTANCE.themes().stream().anyMatch(t -> !(t instanceof Design)), "every theme is a design");
    }
}
