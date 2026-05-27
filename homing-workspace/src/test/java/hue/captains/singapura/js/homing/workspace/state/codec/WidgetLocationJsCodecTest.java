package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.PaneId;
import hue.captains.singapura.js.homing.workspace.state.WidgetLocation;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Second POC test — exercises the sealed-of-records case
 * ({@link WidgetLocation}) and proves <b>codec composition</b>: the
 * WidgetLocation codec uses the PaneId codec for the {@code paneId}
 * field, transitively.
 *
 * <p>The {@code @BeforeEach} loads four sources in order:
 * PaneId class, PaneIdCodec, WidgetLocation namespace + variant classes,
 * WidgetLocationCodec. Each test then operates against the fully composed
 * JS environment.</p>
 *
 * <p>If round-trip soundness holds here, the ontology survives a structural
 * case meaningfully harder than the wrapper-type case — variant dispatch,
 * discriminator handling, nested codec calls, all wired through the same
 * four ontology objects.</p>
 */
class WidgetLocationJsCodecTest {

    private Context js;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js")
                .allowAllAccess(false)
                .option("js.ecmascript-version", "2022")
                .build();
        // Order matters — WidgetLocation depends on PaneId being present.
        js.eval("js", PaneIdJsDefinition.INSTANCE.generate(ObjectDefinition.of(PaneId.class)));
        js.eval("js", PaneIdJsFunctions.INSTANCE.generate(ObjectDefinition.of(PaneId.class)));
        js.eval("js", WidgetLocationJsDefinition.INSTANCE.generate(ObjectDefinition.of(WidgetLocation.class)));
        js.eval("js", WidgetLocationJsFunctions.INSTANCE.generate(ObjectDefinition.of(WidgetLocation.class)));
    }

    @AfterEach
    void teardown() {
        if (js != null) {
            js.close();
            js = null;
        }
    }

    // ── Round-trip soundness for both variants ──────────────────────────────

    @Test
    void roundTripInPanePreservesAllFields() {
        var result = js.eval("js", """
                const original = new WidgetLocation.InPane(new PaneId("p1"), 5, true);
                const wire     = WidgetLocationCodec.transformTo(original);
                const restored = WidgetLocationCodec.transformFrom(wire);
                ({
                    wireKind:       wire.kind,
                    wirePaneId:     wire.paneId,
                    wireTabIndex:   wire.tabIndex,
                    wireIsActive:   wire.isActive,
                    paneIdEqual:    restored.paneId.value === original.paneId.value,
                    tabIndexEqual:  restored.tabIndex === original.tabIndex,
                    isActiveEqual:  restored.isActive === original.isActive,
                    isInPane:       restored instanceof WidgetLocation.InPane
                })
                """);
        assertEquals("InPane", result.getMember("wireKind").asString());
        assertEquals("p1",     result.getMember("wirePaneId").asString());
        assertEquals(5,        result.getMember("wireTabIndex").asInt());
        assertTrue(result.getMember("wireIsActive").asBoolean());
        assertTrue(result.getMember("paneIdEqual").asBoolean());
        assertTrue(result.getMember("tabIndexEqual").asBoolean());
        assertTrue(result.getMember("isActiveEqual").asBoolean());
        assertTrue(result.getMember("isInPane").asBoolean(),
                "restored should be an InPane instance (variant preserved)");
    }

    @Test
    void roundTripInModalIsEmpty() {
        var result = js.eval("js", """
                const original = new WidgetLocation.InModal();
                const wire     = WidgetLocationCodec.transformTo(original);
                const restored = WidgetLocationCodec.transformFrom(wire);
                ({
                    wireKind:       wire.kind,
                    wireKeyCount:   Object.keys(wire).length,
                    isInModal:      restored instanceof WidgetLocation.InModal
                })
                """);
        assertEquals("InModal", result.getMember("wireKind").asString());
        assertEquals(1,         result.getMember("wireKeyCount").asInt(),
                "InModal wire form should carry only the kind discriminator, no payload");
        assertTrue(result.getMember("isInModal").asBoolean());
    }

    // ── Codec composition — WidgetLocation codec calls PaneId codec ──────

    @Test
    void inPaneWireFormUsesPaneIdWireFormForPaneIdField() {
        var result = js.eval("js", """
                const wire = WidgetLocationCodec.transformTo(
                    new WidgetLocation.InPane(new PaneId("composed"), 0, false));
                typeof wire.paneId
                """);
        assertEquals("string", result.asString(),
                "InPane.paneId should be encoded via PaneIdCodec (string wire form), "
              + "not embedded as a PaneId object");
    }

    @Test
    void inPaneDecodeReconstructsPaneIdFromWire() {
        var result = js.eval("js", """
                const wire = { kind: 'InPane', paneId: 'from-wire', tabIndex: 2, isActive: true };
                const decoded = WidgetLocationCodec.transformFrom(wire);
                ({
                    isPaneId:    decoded.paneId instanceof PaneId,
                    paneIdValue: decoded.paneId.value
                })
                """);
        assertTrue(result.getMember("isPaneId").asBoolean(),
                "Decoded paneId must be a real PaneId instance (constructed via PaneIdCodec)");
        assertEquals("from-wire", result.getMember("paneIdValue").asString());
    }

    // ── Encoder dispatches by instanceof, rejects non-variants ────────────

    @Test
    void encoderRejectsNonWidgetLocation() {
        assertTrue(evalThrows("WidgetLocationCodec.transformTo({kind: 'InPane', paneId: new PaneId('p'), tabIndex: 0, isActive: true});"),
                "encoder must reject plain objects that look like wire form");
        assertTrue(evalThrows("WidgetLocationCodec.transformTo('string');"),
                "encoder must reject raw strings");
        assertTrue(evalThrows("WidgetLocationCodec.transformTo(null);"),
                "encoder must reject null");
    }

    // ── Decoder dispatches by kind, rejects unknown kinds ────────────────

    @Test
    void decoderRejectsUnknownKind() {
        assertTrue(evalThrows("WidgetLocationCodec.transformFrom({kind: 'Ghost'});"),
                "unknown kind must fail loudly, not silently return undefined");
    }

    @Test
    void decoderRejectsMissingKind() {
        assertTrue(evalThrows("WidgetLocationCodec.transformFrom({});"),
                "wire object without kind field must fail");
        assertTrue(evalThrows("WidgetLocationCodec.transformFrom(null);"),
                "null wire must fail");
        assertTrue(evalThrows("WidgetLocationCodec.transformFrom({kind: 42});"),
                "non-string kind must fail");
    }

    // ── InPane variant validates fields on construct ─────────────────────

    @Test
    void inPaneRejectsNonPaneId() {
        assertTrue(evalThrows("new WidgetLocation.InPane('not-a-paneId', 0, true);"));
        assertTrue(evalThrows("new WidgetLocation.InPane({value: 'fake'}, 0, true);"));
    }

    @Test
    void inPaneRejectsNegativeTabIndex() {
        assertTrue(evalThrows("new WidgetLocation.InPane(new PaneId('p'), -1, true);"));
    }

    @Test
    void inPaneRejectsNonIntegerTabIndex() {
        assertTrue(evalThrows("new WidgetLocation.InPane(new PaneId('p'), 1.5, true);"));
        assertTrue(evalThrows("new WidgetLocation.InPane(new PaneId('p'), '0', true);"));
    }

    @Test
    void inPaneRejectsNonBooleanIsActive() {
        assertTrue(evalThrows("new WidgetLocation.InPane(new PaneId('p'), 0, 'yes');"));
        assertTrue(evalThrows("new WidgetLocation.InPane(new PaneId('p'), 0, 1);"));
        assertTrue(evalThrows("new WidgetLocation.InPane(new PaneId('p'), 0, null);"));
    }

    // ── Validation runs on decode path too (corrupt wire data caught) ────

    @Test
    void decodeValidatesInPaneFields() {
        assertTrue(evalThrows("WidgetLocationCodec.transformFrom({kind: 'InPane', paneId: 'p', tabIndex: -1, isActive: true});"));
        assertTrue(evalThrows("WidgetLocationCodec.transformFrom({kind: 'InPane', paneId: 'invalid grammar', tabIndex: 0, isActive: true});"));
        assertTrue(evalThrows("WidgetLocationCodec.transformFrom({kind: 'InPane', paneId: 'p', tabIndex: 0, isActive: 'truthy'});"));
    }

    // ── Namespace is frozen (matches sealed semantics) ───────────────────

    @Test
    void widgetLocationNamespaceIsFrozen() {
        var result = js.eval("js", """
                let threw = false;
                try {
                    WidgetLocation.Ghost = class { constructor() {} };
                    Object.isFrozen(WidgetLocation)
                } catch (e) {
                    threw = true;
                }
                ({ stillFrozen: Object.isFrozen(WidgetLocation), threw: threw })
                """);
        assertTrue(result.getMember("stillFrozen").asBoolean(),
                "WidgetLocation namespace must remain frozen — no runtime variant additions");
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private boolean evalThrows(String snippet) {
        try {
            js.eval("js", snippet);
            return false;
        } catch (PolyglotException e) {
            return e.isGuestException();
        }
    }
}
