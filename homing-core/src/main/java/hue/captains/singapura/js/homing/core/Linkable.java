package hue.captains.singapura.js.homing.core;

/**
 * Common supertype for navigation targets — anything that can be the destination
 * of a typed link.
 *
 * <p>Implemented by {@link AppModule} (an internal app served by the kernel) and,
 * once Step 03 lands, by {@code ProxyApp} (a typed declaration for an external
 * URL like a GitHub repo or docs site). Both are addressable by their
 * {@link #simpleName()} and may declare a typed {@link #paramsType()} record.</p>
 *
 * <p>Introduced in RFC 0001.</p>
 *
 * <p><b>Sealed:</b> the {@code permits} clause lists {@link AppModule} (the
 * internal-app case) and {@link ProxyApp} (the typed external-URL case).
 * Both kinds participate in the same registry and the same {@link AppLink}
 * import mechanism; the resolver and writer differentiate at the type
 * level when they need to.</p>
 */
public sealed interface Linkable extends Importable permits AppModule, ProxyApp {

    /**
     * The public URL identifier for this linkable. Used as the value of the
     * {@code ?app=} query parameter. Defaults — when not overridden — to the
     * kebab-case derivation of the simple class name (see {@link SimpleNames#of(Class)}).
     *
     * <p>Override to lock the URL contract independently of the Java class
     * name (e.g., to keep a stable URL through future class renames).</p>
     */
    String simpleName();

    /**
     * The Java record describing this linkable's typed query parameters,
     * or {@code Void.class} for parameter-less linkables (the default).
     *
     * <p>Each component of the record becomes one URL query key on
     * outgoing links, and is parsed back into a typed value on the
     * receiving side.</p>
     */
    Class<?> paramsType();

}
