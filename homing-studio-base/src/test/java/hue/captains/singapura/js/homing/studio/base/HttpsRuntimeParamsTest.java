package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.tao.http.config.TlsCredential;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The HTTPS on/off switch lives in {@link RuntimeParams#tls()}. */
class HttpsRuntimeParamsTest {

    @Test
    void httpByDefault_tlsAbsent() {
        var params = new DefaultRuntimeParams(8080);
        assertTrue(params.tls().isEmpty(), "DefaultRuntimeParams must serve plain HTTP");
    }

    @Test
    void httpsJks_buildsProviderBackedCredential() throws Exception {
        var url = getClass().getResource("/test-keystore.jks");
        assertNotNull(url, "test-keystore.jks fixture must be on the test classpath");
        var keystorePath = Path.of(url.toURI()).toString();

        var params = HttpsRuntimeParams.jks(8443, keystorePath, "testpass");

        assertEquals(8443, params.port());
        assertTrue(params.tls().isPresent(), "HttpsRuntimeParams must switch HTTPS on");

        var jks = assertInstanceOf(TlsCredential.Jks.class, params.tls().get().credential());
        // The providers yield the real material when invoked.
        assertEquals("testpass", new String(jks.password().get()));
        assertTrue(jks.store().get().length > 0, "store provider reads the keystore bytes");
    }
}
