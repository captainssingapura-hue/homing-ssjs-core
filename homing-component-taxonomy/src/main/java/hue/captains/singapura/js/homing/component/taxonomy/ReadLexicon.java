package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Reads a lexicon from the words a deployment declares: it reaches every branch through the
 * parents, and refuses - with every problem it found at once - what the compiler cannot: a word or
 * a branch with no parent; two nodes with one name; a node with no meaning, a meanings file with a
 * section astray, or two nodes that mean the same. A word declared twice is read once.
 *
 * <p>That every node is a stateless functional object is held at build time, by the tests of
 * whoever declares the words, as it is for the taxonomy's trees.</p>
 */
public record ReadLexicon() implements StatelessFunctionalObject {

    public static final ReadLexicon INSTANCE = new ReadLexicon();

    /** The words declared, in their order, and the branches they are filed under. */
    public Lexicon read(Collection<? extends Word<?>> declared) {
        var problems = new ArrayList<TaxonomyProblem>();
        var words = new LinkedHashSet<Word<?>>();
        for (Word<?> w : declared) if (w != null) words.add(w);
        var branches = new LinkedHashSet<WordBranch>();
        for (Word<?> w : words) {
            WordBranch b = w.parent();
            if (b == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.of(w) + " names no parent"));
            else if (b instanceof L1_WordBranch<?> l1) {
                if (l1.parent() == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.of(l1) + " names no parent"));
                branches.add(l1);
            }
        }
        var branchList = List.copyOf(branches);
        var wordList = List.copyOf(words);
        problems.addAll(TreeChecks.INSTANCE.clashes("words", Lexicon.nodes(branchList, wordList), WordNode::name));
        if (!wordList.isEmpty()) problems.addAll(Meanings.INSTANCE.problems(Lexicon.nodes(branchList, wordList)));
        if (!problems.isEmpty()) throw new RefusedTaxonomy(problems);
        return new Lexicon(branchList, wordList);
    }
}
