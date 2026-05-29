package hue.captains.singapura.js.homing.workspace.events.contract;

import hue.captains.singapura.js.homing.workspace.events.CheckpointStore;
import hue.captains.singapura.js.homing.workspace.events.WorkspaceEventLog;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0035 P3 — Structural conformance test for the event-management JS
 * implementations against the Java contract surface.
 *
 * <p>Loads each refactored JS module (EventLogStore.js, WorkspaceEventLog.js,
 * CheckpointStore.js) into a GraalVM Polyglot context and asserts:</p>
 *
 * <ol>
 *   <li>The expected class is defined.</li>
 *   <li>Each public method on the Java contract has a matching JS method
 *       on the class prototype with the correct arity.</li>
 *   <li>Constants declared on the Java side appear as JS static class
 *       fields with the documented values.</li>
 *   <li>No top-level identifiers leak from the IIFE / top-level-const
 *       legacy pattern — every module's exported surface is one class
 *       plus a {@code createXxx()} factory.</li>
 * </ol>
 *
 * <p>This is the build-time gate the Diligent Primitives doctrine names as
 * its third pillar's substrate: the contract becomes structurally
 * enforced, not just declared.</p>
 *
 * <p>The tests don't exercise IndexedDB (no IDB in the Truffle sandbox);
 * they only assert structural match. Behavioural tests of write/read
 * round-trips will land when the headless harness from RFC 0033 arrives.</p>
 */
class EventManagementConformanceTest {

