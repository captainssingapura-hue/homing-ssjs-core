package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.tao.http.config.ResolvedTlsCredential;
import hue.captains.singapura.tao.http.config.TlsConfigResolver;
import hue.captains.singapura.tao.http.config.TlsValidationException;
import hue.captains.singapura.tao.http.config.TlsValidationReport;
import hue.captains.singapura.tao.http.config.TlsValidator;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.io.IOException;
import java.util.Optional;

/**
 * Standalone TLS preflight for a {@link RuntimeParams} — resolves and validates its
 * keystore <em>without starting a server</em>, so a downstream app can check its HTTPS
 * configuration at boot, in a CLI, or in a test and surface a clear error instead of a
 * Vert.x stack trace at bind time.
 *
 * <p>Runs the two ja-http stages — {@link TlsConfigResolver} then {@link TlsValidator} — by
 * invoking the credential's own provider functions, so a passing preflight reflects exactly
 * what the server will load. The downstream never touches keystore loading itself.</p>
 *
 * <pre>{@code
 * var params = HttpsRuntimeParams.jks(8443, "certs/server.jks", "changeit");
 * TlsValidationReport report = new TlsPreflight().inspect(params).orElseThrow();
 * if (!report.validAt(Instant.now())) { ...refuse to start / warn... }
 * }</pre>
 */
public final class TlsPreflight implements StatelessFunctionalObject {

    /**
     * Resolves and validates {@code params}' TLS material.
     *
     * @return the validation report, or empty when {@code params} serves plain HTTP
     * @throws TlsValidationException with {@code MATERIAL_UNRESOLVABLE} if the keystore
     *         cannot be resolved (e.g. missing file), or a load-failure kind
     *         ({@code WRONG_PASSWORD}, {@code BAD_FORMAT}, {@code UNSUPPORTED})
     */
    public Optional<TlsValidationReport> inspect(RuntimeParams params) throws TlsValidationException {
        var tls = params.tls();
        if (tls.isEmpty()) {
            return Optional.empty();
        }
        ResolvedTlsCredential resolved;
        try {
            resolved = new TlsConfigResolver().resolve(tls.get().credential());
        } catch (IOException e) {
            throw new TlsValidationException(TlsValidationException.Kind.MATERIAL_UNRESOLVABLE,
                    "Could not resolve TLS material: " + e.getMessage(), e);
        }
        return Optional.of(new TlsValidator().validate(resolved));
    }
}
