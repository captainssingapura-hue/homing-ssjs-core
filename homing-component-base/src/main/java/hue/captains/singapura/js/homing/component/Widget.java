package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.Linkable;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * The widget contract, in its minimal form: a {@link DomModule} that exports
 * one class,
 *
 * <pre>
 *   new Widget(branch, params) → an instance with root, setActive?, dispose?, …
 * </pre>
 *
 * <p>{@code branch} is the widget's own, handed unactivated; the widget
 * activates it with its owner and mints everything it makes on it. {@code
 * params} are its typed params, decoded — everything the widget IS. Both
 * come in by the constructor, as everything a component depends on does.</p>
 *
 * <p>The instance is the controller. {@code root} is the element the host
 * attaches, and the only one the host ever touches: the host puts it in the
 * DOM to show the widget and takes it out to hide it, and the widget stays
 * constructed in between — one host may hold many constructed widgets and
 * have one of them mounted. {@code setActive(bool)}, if present, is told
 * each time that happens. {@code dispose()}, if present, is called before
 * the widget's branch is dissolved, for what dissolving cannot release: a
 * subscription, a timer. Whatever else the instance carries is the widget's
 * own surface, for a holder that knows what it holds.</p>
 *
 * <p>A class, never a closure. A component built as a function returning a
 * bag of inner functions has state no one can name, no identity, no
 * {@code instanceof}, no method set a contract can hold it to, and a
 * lifetime that is whatever its captures make it. A class has all four, and
 * its dependencies are read off its constructor. The framework knows two
 * shapes of component: an <b>element</b> component tells its caller which
 * element to mint — {@code static TAG} — and takes that element (a button,
 * a chip); a <b>branch</b> component takes the sub-branch its caller made
 * for it and mints its own tree on it (a card, a pane, a widget). A widget
 * is the branch shape with typed params. The conformance rule
 * {@code exports-are-classes} holds every served module to this.</p>
 *
 * <p>There is no channel and no context in the contract. A widget talks to
 * the world in one of two ways: it JOINS a party — the steward, a workspace
 * party — and writes and listens there, which is how a preference widget
 * writes a preference and how everyone else learns of it; or it lets its
 * holder OPERATE it directly, through the instance, which is how a dialog
 * drives the widget it holds. A host that needs to reach a widget it holds
 * has the instance; a widget that needs to reach beyond its holder has the
 * party. Neither needs the contract to carry a bag or a function.</p>
 *
 * <p>This is the shape the workspace has hosted widgets by since RFC 0025
 * ({@code WorkspaceWidget.construct}), as a class the widget writes in its
 * own file. It serves any host that adds and removes widgets — a preferences
 * dialog, a workspace that loads a single widget statically, the multi-tab
 * one — through {@link WidgetSlotModule WidgetSlot}. The studio's
 * {@code Widget} and the workspace's {@code WorkspaceWidget} are untouched
 * and keep serving their hosts until each swaps. How params travel — in an
 * address, in a store — is a host's concern and arrives with the host that
 * has it.</p>
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

    /**
     * The class export: a record named as the JS class, so the identifier
     * matches. A widget module exports its class and nothing else.
     */
    interface _Class<P extends _Param, W extends Widget<P, W>> extends BranchComponent<W> {
        @Override default Shape shape() { return Shape.WIDGET; }
    }

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
