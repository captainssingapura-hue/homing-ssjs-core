package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentLeaf;
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
        var tab = CLASSES.of(TAXONOMY, Tab.INSTANCE);
        assertEquals("styled-tab", tab.styled());
        assertEquals(List.of(":hover", ".styled-tab--disabled", ".styled-tab--selected", ".styled-tab--candidate", ".styled-tab--held"),
                tab.states().stream().map(LeafClasses.Drawn::drawnBy).toList(),
                "quinary, quaternary, tertiary, then the keys' two values, secondary - though the tab lists the keys first");
        assertEquals(List.of(true, false, false, false, false), tab.states().stream().map(LeafClasses.Drawn::pseudo).toList(),
                "the hover is the browser's, drawn by its pseudo-class; every other state is a class");
        assertEquals("quaternary", tab.states().get(1).level());
    }

    @Test
    void aPart_isALeafOfItsOwn_itsClassesItsOwn() {
        ComponentLeaf lead = TAXONOMY.parts().get(0);
        var listing = CLASSES.of(TAXONOMY, lead);
        assertEquals("styled-strip-lead", listing.styled());
        assertEquals(List.of(".styled-strip-lead--selected"), listing.states().stream().map(LeafClasses.Drawn::drawnBy).toList(),
                "the part's one state, none of the tab's four");
    }

    @Test
    void theListing_everyLeaf_aLineAClass() {
        assertEquals(List.of("tab", "strip", "strip-lead"), CLASSES.listing(TAXONOMY).stream().map(l -> l.leaf().token()).toList());
        List<String> lines = CLASSES.lines(TAXONOMY);
        assertEquals("tab  .styled-tab", lines.get(0));
        assertEquals("    :hover  hovered=true  quinary", lines.get(1));
        assertEquals("    .styled-tab--held  keys=held  secondary", lines.get(5));
        assertTrue(lines.contains("strip  .styled-strip"), "a leaf with no states has its class alone");
        assertTrue(lines.contains("    .styled-strip-lead--selected  selected=true  tertiary"), lines.toString());
    }
}
