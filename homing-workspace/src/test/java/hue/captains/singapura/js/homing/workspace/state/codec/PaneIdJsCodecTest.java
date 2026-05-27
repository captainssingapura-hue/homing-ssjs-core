package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.PaneId;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The codec POC's verification — runs the hand-written
 * {@link PaneIdJsDefinition} and {@link PaneIdJsFunctions} sources
 * inside a GraalVM Polyglot JavaScript context and asserts the
 * round-trip contract:
 *
 * <pre>{@code
 *   transformFrom(transformTo(p)).value === p.value
 * }</pre>
 *
 * <p>This is the ground-truth test for the codec sub-category: if the
 * round-trip holds for the first concrete codec, the ontology survives
 * its first contact with reality. If it doesn't, the ontology, the
 * hand-written sources, or our understanding of one of them is wrong —
 * loudly, in this test.</p>
 */
class PaneIdJsCodecTest {

    private Context js;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js")
                .allowAllAccess(false)
                .option("js.ecmascript-version", "2022")
                .build();
        // Load the hand-written Definition (PaneId class) and Functions
        // (PaneIdCodec) into the context. Definition first — the codec
        // depends on the class existing.
        var def = ObjectDefinition.of(PaneId.class);
        js.eval("js", PaneIdJsDefinition.INSTANCE.generate(def));
        js.eval("js", PaneIdJsFunctions.INSTANCE.generate(def));
    }

    @AfterEach
    void teardown() {
        if (js != null) {
            js.close();
            js = null;
        }
    }

    // ── The defining contract: round-trip soundness ─────────────────────────

    @Test
    void roundTripPreservesValue() {
        var result = js.eval("js", """
                const original = new PaneId("test-pane");
                const wire     = PaneIdCodec.transformTo(original);
                const restored = PaneIdCodec.transformFrom(wire);
                ({
                    wire:          wire,
                    restoredValue: restored.value,
                    equalValues:   restored.value === original.value
                })
                """);
        assertEquals("test-pane", result.getMember("wire").asString(),
                "wire form should be the underlying string");
        assertEquals("test-pane", result.getMember("restoredValue").asString(),
                "restored value should equal the original");
        assertTrue(result.getMember("equalValues").asBoolean(),
                "round-trip equivalence must hold: transformFrom(transformTo(p)).value === p.value");
    }

    @Test
    void roundTripHoldsForGrammarBoundaries() {
        // Letters, digits, hyphen, underscore — the full grammar.
        var result = js.eval("js", """
                const cases = ["p1", "left", "A1B2", "pane-with-hyphens", "snake_case", "a"];
                const out = cases.map(s => PaneIdCodec.transformFrom(PaneIdCodec.transformTo(new PaneId(s))).value);
                JSON.stringify(out)
                """);
        assertEquals("[\"p1\",\"left\",\"A1B2\",\"pane-with-hyphens\",\"snake_case\",\"a\"]",
                result.asString());
    }

    // ── Generated class enforces the same validation as the Java record ──

    @Test
    void rejectsBlankValueOnConstruct() {
        var threw = evalThrows("""
                new PaneId("");
                """);
        assertTrue(threw, "PaneId('') should throw (regex requires at least one char)");
    }

    @Test
    void rejectsInvalidGrammarOnConstruct() {
        assertTrue(evalThrows("""
                new PaneId("p with spaces");
                """), "spaces should fail");
        assertTrue(evalThrows("""
                new PaneId("p/slash");
                """), "slashes should fail");
        assertTrue(evalThrows("""
                new PaneId("p.dot");
                """), "dots should fail");
    }

    @Test
    void rejectsNonStringOnConstruct() {
        assertTrue(evalThrows("new PaneId(42);"),
                "number should fail with TypeError");
        assertTrue(evalThrows("new PaneId(null);"),
                "null should fail with TypeError");
        assertTrue(evalThrows("new PaneId({value: 'x'});"),
                "object should fail with TypeError");
    }

    @Test
    void invalidWireFormFailsOnDecode() {
        // The decode path runs the same validation — corrupt wire data
        // can't sneak through.
        assertTrue(evalThrows("PaneIdCodec.transformFrom('invalid spaces');"),
                "wire with invalid grammar should fail to decode");
        assertTrue(evalThrows("PaneIdCodec.transformFrom(42);"),
                "non-string wire should fail to decode");
    }

    // ── transformTo defends against non-PaneId input ─────────────────────────

    @Test
    void transformToRejectsNonPaneId() {
        assertTrue(evalThrows("PaneIdCodec.transformTo({value: 'fake'});"),
                "transformTo should reject plain objects (not PaneId instances)");
        assertTrue(evalThrows("PaneIdCodec.transformTo('just a string');"),
                "transformTo should reject raw strings");
    }

    // ── Generated class is frozen (matches Java record immutability) ─────────

    @Test
    void generatedClassIsFrozen() {
        // Object.freeze is part of the hand-written class. Mutating value
        // should be a no-op in non-strict mode (we're in module-ish script
        // mode here); reading it should still equal the original.
        var result = js.eval("js", """
                const p = new PaneId("p1");
                p.value = "hacked";
                p.value
                """);
        assertEquals("p1", result.asString(),
                "frozen instance: attempted mutation must not alter the value");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean evalThrows(String snippet) {
        try {
            js.eval("js", snippet);
            return false;
        } catch (PolyglotException e) {
            return e.isGuestException();
        }
    }
}
