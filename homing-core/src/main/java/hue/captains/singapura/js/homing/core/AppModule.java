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
     * Marker for the {@code appMain} export.
     * Use the record name {@code appMain} so the JS identifier matches.
     */
    interface _AppMain<P extends AppModule._Param, M extends AppModule<P, M>> extends Exportable._Constant<M> {}

    /**
     * Whether this app accepts the active theme's backdrop event handling.
     * Default {@code true} — most apps (catalogue browsers, doc readers,
     * SVG viewers) coexist fine with a theme that gates pointer-events to
     * forms + framework chrome + the backdrop's own clickable parts.
     *
     * <p>Workspace-style apps that own the full viewport with their own
     * interactive elements (MultiTabPane, future {@code WorkspaceShell})
     * override to {@code false}. The framework then adds a
     * {@code homing-bg-passive} class to {@code <body>}, which themes that
     * declare {@link Theme#backdropInteractivity()} respect by gating
     * their universal {@code body, body * { pointer-events: none }} rule
     * with a {@code :not(.homing-bg-passive)} selector. Net effect: the
     * workspace gets the theme's <i>look</i> (palette + backdrop SVG)
     * without fighting it for <i>event handling</i>.</p>
     *
     * <p>See the case study "Universal Pointer-Events Off Doesn't
     * Compose" for why this opt-out exists and what the deeper fix
     * (deferred) would look like.</p>
     *
     * @since RFC 0025 L2.2 — added when MultiTabPane drag handlers were
     *        blocked by Maple Bridge's universal pointer-events rule
     */
    default boolean acceptsBackdropInteractivity() { return true; }
}
