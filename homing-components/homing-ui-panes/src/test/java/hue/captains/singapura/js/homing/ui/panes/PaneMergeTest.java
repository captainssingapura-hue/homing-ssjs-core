package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The whole merge decided before a tab moves: the order the tabs arrive in
 * when there is room for them, and the reason there is no merge when there
 * is not — a budget that will not hold them, or a name both docks use. The
 * point of planning first is that a merge cannot stop half way: the caller
 * refuses as a whole rather than stranding widgets between two docks, one
 * of which is about to go.
 */
class PaneMergeTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/ui/panes/PaneMergeModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
    }

    private Value eval(String src) { return js.eval("js", src); }

    @Test
    void theTabsArriveInTheOrderTheySat() {
        var plan = eval("PaneMerge.plan(['books', 'shelves'], ['plate'], 8)");
        assertTrue(plan.getMember("ok").asBoolean());
        assertEquals("books,shelves", eval("PaneMerge.plan(['books', 'shelves'], ['plate'], 8).ids.join(',')").asString());
        assertEquals("", eval("PaneMerge.plan([], ['plate'], 8).ids.join(',')").asString(), "an empty dock merges, and moves nothing");
        assertTrue(eval("PaneMerge.plan(['a'], ['b'], 2).ok").asBoolean(), "the budget filled exactly is room enough");
    }

    @Test
    void aBudgetThatWillNotHoldThemIsNoMerge() {
        var plan = eval("PaneMerge.plan(['a', 'b', 'c'], ['x', 'y'], 4)");
        assertFalse(plan.getMember("ok").asBoolean());
        assertEquals("budget", plan.getMember("reason").asString());
        assertEquals("it holds 2 of 4, so there is room for 2, not 3", plan.getMember("says").asString());
    }

    @Test
    void aNameBothDocksUseIsNoMerge() {
        var plan = eval("PaneMerge.plan(['notes', 'books'], ['plate', 'books'], 8)");
        assertFalse(plan.getMember("ok").asBoolean());
        assertEquals("twice", plan.getMember("reason").asString());
        assertTrue(plan.getMember("says").asString().contains("'books'"), plan.getMember("says").asString());
        assertFalse(eval("PaneMerge.plan(['a', 'a'], ['x'], 8).ok").asBoolean(), "a dock that holds a name twice is refused too");
    }
}
