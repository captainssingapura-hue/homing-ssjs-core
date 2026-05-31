package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Diligent-Secretaries-style unit test for {@code LayoutCodec}. Pure
 * functions, no DOM — exercised under GraalVM with minimal stand-in
 * stubs for {@code LayoutNode}, {@code PaneId}, {@code Orientation}.
 *
 * <p>Style note: fixtures are <b>full JS literals</b> built via
 * {@code js.eval(...)}, not Java-side {@code Map} structures marshalled
 * across the boundary. Java's role is lifecycle, the polyglot boundary,
 * and assertions; the JS test content stays in JS.</p>
 */
class LayoutCodecTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/LayoutCodecModule.js";

    /** Stand-in classes — match the contract LayoutCodec exercises. */
    private static final String STUBS = """
            class PaneId {
                constructor(value) { this.value = value; }
            }
            class LayoutNodeLeaf {
                constructor(paneId) { this.paneId = paneId; }
            }
            class LayoutNodeSplit {
                constructor(orientation, ratio, first, second) {
                    this.orientation = orientation;
                    this.ratio = ratio;
                    this.first = first;
                    this.second = second;
                }
            }
            const LayoutNode = { Leaf: LayoutNodeLeaf, Split: LayoutNodeSplit };
            const Orientation = { HORIZONTAL: 'H', VERTICAL: 'V' };
            """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
    }

    private Value codec() { return global("LayoutCodec").getMember("INSTANCE"); }

    /** Evaluate a JS expression and return the {@link Value}.
     *  Tiny helper so tests read as "give me this JS literal". */
    private Value jsLiteral(String expression) {
        return js.eval("js", expression);
    }

    /** Deep-stringify a Value via JSON for whole-tree equality assertions. */
    private String jsonStringify(Value v) {
        Value json = js.getBindings("js").getMember("JSON");
        return json.getMember("stringify").execute(v).asString();
    }

    @Test
    void leafRoundTrip() {
        Value mtLeaf = jsLiteral("({ kind: 'leaf', slotId: 'slot-a' })");
        Value typed  = codec().invokeMember("mtToTyped", mtLeaf);
        assertEquals("slot-a", typed.getMember("paneId").getMember("value").asString());

        Value backMt = codec().invokeMember("typedToMt", typed);
        assertEquals("leaf",   backMt.getMember("kind").asString());
        assertEquals("slot-a", backMt.getMember("slotId").asString());
    }

    @Test
    void splitCarriesFirstChildRatioOnly() {
        Value mtSplit = jsLiteral("""
                ({
                    kind: 'split', orientation: 'horizontal',
                    children: [
                        { pane: { kind: 'leaf', slotId: 'a' }, ratio: 0.3 },
                        { pane: { kind: 'leaf', slotId: 'b' }, ratio: 0.7 }
                    ]
                })""");

        Value typed = codec().invokeMember("mtToTyped", mtSplit);
        assertEquals("H",  typed.getMember("orientation").asString());
        assertEquals(0.3,  typed.getMember("ratio").asDouble(), 1e-9);
        assertEquals("a",  typed.getMember("first").getMember("paneId").getMember("value").asString());
        assertEquals("b",  typed.getMember("second").getMember("paneId").getMember("value").asString());

        Value back = codec().invokeMember("typedToMt", typed);
        assertEquals("split",      back.getMember("kind").asString());
        assertEquals("horizontal", back.getMember("orientation").asString());
        assertEquals(0.3, back.getMember("children").getArrayElement(0).getMember("ratio").asDouble(), 1e-9);
        assertEquals(0.7, back.getMember("children").getArrayElement(1).getMember("ratio").asDouble(), 1e-9);
    }

    @Test
    void ratioClampsAtExtremes() {
        Value atZero = jsLiteral("""
                ({
                    kind: 'split', orientation: 'vertical',
                    children: [
                        { pane: { kind: 'leaf', slotId: 'a' }, ratio: 0.0 },
                        { pane: { kind: 'leaf', slotId: 'b' }, ratio: 1.0 }
                    ]
                })""");
        Value typed0 = codec().invokeMember("mtToTyped", atZero);
        assertTrue(typed0.getMember("ratio").asDouble() > 0.0,
                "ratio 0 must be clamped strictly above 0");

        Value atOne = jsLiteral("""
                ({
                    kind: 'split', orientation: 'vertical',
                    children: [
                        { pane: { kind: 'leaf', slotId: 'a' }, ratio: 1.0 },
                        { pane: { kind: 'leaf', slotId: 'b' }, ratio: 0.0 }
                    ]
                })""");
        Value typed1 = codec().invokeMember("mtToTyped", atOne);
        assertTrue(typed1.getMember("ratio").asDouble() < 1.0,
                "ratio 1 must be clamped strictly below 1");
    }

    @Test
    void nestedRoundTripPreservesStructure() {
        Value original = jsLiteral("""
                ({
                    kind: 'split', orientation: 'horizontal',
                    children: [
                        { pane: { kind: 'leaf', slotId: 'a' }, ratio: 0.5 },
                        { pane: {
                            kind: 'split', orientation: 'vertical',
                            children: [
                                { pane: { kind: 'leaf', slotId: 'b' }, ratio: 0.4 },
                                { pane: { kind: 'leaf', slotId: 'c' }, ratio: 0.6 }
                            ]
                        }, ratio: 0.5 }
                    ]
                })""");
        Value typed = codec().invokeMember("mtToTyped", original);
        Value back  = codec().invokeMember("typedToMt", typed);
        assertEquals(jsonStringify(original), jsonStringify(back),
                "round-trip mt → typed → mt must preserve the layout JSON");
    }

    @Test
    void nullPassesThrough() {
        assertTrue(codec().invokeMember("mtToTyped", (Object) null).isNull());
        assertTrue(codec().invokeMember("typedToMt", (Object) null).isNull());
    }
}
