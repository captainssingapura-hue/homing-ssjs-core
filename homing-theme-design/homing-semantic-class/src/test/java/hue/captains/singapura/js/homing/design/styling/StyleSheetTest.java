package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.Extent;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Ink;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Loudly;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.OverShabby;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Shabby;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Stray;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A style's sheet flattened: one class for every casting, the style's own rule or its base layer's, each whole and saying
 * whose it is; nothing falling back. And a deployment's styling: the two sheets a page wears, each by its own slug.
 */
class StyleSheetTest {

    private static final Taxonomy TAXONOMY = StyledTaxonomy.INSTANCE.read();
    private static final StyleSheet SHEET = StyleSheet.INSTANCE;

    @Test
    void aStyleOverItsBaseLayer_itsOwnFileWhereItAnswersContent_theBaseLayersElsewhere() {
        String ink = SHEET.write(TAXONOMY, Ink.INSTANCE);
        assertEquals(List.of("/* tab: ink's own */", "/* strip: plain's, ink answering absence */", "/* meter: plain's, ink answering absence */",
                        "/* badge: plain's, ink answering absence */", "/* tab_lead: plain's, ink answering absence */"),
                ink.lines().filter(l -> l.matches("/\\* [a-z0-9_-]+: .*('s own|answering absence) \\*/")).toList(),
                "every leaf once, in the taxonomy's order");
        assertTrue(ink.contains("/* tab: ink's own */\n.styled-tab {\n"), ink);
        assertEquals(1, ink.split("\\.styled-tab \\{", -1).length - 1, "the tab's class once: ink's, never plain's beside it");
        assertTrue(ink.contains(Extent.PROPERTY) && ink.contains(hue.captains.singapura.js.homing.design.Growth.SIZE.property()), "the degrees' properties, core's, at the top");
    }

    @Test
    void theBaseLayer_isEveryLeafsOwn() {
        String plain = SHEET.write(TAXONOMY, Plain.INSTANCE);
        assertEquals(5, plain.lines().filter(l -> l.endsWith("'s own */")).count());
        assertTrue(plain.contains("--along-risk: color-mix(in oklab, var(--fp-danger)"), "a leaf's own colours travel with it");
        assertTrue(plain.contains("}\n\n@keyframes meter-sweep {"), "its keyframes served with its rule (D29)");
        assertTrue(SHEET.write(TAXONOMY, Ink.INSTANCE).contains("@keyframes meter-sweep"), "with the base layer's rule, where a style answers absence");
    }

    @Test
    void theFlattener_onlyPutsTogether_theCheckRefuses() {
        assertTrue(SHEET.write(TAXONOMY, Stray.INSTANCE).contains("/* tab: plain's, stray answering absence */"),
                "stray's file it does not answer is never used; the check refuses it");
    }

    @Test
    void nothingFallsBack() {
        var shabby = assertThrows(IllegalArgumentException.class, () -> SHEET.write(TAXONOMY, Shabby.INSTANCE));
        assertEquals("shabby's sheet cannot be written: shabby is a base layer, and answers absence for meter: a base layer "
                + "answers content for every leaf (D26); shabby answers content for badge, and no file of its family rules it", shabby.getMessage());
        var over = assertThrows(IllegalArgumentException.class, () -> SHEET.write(TAXONOMY, OverShabby.INSTANCE));
        assertTrue(over.getMessage().contains("over-shabby answers absence for meter, and so does its base layer, shabby"), over.getMessage());
    }
}
