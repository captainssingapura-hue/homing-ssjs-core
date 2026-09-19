package hue.captains.singapura.js.homing.ui.split;

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
class SplitEventsTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/ui/split/SplitEventsModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
    }

    private Value eval(String src) { return js.eval("js", src); }

    private static List<Class<?>> records() { return Arrays.asList(SplitEvent.class.getPermittedSubclasses()); }

    private static String sample(RecordComponent c) {
        if (c.getType() == String.class) return "'0/1'";
        if (c.getType() == List.class) return "[0.25, 0.75]";
        throw new AssertionError("no sample for " + c);
    }

    @Test
    void everyRecordIsAFactoryWhoseObjectHasKindThenTheComponentsInOrder() {
        for (Class<?> r : records()) {
            String kind = r.getSimpleName();
            assertEquals("function", eval("typeof SplitEvents." + kind).asString(), kind + " has no JS factory");
            var args = new ArrayList<String>();
            var expected = new ArrayList<String>();
            expected.add("kind");
            for (RecordComponent c : r.getRecordComponents()) { args.add(sample(c)); expected.add(c.getName()); }
            String call = "SplitEvents." + kind + "(" + String.join(", ", args) + ")";
            assertEquals(expected.toString(), eval("JSON.stringify(Object.keys(" + call + "))").asString().replace("\"", "").replace(",", ", "), kind + " fields");
            assertEquals(kind, eval(call + ".kind").asString());
            assertTrue(eval("Object.isFrozen(" + call + ") && Object.isFrozen(" + call + ".ratios)").asBoolean(), kind + " is not frozen through");
        }
        assertEquals(records().stream().map(Class::getSimpleName).toList().toString(), eval("'[' + SplitEvents.KINDS.join(', ') + ']'").asString());
    }

    @Test
    void bothSidesRefuseTheSameBadArguments() {
        for (String bad : List.of("SplitEvents.RatioChanged('a', [0.5, 0.5])", "SplitEvents.RatioChanged('0/', [0.5, 0.5])", "SplitEvents.RatioChanged('', [1])",
                                  "SplitEvents.RatioChanged('', [0.5, 0.6])", "SplitEvents.RatioChanged('', [0, 1])", "SplitEvents.RatioChanged('', 'x')")) {
            var ex = assertThrows(PolyglotException.class, () -> eval(bad), bad);
            assertTrue(ex.getMessage().startsWith("Error: [SplitEvents] "), bad + " → " + ex.getMessage());
        }
        assertEquals("", eval("SplitEvents.RatioChanged('', [0.5, 0.5]).path").asString());
        assertThrows(IllegalArgumentException.class, () -> new SplitEvent.RatioChanged("a", List.of(0.5, 0.5)));
        assertThrows(IllegalArgumentException.class, () -> new SplitEvent.RatioChanged("", List.of(1.0)));
        assertThrows(IllegalArgumentException.class, () -> new SplitEvent.RatioChanged("", List.of(0.5, 0.6)));
        assertThrows(IllegalArgumentException.class, () -> new SplitEvent.RatioChanged("", List.of(0.0, 1.0)));
        assertEquals("RatioChanged", new SplitEvent.RatioChanged("0/1", List.of(0.25, 0.75)).kind());
    }
}
