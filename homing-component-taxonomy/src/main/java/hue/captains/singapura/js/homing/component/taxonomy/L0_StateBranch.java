package hue.captains.singapura.js.homing.component.taxonomy;

/** The tree of states at level 0: its root alone. */
public sealed interface L0_StateBranch extends StateBranch permits StateRoot {
    @Override default int level() { return 0; }
}