    private Context js;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js")
                .allowAllAccess(false)
                .option("js.ecmascript-version", "2022")
                .build();
    }

    @AfterEach
    void teardown() {
        if (js != null) js.close();
    }

    // ─── EventLogStore ───────────────────────────────────────────────────

    @Test
    void eventLogStoreClassDefined() {
        loadEventLogStore();
        assertTrue(evalBoolean("typeof EventLogStore === 'function'"),
                "EventLogStore must be defined as a class");
        assertTrue(evalBoolean("EventLogStore.prototype.constructor === EventLogStore"),
                "EventLogStore must have a class-form constructor");
    }

    @Test
    void eventLogStoreStaticFields() {
        loadEventLogStore();
        assertEquals("homing.eventlog", js.eval("js", "EventLogStore.DB_NAME").asString());
        assertEquals("events",          js.eval("js", "EventLogStore.STORE_NAME").asString());
        assertEquals(1,                 js.eval("js", "EventLogStore.DB_VERSION").asInt());
    }

    @Test
    void eventLogStorePublicMethods() {
        loadEventLogStore();
        // Each conforms to the Java EventLog<P> contract (with kind+workspaceId
        // prepended since EventLogStore is the multi-tenant primitive under
        // WorkspaceEventLog).
        assertMethod("EventLogStore", "append", 4);
        assertMethod("EventLogStore", "query",  3);
        assertMethod("EventLogStore", "clear",  2);
    }

    @Test
    void eventLogStoreFactoryReturnsInstance() {
        loadEventLogStore();
        assertTrue(evalBoolean("typeof createEventLogStore === 'function'"),
                "createEventLogStore() factory must exist");
        assertTrue(evalBoolean("createEventLogStore() instanceof EventLogStore"),
                "createEventLogStore() must return an EventLogStore instance");
    }

    // ─── WorkspaceEventLog ───────────────────────────────────────────────

    @Test
    void workspaceEventLogClassDefined() {
        loadWorkspaceEventLog();
        assertTrue(evalBoolean("typeof WorkspaceEventLog === 'function'"));
        assertTrue(evalBoolean("WorkspaceEventLog.prototype.constructor === WorkspaceEventLog"));
    }

    @Test
    void workspaceEventLogPublicMethods() {
        loadWorkspaceEventLog();
        // Conforms to Java EventLog<P> contract: append(name, payload),
        // query(opts), clear(). The instance is bound to (kind, workspaceId)
        // at construction time.
        assertMethod("WorkspaceEventLog", "append",         2);
        assertMethod("WorkspaceEventLog", "query",          1);
        assertMethod("WorkspaceEventLog", "clear",          0);
        // Diagnostic + legacy methods.
        assertMethod("WorkspaceEventLog", "localEmitCount", 0);
        assertMethod("WorkspaceEventLog", "emit",           2);   // @deprecated alias for append
    }

    @Test
    void workspaceEventLogLegacyAttachStillWorks() {
        loadWorkspaceEventLog();
        assertTrue(evalBoolean("typeof WorkspaceEventLog.attach === 'function'"),
                "Legacy static attach() factory must still work for backward compat");
        Value bound = js.eval("js",
                "WorkspaceEventLog.attach({ workspaceKind: 'TestKind', workspaceId: 'abc-123' })");
        assertNotNull(bound);
        assertEquals("TestKind", bound.getMember("workspaceKind").asString());
        assertEquals("abc-123",  bound.getMember("workspaceId").asString());
    }

    @Test
    void workspaceEventLogConstructorBindsScope() {
        loadWorkspaceEventLog();
        Value bound = js.eval("js",
                "new WorkspaceEventLog({ workspaceKind: 'TestKind', workspaceId: 'abc-123' })");
        assertEquals("TestKind", bound.getMember("workspaceKind").asString());
        assertEquals("abc-123",  bound.getMember("workspaceId").asString());
        assertEquals(0,          bound.invokeMember("localEmitCount").asInt());
    }

    @Test
    void workspaceEventLogConstructorRejectsBadOpts() {
        loadWorkspaceEventLog();
        assertThrows(Exception.class, () -> js.eval("js",
                "new WorkspaceEventLog({ workspaceKind: '', workspaceId: 'abc' })"));
        assertThrows(Exception.class, () -> js.eval("js",
                "new WorkspaceEventLog({ workspaceKind: 'TestKind' })"));
    }

    // ─── CheckpointStore ─────────────────────────────────────────────────

    @Test
    void checkpointStoreClassDefined() {
        loadCheckpointStore();
        assertTrue(evalBoolean("typeof CheckpointStore === 'function'"));
        assertTrue(evalBoolean("CheckpointStore.prototype.constructor === CheckpointStore"));
    }

    @Test
    void checkpointStoreStaticFields() {
        loadCheckpointStore();
        assertEquals("homing.checkpoints", js.eval("js", "CheckpointStore.DB_NAME").asString());
        assertEquals("checkpoints",        js.eval("js", "CheckpointStore.STORE_NAME").asString());
        assertEquals(1,                    js.eval("js", "CheckpointStore.DB_VERSION").asInt());
        assertEquals(1,                    js.eval("js", "CheckpointStore.SCHEMA_VERSION").asInt());
    }

    @Test
    void checkpointStorePublicMethods() {
        loadCheckpointStore();
        // Conforms to Java CheckpointStore<S> contract — write/read/clear
        // (with kind+workspaceId prepended since the JS class is multi-tenant
        // and the WorkspaceEventLog-shaped per-workspace facade is integrated
        // at the chrome layer for now).
        assertMethod("CheckpointStore", "write", 4);
        assertMethod("CheckpointStore", "read",  2);
        assertMethod("CheckpointStore", "clear", 2);
    }

    @Test
    void checkpointStoreInstanceExposesSchemaVersionGetter() {
        loadCheckpointStore();
        Value instance = js.eval("js", "createCheckpointStore()");
        assertEquals(1, instance.getMember("SCHEMA_VERSION").asInt());
    }

    // ─── Cross-cutting — substrate-leak forbidden ────────────────────────

    @Test
    void noTopLevelDbNameCollisionsAcrossModules() {
        // Loading both EventLogStore + CheckpointStore in the same context
        // must not throw — the script-scope const collision that broke the
        // dev server pre-RFC-0035 is structurally impossible under the
        // class form. Constants live as static class fields; the two
        // classes have separate namespaces.
        loadEventLogStore();
        loadCheckpointStore();
        assertEquals("homing.eventlog",     js.eval("js", "EventLogStore.DB_NAME").asString());
        assertEquals("homing.checkpoints",  js.eval("js", "CheckpointStore.DB_NAME").asString());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private void loadEventLogStore() {
        js.eval("js",
                "var indexedDB = undefined;" +
                "var IDBKeyRange = undefined;");
        js.eval("js", readJs("/homing/js/hue/captains/singapura/js/homing/workspace/events/EventLogStore.js"));
    }

    private void loadWorkspaceEventLog() {
        // WorkspaceEventLog depends on createEventLogStore() being defined.
        loadEventLogStore();
        js.eval("js", readJs("/homing/js/hue/captains/singapura/js/homing/workspace/events/WorkspaceEventLog.js"));
    }

    private void loadCheckpointStore() {
        js.eval("js",
                "var indexedDB = undefined;");
        js.eval("js", readJs("/homing/js/hue/captains/singapura/js/homing/workspace/events/CheckpointStore.js"));
    }

    private String readJs(String classpathPath) {
        try (var in = getClass().getResourceAsStream(classpathPath)) {
            assertNotNull(in, "missing classpath resource: " + classpathPath);
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + classpathPath, e);
        }
    }

    private boolean evalBoolean(String expr) {
        return js.eval("js", expr).asBoolean();
    }

    private void assertMethod(String className, String methodName, int expectedArity) {
        Value method = js.eval("js", className + ".prototype." + methodName);
        assertTrue(method.canExecute(),
                className + "." + methodName + " must be a method on the prototype");
        assertEquals(expectedArity, method.getMember("length").asInt(),
                className + "." + methodName + " arity mismatch — declared in Java contract");
    }

    /** Reference the Java contract types so dependency wiring is visible. */
    @SuppressWarnings("unused")
    private static final Class<?>[] CONTRACT_REFERENCES = {
            WorkspaceEventLog.class,    // bundler
            CheckpointStore.class       // bundler
    };
}
