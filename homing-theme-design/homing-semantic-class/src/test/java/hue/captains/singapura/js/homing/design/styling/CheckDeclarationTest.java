package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours;
import hue.captains.singapura.js.homing.design.styles.ink.InkStyles;
import hue.captains.singapura.js.homing.design.styles.loudly.LoudlyStyles;
import hue.captains.singapura.js.homing.design.styles.plain.PlainStyles;
import hue.captains.singapura.js.homing.design.styles.shabby.ShabbyStyles;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A jar's declaration held alone (RFC 0067, appendix The Style Jars): each implementation by itself - its types, its
 * CSS, its colours both ways, its delegation - and the jar: no two of one casting, nothing of it left out.
 */
class CheckDeclarationTest {

    private static final Taxonomy TAXONOMY = StyledTaxonomy.INSTANCE.read();
    private static final ColorTree TREE = ResolvingColours.LoudColours.INSTANCE.read();
    private static final CheckDeclaration CHECK = CheckDeclaration.INSTANCE;

    @Test
    void eachValidJar_passes_eachImplementationAlone() {
        for (var declaration : List.of(PlainStyles.INSTANCE, InkStyles.INSTANCE, LoudlyStyles.INSTANCE)) {
            CheckDeclaration.Checked checked = CHECK.check(declaration, TAXONOMY, TREE);
            assertTrue(checked.passes(), declaration.getClass().getSimpleName() + ": " + checked.all());
        }
        CheckDeclaration.Checked plain = CHECK.check(PlainStyles.INSTANCE, TAXONOMY, TREE);
        assertEquals(List.of("plain.Tab", "plain.TabLead", "plain.Strip", "plain.Meter", "plain.Badge"),
                plain.implementations().stream().map(CheckDeclaration.Implementation::name).toList(), "one result per implementation");
        assertEquals(List.of("tab", "tab_lead", "strip", "meter", "badge"),
                plain.implementations().stream().map(CheckDeclaration.Implementation::casting).toList(), "each its casting, read off its types");
    }

    @Test
    void everyMistake_refused_eachWhereItIs() {
        CheckDeclaration.Checked shabby = CHECK.check(ShabbyStyles.INSTANCE, TAXONOMY, TREE);
        Map<String, String> by = shabby.implementations().stream()
                .collect(Collectors.toMap(CheckDeclaration.Implementation::name, i -> String.join(" | ", i.problems())));
        assertTrue(by.get("shabby.Tab").contains("is no mark of the casting"), "its class named: " + by.get("shabby.Tab"));
        assertTrue(by.get("shabby.TabAgain").contains("draws no state tab lists"), "a state its component lists not: " + by.get("shabby.TabAgain"));
        assertTrue(by.get("shabby.Meter").contains("has no hue/captains/singapura/js/homing/design/styles/shabby/Meter.css"),
                "CSS answered, none at its canonical name: " + by.get("shabby.Meter"));
        assertTrue(by.get("shabby.TabLead").contains("delegates round"), "a delegation to itself: " + by.get("shabby.TabLead"));
        assertTrue(by.get("shabby.Badge").contains("a delegation has no CSS of its own"), "a delegation with CSS: " + by.get("shabby.Badge"));
        String jar = String.join(" | ", shabby.problems());
        assertTrue(jar.contains("both implement tab in shabby"), "two of one casting, by their types: " + jar);
        assertTrue(jar.contains("shabby/Stray.css is no CSS of an implementation the declaration lists"), "a file it leaves out: " + jar);
    }

    @Test
    void colours_heldBothWays() {
        CheckDeclaration.Checked shabby = CHECK.check(ShabbyStyles.INSTANCE, TAXONOMY, TREE);
        String strip = String.join(" | ", shabby.implementations().stream().filter(i -> i.name().equals("shabby.Strip")).findFirst().orElseThrow().problems());
        assertTrue(strip.contains("uses danger, which it does not import"), strip);
        assertTrue(strip.contains("imports copy, and its CSS uses it nowhere"), strip);
    }

}
