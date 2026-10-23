package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.FixedValue.Element;
import hue.captains.singapura.js.homing.component.taxonomy.FixedValue.Group;
import hue.captains.singapura.js.homing.component.taxonomy.FixedValue.OneOf;
import hue.captains.singapura.js.homing.component.taxonomy.FixedValue.Text;
import hue.captains.singapura.js.homing.component.taxonomy.FixedValue.Whole;
import hue.captains.singapura.js.homing.component.taxonomy.FixedValue.YesNo;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A component's values, immutable for its life (RFC 0071 port, owner 23 Oct): each named as a field, typed, with its
 * default - none only where it may be none; groups nest them, each name once in its group, and once among the
 * component's own.
 */
class FixedValueTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();

    /** A tray: a leaf with values of its own - its least size, grouped, and where it is drawn. */
    record Tray() implements Leaf<Kinds.Shelf> {
        static final Tray INSTANCE = new Tray();
        @Override public Kinds.Shelf parent() { return Kinds.Shelf.INSTANCE; }
        @Override public List<FixedValue> fixedValues() {
            return List.of(new Group("least", List.of(new Whole("w", 160, false), new Whole("h", 96, false))), new Element("desk"));
        }
    }

    /** A cabinet: a leaf naming a value twice. */
    record Cabinet() implements Leaf<Kinds.Shelf> {
        static final Cabinet INSTANCE = new Cabinet();
        @Override public Kinds.Shelf parent() { return Kinds.Shelf.INSTANCE; }
        @Override public List<FixedValue> fixedValues() { return List.of(new YesNo("open", false, false), new Text("open", "", false)); }
    }

    @Test
    void aComponentsValues_areRead() {
        var values = READ.read(List.of(Tray.INSTANCE)).components().getFirst().fixedValues();
        assertEquals(List.of("least", "desk"), values.stream().map(FixedValue::name).toList());
        assertTrue(values.get(1).orNone() && !values.get(0).orNone(), "an element is none by default, a group never none");
    }

    @Test
    void aValueNamedTwice_isRefused() {
        var e = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(Cabinet.INSTANCE)));
        assertEquals(List.of(Rule.VALUES_BADLY), e.problems().stream().map(TaxonomyProblem::rule).toList(), e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> new Group("size", List.of(new Whole("w", 1, false), new Whole("w", 2, false))), "twice in a group");
        assertThrows(IllegalArgumentException.class, () -> new Group("size", List.of()), "a group of none");
    }

    @Test
    void aValuesName_isAFields() {
        assertEquals("leastWidth", new Whole("leastWidth", 0, false).name());
        assertThrows(IllegalArgumentException.class, () -> new Whole("Least", 0, false));
        assertThrows(IllegalArgumentException.class, () -> new Whole("least-w", 0, false));
        assertThrows(IllegalArgumentException.class, () -> new Element(""));
    }

    @Test
    void itsDefault_isOfItsType_noneOnlyWhereItMayBeNone() {
        assertEquals(null, new Whole("most", null, true).byDefault());
        assertThrows(IllegalArgumentException.class, () -> new Whole("w", null, false), "none, and may not be none");
        assertThrows(IllegalArgumentException.class, () -> new Whole("w", -1, false), "below 0");
        assertThrows(IllegalArgumentException.class, () -> new YesNo("open", null, false));
        assertThrows(IllegalArgumentException.class, () -> new Text("title", null, false));
        assertEquals("left", new OneOf("side", List.of("left", "right"), "left", false).byDefault());
        assertThrows(IllegalArgumentException.class, () -> new OneOf("side", List.of("left", "right"), "up", false), "not one of its names");
        assertThrows(IllegalArgumentException.class, () -> new OneOf("side", List.of("left", "left"), "left", false), "a name twice");
        assertThrows(IllegalArgumentException.class, () -> new OneOf("side", List.of("Left"), "Left", false), "no name's words");
        assertThrows(IllegalArgumentException.class, () -> new OneOf("side", List.of(), null, true), "one of none");
    }
}
