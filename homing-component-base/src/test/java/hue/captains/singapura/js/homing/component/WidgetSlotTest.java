package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The slot against a shimmed host and party: a widget is constructed once
 * and kept, shown by attaching its root and hidden by detaching it, told
 * setActive on the way in and out, and disposed before its branch goes.
 */
class WidgetSlotTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/component/WidgetSlot.js";

    // A host element that only knows its children, and a party branch that only
    // knows its children and whether it was dissolved.
    private static final String SHIM = """
        var log = [];
        var host = { children: [],
            appendChild: function (el) { this.children.push(el); el.parentNode = this; },
            removeChild: function (el) { this.children.splice(this.children.indexOf(el), 1); el.parentNode = null; } };
        function fakeBranch(name) {
            var kids = new Map();
            return { name: name, dissolved: false,
                createBranch: function (n) { var b = fakeBranch(n); kids.set(n, b); return b; },
                dissolveBranch: function (n) { kids.get(n).dissolved = true; kids.delete(n); },
                activate: function () {} };
        }
        var branch = fakeBranch("slot");
        var built = {};
        function widget(key) {
            return function (b, params, ctx) {
                built[key] = (built[key] || 0) + 1;
                var root = { tag: key, parentNode: null };
                return { root: root,
                         setActive: function (on) { log.push(key + ":" + (on ? "on" : "off")); },
                         dispose: function () { log.push(key + ":disposed"); } };
            };
        }
        var slot = createWidgetSlot({ branch: branch, host: host });
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval("js", "var console = { error: function () {} };");
        loadModule(MODULE);
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String log() { return eval("log.join(' ')").asString(); }
    private int shownCount() { return eval("host.children.length").asInt(); }

    @Test
    void showsByAttachingAndHidesByDetaching() {
        eval("slot.show('a', widget('a'))");
        assertEquals(1, shownCount());
        assertEquals("a", eval("host.children[0].tag").asString());
        assertEquals("a", eval("slot.current()").asString());
        assertEquals("a:on", log());

        eval("slot.hide()");
        assertEquals(0, shownCount());
        assertNull(eval("slot.current()").isNull() ? null : "x");
        assertEquals("a:on a:off", log());
        assertTrue(eval("slot.has('a')").asBoolean(), "hidden, not gone");
    }

    @Test
    void constructsOnceAndKeepsWhatItBuilt() {
        eval("slot.show('a', widget('a')); slot.show('b', widget('b')); slot.show('a', widget('a')); slot.show('a')");
        assertEquals(1, eval("built.a").asInt());
        assertEquals(1, eval("built.b").asInt());
        assertEquals(1, shownCount());
        assertEquals("a", eval("host.children[0].tag").asString());
        assertEquals("a:on a:off b:on b:off a:on", log());
        assertEquals(2, eval("slot.keys().length").asInt());
    }

    @Test
    void disposeTellsTheWidgetThenDissolvesItsBranch() {
        eval("slot.show('a', widget('a')); slot.show('b', widget('b')); var aBranch = null;");
        eval("slot.dispose('b')");
        assertEquals("a:on a:off b:on b:off b:disposed", log());
        assertFalse(eval("slot.has('b')").asBoolean());
        assertEquals(0, shownCount(), "the shown one was disposed, so nothing is shown");
        eval("slot.show('a')");
        assertEquals(1, shownCount());
        eval("slot.disposeAll()");
        assertEquals(0, eval("slot.keys().length").asInt());
        assertTrue(log().endsWith("a:off a:disposed"), log());
    }

    @Test
    void refusesAWidgetWithoutARoot() {
        var ex = eval("(function () { try { slot.show('x', function () { return {}; }); return null; } catch (e) { return e.message; } })()");
        assertTrue(ex.asString().contains("must return { root"), ex.asString());
    }
}
