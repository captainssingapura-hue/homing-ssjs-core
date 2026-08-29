package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.AppUrl;
import hue.captains.singapura.js.homing.core.ParamCodec;

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
     * <p>RFC 0051 A9 — THE REFLECTED FALLBACK IS GONE. Until Phase 6 this
     * method branched: an app with no codec had its Params walked by
     * reflection instead. That was the last of D8's six minting sites, and it
     * was also the "schema-by-reflection at request time" the Codegen Over
     * Reflection doctrine bans — silently dropping empty-string components, so
     * {@code Params(id, "")} survived a codec but not the reflection.</p>
     *
     * <p>It survived because five paramless demo apps could not declare a
     * codec: {@code ParamCodec.None} is typed to {@code _None} and throws on
     * any other Params, so an app with an empty {@code record Params()} had
     * nothing to declare. {@code ParamCodec.ofEmpty} closed that, every placed
     * app now carries a correctly-typed codec, and the branch has no live
     * caller left.</p>
     *
     * <p>{@code _None} params still produce just {@code /app?app=<simpleName>},
     * because {@code None.to(_None.INSTANCE)} is the empty map — the same
     * string the reflection produced, by the ordinary path rather than a
     * special case.</p>
     */
    public String url() {
        return AppUrl.flat(app, params);
    }
}
