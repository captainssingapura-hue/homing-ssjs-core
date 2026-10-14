package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A leaf of the taxonomy as a design sees it (RFC 0067, appendix Two Classes per Leaf): a {@link Component}, or a
 * {@link Part} at its role - each a top-level thing in state and in design, independent of the others. A part is the
 * slot an owner declares, not the component that plays it: it lists its own states and its own overrides, and keeping
 * its state the same as another's, where that is wanted, is the application's business. A leaf's token is its own -
 * {@code plain-button}, {@code dialog-go} - and unique across both.
 */
public sealed interface ComponentLeaf extends ComponentNode permits Component, Part {

    /** Its states, in the order it lists them: the order that breaks a tie of importance. */
    List<StateType<?>> states();

    /** Its own levels for some of its states, in place of theirs; none by default. */
    List<ImportanceOverride<?>> importance();
}
