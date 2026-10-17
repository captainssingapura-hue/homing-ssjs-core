package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.*;

import java.util.List;

/**
 * The CSS manager (RFC 0002-ext1, RFC 0064): it handles what does not vary
 * (D25). Every served provided group module calls {@code loadCss(group,
 * subgraph)}; the manager merges the group's dependency subgraph into the
 * page's graph and has the load procedure bring the group's whole tree in —
 * dependencies first, missing ones by name, applied all at once — once, with no
 * theme. A theme switch reloads nothing here: what varies is derived, and is
 * {@link ThemeSheets}'. The rest is the element's: its classes by handle and
 * its degrees.
 *
 * <p>Affiliates: {@link CssHandles} (the handles), {@link CssDependencyGraph}
 * (what depends on what, and the waves), {@link CssLoadProcedure} (how sheets
 * arrive), {@link CssLinks} (the one link factory).</p>
 */
public record CssClassManager() implements EsModule<CssClassManager> {

    public static final CssClassManager INSTANCE = new CssClassManager();

    public record CssClassManagerInstance() implements Exportable._Constant<CssClassManager> {}

    @Override
    public ImportsFor<CssClassManager> imports() {
        return ImportsFor.<CssClassManager>builder()
                .add(new ModuleImports<>(List.of(new CssHandles.CssClass(), new CssHandles.CssUtility()),
                        CssHandles.INSTANCE))
                .add(new ModuleImports<>(List.of(new CssDependencyGraph.createCssDependencyGraph()),
                        CssDependencyGraph.INSTANCE))
                .add(new ModuleImports<>(List.of(new CssLoadProcedure.createCssLoadProcedure()),
                        CssLoadProcedure.INSTANCE))
                .add(new ModuleImports<>(List.of(new CssLinks.CssLink()), CssLinks.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CssClassManager> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new CssClassManagerInstance()));
    }
}
