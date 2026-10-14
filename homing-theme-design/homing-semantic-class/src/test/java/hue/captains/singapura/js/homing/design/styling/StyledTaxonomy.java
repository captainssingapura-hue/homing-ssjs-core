package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ExtentAxis;
import hue.captains.singapura.js.homing.component.taxonomy.ReadTaxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyExtensions;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomySource;

import java.util.List;

/**
 * The leaves the style checks are written for: {@link StatedTaxonomy}'s tab and strip - the strip's lead tab a part of
 * its own - a meter on the colour axis, and whatever extends this source on the classpath: in the tests, the
 * {@link StyledExtension}'s badge.
 */
public record StyledTaxonomy() implements TaxonomySource {

    public static final StyledTaxonomy INSTANCE = new StyledTaxonomy();

    /** Danger at its full on the colour axis, safety turned the other way. */
    public record Meter() implements Component<StatedTaxonomy.Control> {
        public static final Meter INSTANCE = new Meter();
        @Override public StatedTaxonomy.Control parent() { return StatedTaxonomy.Control.INSTANCE; }
        @Override public List<ExtentAxis> extents() { return List.of(ExtentAxis.COLOUR); }
    }

    @Override public Taxonomy read() {
        return new ReadTaxonomy().read(TaxonomyExtensions.INSTANCE.components(StyledTaxonomy.class,
                List.of(StatedTaxonomy.Tab.INSTANCE, StatedTaxonomy.Strip.INSTANCE, Meter.INSTANCE)));
    }
}
