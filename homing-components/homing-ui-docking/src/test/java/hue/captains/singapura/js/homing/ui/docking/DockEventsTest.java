package hue.captains.singapura.js.homing.ui.docking;

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
class DockEventsTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/ui/docking/DockEventsModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
    }

    private Value eval(String src) { return js.eval("js", src); }

    private static List<Class<?>> records() {
        return Arrays.asList(DockEvent.class.getPermittedSubclasses());
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
            assertTrue(eval("typeof DockEvents." + kind).asString().equals("function"), kind + " has no JS factory");
            var args = new ArrayList<String>();
            var expected = new ArrayList<String>();
            expected.add("kind");
            for (RecordComponent c : r.getRecordComponents()) { args.add(sample(c)); expected.add(c.getName()); }
            String call = "DockEvents." + kind + "(" + String.join(", ", args) + ")";
            assertEquals(expected.toString(), eval("JSON.stringify(Object.keys(" + call + "))").asString().replace("\"", "").replace(",", ", "), kind + " fields");
            assertEquals(kind, eval(call + ".kind").asString());
            assertTrue(eval("Object.isFrozen(" + call + ")").asBoolean(), kind + " is not frozen");
        }
    }

    @Test
    void theKindsAreThePermittedSubclassesInOrder() {
        var java = records().stream().map(Class::getSimpleName).toList();
        assertEquals(java.toString(), eval("'[' + DockEvents.KINDS.join(', ') + ']'").asString());
        assertEquals(2, java.size());
    }

    @Test
    void theFieldsCarryWhatWasGiven() {
        assertEquals("t1|main|2", eval("var d = DockEvents.Docked('t1', 'main', 2); [d.tabId, d.slotId, d.index].join('|')").asString());
        assertEquals("t1|main", eval("var u = DockEvents.Undocked('t1', 'main'); [u.tabId, u.slotId].join('|')").asString());
    }

    @Test
    void bothSidesRefuseTheSameBadArguments() {
        for (String bad : List.of("DockEvents.Docked('', 's', 0)", "DockEvents.Docked('t', null, 0)", "DockEvents.Docked('t', 's', -1)", "DockEvents.Docked('t', 's', 1.5)", "DockEvents.Undocked('t', '')")) {
            var ex = assertThrows(PolyglotException.class, () -> eval(bad), bad);
            assertTrue(ex.getMessage().startsWith("Error: [DockEvents] "), bad + " → " + ex.getMessage());
        }
        assertThrows(NullPointerException.class, () -> new DockEvent.Docked(null, "s", 0));
        assertThrows(IllegalArgumentException.class, () -> new DockEvent.Docked("t", "s", -1));
        assertThrows(IllegalArgumentException.class, () -> new DockEvent.Undocked("t", ""));
        assertEquals("Undocked", new DockEvent.Undocked("t", "s").kind());
    }
}
