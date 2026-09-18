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

    /** The planes are orthogonal: no physique word carries a colour, so any physique under any palette is the same design in other colours. */
    @Test
    void noPhysiqueWord_carriesAColour() {
        var colour = java.util.regex.Pattern.compile("#[0-9A-Fa-f]{3,8}\\b|rgba?\\(|hsla?\\(");
        for (Design d : List.of(HomingDefault.INSTANCE, HomingNeoBrutalism.INSTANCE, HomingNeoFuturism.INSTANCE))
            for (var pair : worn()) {
                if (pair.onColourPlane()) continue;
                if (!(d.impl(pair) instanceof hue.captains.singapura.js.homing.design.Impl.Bindings b)) continue;
                b.values().values().forEach(states -> states.values().forEach(props -> props.values().forEach(v ->
                        assertFalse(colour.matcher(v).find(), d.slug() + " " + pair + " carries a colour: " + v))));
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
    void theThreeBases_areListedDefaultFirst_thenEveryCross() {
        var r = StudioThemeRegistry.INSTANCE;
        assertEquals(List.of("default", "neo-brutalism", "neo-futurism"), r.bases().stream().map(Theme::slug).toList());
        assertEquals(List.of("default", "neo-brutalism", "neo-futurism", "forest", "sunset"), r.colours().stream().map(Theme::slug).toList());
        var slugs = r.themes().stream().map(Theme::slug).toList();
        assertEquals(3 + 3 * 4, slugs.size(), "three bases, each in the four other colours: " + slugs);
        assertEquals(List.of("default", "neo-brutalism", "neo-futurism"), slugs.subList(0, 3));
        assertTrue(slugs.contains("neo-brutalism_forest") && slugs.contains("default_sunset"), slugs.toString());
        // dressed: the base in its own colours is the base; in another's, the cross the registry lists
        assertEquals("neo-futurism", r.dressed(r.bases().get(2), r.colours().get(2)).slug());
        assertEquals("neo-futurism_forest", r.dressed(r.bases().get(2), r.colours().get(3)).slug());
        assertFalse(StudioThemeRegistry.INSTANCE.themes().stream().anyMatch(t -> !(t instanceof Design)), "every theme is a design");
    }
}
