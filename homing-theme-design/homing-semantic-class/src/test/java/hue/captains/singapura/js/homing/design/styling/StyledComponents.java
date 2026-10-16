package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;

import java.util.List;

/** The tests' components as one module (D50): the tab, the strip (a tab cast as its lead), the meter and the badge. */
public record StyledComponents() implements ComponentModule {

    public static final StyledComponents INSTANCE = new StyledComponents();

    @Override public List<? extends Component<?>> components() {
        return List.of(StatedTaxonomy.Tab.INSTANCE, StatedTaxonomy.Strip.INSTANCE, StyledTaxonomy.Meter.INSTANCE, StyledExtension.Badge.INSTANCE);
    }
}
