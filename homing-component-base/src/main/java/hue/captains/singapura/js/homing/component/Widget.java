package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.Linkable;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * The widget contract, in its minimal form: a {@link DomModule} that exports
 * one function,
 *
 * <pre>
 *   construct(branch, params, ctx) → { root, setActive?, dispose? }
 * </pre>
 *
 * <p>{@code branch} is the widget's own, handed unactivated; the widget
 * activates it with its owner and mints everything it makes on it. {@code
 * params} are its typed params, decoded. {@code ctx} is what this host
 * offers, documented per host — a widget ignores what it does not know.</p>
 *
 * <p>The controller is a plain object. {@code root} is the element the host
 * attaches, and the only one the host ever touches: the host puts it in the
 * DOM to show the widget and takes it out to hide it, and the widget stays
 * constructed in between — one host may hold many constructed widgets and
 * have one of them mounted. {@code setActive(bool)}, if present, is told
 * each time that happens. {@code dispose()}, if present, is called before
 * the widget's branch is dissolved, for what dissolving cannot release: a
 * subscription, a timer.</p>
 *
 * <p>This is the shape the workspace has hosted widgets by since RFC 0025
 * ({@code WorkspaceWidget.construct}), without that class's generated body:
 * a widget writes its own {@code construct} in its own file. It serves any
 * host that adds and removes widgets — a preferences dialog, a workspace
 * that loads a single widget statically, the multi-tab one — through
 * {@link WidgetSlot}. The studio's {@code Widget} and the workspace's
 * {@code WorkspaceWidget} are untouched and keep serving their hosts until
 * each swaps. How params travel — in an address, in a store — is a host's
 * concern and arrives with the host that has it.</p>
 *
 * @param <P> the widget's params record
 * @param <W> self
 */
public interface Widget<P extends Widget._Param, W extends Widget<P, W>>
        extends DomModule<W>, StatelessFunctionalObject {

    /** Marker for a widget's params record. */
    interface _Param {}

    /** The params of a widget that takes none. */
    record _None() implements _Param {
        public static final _None INSTANCE = new _None();
    }

    /** The {@code construct} export. Named so, so the JS identifier matches. */
    interface _Construct<P extends _Param, W extends Widget<P, W>> extends Exportable._Constant<W> {}

    /** The widget's name for a host that keeps widgets by name; kebab-case of the class by default. */
    default String simpleName() {
        return Linkable.defaultSimpleName(this.getClass());
    }

    /** What a host may show for it. */
    String title();

    /** The params record; {@link _None} unless overridden. */
    @SuppressWarnings("unchecked")
    default Class<P> paramsType() {
        return (Class<P>) _None.class;
    }
}
