package hue.captains.singapura.js.homing.component.taxonomy;

/** The lexicon at level 0: its root alone. */
public sealed interface L0_WordBranch extends WordBranch permits WordRoot {
    @Override default int level() { return 0; }
}
