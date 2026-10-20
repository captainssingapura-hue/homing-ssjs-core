package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.styles.plain.Meter;
import hue.captains.singapura.js.homing.design.styles.plain.Tab;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One implementation's CSS (RFC 0067, appendix The Style Jars): at its class's canonical name in its own jar, its marks
 * invalid CSS - its casting, its states, its keyframes - and no class name; filled in for whichever casting is served.
 */
class CastingCssTest {

    private static final CastingCss CSS = CastingCss.INSTANCE;

    @Test
    void anImplementationsCss_isAtItsCanonicalName_inItsOwnJar() {
        assertEquals("hue/captains/singapura/js/homing/design/styles/plain/Tab.css", CSS.resource(Tab.class));
        assertTrue(CSS.read(Tab.class).orElseThrow().contains("$casting {"), "read from the jar the class came from");
    }

    @Test
    void itsMarks_theCastingItsStatesItsKeyframes() {
        CastingCss.Marked tab = CSS.marks("Tab.css", CSS.read(Tab.class).orElseThrow());
        assertTrue(tab.passes(), tab.problems().toString());
        assertEquals(Set.of("disabled", "selected", "candidate", "held"), tab.states(), "the browser's :hover is its own, no mark");
        CastingCss.Marked meter = CSS.marks("Meter.css", CSS.read(Meter.class).orElseThrow());
        assertTrue(meter.passes(), meter.problems().toString());
        assertEquals(Set.of("sweep"), meter.keyframes());
    }

    @Test
    void filledIn_forTheCastingServed_eachUnderItsOwnClass() {
        String tab = CSS.read(Tab.class).orElseThrow();
        String lead = CSS.filled(tab, new Casting(StatedTaxonomy.Tab.INSTANCE, StatedTaxonomy.Lead.INSTANCE));
        assertTrue(lead.contains(".styled-tab_lead {"), lead);
        assertTrue(lead.contains("&.styled-tab_lead--selected {"), "a state's mark, the served casting's state class");
        assertTrue(lead.contains("&:hover {"), "the browser's own, as it is");
        assertFalse(lead.contains("$"), "no mark left");
        String meter = CSS.filled(CSS.read(Meter.class).orElseThrow(), new Casting(StyledTaxonomy.Meter.INSTANCE));
        assertTrue(meter.contains("@keyframes meter-sweep {"), "a keyframes of its own, named for the casting served");
        assertTrue(meter.contains("animation: meter-sweep 2s ease-out;"), "and where a value uses it");
    }

    @Test
    void refused_aClassNamed_aMarkMiswritten_aKeyframesNotWritten() {
        assertTrue(CSS.marks("a.css", ".styled-tab { color: inherit; }").problems().stream().anyMatch(p -> p.contains("is no mark of the casting")),
                "a class name at its top");
        assertTrue(CSS.marks("b.css", "$casting { &.styled-tab--selected { color: inherit; } }").problems().stream().anyMatch(p -> p.contains("names a class")),
                "a state's class written out");
        assertTrue(CSS.marks("c.css", "$casting { &$Selected { color: inherit; } }").problems().stream().anyMatch(p -> p.contains("&$word alone")),
                "a mark's word lower case");
        assertTrue(CSS.marks("d.css", "$casting { animation: $spin 1s; }").problems().stream().anyMatch(p -> p.contains("writes no @keyframes $spin")),
                "a keyframes used and not written");
        assertTrue(CSS.marks("e.css", "@media (prefers-contrast: more) { $casting { color: inherit; } }").problems().stream()
                .anyMatch(p -> p.contains("no at-rule at its top")), "a reader's setting is nested in the casting's rule");
        assertTrue(CSS.marks("f.css", "/* nothing */").problems().stream().anyMatch(p -> p.contains("marks no $casting")), "no rule at all");
        assertTrue(CSS.marks("g.css", "$casting { color: inherit; } $casting { color: inherit; }").problems().stream()
                .anyMatch(p -> p.contains("ruled twice")), "its one rule, once");
    }

    @Test
    void aRoom_ruledBesideTheCasting_declarationsAlone_filledAsTheCastingsClassAndItsName() {
        String css = "$casting { gap: 0; } $casting__body { padding-block: 14px; gap: 10px; }";
        CastingCss.Marked marked = CSS.marks("panel.css", css);
        assertTrue(marked.passes(), marked.problems().toString());
        assertEquals(Set.of("body"), marked.rooms());
        assertTrue(CSS.filled(css, "c-panel", "panel").contains(".c-panel__body {"), CSS.filled(css, "c-panel", "panel"));
        assertTrue(CSS.marks("h.css", "$casting { gap: 0; } $casting__body { gap: 1px; } $casting__body { gap: 2px; }").problems().stream()
                .anyMatch(p -> p.contains("$casting__body is ruled twice")), "a room once");
        assertTrue(CSS.marks("i.css", "$casting { gap: 0; } $casting__body { &$current { gap: 1px; } }").problems().stream()
                .anyMatch(p -> p.contains("declarations alone")), "a room has no states of its own");
        assertTrue(CSS.marks("j.css", "$casting { gap: 0; } $casting__Body { gap: 1px; }").problems().stream()
                .anyMatch(p -> p.contains("is no mark of the casting")), "a room named in lower-case words");
    }
}
