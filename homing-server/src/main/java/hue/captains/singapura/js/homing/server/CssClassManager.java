package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.*;

import java.util.List;

/**
 * The one module that puts a stylesheet on the page (RFC 0002-ext1), and the
 * page's theme owner (RFC 0064): every served CSS group module calls
 * {@code loadCss(group, fallbackTheme, subgraph)}; the manager merges the
 * group's dependency subgraph into the page's graph, resolves the theme
 * through the preference steward, and has the load procedure bring the
 * group's whole tree in — dependencies first, missing ones by name, applied
 * all at once. {@code switchTheme(to)} is the same procedure over every
 * loaded node, then the old theme retires; the manager follows the store, so
 * another tab's pick reaches this one.
 *
 * <p>Three affiliates, all pure: {@link CssHandles} (the handles),
 * {@link CssDependencyGraph} (what depends on what, and the waves),
 * {@link CssLoadProcedure} (how sheets arrive). The DOM is touched here, in
 * one place.</p>
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
                .add(new ModuleImports<>(List.of(new PreferenceSteward.PreferenceViewInstance()),
                        PreferenceSteward.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CssClassManager> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new CssClassManagerInstance()));
    }
}
