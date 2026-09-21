package hue.captains.singapura.js.homing.ui.icons;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Icon component over a fake element: the classes it wears and takes off, the names it refuses, the table it reads. */
class IconTest extends JsModuleTestBase {

    private static final String P = "/homing/js/hue/captains/singapura/js/homing/ui/icons/";

    private static final String SHIM = """
        function el() {
            var classes = new Set(), attrs = {};
            return { classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); },
                                  remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); } },
                     setAttribute: function (k, v) { attrs[k] = String(v); }, removeAttribute: function (k) { delete attrs[k]; },
                     has: function (c) { return classes.has(c); }, attr: function (k) { return attrs[k]; }, classes: function () { return Array.from(classes).sort().join(' '); } };
        }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); },
                    removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); } };
        var ic_base = "ic_base";
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval("js", SHIM);
        // the wardrobe's class names, as the served module would import them, then the generated table itself
        for (var w : IconStyles.worded()) js.eval("js", "var " + w.getClass().getSimpleName() + " = '" + w.getClass().getSimpleName() + "';");
        js.eval("js", String.join("\n", IconsModule.INSTANCE.selfContent(null)));
        loadModule(P + "IconModule.js");
    }

    private Value eval(String src) { return js.eval("js", src); }

    @Test
    void itWearsTheBaseAndTheWordsClass_setChangesTheWord_clearLeavesTheBase() {
        eval("var e = el(); var icon = new Icon(e, { name: 'check' });");
        assertEquals("ic_base ic_check", eval("e.classes()").asString());
        assertEquals("true", eval("e.attr('aria-hidden')").asString(), "a mark is not a label");
        assertEquals("check", eval("icon.name()").asString());
        eval("icon.set('rotate');");
        assertEquals("ic_base ic_rotate", eval("e.classes()").asString(), "the old word off, the new on");
        eval("icon.set('rotate');");
        assertEquals("ic_base ic_rotate", eval("e.classes()").asString(), "the same word again: nothing changes");
        eval("icon.clear();");
        assertEquals("ic_base", eval("e.classes()").asString(), "blank at its width");
        assertTrue(eval("icon.name() === null").asBoolean());
        eval("new Icon(el())");
        eval("icon.set('pin'); icon.dispose();");
        assertEquals("", eval("e.classes()").asString(), "disposed: nothing of the icon's left on the caller's element");
        assertTrue(eval("e.attr('aria-hidden') === undefined").asBoolean());
    }

    @Test
    void aWordTheVocabularyLacksIsRefused_andTheTableIsTheVocabulary() {
        var ex = assertThrows(PolyglotException.class, () -> eval("new Icon(el(), { name: 'sparkle' })"));
        assertTrue(ex.getMessage().contains("no icon word 'sparkle'"), ex.getMessage());
        assertEquals(String.join(",", IconsModule.names()), eval("Icon.NAMES.join(',')").asString());
        assertTrue(eval("Icon.has('detach') && !Icon.has('toString')").asBoolean());
        assertFalse(eval("Icon.has('sparkle')").asBoolean());
        assertEquals("span", eval("Icon.TAG").asString());
        assertThrows(PolyglotException.class, () -> eval("new Icon(null)"));
    }
}
