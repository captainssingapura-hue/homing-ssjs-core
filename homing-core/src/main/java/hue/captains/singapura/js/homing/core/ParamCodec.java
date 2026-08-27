package hue.captains.singapura.js.homing.core;

import java.util.List;
import java.util.Map;

/**
 * RFC 0051 — an app's query parameters, in both directions, in one object.
 *
 * <p>Reading and writing an app's params were previously separate concerns in
 * separate places: {@code ParamMarshaller._QueryString} implementations pulled
 * strings off the request per action, while {@code Navigable.url()} wrote the
 * query by walking the record's components. Two implementations of one
 * correspondence can disagree, and nothing made them agree — so locking them
 * into a single object makes the round trip correct by construction rather
 * than by a test that has to remember to compare two things.</p>
 *
 * <p><b>Hand-written, by doctrine.</b> Codegen Over Reflection names
 * "schema-by-reflection at request time" as a violation and hand-written
 * codecs as compliant — "small-scale codegen, not an exception". So each app
 * writes its own {@code from}/{@code to} as straight-line field reads and
 * constructor calls. That is also what makes the failure cases nameable: a
 * generic decoder can only say "could not bind", while an app's own codec
 * knows that {@code ws_kind} was the missing key.</p>
 *
 * <p><b>Existence is not a codec concern.</b> {@code from} answers whether the
 * args are well-formed, never whether the thing they name exists. That
 * separation is what lets a handler tell 400 from 404 — a distinction the
 * current actions cannot make, since they return one notFound for a malformed
 * id, an absent id and an id that simply names nothing.</p>
 *
 * @param <P> the app's params record
 */
public interface ParamCodec<P extends AppModule._Param> {

    /**
     * Read params from a query map, or say precisely why not.
     *
     * <p>The input is the uniform {@code Map<String, List<String>>} shape, so
     * a codec never branches on whether a key was repeated.</p>
     */
    Decoded<P> from(Map<String, List<String>> query);

    /**
     * Write params to a query map. Total: a valid {@code P} always has a
     * representation, so there is no error case and no caller has to handle
     * one.
     */
    Map<String, List<String>> to(P params);

    /** Convenience: the encoded query string for these params, without a
     *  leading {@code ?}. */
    default String toQueryString(P params) {
        return QueryString.encode(to(params));
    }

    /** Convenience: decode straight from a raw query string or URL. */
    default Decoded<P> fromQueryString(String query) {
        return from(QueryString.parse(query));
    }

    /**
     * The result of reading params: the value, or a named reason it could not
     * be read.
     *
     * <p>A sum rather than {@code null} or an exception. A null cannot say
     * which key was wrong, and an exception makes the ordinary case of a
     * user-typed URL into a stack trace — both of which push the handler
     * toward a single generic error where it should be producing a specific
     * one.</p>
     */
    sealed interface Decoded<P extends AppModule._Param> {

        /** The params were read. */
        record Ok<P extends AppModule._Param>(P params) implements Decoded<P> {}

        /** A required key was absent. */
        record Missing<P extends AppModule._Param>(String key) implements Decoded<P> {
            @Override public String describe() { return "missing required parameter '" + key + "'"; }
        }

        /**
         * A key was present but its value could not be read as the type the
         * app expects.
         *
         * @param key      the parameter
         * @param value    what arrived, for the message — never re-emitted
         *                 into a page without escaping
         * @param expected what the app wanted, in words
         */
        record Malformed<P extends AppModule._Param>(String key, String value, String expected)
                implements Decoded<P> {
            @Override public String describe() {
                return "parameter '" + key + "' is not " + expected;
            }
        }

        /** The params when this decoded successfully, else null. */
        default P orNull() { return this instanceof Ok<P>(P p) ? p : null; }

        default boolean isOk() { return this instanceof Ok; }

        /** A one-line explanation suitable for a 400 response body. */
        default String describe() { return "ok"; }

        static <P extends AppModule._Param> Decoded<P> ok(P params) { return new Ok<>(params); }
        static <P extends AppModule._Param> Decoded<P> missing(String key) { return new Missing<>(key); }
        static <P extends AppModule._Param> Decoded<P> malformed(String key, String value, String expected) {
            return new Malformed<>(key, value, expected);
        }
    }

    /**
     * The codec for paramless apps: reads anything, writes nothing.
     *
     * <p>Deliberately ignores unknown keys rather than rejecting them. A
     * paramless app reached with {@code ?theme=forest} is a normal event —
     * the action layer owns the reserved keys — and an app has no business
     * refusing a query it was never asked to read.</p>
     */
    final class None implements ParamCodec<AppModule._None> {

        public static final None INSTANCE = new None();

        private None() {}

        @Override public Decoded<AppModule._None> from(Map<String, List<String>> query) {
            return Decoded.ok(AppModule._None.INSTANCE);
        }

        @Override public Map<String, List<String>> to(AppModule._None params) {
            return Map.<String, List<String>>of();
        }
    }

    /** Shorthand used by codecs that read a single required key. */
    static List<String> values(Map<String, List<String>> query, String key) {
        List<String> v = query == null ? null : query.get(key);
        return v == null ? List.of() : v;
    }

    /**
     * A codec for the common shape: one required key, one single-component
     * record. The viewers all have it — {@code Params(String id)} — and
     * writing the same eight lines per app would be copying, not authoring.
     *
     * <p>Still hand-written in the doctrinal sense: the caller supplies the
     * constructor and the accessor as method references, so the runtime path
     * is straight-line calls with nothing introspected.</p>
     *
     * @param key    the query key
     * @param read   builds the record from the key's value
     * @param write  reads the value back out of the record
     */
    /**
     * The codec for an app whose {@code Params} record has no components —
     * reads anything into the one value that record can hold, writes nothing.
     *
     * <p>RFC 0051 — distinct from {@link None}, which is typed to
     * {@link AppModule._None} and throws on any other Params. An app with an
     * empty record of its own is genuinely paramless but cannot use
     * {@code None}, so before this it had no codec at all: its address was
     * minted by reflection and its identity could not be constructed from a
     * request. That is the whole of what kept five demo apps on the reflected
     * fallback.</p>
     *
     * @param only the single value the empty record denotes, e.g. {@code Params::new}
     */
    static <P extends AppModule._Param> ParamCodec<P> ofEmpty(java.util.function.Supplier<P> only) {
        return new ParamCodec<>() {
            @Override public Decoded<P> from(Map<String, List<String>> query) {
                return Decoded.ok(only.get());
            }
            @Override public Map<String, List<String>> to(P params) {
                return Map.of();
            }
        };
    }

    static <P extends AppModule._Param> ParamCodec<P> ofSingle(
            String key, java.util.function.Function<String, P> read,
            java.util.function.Function<P, String> write) {

        return new ParamCodec<>() {
            @Override public Decoded<P> from(Map<String, List<String>> query) {
                String value = QueryString.first(query, key);
                if (value == null || value.isBlank()) return Decoded.missing(key);
                return Decoded.ok(read.apply(value));
            }
            @Override public Map<String, List<String>> to(P params) {
                return QueryString.of(key, write.apply(params));
            }
        };
    }
}
