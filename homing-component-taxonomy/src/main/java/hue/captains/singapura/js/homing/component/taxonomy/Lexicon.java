package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A lexicon, as read: its branches under the root, and its words - the taxonomy of what a mark may
 * mean, apart from the components that show the marks. It cannot be constructed with two nodes of
 * one name - the check is the constructor's, so no lexicon with a clash exists, however it is made.
 *
 * @param branches the branches under the root, in the order first reached
 * @param words    the words: the source's own in its order, then each extension's
 */
public record Lexicon(List<WordBranch> branches, List<Word<?>> words) implements ValueObject {

    public Lexicon {
        branches = List.copyOf(branches);
        words = List.copyOf(words);
        var problems = TreeChecks.INSTANCE.clashes("words", nodes(branches, words), WordNode::name);
        if (!problems.isEmpty()) throw new RefusedTaxonomy(problems);
    }

    /** Every node: the root, the branches, the words. */
    static List<WordNode> nodes(List<? extends WordBranch> branches, List<? extends Word<?>> words) {
        var out = new ArrayList<WordNode>();
        out.add(WordRoot.INSTANCE);
        out.addAll(branches);
        out.addAll(words);
        return out;
    }

    /** The branch a node is filed under; none for the root. */
    public WordBranch parent(WordNode node) {
        return switch (node) {
            case Word<?> w -> w.parent();
            case L1_WordBranch<?> b -> b.parent();
            case L0_WordBranch r -> null;
        };
    }

    /** The branches and words directly under a branch - the root's included - branches first. */
    public List<WordNode> children(WordBranch branch) {
        var out = new ArrayList<WordNode>();
        for (WordBranch b : branches) if (branch.equals(parent(b))) out.add(b);
        for (Word<?> w : words) if (branch.equals(w.parent())) out.add(w);
        return List.copyOf(out);
    }

    /** The word a token names, if the lexicon has it: {@code close}. */
    public Optional<Word<?>> word(String token) {
        return words.stream().filter(w -> w.token().equals(token)).findFirst();
    }

    /** What a node of the lexicon means: read, on a lexicon that was, from the section written for it. */
    public Meaning meaning(WordNode node) { return Meanings.INSTANCE.meant(node); }
}
