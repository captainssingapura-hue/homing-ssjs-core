package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * A component's or a part's own level for one of its states, in place of the state's intrinsic one:
 * {@code new ImportanceOverride<>(Selected.INSTANCE, Importance.Primary.class)}. The level is one of the six by type -
 * only a level names itself as its argument - and the state one the leaf lists, which the reader holds.
 *
 * @param state the state
 * @param level its level for this leaf
 * @param <L>   the level
 */
public record ImportanceOverride<L extends Importance<L>>(StateType<?> state, Class<L> level) implements ValueObject {

    public ImportanceOverride {
        Objects.requireNonNull(state, "ImportanceOverride.state");
        Objects.requireNonNull(level, "ImportanceOverride.level");
    }
}
