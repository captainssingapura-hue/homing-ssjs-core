package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.studio.base.widget.WorkspaceMPA;

import java.util.List;
import java.util.Objects;

/**
 * RFC 0025 — abstract base for workspace AppModules. Subclasses
 * declare:
 *
 * <ol>
 *   <li>{@link #widgetEntries()} — the registry of widget types this
 *       workspace supports (Mechanism 1, this Ext1a).</li>
 *   <li>{@code appMain()} — typed marker (inherited from
 *       {@link WorkspaceMPA} → {@link
 *       hue.captains.singapura.js.homing.studio.base.widget.SingleWidgetMPA}
 *       → {@link hue.captains.singapura.js.homing.studio.base.widget.StandardMPA}).</li>
 *   <li>{@code widget()} — the workspace's <i>chrome</i> widget — an
 *       RFC 0024 Widget that draws the workspace shell itself (layout
 *       chrome, picker affordance, etc.). Later mechanisms (Ext1b/c/d)
 *       define what this widget does; for now it's still a normal RFC
 *       0024 Widget the subclass provides.</li>
 * </ol>
 *
 * <p>WorkspaceShell extends {@link WorkspaceMPA}, so subclasses inherit
 * the full-viewport main slot and the opt-out of theme backdrop
 * interactivity. The "behaviours trapped in a page" profile is the
 * right default for any workspace.</p>
 *
 * <h2>Boot-time validation</h2>
 *
 * <p>The framework calls {@link #widgetEntries()} once at boot. If the
 * returned list is empty, construction throws
 * {@link IllegalStateException} — a workspace with no widget types
 * has no purpose, and refusing fast prevents shipping a broken page.
 * The {@link #validateWidgetEntries()} helper performs this check; it
 * runs in {@link AppModule#imports()} via the lazy {@link #ensureValidated()}
 * latch so the validation happens once per JVM lifetime.</p>
 *
 * <p>Subsequent mechanisms (Ext1b — Picker, Ext1c — Param Input,
 * Ext1d — Seamless Entry) add code-gen, controller bootstrap, and
 * picker UI on top of this base. This Ext1a's WorkspaceShell ships
 * <i>just the registry</i>; the page renders without a picker until
 * the later mechanisms land.</p>
 *
 * @param <P> typed URL Params for the workspace itself
 * @param <M> self-type (CRTP)
 * @since RFC 0025 Ext1a — Mechanism 1 (Widget Type Registry)
 */
public abstract class WorkspaceShell<P extends AppModule._Param, M extends WorkspaceShell<P, M>>
        extends WorkspaceMPA<P, M> {

    private volatile boolean validated = false;

    /**
     * The widget types this workspace supports. Build-time declaration
     * — subclass override. Boot-time alternative: subclasses may hold
     * the list as a constructor field and return it; the framework
     * doesn't care how the list is produced, only that it's stable
     * across calls.
     *
     * <p>Empty lists throw at boot. See class doc for rationale.</p>
     */
    protected abstract List<WidgetEntry> widgetEntries();

    /**
     * Validates the widget entries list — non-null, non-empty, each
     * entry non-null. Called automatically the first time the
     * framework touches this AppModule's surface.
     */
    protected final void validateWidgetEntries() {
        List<WidgetEntry> entries = widgetEntries();
        Objects.requireNonNull(entries,
                getClass().getName() + ".widgetEntries() returned null");
        if (entries.isEmpty()) {
            throw new IllegalStateException(
                    getClass().getName() + ".widgetEntries() returned an empty list. "
                  + "A workspace with no widget types serves no purpose; "
                  + "register at least one WorkspaceWidget. "
                  + "(RFC 0025 Ext1a — Mechanism 1 D1.c, empty registry refused.)");
        }
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i) == null) {
                throw new IllegalStateException(
                        getClass().getName() + ".widgetEntries() contains null at index " + i);
            }
        }
    }

    /** Latch — ensures validation runs exactly once. Subclasses that want
     *  to invoke validation eagerly (e.g., in their constructor) can
     *  call this directly. */
    protected final void ensureValidated() {
        if (!validated) {
            synchronized (this) {
                if (!validated) {
                    validateWidgetEntries();
                    validated = true;
                }
            }
        }
    }

    /**
     * The registry serialized as a JSON literal — chrome widgets emit
     * this into the page (e.g. as a JS variable assignment) so {@code
     * WidgetPickerModule.js} can read it at boot. See {@link
     * WidgetEntriesJson} for the wire shape.
     *
     * <p>Validates the registry on first call (idempotent) — the
     * empty-registry refusal fires here too, so a workspace whose
     * chrome forgets to call any other validation entry-point still
     * fails at boot rather than shipping a broken picker.</p>
     */
    public final String widgetEntriesJson() {
        ensureValidated();
        return WidgetEntriesJson.of(widgetEntries());
    }

    /**
     * The workspace's ribbon contents. Defaults to an empty list —
     * the framework still renders the ribbon with the workspace title
     * (left) and the fullscreen toggle (right); {@code ribbonItems()}
     * fills the middle. Override to add buttons / labels / separators.
     *
     * @see RibbonItem
     * @see WorkspaceLayoutModule
     */
    protected List<RibbonItem> ribbonItems() { return List.of(); }

    /**
     * The workspace's footer contents. Defaults to an empty list —
     * an empty footer is not rendered at all (the DOM slot is omitted,
     * the layout reclaims the space). Override to surface status text,
     * mode toggles, zoom controls, etc.
     *
     * @see FooterItem
     * @see WorkspaceLayoutModule
     */
    protected List<FooterItem> footerItems() { return List.of(); }

    /**
     * The ribbon items serialized as a JSON array literal for JS-side
     * consumption by {@code WorkspaceLayout}. See {@link
     * WorkspaceLayoutJson} for the wire shape.
     */
    public final String ribbonItemsJson() {
        return WorkspaceLayoutJson.ribbonItems(ribbonItems());
    }

    /**
     * The footer items serialized as a JSON array literal. Empty array
     * is the signal to suppress the footer slot.
     */
    public final String footerItemsJson() {
        return WorkspaceLayoutJson.footerItems(footerItems());
    }
}
