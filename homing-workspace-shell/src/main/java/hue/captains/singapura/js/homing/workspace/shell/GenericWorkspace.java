package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Widget;
import hue.captains.singapura.js.homing.studio.base.widget.WorkspaceMPA;

// Params record below implements both AppModule._Param (for URL parsing) and
// Widget._Param (so the same record threads through to GenericWorkspaceChrome
// without a parallel Widget-side Params declaration).


/**
 * The <b>one</b> AppModule for every composition-model workspace. URL:
 * <pre>{@code /app?app=genericWorkspace&ws_kind=<spec-kind>}</pre>
 *
 * <p>The {@code ws_kind} parameter selects a registered
 * {@link WorkspaceSpec} from {@link WorkspaceSpecRegistry}; the chrome
 * serializes the registry to JS and calls
 * {@code mountWorkspaceShell(branch, parent, spec)}. Adding a new
 * workspace kind = register a Spec; no new AppModule, no new widget, no
 * new URL.</p>
 *
 * <p>Backed by {@link GenericWorkspaceChrome} as the single hosted
 * widget (per the {@code SingleWidgetMPA} pattern: "fake AppModule"
 * delegating to a widget for all real work).</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition
 */
public final class GenericWorkspace extends WorkspaceMPA<GenericWorkspace.Params, GenericWorkspace> {

    public static final GenericWorkspace INSTANCE = new GenericWorkspace();

    private GenericWorkspace() {}

    /** URL params — {@code ws_kind} selects which {@link WorkspaceSpec}
     *  to mount. The underscore matches the URL parameter literally
     *  (Java identifiers permit underscores). Implements both
     *  {@link AppModule._Param} (URL marshalling) and {@link Widget._Param}
     *  (so the same record threads through to the chrome widget). */
    public record Params(String ws_kind) implements AppModule._Param, Widget._Param {}

    public record appMain() implements AppModule._AppMain<Params, GenericWorkspace> {}
    public record link()    implements AppLink<GenericWorkspace> {}

    @Override public String simpleName() { return "genericWorkspace"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public String title() { return "Workspace"; }

    @Override
    protected AppModule._AppMain<Params, GenericWorkspace> appMain() {
        return new appMain();
    }

    @Override
    protected Widget<?, ?> widget() {
        return GenericWorkspaceChrome.INSTANCE;
    }
}
