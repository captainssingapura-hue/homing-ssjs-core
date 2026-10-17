package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * RFC 0067, appendix Two Classes per Leaf - how an element wears its leaf's style-aware classes: the leaf's class, on
 * an element wearing no other leaf's, a part's element its part's alone (D10); and a state's class while the state
 * holds, the state's others taken off (D12). A leaf's names are generated from Java and handed in as its description;
 * a wrong name is refused where it is written.
 */
public record StyledLeaves() implements EsModule<StyledLeaves> {

    public static final StyledLeaves INSTANCE = new StyledLeaves();

    /** {@code StyledLeaf.wear(el, leaf)}, {@code StyledLeaf.hold(el, leaf, state, value)}. */
    public record StyledLeaf() implements Exportable._Class<StyledLeaves> {}

    /** Every class through the css manager, the leaf's names minted as its handles - no class touched raw. */
    @Override
    public ImportsFor<StyledLeaves> imports() {
        return ImportsFor.<StyledLeaves>builder()
                .add(new ModuleImports<>(List.of(new CssClassManager.CssClassManagerInstance()), CssClassManager.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<StyledLeaves> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new StyledLeaf()));
    }
}
