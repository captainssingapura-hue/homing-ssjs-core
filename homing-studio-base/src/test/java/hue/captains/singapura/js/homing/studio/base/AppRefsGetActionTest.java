package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.app.AppDoc;
import hue.captains.singapura.js.homing.studio.base.app.Navigable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0025 L cleanup — first deposit toward the Tests Carry the
 * Discipline doctrine. Exercises the {@code /app-refs?app=&lt;simpleName&gt;}
 * endpoint's contract: indexing by simpleName at construction, response
 * shape mirrors {@code /doc-refs}, null/missing-app paths return
 * empty-but-valid JSON, missing query parameter returns 4xx.
 *
 * <p>Catalogue-registry-aware breadcrumbs are covered upstream in
 * {@code DocRefsGetActionTest} (shared serializer) — this test focuses
 * on the path-specific behaviour ({@code AppRefs}'s simpleName → UUID
 * lookup) rather than re-testing the serialiser.</p>
 */
class AppRefsGetActionTest {

    // ── Stub AppModule + Navigable + AppDoc — minimal fixture for indexing. ──

    public static final class StubApp extends AppModuleBase<AppModule._None, StubApp> {
        public static final StubApp INSTANCE = new StubApp();
        private StubApp() {}
        @Override public String simpleName() { return "stub-app"; }
        @Override public String title()      { return "Stub App"; }
    }

    /** Another app with a different simpleName — for the "wrong app" path. */
    public static final class OtherApp extends AppModuleBase<AppModule._None, OtherApp> {
        public static final OtherApp INSTANCE = new OtherApp();
        private OtherApp() {}
        @Override public String simpleName() { return "other-app"; }
        @Override public String title()      { return "Other App"; }
    }

    private static final AppDoc<AppModule._None, StubApp> STUB_APP_DOC =
            new AppDoc<>(new Navigable<>(
                    StubApp.INSTANCE, AppModule._None.INSTANCE,
                    "Stub App tile name", "Tile summary."));

    private static final AppDoc<AppModule._None, OtherApp> OTHER_APP_DOC =
            new AppDoc<>(new Navigable<>(
                    OtherApp.INSTANCE, AppModule._None.INSTANCE,
                    "Other tile", "Other summary."));

    private final DocRegistry registry = new DocRegistry(List.of(STUB_APP_DOC, OTHER_APP_DOC));
    private final AppRefsGetAction action = new AppRefsGetAction(registry, null);

    @Test
    void execute_returnsTitleAndEmptyBreadcrumbsForKnownApp() throws Exception {
        var result = action.execute(new AppRefsGetAction.Query("stub-app"),
                                    new EmptyParam.NoHeaders()).get();

        assertNotNull(result);
        assertEquals("application/json; charset=utf-8", result.contentType());
        var body = result.body();
        assertTrue(body.contains("\"title\":\"Stub App tile name\""),
                "title should reflect the AppDoc's title (which is the Navigable's name): " + body);
        // No CatalogueRegistry → empty breadcrumb array.
        assertTrue(body.contains("\"breadcrumbs\":[]"),
                "breadcrumbs should be empty array when no CatalogueRegistry: " + body);
        assertTrue(body.contains("\"references\":[]"),
                "references should be empty array — AppDocs declare no references: " + body);
    }

    @Test
    void execute_returnsEmptyJsonForUnknownApp() throws Exception {
        // Not an error — many apps aren't surfaced via Navigable (e.g. the
        // catalogue host). Chrome falls back to its default crumb.
        var result = action.execute(new AppRefsGetAction.Query("nonexistent-app"),
                                    new EmptyParam.NoHeaders()).get();

        assertNotNull(result);
        assertEquals("application/json; charset=utf-8", result.contentType());
        assertEquals("{\"title\":null,\"breadcrumbs\":[],\"references\":[]}", result.body());
    }

    @Test
    void execute_failsOnMissingApp() {
        var future = action.execute(new AppRefsGetAction.Query(null),
                                    new EmptyParam.NoHeaders());
        var ex = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(ResourceNotFound.class, ex.getCause());
    }

    @Test
    void execute_failsOnBlankApp() {
        var future = action.execute(new AppRefsGetAction.Query("   "),
                                    new EmptyParam.NoHeaders());
        var ex = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(ResourceNotFound.class, ex.getCause());
    }

    @Test
    void index_doesNotConfuseDifferentApps() throws Exception {
        // Boundary case — the indexing scans DocRegistry once; verify the
        // second app is also reachable, not just the first.
        var result = action.execute(new AppRefsGetAction.Query("other-app"),
                                    new EmptyParam.NoHeaders()).get();
        assertTrue(result.body().contains("\"title\":\"Other tile\""),
                "second registered app should be reachable: " + result.body());
    }

    @Test
    void emptyRegistry_returnsEmptyForAnyApp() throws Exception {
        // Boundary case — a deployment with no AppDocs (only ProseDocs etc.)
        // should still construct the action without failure; all lookups
        // return empty-but-valid JSON.
        var emptyAction = new AppRefsGetAction(new DocRegistry(List.of()), null);
        var result = emptyAction.execute(new AppRefsGetAction.Query("anything"),
                                         new EmptyParam.NoHeaders()).get();
        assertEquals("{\"title\":null,\"breadcrumbs\":[],\"references\":[]}", result.body());
    }

    // ── Minimal AppModule base for the stubs — fills in the boilerplate. ──

    private static abstract class AppModuleBase<
            P extends AppModule._Param, M extends AppModule<P, M>>
            implements AppModule<P, M> {

        protected record appMain<P extends AppModule._Param, M extends AppModule<P, M>>()
                implements AppModule._AppMain<P, M> {}

        @Override public ImportsFor<M> imports() { return ImportsFor.noImports(); }

        @SuppressWarnings("unchecked")
        @Override public ExportsOf<M> exports() {
            return new ExportsOf<>((M) this, List.<Exportable<M>>of(new appMain<>()));
        }
    }

    /** Unused but resolves a compile warning about AppLink being unreferenced. */
    @SuppressWarnings("unused")
    private record _unusedAppLink() implements AppLink<StubApp> {}
}
