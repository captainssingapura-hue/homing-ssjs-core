package hue.captains.singapura.js.homing.core;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RFC 0051 Phase 6 — an app address as the STRUCTURED pair it always was:
 * an app's simple name plus its arguments.
 *
 * <p>This is the type {@code Doc.url()} was standing in for. A Doc held a
 * viewer and an identifier, and the only way it could hand them over was to
 * serialise them into a URL that the framework then parsed back — the registry
 * to learn which keys identify the doc, the path route to learn which app opens
 * it. String to map to string, to recover a pair that was never given away.</p>
 *
 * <p>Being structured is the whole point: {@link #args()} answers the identity
 * question directly ({@code args.keySet()}), {@link #app()} answers the routing
 * question directly, and {@link #flat()} renders the string for the one job a
 * string is actually good for — being a link.</p>
 *
 * @param app  the AppModule's {@code simpleName()} — the dispatch, not an argument
 * @param args the app's own parameters, already encoded by its {@link ParamCodec}
 *
 * @since RFC 0051 Phase 6
 */
public record AppAddress(String app, Map<String, List<String>> args) {

    public AppAddress {
        Objects.requireNonNull(app,  "AppAddress.app");
        Objects.requireNonNull(args, "AppAddress.args");
        if (app.isBlank()) throw new IllegalArgumentException("AppAddress.app must not be blank");
        args = Map.copyOf(args);
    }

    /** Build from an app's typed params through its own codec — the normal call. */
    public static <P extends AppModule._Param> AppAddress of(
            String appName, ParamCodec<P> codec, P params) {
        return new AppAddress(appName, codec.to(params));
    }

    /** The flat {@code /app?app=…&…} rendering. */
    public String flat() {
        return AppUrl.flat(app, args);
    }
}
