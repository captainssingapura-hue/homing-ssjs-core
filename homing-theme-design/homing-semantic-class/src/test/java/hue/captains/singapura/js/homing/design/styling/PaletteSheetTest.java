package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.BaseColours;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A palette's sheet: every palettable colour its scheme sees as an --fp- variable, worked out through the two layers and
 * saying who gave it where the palette did not; fixed and interpolated colours none of it; refused when incomplete.
 */
class PaletteSheetTest {

    private static final ColorTree TREE = BaseColours.LOUD_TREE;
    private static final PaletteSheet SHEET = PaletteSheet.INSTANCE;

    @Test
    void aBasePalette_everyPalettableColourOfTheBase() {
        var paper = SHEET.write(TREE, BaseColours.Paper.INSTANCE);
        assertTrue(paper.css().endsWith(":root {\n"
                + "    --fp-page: #ffffff;\n"
                + "    --fp-raised: #f4f4f4;\n"
                + "    --fp-copy: #1f2937;\n"
                + "    --fp-danger: #dc2626;\n"
                + "    --fp-safe: #16a34a;\n"
                + "    --fp-flag: #e53935;\n"
                + "}\n"), paper.css());
        assertTrue(!paper.css().contains("--fp-glare"), "the loud scheme's glare is none of the base's");
        assertTrue(!paper.css().contains("clear") && !paper.css().contains("alarm") && !paper.css().contains("warning"),
                "fixed and interpolated colours are assigned where they are used, never in a palette's sheet");
    }

    @Test
    void aPaletteOfASchemeOnTop_itsOwn_thenThePalettesBelow() {
        var neon = SHEET.write(TREE, BaseColours.Neon.INSTANCE);
        assertTrue(neon.css().contains("    --fp-page: #ffffff;   /* paper, below neon */\n"), neon.css());
        assertTrue(neon.css().contains("    --fp-danger: #ff0055;\n"), "neon's own danger, said by neon itself");
        assertTrue(neon.css().contains("    --fp-glare: #39ff14;\n"), "the colour its scheme adds");
        assertTrue(neon.css().contains("    --fp-flag: #e53935;   /* paper, below neon */\n"), neon.css());
    }

    @Test
    void everyPaletteOfOneScheme_definesTheSameVariables() {
        var paper = SHEET.write(TREE, BaseColours.Paper.INSTANCE);
        var night = SHEET.write(TREE, BaseColours.Night.INSTANCE);
        assertEquals(List.of("--fp-page", "--fp-raised", "--fp-copy", "--fp-danger", "--fp-safe", "--fp-flag"), paper.variables());
        assertEquals(paper.variables(), night.variables(), "a style's CSS is the same in every palette of its scheme");
        assertTrue(night.css().contains("    --fp-copy: oklch(0.93 0.01 260);\n"), night.css());
        assertTrue(night.css().contains("    --fp-flag: #ff6f00;\n"), "the night's own flag");
        assertEquals(7, SHEET.write(TREE, BaseColours.Neon.INSTANCE).variables().size(), "the base's six, and the glare");
    }

    @Test
    void anIncompletePalette_orOneThatIsNoChain_isRefused() {
        var unsafe = assertThrows(IllegalArgumentException.class, () -> SHEET.write(TREE, new BaseColours.Unsafe()));
        assertEquals("unsafe's sheet cannot be written: unsafe leaves safe unanswered", unsafe.getMessage());
        var stranded = assertThrows(IllegalArgumentException.class, () -> SHEET.write(TREE, new BaseColours.Stranded()));
        assertTrue(stranded.getMessage().contains("stranded sits on nothing, and LoudScheme is on top of BaseScheme"), stranded.getMessage());
    }

    @Test
    void theVariables_areNamedByTheirLeaves() {
        var v = ColorVariables.INSTANCE;
        assertEquals("--fp-copy", v.of(new BaseColours.Copy()));
        assertEquals("--fixed-alarm", v.of(new BaseColours.Alarm()));
        assertEquals("--it-warning", v.of(new BaseColours.Warning()));
        assertEquals("--along-risk", v.along(new BaseColours.Risk()));
    }
}
