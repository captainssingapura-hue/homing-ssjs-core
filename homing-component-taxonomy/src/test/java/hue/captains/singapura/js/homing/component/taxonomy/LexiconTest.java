package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A lexicon - the taxonomy of what a mark may mean - read as the trees are: its words in the order
 * declared, its branches through their parents, every node meaning something; extended by the modules
 * on the classpath, whose words come after its own; and refused, with every problem at once, when a
 * name is said twice, a word means nothing, or one is filed nowhere.
 */
class LexiconTest {

    @Test
    void theWordsDeclared_theirBranchesThroughTheirParents() {
        Lexicon l = ReadLexicon.INSTANCE.read(Pictures.DECLARED);
        assertEquals(Pictures.DECLARED, l.words());
        assertEquals(List.of(Pictures.Doing.INSTANCE), l.branches());
        assertEquals(List.of(Pictures.Doing.INSTANCE, Pictures.Check.INSTANCE), l.children(WordRoot.INSTANCE), "branches first");
        assertEquals(List.of(Pictures.Close.INSTANCE, Pictures.Add.INSTANCE), l.children(Pictures.Doing.INSTANCE));
        assertEquals(WordRoot.INSTANCE, l.parent(Pictures.Doing.INSTANCE));
        assertEquals(null, l.parent(WordRoot.INSTANCE), "the root is filed under nothing");
    }

    @Test
    void aWordIsShownByItsToken_andMeansWhatItsSectionSays() {
        Lexicon l = ReadLexicon.INSTANCE.read(Pictures.DECLARED);
        assertEquals("close", Pictures.Close.INSTANCE.token());
        assertEquals(Optional.of(Pictures.Add.INSTANCE), l.word("add"));
        assertEquals(Optional.empty(), l.word("ship"), "a word the lexicon read has not");
        assertTrue(l.meaning(Pictures.Check.INSTANCE).markdown().contains("chosen"));
        assertTrue(l.meaning(WordRoot.INSTANCE).markdown().contains("never what it looks like"), "the root means something too");
    }

    @Test
    void theExtensionsOnTheClasspath_addTheirWordsAfterTheSourcesOwn() {
        assertEquals(List.of(new ShipPictures.Extension()), LexiconExtensions.INSTANCE.of(PicturesSource.class));
        Lexicon l = PicturesSource.INSTANCE.read();
        assertEquals(Pictures.DECLARED, l.words().subList(0, Pictures.DECLARED.size()));
        assertEquals(List.of(ShipPictures.Ship.INSTANCE, ShipPictures.Moor.INSTANCE), l.words().subList(Pictures.DECLARED.size(), l.words().size()));
        assertTrue(l.children(WordRoot.INSTANCE).contains(ShipPictures.Shipping.INSTANCE), "the module's own branch");
        assertTrue(l.children(Pictures.Doing.INSTANCE).contains(ShipPictures.Moor.INSTANCE), "and its word under the lexicon's branch");
        assertFalse(ReadLexicon.INSTANCE.read(Pictures.DECLARED).words().contains(ShipPictures.Ship.INSTANCE), "the lexicon alone has none of it");
    }

    @Test
    void aWordDeclaredTwice_isReadOnce() {
        assertEquals(Pictures.DECLARED, ReadLexicon.INSTANCE.read(List.of(Pictures.Close.INSTANCE, Pictures.Add.INSTANCE,
                Pictures.Close.INSTANCE, Pictures.Check.INSTANCE)).words());
    }

    @Test
    void aNameTwice_aWordMeaningNothing_andOneFiledNowhere_areRefusedAtOnce() {
        var refused = assertThrows(RefusedTaxonomy.class, () -> ReadLexicon.INSTANCE.read(List.of(Pictures.Close.INSTANCE,
                BrokenPictures.Close.INSTANCE, BrokenPictures.Mute.INSTANCE, BrokenPictures.Orphan.INSTANCE)));
        Set<Rule> rules = refused.problems().stream().map(TaxonomyProblem::rule).collect(Collectors.toSet());
        assertEquals(Set.of(Rule.TREE_NAME_TWICE, Rule.NO_MEANING, Rule.NO_PARENT), rules, refused.getMessage());
        assertTrue(refused.getMessage().contains("'close' names 2 nodes of the tree of words"), refused.getMessage());
        assertThrows(RefusedTaxonomy.class, () -> new Lexicon(List.of(), List.of(Pictures.Close.INSTANCE, BrokenPictures.Close.INSTANCE)),
                "nor can one be made with a clash");
    }
}
