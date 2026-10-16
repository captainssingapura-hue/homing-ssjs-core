package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.core.util.ResourceReader;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours;
import hue.captains.singapura.js.homing.design.styles.ink.InkStyles;
import hue.captains.singapura.js.homing.design.styles.loudly.LoudlyStyles;
import hue.captains.singapura.js.homing.design.styles.plain.PlainStyles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A styling's family files, for reading (RFC 0067, appendix The Style Jars): read as the modules' scripts are - in dev
 * mode made afresh from the casting files on the file system, deployed the ones the instance's jar was built with,
 * refused when they were made from another styling; a delegating casting one line naming the rule it takes.
 */
class StyleFamiliesTest {

    private static final StyleManager MANAGER = StyleManager.of(StyledTaxonomy.INSTANCE.read(), List.of(ResolvingColours.INSTANCE, ResolvingColours.LoudColours.INSTANCE), List.of(
            StyleRegistration.of(StyledStyles.Plain.INSTANCE, PlainStyles.INSTANCE), StyleRegistration.of(StyledStyles.Ink.INSTANCE, InkStyles.INSTANCE),
            StyleRegistration.of(StyledStyles.Loudly.INSTANCE, LoudlyStyles.INSTANCE)));
    private static final Styling STYLING = new Styling(MANAGER, List.of(StyledStyles.Ink.INSTANCE, StyledStyles.Loudly.INSTANCE),
            List.of(ResolvingColours.Night.INSTANCE, ResolvingColours.Neon.INSTANCE), Map.of(StyledStyles.Ink.INSTANCE, ResolvingColours.Night.INSTANCE,
                    StyledStyles.Loudly.INSTANCE, ResolvingColours.Neon.INSTANCE));

    /** Ink's own tab, at its canonical name. */
    private static final String INK_TAB = "hue/captains/singapura/js/homing/design/styles/ink/Tab.css";

    @Test
    void aFamilysFile_eachCastingAsTheStyleServesIt_aDelegationOneLine() {
        var families = new StyleFamilies(STYLING, ResourceReader.INSTANCE, getClass().getClassLoader());
        assertEquals(List.of("plain", "ink", "loudly"), families.styles(), "every style registered, the base layer among them");
        assertEquals(List.of("control", "gauge"), families.families());
        String control = MANAGER.family(StyledStyles.Ink.INSTANCE, MANAGER.taxonomy().heads().get(0), ResourceReader.INSTANCE);
        assertTrue(control.startsWith("/* ink - the control family"), control);
        assertTrue(control.contains("/* tab: ink's own */\n.styled-tab {"), control);
        assertTrue(control.contains("/* tab_lead: plain's, ink answering absence, as plain's Tab - its rule tab's, under .styled-tab_lead */\n"),
                "a delegation: one line, naming the rule it takes");
        assertFalse(control.contains(".styled-tab_lead {"), "its rule not repeated");
    }

    @Test
    void inDevMode_madeAfreshFromTheFileSystem_everyRead(@TempDir Path root) throws IOException {
        var families = new StyleFamilies(STYLING, new ResourceReader(root), getClass().getClassLoader());
        assertTrue(families.dev());
        assertTrue(families.read("ink", "control").contains("color: var(--fp-copy);"), "no file there: the classpath's");
        Path tab = root.resolve(INK_TAB);
        Files.createDirectories(tab.getParent());
        Files.writeString(tab, "$casting {\n    color: var(--fp-danger);\n}\n");
        assertTrue(families.read("ink", "control").contains(".styled-tab {\n    color: var(--fp-danger);"), "the file as it is now");
        Files.writeString(tab, "$casting {\n    color: var(--fp-raised);\n}\n");
        assertTrue(families.read("ink", "control").contains("color: var(--fp-raised);"), "and again, after an edit");
        assertThrows(IllegalArgumentException.class, () -> families.read("shabby", "control"));
        assertThrows(IllegalArgumentException.class, () -> families.read("ink", "nothing"));
    }

    @Test
    void deployed_theFilesItsJarWasBuiltWith_refusedForAnotherStyling(@TempDir Path classes) throws IOException {
        String none = assertThrows(IllegalStateException.class,
                () -> new StyleFamilies(STYLING, ResourceReader.INSTANCE, getClass().getClassLoader()).read("ink", "control")).getMessage();
        assertTrue(none.contains("no family files are on the classpath"), none);

        Map<String, String> written = WriteStyleFamilies.write(classes, STYLING);
        assertEquals(1 + 3 * 2, written.size(), "the index, and each of three styles' two families");
        try (var jar = new URLClassLoader(new URL[] { classes.toUri().toURL() }, null)) {
            var families = new StyleFamilies(STYLING, ResourceReader.INSTANCE, jar);
            assertFalse(families.dev());
            assertEquals(written.get(StyleFamilies.path("loudly", "gauge")), families.read("loudly", "gauge"), "the built file, as written");
        }
        Files.writeString(classes.resolve(StyleFamilies.INDEX), "# another styling's\n", StandardCharsets.UTF_8);
        try (var jar = new URLClassLoader(new URL[] { classes.toUri().toURL() }, null)) {
            String another = assertThrows(IllegalStateException.class,
                    () -> new StyleFamilies(STYLING, ResourceReader.INSTANCE, jar).read("ink", "control")).getMessage();
            assertTrue(another.contains("made from another styling"), another);
        }
        Files.writeString(classes.resolve(StyleFamilies.ROOT + "stale.css"), "stale");
        WriteStyleFamilies.write(classes, STYLING);
        assertFalse(Files.exists(classes.resolve(StyleFamilies.ROOT + "stale.css")), "what was there before is cleared");
    }
}
