package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/** The {@link TradingDesk}'s module extending the {@link Sketch} house: named in the tests' {@code META-INF/services}. */
public record TradingDeskExtension() implements TaxonomyExtension {

    @Override public Class<? extends TaxonomySource> extending() { return SketchSource.class; }

    @Override public List<Component<?>> components() { return List.of(TradingDesk.OrderTicket.INSTANCE); }

    @Override public List<Role<?>> roles() { return TradingDesk.CATALOGUE; }
}
