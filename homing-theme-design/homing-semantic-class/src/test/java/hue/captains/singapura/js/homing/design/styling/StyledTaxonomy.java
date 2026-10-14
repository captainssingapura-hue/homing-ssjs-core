package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ExtentAxis;
import hue.captains.singapura.js.homing.component.taxonomy.FamilyHead;
import hue.captains.singapura.js.homing.component.taxonomy.L1_ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.ReadTaxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyExtensions;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomySource;

import java.util.List;

/**
 * The leaves the style checks are written for, in two families: {@link StatedTaxonomy}'s controls - the tab and the
 * strip, the strip's lead tab a casting of its own - and whatever extends this source on the classpath: in the tests,
 * the {@link StyledExtension}'s badge; and the gauges, a meter on the colour axis.
 */
public record StyledTaxonomy() implements TaxonomySource {

    public static final StyledTaxonomy INSTANCE = new StyledTaxonomy();

    /** What reads out a measure: a family of its own. */
    public record Gauge() implements L1_ComponentBranch<Root>, FamilyHead {
        public static final Gauge INSTANCE = new Gauge();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    /** Danger at its full on the colour axis, safety turned the other way. */
    public record Meter() implements Component<Gauge> {
        public static final Meter INSTANCE = new Meter();
        @Override public Gauge parent() { return Gauge.INSTANCE; }
        @Override public List<ExtentAxis> extents() { return List.of(ExtentAxis.COLOUR); }
    }

    @Override public Taxonomy read() {
        return new ReadTaxonomy().read(TaxonomyExtensions.INSTANCE.components(StyledTaxonomy.class,
                List.of(StatedTaxonomy.Tab.INSTANCE, StatedTaxonomy.Strip.INSTANCE, Meter.INSTANCE)));
    }
}
