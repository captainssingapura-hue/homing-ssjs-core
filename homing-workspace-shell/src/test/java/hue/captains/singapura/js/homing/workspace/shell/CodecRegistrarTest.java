package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@code CodecRegistrar}. Constructor-injected registry +
 * importer; per-call methods receive only data. Tests build a fresh
 * registrar with stubs injected.
 */
class CodecRegistrarTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/CodecRegistrarModule.js";

    private static final String STUBS = """
            class StubRegistry {
                constructor() { this.calls = []; }
                register(kind, codec) { this.calls.push({ kind, codec }); }
            }
            // Provide a global so CodecRegistrar.INSTANCE construction at
            // module load won't throw. Instance tests use freshRegistrar(...)
            // with their own stub.
            globalThis.WidgetParamsCodecRegistry = new StubRegistry();
            """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
    }

    /** Fresh registrar with the given stub registry injected. */
    private Value freshRegistrar(Value registry) {
        Value deps = js.eval("js", "({})");
        deps.putMember("registry", registry);
        return global("CodecRegistrar").newInstance(deps);
    }

    @Test
    void registerDefaultsInstallsIdentityCodecPerEntry() {
        Value entries = js.eval("js", """
                [
                    { simpleName: 'A' },
                    { simpleName: 'B' },
                    { simpleName: 'C' }
                ]""");
        Value registry = js.eval("js", "new StubRegistry()");
        freshRegistrar(registry).invokeMember("_registerDefaults", entries);

        Value calls = registry.getMember("calls");
        assertEquals(3, calls.getArraySize());
        assertEquals("A", calls.getArrayElement(0).getMember("kind").asString());
        assertEquals("B", calls.getArrayElement(1).getMember("kind").asString());
        assertEquals("C", calls.getArrayElement(2).getMember("kind").asString());

        Value codec = calls.getArrayElement(0).getMember("codec");
        Value toResult = codec.getMember("transformTo").execute(js.eval("js", "({x:1, y:2})"));
        assertEquals(0, toResult.getMemberKeys().size());
    }

    @Test
    void registerCustomReplacesIdentityForNamedKind() {
        Value refs = js.eval("js", """
                [{ widgetKind: 'MovingAnimalWidget', moduleUrl: '/m', exportName: 'MovingCodec' }]""");
        Value modules = js.eval("js", """
                [{
                    MovingCodec: {
                        transformTo:   p => ({ x: p.x }),
                        transformFrom: w => ({ x: w.x })
                    }
                }]""");
        Value registry = js.eval("js", "new StubRegistry()");
        freshRegistrar(registry).invokeMember("_registerCustom", refs, modules);

        Value calls = registry.getMember("calls");
        assertEquals(1, calls.getArraySize());
        assertEquals("MovingAnimalWidget", calls.getArrayElement(0).getMember("kind").asString());
        Value codec = calls.getArrayElement(0).getMember("codec");
        Value out  = codec.getMember("transformTo").execute(js.eval("js", "({x:42})"));
        assertEquals(42, out.getMember("x").asInt());
    }

    @Test
    void registerCustomSkipsMissingExport() {
        Value refs = js.eval("js", """
                [{ widgetKind: 'Foo', moduleUrl: '/m', exportName: 'NotThere' }]""");
        Value modules = js.eval("js", "[{}]");
        Value registry = js.eval("js", "new StubRegistry()");
        freshRegistrar(registry).invokeMember("_registerCustom", refs, modules);
        assertEquals(0, registry.getMember("calls").getArraySize());
    }

    @Test
    void registerCustomSkipsInvalidCodecShape() {
        Value refs = js.eval("js", """
                [{ widgetKind: 'Bad', moduleUrl: '/m', exportName: 'BadCodec' }]""");
        Value modules = js.eval("js", "[{ BadCodec: { transformTo: 'not-a-fn' } }]");
        Value registry = js.eval("js", "new StubRegistry()");
        freshRegistrar(registry).invokeMember("_registerCustom", refs, modules);
        assertEquals(0, registry.getMember("calls").getArraySize());
    }

    @Test
    void registerAllWithNoCustomCodecsStillInstallsDefaults() {
        Value registry = js.eval("js", "new StubRegistry()");
        Value opts = js.eval("js", """
                ({ entries: [{simpleName:'X'}, {simpleName:'Y'}], widgetCodecs: [] })""");
        freshRegistrar(registry).invokeMember("registerAll", opts);

        Value calls = registry.getMember("calls");
        assertEquals(2, calls.getArraySize());
        assertTrue(calls.getArrayElement(0).getMember("kind").asString().equals("X"));
        assertTrue(calls.getArrayElement(1).getMember("kind").asString().equals("Y"));
    }

    @Test
    void identityCodecRoundTripsToEmptyObject() {
        Value codec = freshRegistrar(js.eval("js", "new StubRegistry()"))
                        .invokeMember("_identityCodec");
        Value toResult   = codec.getMember("transformTo").execute(js.eval("js", "({any:1})"));
        Value fromResult = codec.getMember("transformFrom").execute(js.eval("js", "({any:1})"));
        assertEquals(0, toResult.getMemberKeys().size());
        assertEquals(0, fromResult.getMemberKeys().size());
    }
}
