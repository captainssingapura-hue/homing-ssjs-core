package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The layouts' styling (owner, 21 Oct): a component the design alone sees, owning parts for their castings, with no
 * casting of its own and no part in anything; and a layout, apart from the taxonomy - its name, static or dynamic, its
 * one kind of sub-container.
 */
class LayoutStylingTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();
    private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;

    /** The styling of the shelf's layouts: it owns a knob in a spot, for its design. */
    record ShelfStyling() implements LayoutStyling<Kinds.Shelf> {
        static final ShelfStyling INSTANCE = new ShelfStyling();
        @Override public Kinds.Shelf parent() { return Kinds.Shelf.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Kinds.Knob.INSTANCE).as(Kinds.Spot.INSTANCE).any()); }
    }

    /** A crate that would hold the styling as a part. */
    record Crate() implements Composite<Kinds.Shelf> {
        static final Crate INSTANCE = new Crate();
        @Override public Kinds.Shelf parent() { return Kinds.Shelf.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(ShelfStyling.INSTANCE).as(Kinds.Spot.INSTANCE).one()); }
    }

    @Test
    void itsPartsAreCast_itNever() {
        Taxonomy t = READ.read(List.of(ShelfStyling.INSTANCE));
        var castings = t.castings().stream().map(Casting::token).toList();
        assertTrue(castings.contains(new Casting(Kinds.Knob.INSTANCE, Kinds.Spot.INSTANCE).token()), "the knob in its spot, cast for its design: " + castings);
        assertFalse(castings.contains(new Casting(ShelfStyling.INSTANCE).token()), "the styling has no casting of its own: " + castings);
    }

    @Test
    void itPlaysNoPart() {
        var e = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(Crate.INSTANCE)));
        assertTrue(e.problems().stream().map(TaxonomyProblem::rule).toList().contains(Rule.LAYOUT_STYLING_PLAYS_A_PART), e.getMessage());
    }

    @Test
    void aLayout_itsName_staticOrDynamic_itsOneSubContainer() {
        var grid = new Layout("split-grid", Cardinality.DYNAMIC, Kinds.Knob.INSTANCE);
        assertTrue(grid.dynamic());
        assertFalse(new Layout("row", Cardinality.Static.INSTANCE, Kinds.Knob.INSTANCE).dynamic());
        assertThrows(IllegalArgumentException.class, () -> new Layout("Split Grid", Cardinality.DYNAMIC, Kinds.Knob.INSTANCE));
        assertThrows(NullPointerException.class, () -> new Layout("row", null, Kinds.Knob.INSTANCE));
        assertThrows(IllegalArgumentException.class, () -> new Layout("row", Cardinality.Static.INSTANCE, ShelfStyling.INSTANCE), "the styling is no sub-container");
        assertEquals("split-grid", grid.token());
    }
}
