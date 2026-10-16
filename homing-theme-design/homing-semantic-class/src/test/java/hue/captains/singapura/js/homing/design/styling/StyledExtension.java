package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Component;

import java.util.List;

/** A leaf a downstream adds: a badge, which every base layer must answer, from its own files (D26) - registered with the tests' components (StyledComponents), as an instance registers a downstream's module. */
public record StyledExtension() {

    /** A small mark on what it labels. */
    public record Badge() implements Component<StatedTaxonomy.Control> {
        public static final Badge INSTANCE = new Badge();
        @Override public StatedTaxonomy.Control parent() { return StatedTaxonomy.Control.INSTANCE; }
    }


}
