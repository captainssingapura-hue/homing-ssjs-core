package hue.captains.singapura.js.homing.workspace.catalogue.contract;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0031 V1 — Structural conformance test for {@code
 * WorkspaceCatalogueStore.js} against the Java {@link WorkspaceCatalogue}
 * interface.
 *
 * <p>Loads the JS class in a GraalVM Polyglot context and asserts:</p>
 *
 * <ol>
 *   <li>The class is defined with the expected name.</li>
 *   <li>Each method declared on the Java contract exists on the JS
 *       prototype with matching arity.</li>
 *   <li>Static class fields hold the documented database identity.</li>
 *   <li>The factory function returns an instance of the class.</li>
 *   <li>Argument validation throws on bad input.</li>
 * </ol>
 *
 * <p>Behavioural tests (round-trip via real IndexedDB) belong in the
 * RFC 0033 headless harness; this test gates the structural surface
 * at build time.</p>
 */
class WorkspaceCatalogueConformanceTest {

    private Context js;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js")
                .allowAllAccess(false)
                .option("js.ecmascript-version", "2022")
                .build();
        loadStore();
    }

    @AfterEach
    void teardown() {
        if (js != null) js.close();
    }

    @Test
    void classIsDefined() {
        assertTrue(evalBool("typeof WorkspaceCatalogueStore === 'function'"));
        assertTrue(evalBool(
                "WorkspaceCatalogueStore.prototype.constructor === WorkspaceCatalogueStore"));
    }

    @Test
    void staticFieldsMatchContract() {
        assertEquals("homing.catalogue",
                js.eval("js", "WorkspaceCatalogueStore.DB_NAME").asString());
        assertEquals("entries",
                js.eval("js", "WorkspaceCatalogueStore.STORE_NAME").asString());
        assertEquals(1,
                js.eval("js", "WorkspaceCatalogueStore.DB_VERSION").asInt());
        assertEquals("by_name",
                js.eval("js", "WorkspaceCatalogueStore.NAME_INDEX").asString());
    }

    @Test
    void publicMethodsExistWithRightArity() {
        // Each method maps 1:1 to a Java WorkspaceCatalogue method.
        // The arity is the contract's method-parameter count.
        assertMethod("lookupByUuid",   2);
        assertMethod("lookupByName",   2);
        assertMethod("resolveDefault", 1);
        assertMethod("listByKind",     1);
        assertMethod("create",         1);
        assertMethod("rename",         3);
        assertMethod("delete",         2);
        assertMethod("setDefault",     2);
        assertMethod("touch",          2);
    }

    @Test
    void factoryReturnsInstance() {
        assertTrue(evalBool("typeof createWorkspaceCatalogueStore === 'function'"));
        assertTrue(evalBool(
                "createWorkspaceCatalogueStore() instanceof WorkspaceCatalogueStore"));
    }

    @Test
    void argumentValidationRejectsBadInputs() {
        // lookupByUuid requires non-empty kind + id strings.
        assertThrows(Exception.class, () -> js.eval("js",
                "WorkspaceCatalogueStore._requireKind('')"));
        assertThrows(Exception.class, () -> js.eval("js",
                "WorkspaceCatalogueStore._requireId('')"));
        assertThrows(Exception.class, () -> js.eval("js",
                "WorkspaceCatalogueStore._requireName('')"));
    }

    @Test
    void createEntryRequiresAllFields() {
        // The Java WorkspaceCatalogueEntry has 6 required fields; the JS
        // _requireEntry mirrors that. Missing any field is a TypeError.
        assertThrows(Exception.class, () -> js.eval("js",
                "WorkspaceCatalogueStore._requireEntry(null)"));
        assertThrows(Exception.class, () -> js.eval("js",
                "WorkspaceCatalogueStore._requireEntry({ kind: 'x', id: 'y' })"));
        assertThrows(Exception.class, () -> js.eval("js",
                "WorkspaceCatalogueStore._requireEntry({"
                + "  kind: 'x', id: 'y', name: 'n',"
                + "  createdAt: 0, lastOpenedAt: 0"
                + "  /* missing isDefault */"
                + "})"));
        // The happy case should not throw.
        js.eval("js",
                "WorkspaceCatalogueStore._requireEntry({"
                + "  kind: 'AnimalsPlayground', id: 'abc-123', name: 'default',"
                + "  createdAt: 0, lastOpenedAt: 0, isDefault: true"
                + "})");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private void loadStore() {
        js.eval("js",
                "var indexedDB = undefined;" +
                "var IDBKeyRange = undefined;");
        js.eval("js", readJs(
                "/homing/js/hue/captains/singapura/js/homing/workspace/catalogue/WorkspaceCatalogueModule.js"));
    }

    private String readJs(String classpathPath) {
        try (var in = getClass().getResourceAsStream(classpathPath)) {
            assertNotNull(in, "missing classpath resource: " + classpathPath);
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + classpathPath, e);
        }
    }

    private boolean evalBool(String expr) {
        return js.eval("js", expr).asBoolean();
    }

    private void assertMethod(String methodName, int expectedArity) {
        Value m = js.eval("js", "WorkspaceCatalogueStore.prototype." + methodName);
        assertTrue(m.canExecute(),
                "WorkspaceCatalogueStore." + methodName
                + " must be a method on the prototype");
        assertEquals(expectedArity, m.getMember("length").asInt(),
                "WorkspaceCatalogueStore." + methodName
                + " arity mismatch — declared in WorkspaceCatalogue Java contract");
    }
}
