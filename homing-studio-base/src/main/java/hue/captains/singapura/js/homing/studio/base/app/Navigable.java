package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.AppUrl;
import hue.captains.singapura.js.homing.core.ParamCodec;

import java.lang.reflect.RecordComponent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A typed binding of an {@link AppModule} to its {@code Params} record, plus
 * the catalogue-tile display data ({@code name} + {@code summary}). Wrapped
 * inside {@link Entry.OfApp} when an {@link AppModule} appears as a catalogue
 * tile.
 *
 * <p>The framework's URL is fully formed only when both halves — the AppModule
 * and its bound Params — are known. {@code AppModule} alone is half: a bare
 * URL like {@code /app?app=catalogue} is broken without {@code &id=…}. The
 * {@code Navigable} record supplies the missing half plus the user-facing
 * tile name/summary.</p>
 *
 * <p>Strong typing: {@code P} matches the AppModule's declared params type,
 * enforced at compile time via the type parameter on {@link AppModule}. A
 * compile error catches "wrong params for app" — no runtime reflection
 * needed, no opportunity for silent broken URLs.</p>
 *
 * <p>Construction examples:</p>
 *
 * <pre>{@code
 * // Paramless app — pass _None.INSTANCE for the params slot.
 * new Navigable<>(MyDocBrowser.INSTANCE, AppModule._None.INSTANCE,
 *                 "Documents", "Browse all docs.");
 *
 * // App with typed params — compiler enforces P matches the app's type.
 * new Navigable<>(CatalogueAppHost.INSTANCE,
 *                 new CatalogueAppHost.Params("...MyHomeCatalogue"),
 *                 "Doctrines", "The rules that hold the design together.");
 * }</pre>
 *
 * @param <P> the AppModule's {@code Params} record type ({@link AppModule._None} for paramless)
 * @param <M> self-type of the bound AppModule
 *
 * @since v1 (re-introduced post-RFC-0005-ext1, as a typed record rather than the original marker)
 */
public record Navigable<P extends AppModule._Param, M extends AppModule<P, M>>(
        M app,
        P params,
        String name,
        String summary
) {

    public Navigable {
        Objects.requireNonNull(app,    "Navigable.app");
        Objects.requireNonNull(params, "Navigable.params (use AppModule._None.INSTANCE for paramless apps)");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Navigable.name must not be blank");
        }
        if (summary == null) summary = "";
    }

    /**
     * The fully-formed URL for this binding — {@code /app?app=<simpleName>}
     * plus the params as a query string.
     *
     * <p>RFC 0051 (D8): an app that declares a {@link ParamCodec} mints
     * through it, so the address and the app's own parse are one statement —
     * {@code from(to(p)) == p} is asserted by {@code ParamCodecLaw}, which
     * means a URL minted here is a URL that app can read back.</p>
     *
     * <p>An app WITHOUT a codec still falls back to reflecting over the
     * Params record. That fallback is the last of D8's six minting sites and
     * the reason url() is not yet retired: it is also the "schema-by-
     * reflection at request time" the Codegen Over Reflection doctrine bans,
     * and it silently drops empty-string components — so {@code Params(id,
     * "")} survives a codec but not this. Every app that carries a codec has
     * left the fallback; the remaining ones are demo apps, tracked with the
     * ParamsWriter retirement they share a cause with.</p>
     *
     * <p>{@code _None} params produce just {@code /app?app=<simpleName>} on
     * either path.</p>
     */
    public String url() {
        var codec = app.paramCodec();
        if (codec != ParamCodec.None.INSTANCE) {
            return AppUrl.flat(app, params);
        }
        return reflectedUrl();
    }

    /** Pre-codec minting — see {@link #url()} for why it is still here. */
    private String reflectedUrl() {
        StringBuilder sb = new StringBuilder("/app?app=").append(app.simpleName());
        if (params instanceof AppModule._None) return sb.toString();

        Class<?> recCls = params.getClass();
        if (!recCls.isRecord()) return sb.toString();   // should be unreachable given the type bound
        for (RecordComponent rc : recCls.getRecordComponents()) {
            try {
                Object val = rc.getAccessor().invoke(params);
                if (val == null) continue;
                String s = val.toString();
                if (s.isEmpty()) continue;
                sb.append('&')
                  .append(URLEncoder.encode(rc.getName(), StandardCharsets.UTF_8))
                  .append('=')
                  .append(URLEncoder.encode(s, StandardCharsets.UTF_8));
            } catch (ReflectiveOperationException e) {
                // best-effort: skip unreachable components
            }
        }
        return sb.toString();
    }
}
