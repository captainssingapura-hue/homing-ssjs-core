package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * RFC 0024 — the framework's unit of navigable composition.
 *
 * <p>A Widget is a self-contained {@link DomModule} that exports a
 * {@code mountInto(branch, params)} JS function: given a DomOpsParty
 * branch handle (the L2 widget slot the shell allocates) and typed
 * params, it populates the branch with its DOM subtree. The widget
 * owns nothing above its branch — chrome, layout, navigation are the
 * shell's concern.</p>
 *
 * <h2>Two consumption modes</h2>
 *
 * <ol>
 *   <li><b>Direct addressing</b> — URL like {@code /app?app=standard-mpa&widget=X&id=...}
 *       routes to a {@code StandardMPA} shell, which reads the {@code widget}
 *       param, looks up the matching Widget from its registered set, and
 *       calls its {@code mountInto} against the shell's L2 widget slot.
 *       The framework adds the chrome.</li>
 *   <li><b>Composition</b> — any other {@link DomModule} declares an
 *       {@link ImportsFor} entry on the widget's {@code _MountInto} export,
 *       receives the {@code mountInto} function, and calls it against a
 *       sub-branch of its own. The shell is not involved. The widget code
 *       is identical to the directly-addressed case.</li>
 * </ol>
 *
 * <p>The unification: a widget is the unit; the shell is one specific
 * consumer. Future SPA workspace shells are other consumers. Composition
 * inside other widgets is a third consumer. All three reuse the same
 * widget code.</p>
 *
 * <h2>Relationship to {@link AppModule}</h2>
 *
 * <p>Widget is a <i>sibling</i> of AppModule, not a sub-type. AppModule is
 * a full-page entry point (its {@code appMain(rootElement)} owns the whole
 * document body); Widget is a slot-filler (its {@code mountInto(branch,
 * params)} owns a sub-branch of someone else's tree).</p>
 *
 * <p>Concretely:</p>
 * <ul>
 *   <li>{@code StandardMPA} is an {@code AppModule} (the framework's
 *       default shell — owns the whole document, hosts one widget at a
 *       time at L2 under main).</li>
 *   <li>{@code SvgWidget}, {@code ComposedWidget}, etc. are Widgets
 *       (hosted by the shell; never own a document directly).</li>
 *   <li>Pre-widget free-form apps stay as {@code @LegacyAppMain}-marked
 *       AppModules — the permanent free-form bucket.</li>
 * </ul>
 *
 * <h2>The {@code _MountInto} export contract</h2>
 *
 * <p>Each concrete Widget declares an inner record {@code public record
 * mountInto() implements Widget._MountInto<Params, Self> {}} and includes
 * it in its {@link #exports()}. The JS-side identifier the framework's
 * {@code EsModuleWriter} produces is {@code mountInto} (matching the
 * record's simple name). Importers see it as a JS function with the
 * signature {@code mountInto(branch, params)}.</p>
 *
 * @param <P> typed Params record this widget accepts
 * @param <W> self-type (CRTP)
 * @since RFC 0024 Phase P1a
 */
public interface Widget<P extends Widget._Param, W extends Widget<P, W>>
        extends DomModule<W>, StatelessFunctionalObject {

    /**
     * Marker for the typed Params record this Widget accepts. Same shape
     * as {@link AppModule._Param} — a sibling marker rather than a shared
     * one so the type system can distinguish widget-params from
     * app-params at the boundary.
     */
    interface _Param {}

    /** Sentinel for paramless widgets. */
    record _None() implements _Param {
        public static final _None INSTANCE = new _None();
    }

    /**
     * Public identifier for this widget — used as the URL {@code ?widget=}
     * value when hosted by a shell, and as the L2 branch name under the
     * shell's main slot. Defaults to the kebab-case derivation of the
     * concrete class's simple name (e.g. {@code SvgWidget} →
     * {@code "svg-widget"}). Override to lock the URL contract
     * independently of the Java class name.
     */
    default String simpleName() {
        return Linkable.defaultSimpleName(this.getClass());
    }

    /**
     * Human-readable label for this widget — used by shell chrome as the
     * breadcrumb leaf hint when the widget is mounted directly. Concrete
     * widgets that fetch a Doc by id typically overwrite the breadcrumb
     * leaf with the Doc's title at fetch time; this string is the
     * pre-fetch placeholder.
     */
    String title();

    /**
     * The Java record describing this widget's typed query parameters.
     * Defaults to {@link _None}{@code .class} for paramless widgets;
     * widgets with params declare {@code Widget<MyParams, MyWidget>} and
     * override this to {@code MyParams.class}.
     *
     * <p>Each component of the record becomes one URL query key on
     * outgoing links and is parsed back into a typed value on the
     * receiving side — same reflection mechanism the framework already
     * uses for {@link AppModule#paramsType()}.</p>
     */
    @SuppressWarnings("unchecked")
    default Class<P> paramsType() {
        return (Class<P>) _None.class;
    }

    /**
     * Marker for the {@code mountInto} export. Each concrete Widget
     * declares an inner record {@code public record mountInto()
     * implements Widget._MountInto<Params, Self> {}} so the framework's
     * {@code EsModuleWriter} emits the function under the JS identifier
     * {@code mountInto} (matching the record's simple name).
     */
    interface _MountInto<P extends Widget._Param, W extends Widget<P, W>>
            extends Exportable._Constant<W> {}
}
