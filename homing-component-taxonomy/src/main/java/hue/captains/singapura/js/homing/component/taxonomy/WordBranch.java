package hue.captains.singapura.js.homing.component.taxonomy;

/** A branch of the lexicon, at its level: the root, or one under it. Levels are added as the lexicon grows. */
public sealed interface WordBranch extends WordNode permits L0_WordBranch, L1_WordBranch {
    /** Its level: 0 for the root, one more than its parent's for every other. */
    int level();
}
