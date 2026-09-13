package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Widget;
import hue.captains.singapura.js.homing.studio.base.app.AnchorAddressable;
import hue.captains.singapura.js.homing.studio.base.widget.WorkspaceMPA;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Params record below implements both AppModule._Param (for URL parsing) and
// Widget._Param (so the same record threads through to GenericWorkspaceChrome
// without a parallel Widget-side Params declaration).


/**
 * The <b>one</b> AppModule for every composition-model workspace. URL:
 * <pre>{@code /app?app=genericWorkspace&ws_group=<group-id>}</pre>
 *
 * <p>RFC 0058 — the parameter names a {@link WorkspaceGroup}; the kind inside
 * it is the anchor, {@code #ws/<section>/<kind>}, which the server never sees.
 * The chrome serialises the group's specs and the client picks by anchor —
 * the group's default kind when there is none. Adding a new workspace kind =
 * register a Spec and hold it in a group; no new AppModule, no new widget, no
 * new URL, and no leaf — the group is the leaf.</p>
 *
 * <p><b>{@code ws_kind} is the legacy form.</b> Every permalink minted before
 * RFC 0058 carries it, so the codec still reads it: a kind resolves to the group
 * holding it, and the kind rides along in the params so a flat render opens
 * that kind. {@code /goto} canonicalises the pair to the group's path plus the
 * kind's anchor ({@link AnchorAddressable}). A kind no group holds renders
 * alone, as an implicit group of one — the shape every deployment had before
 * groups existed, so an unmigrated studio keeps working.</p>
 *
 * <p>Backed by {@link GenericWorkspaceChrome} as the single hosted
 * widget (per the {@code SingleWidgetMPA} pattern: "fake AppModule"
 * delegating to a widget for all real work).</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition; RFC 0058 groups
 */
public final class GenericWorkspace extends WorkspaceMPA<GenericWorkspace.Params, GenericWorkspace>
        implements AnchorAddressable<GenericWorkspace.Params> {

    public static final GenericWorkspace INSTANCE = new GenericWorkspace();

    private GenericWorkspace() {}

    /**
     * URL params. {@code ws_group} names the {@link WorkspaceGroup} — the
     * canonical form, and what a placement writes ({@link #ofGroup}). {@code
     * ws_kind} is the legacy form and the flat render's way of opening one
     * kind: null in a placement, set when a {@code ws_kind} address was read.
     * A null {@code ws_group} with a kind means no group holds that kind.
     * The underscores match the URL parameters literally. Implements both
     * {@link AppModule._Param} (URL marshalling) and {@link Widget._Param}
     * (so the same record threads through to the chrome widget).
     */
    public record Params(String ws_group, String ws_kind) implements AppModule._Param, Widget._Param {
        public Params {
            if (ws_group == null && ws_kind == null) {
                throw new IllegalArgumentException("GenericWorkspace.Params: a group or a kind is required");
            }
        }
    }

    /** The params a placement writes: the group, and nothing else. */
    public static Params ofGroup(WorkspaceGroup group) {
        return new Params(group.id(), null);
    }

    public record appMain() implements AppModule._AppMain<Params, GenericWorkspace> {}
    public record link()    implements AppLink<GenericWorkspace> {}

    @Override public String simpleName() { return "genericWorkspace"; }

    /**
     * RFC 0051 / RFC 0058 — {@code ws_group} canonical, {@code ws_kind} legacy;
     * one of them is required. This app mounts a registered group (or a
     * registered kind, alone), and there is no sensible default to fall back
     * to, so an absent pair is a malformed request rather than a page.
     * {@code from(to(p)) == p} holds for every value {@code from} produces: a
     * kind no group holds stays groupless, a kind a group holds stays paired.
     */
    public static final ParamCodec<Params> CODEC = new ParamCodec<>() {

        @Override public Decoded<Params> from(Map<String, List<String>> query) {
            String group = blankToNull(QueryString.first(query, "ws_group"));
            String kind  = blankToNull(QueryString.first(query, "ws_kind"));
            if (group == null && kind == null) return Decoded.missing("ws_group");
            if (group == null) {
                // Legacy: the kind names its group, when one holds it.
                group = WorkspaceGroupRegistry.INSTANCE.groupOf(kind).map(WorkspaceGroup::id).orElse(null);
            }
            return Decoded.ok(new Params(group, kind));
        }

        @Override public Map<String, List<String>> to(Params params) {
            var out = new LinkedHashMap<String, List<String>>();
            if (params.ws_group() != null) out.put("ws_group", List.of(params.ws_group()));
            if (params.ws_kind()  != null) out.put("ws_kind",  List.of(params.ws_kind()));
            return out;
        }

        private static String blankToNull(String s) {
            return (s == null || s.isBlank()) ? null : s;
        }
    };

    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public ParamCodec<Params> paramCodec() { return CODEC; }
    @Override public String title() { return "Workspace"; }

    /**
     * RFC 0058 — a kind is a sub-node of its group. Params carrying a kind that
     * a registered group holds live at {@code Params(group, null)} — the
     * placement's form — under {@code ws/<section>/<kind>}. Params naming only
     * the group, or a kind no group holds, name no sub-node.
     */
    @Override
    public Optional<AnchorAddressable.Anchored<Params>> anchorOf(Params params) {
        if (params.ws_kind() == null || params.ws_group() == null) return Optional.empty();
        return WorkspaceGroupRegistry.INSTANCE.get(params.ws_group())
                .flatMap(group -> group.anchorFor(params.ws_kind())
                        .map(anchor -> new AnchorAddressable.Anchored<>(ofGroup(group), anchor)));
    }

    @Override
    protected AppModule._AppMain<Params, GenericWorkspace> appMain() {
        return new appMain();
    }

    @Override
    protected Widget<?, ?> widget() {
        return GenericWorkspaceChrome.INSTANCE;
    }
}
