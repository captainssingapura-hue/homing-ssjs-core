package hue.captains.singapura.js.homing.ui.floating;

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

/**
 * The two vocabularies held together: every record of the sealed sum is a
 * JS factory of the same name, every factory's object carries {@code kind}
 * and then the record's components, in order, and nothing else; the kinds
 * list is the permitted subclasses in declaration order; both sides refuse
 * the same bad arguments; the objects are frozen.
 */
class FloatEventsTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/ui/floating/FloatEventsModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
    }

    private Value eval(String src) { return js.eval("js", src); }

    private static List<Class<?>> records() {
        return Arrays.asList(FloatEvent.class.getPermittedSubclasses());
    }

    /** A sample argument for a component: the JS expression that stands for it. */
    private static String sample(RecordComponent c) {
        if (c.getType() == String.class) return "'" + c.getName() + "'";
        if (c.getType() == int.class) return "2";
        throw new AssertionError("no sample for " + c);
    }

    @Test
    void everyRecordIsAFactoryWhoseObjectHasKindThenTheComponentsInOrder() {
        for (Class<?> r : records()) {
            String kind = r.getSimpleName();
            assertTrue(eval("typeof FloatEvents." + kind).asString().equals("function"), kind + " has no JS factory");
            var args = new ArrayList<String>();
            var expected = new ArrayList<String>();
            expected.add("kind");
            for (RecordComponent c : r.getRecordComponents()) { args.add(sample(c)); expected.add(c.getName()); }
            String call = "FloatEvents." + kind + "(" + String.join(", ", args) + ")";
            assertEquals(expected.toString(), eval("JSON.stringify(Object.keys(" + call + "))").asString().replace("\"", "").replace(",", ", "), kind + " fields");
            assertEquals(kind, eval(call + ".kind").asString());
            assertTrue(eval("Object.isFrozen(" + call + ")").asBoolean(), kind + " is not frozen");
        }
    }

    @Test
    void theKindsAreThePermittedSubclassesInOrder() {
        var java = records().stream().map(Class::getSimpleName).toList();
        assertEquals(java.toString(), eval("'[' + FloatEvents.KINDS.join(', ') + ']'").asString());
        assertEquals(5, java.size());
    }

    @Test
    void theFieldsCarryWhatWasGiven() {
        assertEquals("p1|Notes|24|52|320|220", eval("var e = FloatEvents.Opened('p1', 'Notes', 24, 52, 320, 220); [e.id, e.title, e.x, e.y, e.w, e.h].join('|')").asString());
        assertEquals("p1|-10|0", eval("var m = FloatEvents.Moved('p1', -10, 0); [m.id, m.x, m.y].join('|')").asString());
        assertEquals("p1|200|100", eval("var r = FloatEvents.Resized('p1', 200, 100); [r.id, r.w, r.h].join('|')").asString());
    }

    @Test
    void bothSidesRefuseTheSameBadArguments() {
        for (String bad : List.of("FloatEvents.Opened('', 'T', 0, 0, 1, 1)", "FloatEvents.Opened('p', null, 0, 0, 1, 1)", "FloatEvents.Opened('p', 'T', 0.5, 0, 1, 1)",
                                  "FloatEvents.Opened('p', 'T', 0, 0, 0, 1)", "FloatEvents.Resized('p', 10, -1)", "FloatEvents.Moved('p', 1, '2')",
                                  "FloatEvents.Raised('')", "FloatEvents.Closed(undefined)")) {
            var ex = assertThrows(PolyglotException.class, () -> eval(bad), bad);
            assertTrue(ex.getMessage().startsWith("Error: [FloatEvents] "), bad + " → " + ex.getMessage());
        }
        assertThrows(NullPointerException.class, () -> new FloatEvent.Opened(null, "T", 0, 0, 1, 1));
        assertThrows(NullPointerException.class, () -> new FloatEvent.Opened("p", null, 0, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new FloatEvent.Opened("", "T", 0, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new FloatEvent.Opened("p", "T", 0, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new FloatEvent.Resized("p", 10, -1));
        assertThrows(IllegalArgumentException.class, () -> new FloatEvent.Raised(""));
        assertEquals("Moved", new FloatEvent.Moved("p", -10, 0).kind());
    }
}
