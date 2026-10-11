package hue.captains.singapura.js.homing.component.taxonomy;

/** A branch of the lexicon at level 1, under the root. */
public non-sealed interface L1_WordBranch<P extends L0_WordBranch> extends WordBranch {
    /** The branch it is filed under, one level up. */
    P parent();
    @Override default int level() { return 1; }
}
