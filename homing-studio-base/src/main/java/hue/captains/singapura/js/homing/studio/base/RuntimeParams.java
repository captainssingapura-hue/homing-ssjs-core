package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.core.util.ResourceReader;
import hue.captains.singapura.tao.http.config.TlsConfig;
import hue.captains.singapura.tao.ontology.Immutable;

import java.util.Optional;

/**
 * RFC 0012 — deployment knobs. Port + environment-derived defaults.
 * Independent of {@link Fixtures} — Fixtures arrives already-initialised
 * with everything downstream provides; RuntimeParams only describes how
 * this deployment <em>process</em> is wired.
 *
 * <p>Operators with env-specific fields (TLS, bind address, profiling
 * toggles) implement a richer subtype; the bootstrap only reads through
 * this base contract.</p>
 *
 * <pre>{@code
 * RuntimeParams params = new DefaultRuntimeParams(8080);
 * }</pre>
 */
public interface RuntimeParams extends Immutable {

    /** Port to bind. */
    int port();

    /**
     * TLS configuration for this deployment. When present, the bootstrap serves
     * HTTPS on {@link #port()}; when empty (the default) it serves plain HTTP.
     *
     * <p>This is the on/off switch for HTTPS: a downstream app keeps
     * {@code new DefaultRuntimeParams(port)} for HTTP, or supplies
     * {@link HttpsRuntimeParams#jks(int, String, String)} to switch HTTPS on
     * without touching any transport-level types.</p>
     */
    default Optional<TlsConfig> tls() {
        return Optional.empty();
    }

    /** Resource reader for classpath / disk lookups. Default: {@link ResourceReader#fromSystemProperty()}. */
    default ResourceReader resourceReader() {
        return ResourceReader.fromSystemProperty();
    }

    /**
     * RFC 0014 Phase 1 — opt-in diagnostic surfaces.
     *
     * <p>When {@code true}, {@code Bootstrap.compose()} registers the
     * {@code StudioGraphInspector} app and its backing {@code /graph-dump}
     * action, making the live object graph visible in the UI. When
     * {@code false} (default), neither the app nor the endpoint is
     * registered — diagnostic surfaces are absent from the running server.</p>
     *
     * <p>Default reads the {@code homing.diagnostics} system property —
     * launching with {@code -Dhoming.diagnostics=true} enables. Downstream
     * studios can override to enable diagnostics by other means (CLI flag,
     * env var, config file) by implementing their own {@code RuntimeParams}
     * subtype.</p>
     */
    default boolean diagnosticsEnabled() {
        return Boolean.getBoolean("homing.diagnostics");
    }
}
