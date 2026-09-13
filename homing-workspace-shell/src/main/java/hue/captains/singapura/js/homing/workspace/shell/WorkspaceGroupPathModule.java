package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0058 — the anchor is a kind's path inside its group:
 * {@code #ws/<section-slug>/<kind>}. Pure functions over the group the server
 * served and the fragment the browser holds: parse an anchor, mint one, decide
 * which kind a page opens (anchor, then legacy {@code ws_kind}, then the
 * default — canonicalising and noticing as the RFC says), and the crumbs the
 * chrome appends after the stamped trail. {@code PURE_LOGIC}: no DOM, no
 * window — the chrome hands it the hash.
 */
public record WorkspaceGroupPathModule() implements DomModule<WorkspaceGroupPathModule> {

    public record parseAnchor()  implements Exportable._Constant<WorkspaceGroupPathModule> {}
    public record anchorOf()     implements Exportable._Constant<WorkspaceGroupPathModule> {}
    public record resolveKind()  implements Exportable._Constant<WorkspaceGroupPathModule> {}
    public record innerCrumbs()  implements Exportable._Constant<WorkspaceGroupPathModule> {}
    public record soloGroup()    implements Exportable._Constant<WorkspaceGroupPathModule> {}

    public static final WorkspaceGroupPathModule INSTANCE = new WorkspaceGroupPathModule();

    @Override
    public ImportsFor<WorkspaceGroupPathModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WorkspaceGroupPathModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new parseAnchor(), new anchorOf(), new resolveKind(), new innerCrumbs(), new soloGroup()));
    }
}
