package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.styling.StatedTaxonomy.Tab;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every leaf's style-aware classes, generated from its type: its class, a class for each value of each application state
 * it lists, and its pseudo states drawn by their pseudo-classes - in its precedence, the least important first. A part
 * is a leaf of its own, its classes and states its own.
 */
class LeafClassesTest {

    private static final Taxonomy TAXONOMY = StatedTaxonomy.INSTANCE.read();
    private static final LeafClasses CLASSES = LeafClasses.INSTANCE;

    @Test
    void aComponentsClasses_inItsPrecedence_theLeastImportantFirst() {
        var tab = CLASSES.of(TAXONOMY, new Casting(Tab.INSTANCE));
        assertEquals("styled-tab", tab.styled());
        assertEquals(List.of(":hover", ".styled-tab--disabled", ".styled-tab--selected", ".styled-tab--candidate", ".styled-tab--held"),
                tab.states().stream().map(LeafClasses.Drawn::drawnBy).toList(),
                "quinary, quaternary, tertiary, then the keys' two values, secondary - though the tab lists the keys first");
        assertEquals(List.of(true, false, false, false, false), tab.states().stream().map(LeafClasses.Drawn::pseudo).toList(),
                "the hover is the browser's, drawn by its pseudo-class; every other state is a class");
        assertEquals("quaternary", tab.states().get(1).level());
    }

    @Test
    void aCastingInARole_isALeafForItsClass_withItsComponentsStates_neverItsOwners() {
        Casting lead = TAXONOMY.castings().stream().filter(c -> !c.standalone()).findFirst().orElseThrow();
        assertEquals("tab_lead", lead.token(), "the tab cast as Lead: the component and its role, the strip that owns it never in it");
        var listing = CLASSES.of(TAXONOMY, lead);
        assertEquals("styled-tab_lead", listing.styled(), "a class of its own: the role changes the look");
        assertEquals(List.of(":hover", ".styled-tab_lead--disabled", ".styled-tab_lead--selected", ".styled-tab_lead--candidate", ".styled-tab_lead--held"),
                listing.states().stream().map(LeafClasses.Drawn::drawnBy).toList(), "the tab's states, in the tab's precedence: a role adds none");
    }

    @Test
    void theListing_everyLeaf_aLineAClass() {
        assertEquals(List.of("tab", "strip", "tab_lead"), CLASSES.listing(TAXONOMY).stream().map(l -> l.leaf().token()).toList());
        List<String> lines = CLASSES.lines(TAXONOMY);
        assertEquals("tab  .styled-tab", lines.get(0));
        assertEquals("    :hover  hovered=true  quinary", lines.get(1));
        assertEquals("    .styled-tab--held  keys=held  secondary", lines.get(5));
        assertTrue(lines.contains("strip  .styled-strip"), "a leaf with no states has its class alone");
        assertTrue(lines.contains("    .styled-tab_lead--selected  selected=true  tertiary"), lines.toString());
    }
}
