package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/** A module that extends some other taxonomy, on the same classpath: never read into the {@link Sketch} house. */
public record ElsewhereExtension() implements TaxonomyExtension {

    /** The taxonomy it extends, which is not the sketch. */
    record Elsewhere() implements TaxonomySource {
        @Override public Taxonomy read() { return new ReadTaxonomy().read(List.of()); }
    }

    @Override public Class<? extends TaxonomySource> extending() { return Elsewhere.class; }

    @Override public List<Component<?>> components() { return List.of(Sketch.Gauge.INSTANCE); }
}
