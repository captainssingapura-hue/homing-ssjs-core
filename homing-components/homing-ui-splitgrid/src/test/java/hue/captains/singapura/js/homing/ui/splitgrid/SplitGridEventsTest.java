package hue.captains.singapura.js.homing.ui.splitgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The two vocabularies held together, as the panes' are: records ↔ factories, kind then components, both sides refusing the same. */
class SplitGridEventsTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/ui/splitgrid/SplitGridEventsModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
    }

    private Value eval(String src) { return js.eval("js", src); }

    private static List<Class<?>> records() { return Arrays.asList(SplitGridEvent.class.getPermittedSubclasses()); }

    private static String sample(RecordComponent c) {
        if (c.getName().equals("side")) return "'right'";
        if (c.getName().equals("path")) return "'0/1'";
        if (c.getType() == String.class) return "'" + c.getName() + "'";
        if (c.getType() == List.class) return "[0.25, 0.75]";
        throw new AssertionError("no sample for " + c);
    }

    @Test
    void everyRecordIsAFactoryWhoseObjectHasKindThenTheComponentsInOrder() {
        for (Class<?> r : records()) {
            String kind = r.getSimpleName();
            assertEquals("function", eval("typeof SplitGridEvents." + kind).asString(), kind + " has no JS factory");
            var args = new ArrayList<String>();
            var expected = new ArrayList<String>();
            expected.add("kind");
            for (RecordComponent c : r.getRecordComponents()) { args.add(sample(c)); expected.add(c.getName()); }
            String call = "SplitGridEvents." + kind + "(" + String.join(", ", args) + ")";
            assertEquals(expected.toString(), eval("JSON.stringify(Object.keys(" + call + "))").asString().replace("\"", "").replace(",", ", "), kind + " fields");
            assertEquals(kind, eval(call + ".kind").asString());
            assertTrue(eval("Object.isFrozen(" + call + ") && (" + call + ".ratios === undefined || Object.isFrozen(" + call + ".ratios))").asBoolean(), kind + " is not frozen through");
        }
        assertEquals(records().stream().map(Class::getSimpleName).toList().toString(), eval("'[' + SplitGridEvents.KINDS.join(', ') + ']'").asString());
    }

    @Test
    void bothSidesRefuseTheSameBadArguments() {
        for (String bad : List.of("SplitGridEvents.TracksChanged('a', [0.5, 0.5])", "SplitGridEvents.TracksChanged('0/', [0.5, 0.5])", "SplitGridEvents.TracksChanged('', [1])",
                                  "SplitGridEvents.TracksChanged('', [0.5, 0.6])", "SplitGridEvents.TracksChanged('', [0, 1])", "SplitGridEvents.TracksChanged('', 'x')")) {
            var ex = assertThrows(PolyglotException.class, () -> eval(bad), bad);
            assertTrue(ex.getMessage().startsWith("Error: [SplitGridEvents] "), bad + " → " + ex.getMessage());
        }
        assertEquals("", eval("SplitGridEvents.TracksChanged('', [0.5, 0.5]).path").asString());
        assertThrows(IllegalArgumentException.class, () -> new SplitGridEvent.TracksChanged("a", List.of(0.5, 0.5)));
        assertThrows(IllegalArgumentException.class, () -> new SplitGridEvent.TracksChanged("", List.of(1.0)));
        assertThrows(IllegalArgumentException.class, () -> new SplitGridEvent.TracksChanged("", List.of(0.5, 0.6)));
        assertThrows(IllegalArgumentException.class, () -> new SplitGridEvent.TracksChanged("", List.of(0.0, 1.0)));
        assertEquals("TracksChanged", new SplitGridEvent.TracksChanged("0/1", List.of(0.25, 0.75)).kind());
        for (String bad : List.of("SplitGridEvents.Subdivided('', 'b', 'left')", "SplitGridEvents.Subdivided('a', 'b', 'up')", "SplitGridEvents.Removed(undefined)")) {
            var ex = assertThrows(PolyglotException.class, () -> eval(bad), bad);
            assertTrue(ex.getMessage().startsWith("Error: [SplitGridEvents] "), bad + " → " + ex.getMessage());
        }
        assertThrows(IllegalArgumentException.class, () -> new SplitGridEvent.Subdivided("a", "b", "up"));
        assertThrows(IllegalArgumentException.class, () -> new SplitGridEvent.Removed(""));
        assertEquals("a|b|top", eval("var s = SplitGridEvents.Subdivided('a', 'b', 'top'); [s.cellId, s.newCellId, s.side].join('|')").asString());
    }
}
