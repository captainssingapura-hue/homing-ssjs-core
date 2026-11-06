package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.List;

/** Every node of the states' tree: the root, the branches, the types. */
public record StateNodes() implements StatelessFunctionalObject {

    public static final StateNodes INSTANCE = new StateNodes();

    /** Every node: the root, the branches, the types. */
    public List<StateNode> of(List<? extends StateBranch> branches, List<? extends StateType<?>> types) {
        var out = new ArrayList<StateNode>();
        out.add(StateRoot.INSTANCE);
        out.addAll(branches);
        out.addAll(types);
        return out;
    }
}
