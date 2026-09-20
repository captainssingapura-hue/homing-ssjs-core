package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The drag's arithmetic, headless: the bar as slots at one pitch, the chip
 * kept within them and placed by its left, the slot it is nearest, and how
 * the others step aside.
 */
class TabDragTest extends JsModuleTestBase {

    private static final String P = "/homing/js/hue/captains/singapura/js/homing/ui/panes/";

    // five chips of 160 at a pitch of 160, the first pinned; the strip 34 tall with the chips on its bottom edge
    private static final String SHIM = """
        var S = [0, 1, 2, 3, 4].map(function (i) { return { left: 100 + 160 * i, top: 4, width: 160, height: 28 }; });
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(P + "TabDragModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }

    @Test
    void thePitchIsTheSlotsApart_orTheOneSlotsWidth() {
        assertEquals("160", eval("String(TabDrag.pitch(S))").asString());
        assertEquals("160", eval("String(TabDrag.pitch([S[0]]))").asString());
        assertEquals("0", eval("String(TabDrag.pitch([]))").asString());
    }

    @Test
    void theChipIsKeptWithinTheSlots_fromTheFirstUnpinned() {
        assertEquals("100,740,300", eval("[TabDrag.clamp(-50, S, 0), TabDrag.clamp(2000, S, 0), TabDrag.clamp(300, S, 0)].join(',')").asString());
        assertEquals("260", eval("String(TabDrag.clamp(150, S, 1))").asString(), "never before the pinned one");
    }

    @Test
    void theSlotNearestIsWhereItLands_theSwapAtHalfAPitch() {
        assertEquals("0,0,1,1,2,4,4", eval("[TabDrag.dest(100, S, 0), TabDrag.dest(179, S, 0), TabDrag.dest(180, S, 0), TabDrag.dest(300, S, 0), TabDrag.dest(420, S, 0), TabDrag.dest(740, S, 0), TabDrag.dest(9000, S, 0)].join(',')").asString());
        assertEquals("1", eval("String(TabDrag.dest(100, S, 1))").asString(), "the pinned slot is never a destination");
        assertEquals("2", eval("String(TabDrag.dest(500, [], 2))").asString(), "no slots: the floor");
    }

    @Test
    void theChipsBetweenStepAside_theRestStand() {
        // the one at 1 taken to 3: 2 and 3 step left, 0 and 4 stand; the mover itself is told how far it went
        assertEquals("0,2,-1,-1,0", eval("[0, 1, 2, 3, 4].map(function (j) { return TabDrag.shift(j, 1, 3); }).join(',')").asString());
        // the one at 3 taken to 1: 1 and 2 step right
        assertEquals("0,1,1,-2,0", eval("[0, 1, 2, 3, 4].map(function (j) { return TabDrag.shift(j, 3, 1); }).join(',')").asString());
        assertEquals("0,0,0,0,0", eval("[0, 1, 2, 3, 4].map(function (j) { return TabDrag.shift(j, 2, 2); }).join(',')").asString(), "back on its own slot, everyone stands");
    }
}
