package hue.captains.singapura.js.homing.studio.base.conformance;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.studio.base.Fixtures;
import hue.captains.singapura.js.homing.studio.base.Studio;
import hue.captains.singapura.js.homing.studio.base.app.ContentViewer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * RFC 0015 Phase 5 — build-time gate over a studio's {@link ContentViewer}
 * registrations.
 *
 * <p>A {@code ContentViewer} binds a Doc {@code kind()} to a viewer
 * {@link AppModule}; the framework's polymorphic doc router consults
 * the registry, looks up the bound app by its {@code simpleName()}, and
 * routes accordingly. If the bound app is never added to
 * {@link Fixtures#harnessApps()} (or any studio's apps), the lookup
 * fails at request time with {@code "No app registered with this simple
 * name"} — and the failure only surfaces when a user actually clicks a
 * tile of that kind. This test pins the invariant at build time so the
 * mistake never reaches the running studio.</p>
 *
 * <p>Generates dynamic tests that assert, for the supplied
 * {@link Fixtures}:</p>
 * <ol>
 *   <li>every {@link ContentViewer#kind()} is non-blank;</li>
 *   <li>kinds are unique within the registry (V3 — first-registration
 *       wins is fine, but a duplicated kind is almost always a copy-paste
 *       bug);</li>
 *   <li>every {@link ContentViewer#app()} is non-null;</li>
 *   <li><b>every {@code contentViewer.app()} is also present (by class)
 *       in the studio's app closure</b> — the harness apps unioned with
 *       each studio's apps, deduped by class, the same union the
 *       framework's {@code Bootstrap} computes. Catches the
 *       "registered the viewer but forgot the AppModule" bug.</li>
 * </ol>
 *
 * <p>Per the existing conformance test pattern, downstream subclasses
 * provide the inputs via {@link #fixtures()} and {@link #studios()};
 * the test factory does the rest.</p>
 *
 * @since RFC 0015 Phase 5 (relocated in b.2j cleanup — see
 *        {@link DocConformanceTest} for the layering rationale)
 */
public abstract class ContentViewerConformanceTest {

    /** The Fixtures under test — its {@code contentViewers()} and
     *  {@code harnessApps()} are exercised against {@link #studios()}. */
    protected abstract Fixtures<?> fixtures();

    /** The studios this fixtures wraps. The framework unions each
     *  studio's {@code apps()} with the harness apps when composing the
     *  app resolver; the conformance check mirrors that union. */
    protected abstract List<? extends Studio<?>> studios();

    @TestFactory
    public Stream<DynamicTest> contentViewerConformance() {
        var fixtures = fixtures();
        var viewers  = fixtures.contentViewers();

        // Mirror Bootstrap.unionAppsByClass: harness apps first, then each
        // studio's intrinsic apps, deduped by class.
        Map<Class<?>, AppModule<?, ?>> appsByClass = new LinkedHashMap<>();
        for (var a : fixtures.harnessApps()) appsByClass.putIfAbsent(a.getClass(), a);
        for (var s : studios()) {
            for (var a : s.apps()) appsByClass.putIfAbsent(a.getClass(), a);
        }
        Set<Class<?>> registeredAppClasses = appsByClass.keySet();

        var tests = Stream.<DynamicTest>builder();

        // (1) Kinds non-blank.
        for (ContentViewer cv : viewers) {
            tests.add(DynamicTest.dynamicTest(
                    "kind not blank: " + cv.getClass().getSimpleName(),
                    () -> {
                        assertNotNull(cv.kind(),
                                cv.getClass().getName() + " returned null kind()");
                        assertTrue(!cv.kind().isBlank(),
                                cv.getClass().getName() + " returned blank kind()");
                    }));
        }

        // (2) Kinds unique within the registry.
        tests.add(DynamicTest.dynamicTest(
                "ContentViewer kinds are unique",
                () -> {
                    Set<String> seen = new HashSet<>();
                    for (ContentViewer cv : viewers) {
                        if (cv.kind() != null && !seen.add(cv.kind())) {
                            fail("ContentViewer kind collision: '" + cv.kind()
                                    + "' is bound by more than one ContentViewer "
                                    + "(second offender: " + cv.getClass().getName() + ")");
                        }
                    }
                }));

        // (3) app() bound to a registered AppModule. A null app() is the
        // documented per-Doc-dispatch sentinel (e.g. AppContentViewer for
        // kind "app" — each AppDoc carries its own renderer), so null
        // exempts the viewer from the registration check.
        for (ContentViewer cv : viewers) {
            tests.add(DynamicTest.dynamicTest(
                    "app() registered for kind '" + cv.kind() + "': " + cv.getClass().getSimpleName(),
                    () -> {
                        AppModule<?, ?> app = cv.app();
                        if (app == null) {
                            // Per-Doc dispatch sentinel — nothing to verify here.
                            return;
                        }
                        assertTrue(registeredAppClasses.contains(app.getClass()),
                                "ContentViewer " + cv.getClass().getName()
                                        + " (kind '" + cv.kind() + "') is bound to "
                                        + app.getClass().getName()
                                        + ", but that AppModule is not present in the "
                                        + "studio's app closure (harnessApps() unioned with "
                                        + "studio apps). Add it to Fixtures.harnessApps() — "
                                        + "otherwise the doc router will fail at request time "
                                        + "with 'No app registered with this simple name: "
                                        + app.simpleName() + "'.");
                    }));
        }

        return tests.build();
    }
}
