package hue.captains.singapura.js.homing.component.taxonomy;

/** The tree of events at level 0: its root alone. */
public sealed interface L0_EventBranch extends EventBranch permits EventRoot {
    @Override default int level() { return 0; }
}
