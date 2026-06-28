package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.tao.http.config.TlsCredential;
import hue.captains.singapura.tao.http.config.builtin.FileByteSource;
import hue.captains.singapura.tao.http.config.builtin.LiteralPassword;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The HTTPS on/off switch lives in {@link RuntimeParams#tls()}. */
class HttpsRuntimeParamsTest {

    @Test
    void httpByDefault_tlsAbsent() {
        var params = new DefaultRuntimeParams(8080);
        assertTrue(params.tls().isEmpty(), "DefaultRuntimeParams must serve plain HTTP");
    }

    @Test
    void httpsJks_carriesKeystorePathAndPassword() {
        var params = HttpsRuntimeParams.jks(8443, "certs/dev-keystore.jks", "changeit");

        assertEquals(8443, params.port());
        assertTrue(params.tls().isPresent(), "HttpsRuntimeParams must switch HTTPS on");

        var credential = params.tls().get().credential();
        var jks = assertInstanceOf(TlsCredential.Jks.class, credential);
        var store = assertInstanceOf(FileByteSource.class, jks.store());
        var password = assertInstanceOf(LiteralPassword.class, jks.password());

        assertEquals("certs/dev-keystore.jks", store.path());
        assertEquals("changeit", new String(password.value()));
    }
}
