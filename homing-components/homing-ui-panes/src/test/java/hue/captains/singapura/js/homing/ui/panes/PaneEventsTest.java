package hue.captains.singapura.js.homing.ui.panes;

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
class PaneEventsTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/ui/panes/PaneEventsModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
        js.eval("js", "var tab = Object.freeze({ id: 't1', title: 'T', pinned: false, widget: {} });");
    }

    private Value eval(String src) { return js.eval("js", src); }

    private static List<Class<?>> records() {
        return Arrays.asList(PaneEvent.class.getPermittedSubclasses());
    }

    /** A sample argument for a component: the JS expression that stands for it. */
    private static String sample(RecordComponent c) {
        if (c.getType() == String.class) return "'" + c.getName() + "'";
        if (c.getType() == int.class) return "2";
        if (c.getType() == PaneEvent.Tab.class) return "tab";
        throw new AssertionError("no sample for " + c);
    }

    @Test
    void everyRecordIsAFactoryWhoseObjectHasKindThenTheComponentsInOrder() {
        for (Class<?> r : records()) {
            String kind = r.getSimpleName();
            assertTrue(eval("typeof PaneEvents." + kind).asString().equals("function"), kind + " has no JS factory");
            var args = new ArrayList<String>();
            var expected = new ArrayList<String>();
            expected.add("kind");
            for (RecordComponent c : r.getRecordComponents()) { args.add(sample(c)); expected.add(c.getName()); }
            String call = "PaneEvents." + kind + "(" + String.join(", ", args) + ")";
            assertEquals(expected.toString(), eval("JSON.stringify(Object.keys(" + call + "))").asString().replace("\"", "").replace(",", ", "), kind + " fields");
            assertEquals(kind, eval(call + ".kind").asString());
            assertTrue(eval("Object.isFrozen(" + call + ")").asBoolean(), kind + " is not frozen");
        }
    }

    @Test
    void theKindsAreThePermittedSubclassesInOrder() {
        var java = records().stream().map(Class::getSimpleName).toList();
        assertEquals(java.toString(), eval("'[' + PaneEvents.KINDS.join(', ') + ']'").asString());
        assertEquals(7, java.size());
    }

    @Test
    void theFieldsCarryWhatWasGiven() {
        assertEquals("s1|t1|0|s2|3", eval("var e = PaneEvents.TabMoved('s1', tab, 0, 's2', 3); [e.srcSlotId, e.tab.id, e.srcIndex, e.destSlotId, e.destIndex].join('|')").asString());
        assertEquals("main|t1", eval("var a = PaneEvents.TabActivated('main', 't1'); [a.slotId, a.tabId].join('|')").asString());
        assertTrue(eval("PaneEvents.TabAdded('main', tab, 0).tab === tab").asBoolean(), "the tab travels as the holder gave it");
    }

    @Test
    void bothSidesRefuseTheSameBadArguments() {
        for (String bad : List.of("PaneEvents.TabAdded('', tab, 0)", "PaneEvents.TabAdded('s', null, 0)", "PaneEvents.TabAdded('s', {}, 0)",
                                  "PaneEvents.TabAdded('s', tab, -1)", "PaneEvents.TabAdded('s', tab, 1.5)", "PaneEvents.TabActivated('s', '')",
                                  "PaneEvents.TabMoved('s', tab, 0, '', 0)", "PaneEvents.AddRequested(undefined)")) {
            var ex = assertThrows(PolyglotException.class, () -> eval(bad), bad);
            assertTrue(ex.getMessage().startsWith("Error: [PaneEvents] "), bad + " → " + ex.getMessage());
        }
        var tab = new PaneEvent.Tab("t1", "T", false);
        assertThrows(NullPointerException.class, () -> new PaneEvent.TabAdded(null, tab, 0));
        assertThrows(NullPointerException.class, () -> new PaneEvent.TabAdded("s", null, 0));
        assertThrows(IllegalArgumentException.class, () -> new PaneEvent.TabAdded("s", tab, -1));
        assertThrows(IllegalArgumentException.class, () -> new PaneEvent.TabMoved("s", tab, 0, "d", -1));
        assertThrows(IllegalArgumentException.class, () -> new PaneEvent.Tab("", "T", false));
        assertEquals("TabMoved", new PaneEvent.TabMoved("s", tab, 0, "d", 1).kind());
    }
}
