package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;

/**
 * What a theme answers: whether it has a word for a node in a state on a target. Resolution asks
 * nothing else of it - which values the theme gives is the sheet's concern, not the walk's - so the
 * same tree is walked under every theme, and a theme that answers more precisely is found by the
 * same walk.
 */
public interface Answers {
    /** Whether the theme answers the node, in the state, on the target. */
    boolean answers(Casting node, State state, Target target);
}
