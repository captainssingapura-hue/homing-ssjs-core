package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@code PersistenceAttacher}. Sync wrapper; tests pass a
 * stub WorkspaceStatePersistence + stub storage and verify the attach
 * call is forwarded with the right shape.
 *
 * <p>Constructor-only DI: persistence is fixed at construct time
 * (Explicit Substrate — every test builds a fresh attacher with the
 * stub persistence injected; per-call opts carry only data).</p>
 */
class PersistenceAttacherTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/PersistenceAttacherModule.js";

    private static final String STUBS = """
            globalThis.makePersistence = function () {
                return {
                    attachCalls: [],
                    attach(opts) {
                        this.attachCalls.push(opts);
                        return {
                            store: { _kind: opts.workspaceKind, clear() {} },
                            workspaceKind: opts.workspaceKind
                        };
                    }
                };
            };
            globalThis.makeStorage = function () {
                return {
                    getItem() { return null; },
                    setItem() {},
                    removeItem() {}
                };
            };
            // Provide a global so PersistenceAttacher.INSTANCE construction
            // does not throw when this module loads — instance tests use a
            // freshly constructed attacher with their own stub.
            globalThis.WorkspaceStatePersistence = makePersistence();
            """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
    }

    /** Fresh attacher with the given stub persistence injected. */
    private Value freshAttacher(Value persistence) {
        Value deps = js.eval("js", "({})");
        deps.putMember("persistence", persistence);
        return global("PersistenceAttacher").newInstance(deps);
    }

    @Test
    void attachForwardsWorkspaceKindAndStorage() {
        Value persistence = js.eval("js", "makePersistence()");
        Value opts = js.eval("js", """
                ({ workspaceKind: 'animalPlayground',
                   storage:       makeStorage() })""");

        Value layer = freshAttacher(persistence).invokeMember("attach", opts);

        assertTrue(!layer.isNull(), "attach returns a non-null layer on happy path");
        assertEquals("animalPlayground", layer.getMember("workspaceKind").asString());

        Value calls = persistence.getMember("attachCalls");
        assertEquals(1, calls.getArraySize());
        Value forwarded = calls.getArrayElement(0);
        assertEquals("animalPlayground", forwarded.getMember("workspaceKind").asString());
        // paramsCodecs is intentionally empty — CodecRegistrar populates the registry separately.
        assertEquals(0, forwarded.getMember("paramsCodecs").getMemberKeys().size());
    }

    @Test
    void missingStorageReturnsNullAndDoesNotAttach() {
        Value persistence = js.eval("js", "makePersistence()");
        // Explicit null storage; no window.localStorage in this stub env.
        Value opts = js.eval("js", """
                ({ workspaceKind: 'animalPlayground', storage: null })""");

        Value layer = freshAttacher(persistence).invokeMember("attach", opts);

        assertTrue(layer.isNull(), "no storage → null layer");
        assertEquals(0, persistence.getMember("attachCalls").getArraySize(),
                "no storage → persistence.attach not called");
    }

    @Test
    void persistenceThrowsReturnsNull() {
        Value persistence = js.eval("js", "({ attach() { throw new Error('boom'); } })");
        Value opts = js.eval("js", """
                ({ workspaceKind: 'animalPlayground', storage: makeStorage() })""");

        Value layer = freshAttacher(persistence).invokeMember("attach", opts);
        assertTrue(layer.isNull(), "persistence.attach throws → wrapper logs + returns null");
    }
}
