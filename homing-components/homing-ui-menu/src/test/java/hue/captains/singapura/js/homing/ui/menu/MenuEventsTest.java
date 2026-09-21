package hue.captains.singapura.js.homing.ui.menu;

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
 * list is the permitted subclasses in declaration order; the reasons are
 * the same set on both sides; both sides refuse the same bad arguments; the
 * objects are frozen.
 */
class MenuEventsTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/ui/menu/MenuEventsModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
    }

    private Value eval(String src) { return js.eval("js", src); }

    private static List<Class<?>> records() { return Arrays.asList(MenuEvent.class.getPermittedSubclasses()); }

    private static String sample(RecordComponent c) {
        if (c.getName().equals("reason")) return "'pick'";
        if (c.getType() == String.class) return "'" + c.getName() + "'";
        if (c.getType() == double.class) return "2.5";
        throw new AssertionError("no sample for " + c);
    }

    @Test
    void everyRecordIsAFactoryWhoseObjectHasKindThenTheComponentsInOrder() {
        for (Class<?> r : records()) {
            String kind = r.getSimpleName();
            assertTrue(eval("typeof MenuEvents." + kind).asString().equals("function"), kind + " has no JS factory");
            var args = new ArrayList<String>();
            var expected = new ArrayList<String>();
            expected.add("kind");
            for (RecordComponent c : r.getRecordComponents()) { args.add(sample(c)); expected.add(c.getName()); }
            String call = "MenuEvents." + kind + "(" + String.join(", ", args) + ")";
            assertEquals(expected.toString(), eval("JSON.stringify(Object.keys(" + call + "))").asString().replace("\"", "").replace(",", ", "), kind + " fields");
            assertEquals(kind, eval(call + ".kind").asString());
            assertTrue(eval("Object.isFrozen(" + call + ")").asBoolean(), kind + " is not frozen");
        }
    }

    @Test
    void theKindsAndTheReasonsAreTheSameOnBothSides() {
        var java = records().stream().map(Class::getSimpleName).toList();
        assertEquals(java.toString(), eval("'[' + MenuEvents.KINDS.join(', ') + ']'").asString());
        assertEquals(3, java.size());
        assertEquals(MenuEvent.REASONS.stream().sorted().toList().toString(), eval("'[' + MenuEvents.REASONS.slice().sort().join(', ') + ']'").asString());
    }

    @Test
    void bothSidesRefuseTheSameBadArguments() {
        for (String bad : List.of("MenuEvents.Opened('', 1, 2)", "MenuEvents.Opened('k', NaN, 2)", "MenuEvents.Picked('k', '')", "MenuEvents.Closed('k', 'gone')", "MenuEvents.Closed('', 'pick')")) {
            var ex = assertThrows(PolyglotException.class, () -> eval(bad), bad);
            assertTrue(ex.getMessage().startsWith("Error: [MenuEvents] "), bad + " → " + ex.getMessage());
        }
        assertThrows(IllegalArgumentException.class, () -> new MenuEvent.Opened("k", Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> new MenuEvent.Picked("k", ""));
        assertThrows(IllegalArgumentException.class, () -> new MenuEvent.Closed("k", "gone"));
        assertEquals("Closed", new MenuEvent.Closed("k", "outside").kind());
    }
}
