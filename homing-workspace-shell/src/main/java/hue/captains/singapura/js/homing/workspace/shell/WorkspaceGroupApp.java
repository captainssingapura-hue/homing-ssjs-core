package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.Widget;
import hue.captains.singapura.js.homing.studio.base.widget.WorkspaceMPA;

/**
 * RFC 0058 — the <b>authentic-path</b> workspace app: one page per
 * {@link WorkspaceGroup}, placed once in a studio's catalogue as an ordinary
 * leaf. URL:
 * <pre>{@code /app?app=workspaceGroup&ws_group=<group-id>}</pre>
 * — and, once placed, {@code /cat/<…>/<leaf>}. The kind inside the group is the
 * anchor, {@code #ws/<section>/<kind>}, which the server never sees: the chrome
 * serialises the group's specs and the client picks by anchor, the group's
 * default kind when there is none.
 *
 * <p>A <em>second</em> app over the same shell, on purpose. {@link GenericWorkspace}
 * — {@code ?ws_kind=<kind>}, the whole registry inlined, kind switching through
 * {@code /goto} — is the flat address every pre-0058 link carries, and it stays
 * exactly as it is: a permalink is a permalink. This app is what a studio places
 * to give its workspaces an authentic path; a studio that has not is not broken,
 * only unpositioned. Nothing here reads {@code ws_kind}, and nothing in
 * {@code GenericWorkspace} knows groups exist.</p>
 *
 * <p>Backed by {@link WorkspaceGroupChrome} as the single hosted widget (the
 * {@code SingleWidgetMPA} pattern), which hands the chosen spec — with the group
 * on it — to the same {@code mountWorkspaceShell} the legacy app uses.</p>
 */
public final class WorkspaceGroupApp extends WorkspaceMPA<WorkspaceGroupApp.Params, WorkspaceGroupApp> {

    public static final WorkspaceGroupApp INSTANCE = new WorkspaceGroupApp();

    private WorkspaceGroupApp() {}

    /**
     * URL params — {@code ws_group} names the {@link WorkspaceGroup}; that is the
     * whole record. The kind is the anchor and is not a parameter. The underscore
     * matches the URL parameter literally. Implements both {@link AppModule._Param}
     * (URL marshalling) and {@link Widget._Param} (the same record threads through
     * to the chrome widget).
     */
    public record Params(String ws_group) implements AppModule._Param, Widget._Param {
        public Params {
            if (ws_group == null || ws_group.isBlank()) {
                throw new IllegalArgumentException("WorkspaceGroupApp.Params: ws_group is required");
            }
        }
    }

    /** The params a placement writes. */
    public static Params of(WorkspaceGroup group) {
        return new Params(group.id());
    }

    public record appMain() implements AppModule._AppMain<Params, WorkspaceGroupApp> {}
    public record link()    implements AppLink<WorkspaceGroupApp> {}

    @Override public String simpleName() { return "workspaceGroup"; }

    /** {@code ws_group} is required: there is no sensible default group to fall back to. */
    public static final ParamCodec<Params> CODEC = new ParamCodec<>() {

        @Override public Decoded<Params> from(java.util.Map<String, java.util.List<String>> query) {
            String group = QueryString.first(query, "ws_group");
            if (group == null || group.isBlank()) return Decoded.missing("ws_group");
            return Decoded.ok(new Params(group));
        }

        @Override public java.util.Map<String, java.util.List<String>> to(Params params) {
            return QueryString.of("ws_group", params.ws_group());
        }
    };

    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public ParamCodec<Params> paramCodec() { return CODEC; }
    @Override public String title() { return "Workspaces"; }

    @Override
    protected AppModule._AppMain<Params, WorkspaceGroupApp> appMain() {
        return new appMain();
    }

    @Override
    protected Widget<?, ?> widget() {
        return WorkspaceGroupChrome.INSTANCE;
    }
}
