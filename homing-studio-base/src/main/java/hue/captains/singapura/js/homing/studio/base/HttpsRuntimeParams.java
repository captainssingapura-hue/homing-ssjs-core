package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.tao.http.config.TlsConfig;
import hue.captains.singapura.tao.http.config.TlsCredential;
import hue.captains.singapura.tao.http.config.builtin.FileByteSource;
import hue.captains.singapura.tao.http.config.builtin.LiteralPassword;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Optional;

/**
 * RFC 0012 — the HTTPS counterpart to {@link DefaultRuntimeParams}. Carries a
 * port plus a {@link TlsConfig}; presence of the TLS config is what flips the
 * bootstrap from HTTP to HTTPS.
 *
 * <p>The common case — a JKS keystore on disk — needs no knowledge of the
 * transport's TLS types:</p>
 *
 * <pre>{@code
 * // HTTP (off):
 * new Bootstrap<>(fixtures, new DefaultRuntimeParams(8080)).start();
 *
 * // HTTPS (on):
 * new Bootstrap<>(fixtures,
 *         HttpsRuntimeParams.jks(8443, "certs/dev-keystore.jks", "changeit")).start();
 * }</pre>
 *
 * <p>Advanced operators who build their own {@link TlsConfig} (e.g. a keystore
 * sourced from a vault) can use the canonical constructor directly.</p>
 */
public record HttpsRuntimeParams(int port, TlsConfig tlsConfig)
        implements RuntimeParams, ValueObject {

    /**
     * HTTPS backed by a Java KeyStore (JKS) file on the local filesystem.
     *
     * @param port         the port to bind
     * @param keystorePath path to the {@code .jks} file
     * @param password     the keystore password
     */
    public static HttpsRuntimeParams jks(int port, String keystorePath, String password) {
        var credential = new TlsCredential.Jks(
                new FileByteSource(keystorePath),
                LiteralPassword.of(password));
        return new HttpsRuntimeParams(port, new TlsConfig(credential));
    }

    @Override
    public Optional<TlsConfig> tls() {
        return Optional.of(tlsConfig);
    }
}
