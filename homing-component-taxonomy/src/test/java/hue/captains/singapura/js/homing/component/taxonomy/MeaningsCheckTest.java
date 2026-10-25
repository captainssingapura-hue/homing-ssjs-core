package hue.captains.singapura.js.homing.component.taxonomy;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The meanings check, run at build over what a taxonomy read holds. Each taxonomy here reads the sketch whole, so
 * every section of the sketch's file is a node read, and the fixtures beside it.
 */
class MeaningsCheckTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();

    private static Taxonomy read(Component<?>... more) {
        var declared = new ArrayList<Component<?>>(Sketch.DECLARED);
        declared.addAll(List.of(more));
        return READ.read(declared, Sketch.CATALOGUE);
    }

    private static List<String> problems(Taxonomy t) {
        return MeaningsCheck.INSTANCE.problems(t).stream().map(TaxonomyProblem::toString).toList();
    }

    @Test
    void aTaxonomyWhoseFilesAreWhole_hasNothingWrong() {
        assertEquals(List.of(), problems(read()));
    }

    @Test
    void aNodeThatMeansNothing_twoThatMeanTheSame() {
        assertDoesNotThrow(() -> read(Meaningless.Silent.INSTANCE, Meaningless.Blank.INSTANCE), "the read no longer refuses them");

        var file = "meanings/hue/captains/singapura/js/homing/component/taxonomy/Meaningless.md";
        assertEquals(List.of("NO_MEANING: Meaningless$Silent means nothing: no section '## Silent' with words in it, in " + file,
                             "NO_MEANING: Meaningless$Blank means nothing: no section '## Blank' with words in it, in " + file,
                             "MEANING_TWICE: Meaningless$Twin and Meaningless$Twain mean the same: one node, or two meanings"),
                problems(read(Meaningless.Silent.INSTANCE, Meaningless.Blank.INSTANCE, Meaningless.Twin.INSTANCE, Meaningless.Twain.INSTANCE)),
                "no section, a section with no words in it, and the same words however they are spaced");
    }

    @Test
    void aFileAstray_aStaleHeadingNamedWithItsFile() {
        var file = "meanings/hue/captains/singapura/js/homing/component/taxonomy/Astray.md: ";
        assertEquals(List.of("MEANING_ASTRAY: " + file + "words before the first section: 'Words before any section, which no node means.'",
                             "MEANING_ASTRAY: " + file + "'## Lost' twice",
                             "MEANING_ASTRAY: " + file + "'## Gone' names no node Astray declares"),
                problems(read(Astray.Lost.INSTANCE)));
    }
}
