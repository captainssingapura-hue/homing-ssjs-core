package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * The {@link Sketch} house as a repository serves it: its own leaves and catalogue, then those of
 * every module on the classpath that extends it - in the tests, the {@link TradingDeskExtension}.
 */
record SketchSource() implements TaxonomySource {

    static final SketchSource INSTANCE = new SketchSource();

    @Override public Taxonomy read() {
        var extensions = TaxonomyExtensions.INSTANCE;
        return new ReadTaxonomy().read(extensions.components(SketchSource.class, Sketch.DECLARED),
                                       extensions.roles(SketchSource.class, Sketch.CATALOGUE),
                                       extensions.states(SketchSource.class, List.of()),
                                       extensions.events(SketchSource.class, List.of()));
    }
}
