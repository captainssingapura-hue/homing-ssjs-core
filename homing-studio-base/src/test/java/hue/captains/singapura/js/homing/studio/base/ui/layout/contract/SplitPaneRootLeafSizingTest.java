package hue.captains.singapura.js.homing.studio.base.ui.layout.contract;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A layout that is a single LEAF must still fill its container.
 *
 * <p>{@code .hsp-leaf} is {@code position:absolute} with no offsets of its own,
 * so a leaf is only ever as big as the box {@code _applySizes} writes onto it.
 * That method used to open with {@code if (_isLeaf(node)) return;} — above the
 * block that fills the root — so a root that <i>was</i> a leaf got no box at all
 * and collapsed to the width of its content. A workspace whose layout is one
 * empty pane rendered as a narrow column against an empty expanse.</p>
 *
 * <p>It stayed hidden while MultiTabPane's default layout was a 2×2, because the
 * root was then always a split. RFC 0060 made a single pane the default, and
 * every workspace that declares no arrangement takes this path.</p>
 *
 * <p>Two ways in, both pinned below: <b>boot</b> with a single-leaf layout, and
 * <b>merge</b> down to one pane, where the survivor would otherwise keep the box
 * it had while it was a child.</p>
 */
class SplitPaneRootLeafSizingTest {

    private Context js;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js")
                .allowAllAccess(false)
                .option("js.ecmascript-version", "2022")
                .build();
        js.eval("js", DOM);
        js.eval("js", readJs(
                "/homing/js/hue/captains/singapura/js/homing/studio/base/ui/layout/SplitPaneModule.js"));
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    @Test
    void aRootLeafFillsTheContainerAtBoot() {
        Value root = js.eval("js", """
                (() => {
                    const container = newEl('div');
                    container._w = 1200; container._h = 800;
                    const sp = new SplitPane({
                        container:  container,
                        layout:     { kind: 'leaf', slotId: 'main' },
                        renderSlot: function () {}
                    });
                    const s = sp._rootEl.style;
                    return { position: s.position || "(unset)", left: s.left || "(unset)",
                             top: s.top || "(unset)", width: s.width || "(unset)",
                             height: s.height || "(unset)" };
                })()""");

        assertEquals("1200px", root.getMember("width").asString(),
                     "a single-pane layout must take the container's full width — this is the "
                   + "bug: it rendered as a narrow column shrunk to its content");
        assertEquals("800px", root.getMember("height").asString());
        assertEquals("absolute", root.getMember("position").asString());
        assertEquals("0px", root.getMember("left").asString());
        assertEquals("0px", root.getMember("top").asString());
    }

    @Test
    void aSplitRootStillFillsTheContainer() {
        Value root = js.eval("js", """
                (() => {
                    const container = newEl('div');
                    container._w = 1000; container._h = 600;
                    const sp = new SplitPane({
                        container:  container,
                        layout:     { kind: 'split', orientation: 'horizontal', children: [
                            { pane: { kind: 'leaf', slotId: 'l' }, ratio: 0.5 },
                            { pane: { kind: 'leaf', slotId: 'r' }, ratio: 0.5 }
                        ]},
                        renderSlot: function () {}
                    });
                    const s = sp._rootEl.style;
                    return { position: s.position || "(unset)", left: s.left || "(unset)",
                             top: s.top || "(unset)", width: s.width || "(unset)",
                             height: s.height || "(unset)" };
                })()""");

        assertEquals("1000px", root.getMember("width").asString());
        assertEquals("600px",  root.getMember("height").asString());
    }

    /**
     * The survivor of a merge becomes the root. It already carries the box it
     * had as a child, so "no box" does not describe this case — a stale one
     * does, which is the same defect wearing different clothes.
     */
    @Test
    void mergingDownToOnePaneExpandsTheSurvivor() {
        Value root = js.eval("js", """
                (() => {
                    const container = newEl('div');
                    container._w = 1000; container._h = 600;
                    const sp = new SplitPane({
                        container:  container,
                        layout:     { kind: 'split', orientation: 'horizontal', children: [
                            { pane: { kind: 'leaf', slotId: 'keep' }, ratio: 0.5 },
                            { pane: { kind: 'leaf', slotId: 'gone' }, ratio: 0.5 }
                        ]},
                        renderSlot: function () {}
                    });
                    sp.merge('gone');
                    const s = sp._rootEl.style;
                    return { position: s.position || "(unset)", left: s.left || "(unset)",
                             top: s.top || "(unset)", width: s.width || "(unset)",
                             height: s.height || "(unset)" };
                })()""");

        assertEquals("1000px", root.getMember("width").asString(),
                     "the survivor must expand to the whole container, not keep its half");
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Enough DOM for SplitPane's build + sizing pass, and no more. Sizes are
     * the point, so an element reports {@code clientWidth/Height} from its own
     * inline style — which is what a browser does once a box is written, and
     * what makes an UNwritten box observable as zero rather than as a number
     * the stub invented.
     */
    private static final String DOM = """
        function newEl(tag) {
            const e = {
                tagName: tag, id: '', textContent: '', className: '',
                style: {}, children: [], parentNode: null,
                _w: null, _h: null,
                classList: {
                    add:    function (c) { e.className = (e.className + ' ' + c).trim(); },
                    remove: function ()  {},
                    toggle: function ()  {},
                    contains: function (c) { return e.className.split(' ').indexOf(c) >= 0; }
                },
                setAttribute: function () {},
                appendChild:  function (c) { c.parentNode = e; e.children.push(c); return c; },
                removeChild:  function (c) {
                    const i = e.children.indexOf(c);
                    if (i >= 0) e.children.splice(i, 1);
                    c.parentNode = null; return c;
                },
                insertBefore: function (c, ref) {
                    const i = e.children.indexOf(ref);
                    e.children.splice(i < 0 ? e.children.length : i, 0, c);
                    c.parentNode = e; return c;
                },
                addEventListener: function () {}, removeEventListener: function () {},
                getBoundingClientRect: function () {
                    return { left: 0, top: 0, width: e.clientWidth, height: e.clientHeight };
                }
            };
            // A written box wins; otherwise the explicit _w/_h a test set for a
            // container; otherwise zero, which is the honest answer for an
            // element nobody has sized.
            Object.defineProperty(e, 'clientWidth', { get: function () {
                if (e.style.width)  return parseInt(e.style.width, 10);
                return e._w != null ? e._w : 0;
            }});
            Object.defineProperty(e, 'clientHeight', { get: function () {
                if (e.style.height) return parseInt(e.style.height, 10);
                return e._h != null ? e._h : 0;
            }});
            return e;
        }
        globalThis.newEl = newEl;
        globalThis.document = {
            head: newEl('head'),
            getElementById: function () { return null; },
            createElement: newEl
        };
        globalThis.window = { addEventListener: function () {}, removeEventListener: function () {} };
        globalThis.requestAnimationFrame = function (fn) { fn(); };
        """;

    private static String readJs(String resource) {
        try (InputStream in = SplitPaneRootLeafSizingTest.class.getResourceAsStream(resource)) {
            if (in == null) throw new IllegalStateException("missing resource " + resource);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
