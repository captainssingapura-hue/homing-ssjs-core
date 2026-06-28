package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.tao.http.config.TlsValidationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Standalone TLS preflight over {@link RuntimeParams}, no server involved. */
class TlsPreflightTest {

    private static String keystorePath;

    @BeforeAll
    static void locateFixture() throws Exception {
        var url = TlsPreflightTest.class.getResource("/test-keystore.jks");
        assertNotNull(url, "test-keystore.jks fixture must be on the test classpath");
        keystorePath = Path.of(url.toURI()).toString();
    }

    @Test
    void httpParams_yieldNoReport() throws Exception {
        var report = new TlsPreflight().inspect(new DefaultRuntimeParams(8080));
        assertTrue(report.isEmpty(), "plain HTTP params have nothing to validate");
    }

    @Test
    void httpsParams_validateKeystore() throws Exception {
        var params = HttpsRuntimeParams.jks(8443, keystorePath, "testpass");

        var report = new TlsPreflight().inspect(params).orElseThrow();

        assertEquals("JKS", report.storeType());
        assertEquals("testcert", report.entries().get(0).alias());
        assertTrue(report.validAt(Instant.now()));
    }

    @Test
    void missingKeystore_isMaterialUnresolvable() {
        var params = HttpsRuntimeParams.jks(8443, "does/not/exist.jks", "testpass");

        var ex = assertThrows(TlsValidationException.class,
                () -> new TlsPreflight().inspect(params));
        assertEquals(TlsValidationException.Kind.MATERIAL_UNRESOLVABLE, ex.kind());
    }

    @Test
    void wrongPassword_isClassified() {
        var params = HttpsRuntimeParams.jks(8443, keystorePath, "wrongpass");

        var ex = assertThrows(TlsValidationException.class,
                () -> new TlsPreflight().inspect(params));
        assertEquals(TlsValidationException.Kind.WRONG_PASSWORD, ex.kind());
    }
}
