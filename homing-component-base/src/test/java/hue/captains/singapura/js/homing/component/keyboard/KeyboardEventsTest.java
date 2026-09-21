package hue.captains.singapura.js.homing.component.keyboard;

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
class KeyboardEventsTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeyboardEventsModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
    }

    private Value eval(String src) { return js.eval("js", src); }

    private static List<Class<?>> records() {
        return Arrays.asList(KeyboardEvent.class.getPermittedSubclasses());
    }

    private static String sample(RecordComponent c) {
        if (c.getType() == String.class) return "'" + c.getName() + "'";
        throw new AssertionError("no sample for " + c);
    }

    @Test
    void everyRecordIsAFactoryWhoseObjectHasKindThenTheComponentsInOrder() {
        for (Class<?> r : records()) {
            String kind = r.getSimpleName();
            assertEquals("function", eval("typeof KeyboardEvents." + kind).asString(), kind + " has no JS factory");
            var args = new ArrayList<String>();
            var expected = new ArrayList<String>();
            expected.add("kind");
            for (RecordComponent c : r.getRecordComponents()) { args.add(sample(c)); expected.add(c.getName()); }
            String call = "KeyboardEvents." + kind + "(" + String.join(", ", args) + ")";
            assertEquals(expected.toString(), eval("JSON.stringify(Object.keys(" + call + "))").asString().replace("\"", "").replace(",", ", "), kind + " fields");
            assertEquals(kind, eval(call + ".kind").asString());
            assertTrue(eval("Object.isFrozen(" + call + ")").asBoolean(), kind + " is not frozen");
        }
    }

    @Test
    void theKindsAreThePermittedSubclassesInOrder() {
        var java = records().stream().map(Class::getSimpleName).toList();
        assertEquals(java.toString(), eval("'[' + KeyboardEvents.KINDS.join(', ') + ']'").asString());
        assertEquals(3, java.size());
    }

    @Test
    void takenCarriesWhoTookOrNullForNoOne() {
        assertEquals("a|b", eval("var t = KeyboardEvents.Taken('a', 'b'); [t.id, t.by].join('|')").asString());
        assertTrue(eval("KeyboardEvents.Taken('a', null).by === null").asBoolean());
        assertTrue(eval("KeyboardEvents.Taken('a').by === null").asBoolean());
        assertEquals("a", eval("KeyboardEvents.Granted('a').id").asString());
        assertEquals("a", eval("KeyboardEvents.Released('a').id").asString());
    }

    @Test
    void bothSidesRefuseTheSameBadArguments() {
        for (String bad : List.of("KeyboardEvents.Granted('')", "KeyboardEvents.Granted(null)", "KeyboardEvents.Taken('', 'b')", "KeyboardEvents.Taken('a', '')", "KeyboardEvents.Released(3)")) {
            var ex = assertThrows(PolyglotException.class, () -> eval(bad), bad);
            assertTrue(ex.getMessage().startsWith("Error: [KeyboardEvents] "), bad + " -> " + ex.getMessage());
        }
        assertThrows(NullPointerException.class, () -> new KeyboardEvent.Granted(null));
        assertThrows(IllegalArgumentException.class, () -> new KeyboardEvent.Taken("", "b"));
        assertThrows(IllegalArgumentException.class, () -> new KeyboardEvent.Taken("a", ""));
        assertEquals("Taken", new KeyboardEvent.Taken("a", null).kind());
    }
}
