package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The tree in JS: the same record Java stamps, walked, found, pathed, divided, and checked as Java checks. */
class MenuTreeTest extends JsModuleTestBase {

    private static final String P = "/homing/js/hue/captains/singapura/js/homing/ui/menu/";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(P + "MenuTreeModule.js");
        js.eval("js", """
            var T = { kind: "animal", nodes: [ { id: "rotate", label: "Rotate", icon: "rotate" }, { id: "flip", label: "Flip" },
                      { id: "animal", label: "Animal", section: 1, nodes: [ { id: "cat", label: "Cat" }, { id: "dog", label: "Dog", nodes: [ { id: "big", label: "Big" } ] } ] } ] };
            """);
    }

    @Test
    void rows_find_path_divided() {
        assertEquals("rotate,flip,animal,cat,dog,big", js.eval("js", "MenuTree.rows(T).map(function (n) { return n.id; }).join(',')").asString(), "parents before children");
        assertEquals("Big", js.eval("js", "MenuTree.find(T, 'big').label").asString());
        assertTrue(js.eval("js", "MenuTree.find(T, 'owl') === null").asBoolean());
        assertEquals("animal>dog>big", js.eval("js", "MenuTree.path(T, 'big').map(function (n) { return n.id; }).join('>')").asString());
        assertTrue(js.eval("js", "MenuTree.path(T, 'owl') === null").asBoolean());
        assertEquals("false,false,true", js.eval("js", "MenuTree.divided(T.nodes).map(function (d) { return d.divider; }).join(',')").asString(), "a divider where the section changes");
        assertEquals("3", js.eval("js", "String(MenuTree.MAX_DEPTH)").asString());
    }

    @Test
    void check_refusesWhatJavaRefuses() {
        assertTrue(js.eval("js", "MenuTree.check('animal', T.nodes) === T.nodes").asBoolean());
        var twice = assertThrows(PolyglotException.class, () -> js.eval("js", "MenuTree.check('k', [ { id: 'x', label: 'X' }, { id: 's', label: 'S', nodes: [ { id: 'x', label: 'again' } ] } ])"));
        assertTrue(twice.getMessage().contains("row id repeated: x"), twice.getMessage());
        var blank = assertThrows(PolyglotException.class, () -> js.eval("js", "MenuTree.check('k', [ { id: 'x', label: '' } ])"));
        assertTrue(blank.getMessage().contains("x has no label"), blank.getMessage());
        var none = assertThrows(PolyglotException.class, () -> js.eval("js", "MenuTree.check('k', [])"));
        assertTrue(none.getMessage().contains("lists no rows"), none.getMessage());
        var deep = assertThrows(PolyglotException.class, () -> js.eval("js", "MenuTree.check('k', [ { id: 'a', label: 'A', nodes: [ { id: 'b', label: 'B', nodes: [ { id: 'c', label: 'C', nodes: [ { id: 'd', label: 'D' } ] } ] } ] } ])"));
        assertTrue(deep.getMessage().contains("c is at the last level and lists 1"), deep.getMessage());
        js.eval("js", "MenuTree.check('k', [ { id: 'a', label: 'A', nodes: [ { id: 'b', label: 'B', nodes: [ { id: 'c', label: 'C', nodes: [] } ] } ] } ])");
        var icon = assertThrows(PolyglotException.class, () -> js.eval("js", "MenuTree.check('k', [ { id: 'a', label: 'A', icon: 3 } ])"));
        assertTrue(icon.getMessage().contains("not a word"), icon.getMessage());
    }
}
