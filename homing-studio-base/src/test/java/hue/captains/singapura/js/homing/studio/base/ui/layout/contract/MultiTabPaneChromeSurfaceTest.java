package hue.captains.singapura.js.homing.studio.base.ui.layout.contract;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The chrome bar belongs to its pane — RFC 0052, the pointer axis widened.
 *
 * <p>Two different widths, on purpose:</p>
 *
 * <ul>
 *   <li><b>Hover</b> asks where the pointer <i>is</i>, so the whole leaf counts.
 *       The strip and corner used to report {@code null}, which did not merely
 *       fail to register — it <b>revoked</b> liveness, re-inerting a pane whose
 *       tab bar the pointer was resting on.</li>
 *   <li><b>Press</b> is a deliberate entry, so only the chrome's <i>background</i>
 *       counts. Every control on it keeps its own meaning.</li>
 * </ul>
 *
 * <p>The exclusion is by <b>identity</b> rather than descent, and it cannot be
 * left to each control's {@code stopPropagation}: {@code _onPanePress} is a
 * capture listener on the container, so it runs first regardless. These tests
 * pin that, because the failure mode is silent — a {@code ×} that enters the
 * pane it is closing, or a chip firing entry twice.</p>
 *
 * <p>Driven against element stubs rather than a DOM: the unit under test is a
 * pure target→slot classification, and a DOM engine would add nothing but a
 * dependency.</p>
 */
class MultiTabPaneChromeSurfaceTest {

    private Context js;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js")
                .allowAllAccess(false)
                .option("js.ecmascript-version", "2022")
                .build();
        js.eval("js", readJs("/homing/js/hue/captains/singapura/js/homing/studio/base/ui/layout/SplitPaneModule.js"));
        js.eval("js", readJs("/homing/js/hue/captains/singapura/js/homing/studio/base/ui/layout/MultiTabPaneDragModule.js"));
        js.eval("js", readJs("/homing/js/hue/captains/singapura/js/homing/studio/base/ui/layout/MultiTabPaneModule.js"));
        js.eval("js", HARNESS);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    /**
     * A leaf's worth of element stubs. Each knows its class and its ancestors,
     * which is all the classification actually reads.
     */
    private static final String HARNESS = """
        function el(cls, parent) {
            var e = {
                _cls: cls, _parent: parent || null,
                classList: { contains: function (c) { return e._cls === c || e._cls.split(' ').indexOf(c) >= 0; } },
                closest: function (sel) {
                    var want = sel.replace('.', '');
                    for (var n = e; n; n = n._parent) if (n.classList.contains(want)) return n;
                    return null;
                }
            };
            return e;
        }
        var leaf    = el('hsp-leaf hmtp-leaf');
        var strip   = el('hmtp-strip',  leaf);
        var content = el('hmtp-content', leaf);
        var corner  = el('hmtp-corner', leaf);
        var chip    = el('hmtp-chip',   strip);
        var close   = el('hmtp-chip-close', chip);
        var addBtn  = el('hmtp-strip-add',  strip);
        var pill    = el('hmtp-pill',   strip);
        var cbtn    = el('hmtp-cbtn',   corner);
        var widget  = el('some-widget', content);
        var outside = el('st-header');

        // Enough of an MTP to answer the classification questions.
        var mtp = Object.create(MultiTabPane.prototype);
        mtp._leafBySlot = new Map([['s1', leaf]]);

        function pressSlot(t) {
            var slot = mtp._slotOfContentTarget(t);
            if (slot == null && mtp._isChromeSurface(t)) slot = mtp._slotOfLeafTarget(t);
            return slot;
        }
        function hoverSlot(t) { return mtp._slotOfLeafTarget(t); }
        """;

    // ── Hover: the whole leaf is one place ───────────────────────────────────

    @Test
    void hoverOnAnyPartOfTheLeafReportsThePane() {
        for (String target : new String[]{"content", "widget", "strip", "corner", "chip", "pill", "cbtn", "addBtn"}) {
            assertEquals("s1", js.eval("js", "hoverSlot(" + target + ")").asString(),
                    "hover on " + target + " must report the pane it sits on");
        }
    }

    @Test
    void hoverOutsideAnyLeafReportsNothing() {
        assertTrue(js.eval("js", "hoverSlot(outside) === null").asBoolean());
    }

    // ── Press: the background enters, the controls do not ────────────────────

    @Test
    void pressOnTheChromeBackgroundEntersThePane() {
        for (String target : new String[]{"strip", "corner", "pill"}) {
            assertEquals("s1", js.eval("js", "pressSlot(" + target + ")").asString(),
                    target + " is the chrome's background — pressing it enters the pane");
        }
    }

    @Test
    void pressOnThePaneBodyStillEnters() {
        assertEquals("s1", js.eval("js", "pressSlot(content)").asString());
        assertEquals("s1", js.eval("js", "pressSlot(widget)").asString());
    }

    @Test
    void pressOnAChromeControlDoesNotEnter() {
        // A chip enters through chip-click (which also switches the tab); the
        // × is closing the tab; the + opens the picker, which claims focus for
        // itself; split/merge are their own gestures. Entering here would be a
        // second, competing decision — and for the chip, a double-fire.
        for (String target : new String[]{"chip", "close", "addBtn", "cbtn"}) {
            assertTrue(js.eval("js", "pressSlot(" + target + ") === null").asBoolean(),
                    target + " keeps its own meaning — it must not enter the pane");
        }
    }

    @Test
    void pressOutsideAnyLeafReportsNothing() {
        assertTrue(js.eval("js", "pressSlot(outside) === null").asBoolean());
    }

    private String readJs(String path) {
        try (var in = getClass().getResourceAsStream(path)) {
            assertNotNull(in, "missing classpath resource: " + path);
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + path, e);
        }
    }
}
