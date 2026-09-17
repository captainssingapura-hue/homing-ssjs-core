package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * An EsModule that serves as the entry point of a single-page application.
 * <p>By convention, an AppModule exports a function named {@code appMain}
 * that accepts a root DOM element. The generated HTML scaffold imports
 * and invokes this function automatically.</p>
 *
 * <p>As of RFC 0001, AppModule is also a {@link Linkable} — every AppModule
 * has a {@link #simpleName()} (defaulting to a kebab-case derivation of its
 * class name) usable as the {@code ?app=} URL identifier, and may declare
 * a typed {@link #paramsType()} for query parameters.</p>
 *
 * @param <M> self-type
 */
public non-sealed interface AppModule<P extends AppModule._Param, M extends AppModule<P, M>>
        extends DomModule<M>, Linkable, StatelessFunctionalObject {

    /**
     * Marker for the {@code Params} record an AppModule accepts via its URL
     * query string. Every concrete params record implements this.
     *
     * <p>Apps with no params declare {@code AppModule<AppModule._None, MyApp>}
     * and pass {@link _None#INSTANCE} where a {@code P} value is required.</p>
     */
    interface _Param {}

    /** Sentinel for paramless apps. Use as the {@code P} type argument and
     *  pass {@link #INSTANCE} where a params value is needed. */
    record _None() implements _Param {
        public static final _None INSTANCE = new _None();
    }

    /**
     * The title for the generated HTML page (browser tab + scaffold). Typically
     * a fully-qualified label like {@code "Homing · studio · browse"}.
     */
    String title();

    /**
     * Public URL identifier for this app. Defaults to a kebab-case
     * derivation of the simple class name (e.g., {@code PitchDeck} →
     * {@code "pitch-deck"}). Override to lock the URL contract
     * independently of the Java class name.
     */
    @Override
    default String simpleName() {
        return Linkable.defaultSimpleName(this.getClass());
    }

    /**
     * The Java record describing this app's typed query parameters. Defaults
     * to {@link _None}{@code .class} for paramless apps; with-params apps
     * declare {@code AppModule<MyParams, MyApp>} and override this to
     * {@code MyParams.class}.
     *
     * <p>The unchecked cast is sound when {@code P} is {@link _None} and
     * implementor doesn't override; with-params apps that fail to override
     * get a class-cast lie at runtime — a discipline error caught by the
     * type-parameter mismatch with {@link _None}.</p>
     *
     * <p>Each component of the record becomes one URL query key on outgoing
     * links and is parsed back into a typed value on the receiving side
     * (RFC 0001).</p>
     */
    @Override
    @SuppressWarnings("unchecked")
    default Class<P> paramsType() {
        return (Class<P>) _None.class;
    }

    /**
     * RFC 0051 — this app's query parameters, read and written by one object.
     *
     * <p>{@link #paramsType()} says what the params ARE; this says how they
     * travel. The pair was previously split across a hand-written
     * {@code ParamMarshaller} per action and {@code Navigable.url()}'s
     * component walk, neither of which knew about the other.</p>
     *
     * <p>Defaults to the paramless codec, which is correct exactly when
     * {@link #paramsType()} was also left alone. An app that declares params
     * and forgets to override this gets the same class-cast mismatch the
     * paramsType contract already describes — the two are overridden
     * together or neither is.</p>
     */
    @SuppressWarnings("unchecked")
    default ParamCodec<P> paramCodec() {
        return (ParamCodec<P>) ParamCodec.None.INSTANCE;
    }

    /**
     * Marker for the {@code appMain} export.
     * Use the record name {@code appMain} so the JS identifier matches.
     */
    interface _AppMain<P extends AppModule._Param, M extends AppModule<P, M>> extends Exportable._Constant<M> {}
}
