package hue.captains.singapura.js.homing.core;

/**
 * RFC 0051 (D8) — the one place a flat {@code /app?app=…&…} address is minted.
 *
 * <p>Before this, six hand-rolled implementations of the same idea coexisted:
 * {@code Navigable.url()} reflecting over the Params record, the four content
 * Doc kinds each concatenating {@code "/app?app=x&id=" + uuid}, and the two
 * host {@code urlFor} helpers. They agreed only because the values they were
 * given — UUIDs and class names — happen to contain nothing that needs
 * escaping. The first value that did would have made them disagree one at a
 * time, and the emitter that produced a broken link would be whichever one
 * the caller happened to reach.</p>
 *
 * <p>Minting through the app's own {@link ParamCodec} also makes the address
 * and its parse the same statement: {@code from(to(p)) == p} is asserted by
 * {@link ParamCodecLaw}, so a URL this class mints is a URL the app can read
 * back. Concatenation carried no such guarantee.</p>
 *
 * @since RFC 0051 Phase 3
 */
public final class AppUrl {

    private AppUrl() {}

    /** The flat address for {@code app} carrying {@code params}. */
    public static <P extends AppModule._Param> String flat(AppModule<P, ?> app, P params) {
        return flat(app.simpleName(), app.paramCodec(), params);
    }

    /**
     * The flat address for an app named {@code appName}, encoded by
     * {@code codec}. Use this form where the AppModule instance would drag an
     * import cycle in — a Doc kind naming its viewer, for instance.
     */
    public static <P extends AppModule._Param> String flat(
            String appName, ParamCodec<P> codec, P params) {

        String query = codec.toQueryString(params);
        String head = "/app?app=" + appName;
        return query.isEmpty() ? head : head + "&" + query;
    }
}
