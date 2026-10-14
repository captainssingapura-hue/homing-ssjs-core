package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyExtension;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomySource;

import java.util.List;

/** A module that adds a leaf: a badge, which every base layer must answer, from this module's own files (D26). */
public record StyledExtension() implements TaxonomyExtension {

    /** A small mark on what it labels. */
    public record Badge() implements Component<StatedTaxonomy.Control> {
        public static final Badge INSTANCE = new Badge();
        @Override public StatedTaxonomy.Control parent() { return StatedTaxonomy.Control.INSTANCE; }
    }

    @Override public Class<? extends TaxonomySource> extending() { return StyledTaxonomy.class; }

    @Override public List<Component<?>> components() { return List.of(Badge.INSTANCE); }
}
