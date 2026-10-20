package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A component's rooms (RFC 0067): elements of its own that are no parts, each named once, its name lower-case words; a
 * leaf may have rooms as a composite may - a room is no part.
 */
class RoomTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();

    /** A tray: a leaf with a room of its own. */
    record Tray() implements Leaf<Kinds.Shelf> {
        static final Tray INSTANCE = new Tray();
        @Override public Kinds.Shelf parent() { return Kinds.Shelf.INSTANCE; }
        @Override public List<Room> rooms() { return List.of(new Room("well")); }
    }

    /** A cabinet: a composite naming one room twice. */
    record Cabinet() implements Composite<Kinds.Shelf> {
        static final Cabinet INSTANCE = new Cabinet();
        @Override public Kinds.Shelf parent() { return Kinds.Shelf.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(ComponentPartDSL.INSTANCE.part(Kinds.Knob.INSTANCE).as(Kinds.Spot.INSTANCE).one()); }
        @Override public List<Room> rooms() { return List.of(new Room("body"), new Room("foot"), new Room("body")); }
    }

    @Test
    void aComponentsRooms_areRead_aLeafsAsACompositesAre() {
        assertEquals(List.of(new Room("well")), READ.read(List.of(Tray.INSTANCE)).components().getFirst().rooms());
    }

    @Test
    void aRoomNamedTwice_isRefused() {
        var e = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(Cabinet.INSTANCE)));
        assertEquals(List.of(Rule.ROOMS_BADLY), e.problems().stream().map(TaxonomyProblem::rule).toList(), e.getMessage());
    }

    @Test
    void aRoomsName_isLowerCaseWords() {
        assertEquals("slider-rack", new Room("slider-rack").name());
        assertThrows(IllegalArgumentException.class, () -> new Room("Body"));
        assertThrows(IllegalArgumentException.class, () -> new Room("the body"));
        assertThrows(IllegalArgumentException.class, () -> new Room(""));
    }
}
