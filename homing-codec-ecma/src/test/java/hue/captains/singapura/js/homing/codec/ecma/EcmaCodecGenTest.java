package hue.captains.singapura.js.homing.codec.ecma;

import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests for the ECMAScript code generators against a tiny in-test
 * record. Real workspace-record codec generation lives in
 * {@code homing-workspace-codecs}; these tests cover only the generator
 * mechanics in isolation.
 */
class EcmaCodecGenTest {

    /** One-component record — the smallest non-trivial shape. */
    record SimpleId(String value) {}

    /** Multi-component record — three JS-native scalars. */
    record SimplePoint(int x, int y, boolean visible) {}

    @Test
    void definitionEmitsClassWithConstructorAndFreeze() {
        String js = EcmaDefinitionCodeGen.INSTANCE.generate(
                ObjectDefinition.of(SimpleId.class));
        assertTrue(js.contains("class SimpleId"),    "class header present");
        assertTrue(js.contains("constructor(value)"), "constructor params from components");
        assertTrue(js.contains("this.value = value"), "field assignment");
        assertTrue(js.contains("Object.freeze(this)"), "structural immutability");
    }

    @Test
    void functionsEmitsCodecWithRoundTripMethods() {
        String js = EcmaFunctionsCodeGen.INSTANCE.generate(
                ObjectDefinition.of(SimpleId.class));
        assertTrue(js.contains("class SimpleIdCodec"),                "codec class header");
        assertTrue(js.contains("static KIND = 'SimpleId'"),           "kind discriminator");
        assertTrue(js.contains("static transformTo(typed)"),          "encode method");
        assertTrue(js.contains("static transformFrom(wire)"),         "decode method");
        assertTrue(js.contains("value: typed.value"),                 "encode reads component");
        assertTrue(js.contains("new SimpleId(wire.value)"),           "decode constructs typed");
    }

    @Test
    void multiComponentRecordEnumeratesAllComponentsInOrder() {
        String js = EcmaFunctionsCodeGen.INSTANCE.generate(
                ObjectDefinition.of(SimplePoint.class));
        // The order must follow the record's component order — x, y, visible.
        int xIdx = js.indexOf("x: typed.x");
        int yIdx = js.indexOf("y: typed.y");
        int vIdx = js.indexOf("visible: typed.visible");
        assertTrue(xIdx >= 0 && yIdx > xIdx && vIdx > yIdx,
                "components emitted in record-declared order");
        assertTrue(js.contains("new SimplePoint(wire.x, wire.y, wire.visible)"),
                "decode passes arguments in order");
    }

    @Test
    void nonRecordTypeIsRejectedByBothCodegens() {
        var def = ObjectDefinition.of(String.class);
        assertThrows(IllegalArgumentException.class,
                () -> EcmaDefinitionCodeGen.INSTANCE.generate(def));
        assertThrows(IllegalArgumentException.class,
                () -> EcmaFunctionsCodeGen.INSTANCE.generate(def));
    }

    /** Smoke enum for the enum-path tests below. */
    enum Color { RED, GREEN, BLUE }

    @Test
    void enumDefinitionEmitsFrozenStringMap() {
        String js = EcmaDefinitionCodeGen.INSTANCE.generate(ObjectDefinition.of(Color.class));
        assertTrue(js.contains("const Color = Object.freeze({"), "frozen-object form");
        assertTrue(js.contains("RED: 'RED'"),   "constant emitted as string-keyed identity");
        assertTrue(js.contains("GREEN: 'GREEN'"));
        assertTrue(js.contains("BLUE: 'BLUE'"));
    }

    @Test
    void enumCodecEmitsValidatingTransformFrom() {
        String js = EcmaFunctionsCodeGen.INSTANCE.generate(ObjectDefinition.of(Color.class));
        assertTrue(js.contains("class ColorCodec"),                  "codec header");
        assertTrue(js.contains("static KIND = 'Color'"),             "kind discriminator");
        assertTrue(js.contains("static VALUES = Object.freeze(["),   "allowed-values set");
        assertTrue(js.contains("'RED'"));
        assertTrue(js.contains("'GREEN'"));
        assertTrue(js.contains("'BLUE'"));
        assertTrue(js.contains("ColorCodec.VALUES.includes(wire)"),  "validating decode");
    }

    @Test
    void generatorsAreStateless() {
        // Two independent calls produce identical strings.
        var def = ObjectDefinition.of(SimpleId.class);
        assertEquals(
                EcmaFunctionsCodeGen.INSTANCE.generate(def),
                new EcmaFunctionsCodeGen().generate(def),
                "FunctionsCodeGen output is deterministic and independent of instance identity");
        assertEquals(
                EcmaDefinitionCodeGen.INSTANCE.generate(def),
                new EcmaDefinitionCodeGen().generate(def),
                "DefinitionCodeGen output is deterministic and independent of instance identity");
    }
}
