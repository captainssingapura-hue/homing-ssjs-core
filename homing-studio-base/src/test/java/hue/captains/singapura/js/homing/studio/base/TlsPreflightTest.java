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
    private static String pkcs12Path;

    @BeforeAll
    static void locateFixture() throws Exception {
        keystorePath = fixturePath("/test-keystore.jks");
        pkcs12Path = fixturePath("/test-keystore.p12");
    }

    private static String fixturePath(String resource) throws Exception {
        var url = TlsPreflightTest.class.getResource(resource);
        assertNotNull(url, resource + " fixture must be on the test classpath");
        return Path.of(url.toURI()).toString();
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
    void pkcs12Params_validateKeystore() throws Exception {
        var params = HttpsRuntimeParams.pkcs12(8443, pkcs12Path, "testpass");

        var report = new TlsPreflight().inspect(params).orElseThrow();

        assertEquals("PKCS12", report.storeType());
        assertEquals("testcert", report.entries().get(0).alias());
        assertTrue(report.validAt(Instant.now()));
    }

    @Test
    void pkcs12WrongPassword_isClassified() {
        var params = HttpsRuntimeParams.pkcs12(8443, pkcs12Path, "wrongpass");

        var ex = assertThrows(TlsValidationException.class,
                () -> new TlsPreflight().inspect(params));
        assertEquals(TlsValidationException.Kind.WRONG_PASSWORD, ex.kind());
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
