package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.*;

import java.util.List;

/**
 * RFC 0064 — the CSS manager's affiliate: what depends on what, and in what
 * order it loads. Pure — no DOM, no fetch, no theme. A graph of group names
 * merged from the subgraphs each served group module carries, and one
 * operation, a plan: the theme bundle first, then the priors (groups that
 * predate the dependency discipline and that everything leans on), then the
 * graph in waves by Kahn's rule — a wave is every remaining node whose
 * dependencies all sit in earlier waves, so a wave may load in parallel and
 * waves run one after another. A cycle is refused with its names.
 *
 * <p>Exports the factory; the manager holds the one instance for the page,
 * and its {@code snapshot()} is the frozen projection the workbench draws.</p>
 */
public record CssDependencyGraph() implements EsModule<CssDependencyGraph> {

    public static final CssDependencyGraph INSTANCE = new CssDependencyGraph();

    public record createCssDependencyGraph() implements Exportable._Constant<CssDependencyGraph> {}

    @Override
    public ImportsFor<CssDependencyGraph> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<CssDependencyGraph> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createCssDependencyGraph()));
    }
}
